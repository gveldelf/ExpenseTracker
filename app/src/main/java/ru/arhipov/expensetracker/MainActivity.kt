package ru.arhipov.expensetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.Legend
import kotlinx.coroutines.launch
import ru.arhipov.expensetracker.data.database.AppDatabase
import ru.arhipov.expensetracker.data.repository.TransactionRepository
import ru.arhipov.expensetracker.databinding.ActivityMainBinding
import ru.arhipov.expensetracker.ui.viewmodel.TransactionViewModel
import ru.arhipov.expensetracker.util.CurrencyUtil
import ru.arhipov.expensetracker.util.LocaleHelper
import ru.arhipov.expensetracker.util.ThemeHelper
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.wrapContext(it) })
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // Применяем сохраненную тему перед вызовом super.onCreate
        val savedTheme = ThemeHelper.getSavedTheme(this)
        ThemeHelper.applyTheme(savedTheme)

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

            val dataSet = PieDataSet(entries, "")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

            // ВСЕГДА белые надписи на графике (цифры и категории)
            dataSet.valueTextSize = 13f
            dataSet.valueTextColor = Color.WHITE

            val pieData = PieData(dataSet)
            binding.pieChart.data = pieData

            // Отключаем описание
            binding.pieChart.description.isEnabled = false

            // Определяем ночной режим для легенды
            val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            // Настройка легенды (подписи под графиком)
            binding.pieChart.legend.isEnabled = true
            binding.pieChart.legend.textSize = 14f
            binding.pieChart.legend.textColor = if (isNightMode) Color.WHITE else Color.BLACK
            binding.pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            binding.pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            binding.pieChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
            binding.pieChart.legend.setDrawInside(false)
            binding.pieChart.legend.yOffset = 5f
            binding.pieChart.legend.xEntrySpace = 30f
            binding.pieChart.legend.maxSizePercent = 0.90f  // Максимум 95% ширины
            binding.pieChart.legend.yEntrySpace = 5f
            binding.pieChart.legend.formToTextSpace = 5f
            binding.pieChart.legend.xOffset = 15f
            // Настройка внутреннего кольца
            binding.pieChart.isDrawHoleEnabled = true
            binding.pieChart.holeRadius = 40f
            binding.pieChart.transparentCircleRadius = 45f
            binding.pieChart.setHoleColor(if (isNightMode) Color.parseColor("#121212") else Color.WHITE)

            // Отключаем текст в центре
            binding.pieChart.setDrawCenterText(false)

            // Дополнительные настройки для предотвращения наплывания
            binding.pieChart.setExtraOffsets(5f, 10f, 5f, 10f)
            binding.pieChart.setUsePercentValues(false)

            // Анимация
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