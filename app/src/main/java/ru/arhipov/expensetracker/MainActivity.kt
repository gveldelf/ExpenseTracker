package ru.arhipov.expensetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import ru.arhipov.expensetracker.data.database.AppDatabase
import ru.arhipov.expensetracker.data.repository.TransactionRepository
import ru.arhipov.expensetracker.databinding.ActivityMainBinding
import ru.arhipov.expensetracker.ui.viewmodel.TransactionViewModel
import ru.arhipov.expensetracker.util.CurrencyUtil
import ru.arhipov.expensetracker.util.LocaleHelper
import android.content.Context

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.wrapContext(it) })
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val db = AppDatabase.getDatabase(this)
        val repo = TransactionRepository(db.transactionDao())
        viewModel = TransactionViewModel(repo)

        setupButtons()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        updateBalance()
        updateChart()
    }

    private fun updateBalance() {
        lifecycleScope.launch {
            val balanceRub = viewModel.getBalance()
            val formatted = CurrencyUtil.formatForDisplay(this@MainActivity, balanceRub)
            binding.tvBalance.text = getString(R.string.balance, formatted)
        }
    }

    private fun updateChart() {
        lifecycleScope.launch {
            val expenses = viewModel.getExpensesByCategory()

            val entries = expenses.map {
                val converted = CurrencyUtil.convertFromRub(it.total, CurrencyUtil.getSelectedCurrency(this@MainActivity))
                PieEntry(converted.toFloat(), getCategoryLabel(it.categoryKey))
            }

            val dataSet = PieDataSet(entries, getString(R.string.expenses_by_category))
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            val pieData = PieData(dataSet)
            binding.pieChart.data = pieData
            binding.pieChart.description.isEnabled = false
            binding.pieChart.legend.isEnabled = true
            binding.pieChart.animateY(800)
            binding.pieChart.invalidate()
        }
    }

    private fun getCategoryLabel(key: String): String {
        val resId = resources.getIdentifier("category_$key", "string", packageName)
        return if (resId != 0) getString(resId) else key
    }

    private fun setupButtons() {
        binding.btnAddIncome.setOnClickListener {
            val intent = Intent(this, EditTransactionActivity::class.java)
            intent.putExtra("type", "income")
            startActivity(intent)
        }
        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(this, EditTransactionActivity::class.java)
            intent.putExtra("type", "expense")
            startActivity(intent)
        }
        binding.btnTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
        }
    }

    private fun setupFab() {
        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
