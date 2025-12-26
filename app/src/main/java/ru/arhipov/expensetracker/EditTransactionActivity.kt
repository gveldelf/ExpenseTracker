package ru.arhipov.expensetracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.arhipov.expensetracker.data.database.AppDatabase
import ru.arhipov.expensetracker.data.entity.Transaction
import ru.arhipov.expensetracker.data.repository.TransactionRepository
import ru.arhipov.expensetracker.databinding.ActivityEditTransactionBinding
import ru.arhipov.expensetracker.ui.viewmodel.TransactionViewModel
import ru.arhipov.expensetracker.util.LocaleHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToLong

import android.content.Context


class EditTransactionActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.wrapContext(it) })
    }

    private lateinit var binding: ActivityEditTransactionBinding
    private lateinit var viewModel: TransactionViewModel
    private var transactionUid: String? = null
    private var currentTransaction: Transaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)
        val repo = TransactionRepository(db.transactionDao())
        viewModel = TransactionViewModel(repo)

        transactionUid = intent.getStringExtra("transactionUid")
        val defaultType = intent.getStringExtra("type") ?: "expense"

        setupTypeSpinner(defaultType)
        setupCategorySpinner()
        setupDatePicker()
        setupAmountInput()
        setupButtons()

        transactionUid?.let {
            binding.btnDelete.visibility = android.view.View.VISIBLE
            lifecycleScope.launch { loadTransaction(it) }
        }
    }

    private fun setupAmountInput() {
        // InputFilter для ограничения ввода: максимум 2 знака после запятой
        val decimalFilter = object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                val input = (dest?.toString() ?: "") + (source?.toString() ?: "")

                // Проверяем формат: цифры и одна точка/запятая
                if (input.matches(Regex("^\\d*[.,]?\\d{0,2}$"))) {
                    return null // разрешаем ввод
                }
                return "" // блокируем ввод
            }
        }

        binding.etAmount.filters = arrayOf(decimalFilter)
    }

    private fun setupTypeSpinner(defaultType: String) {
        val types = resources.getStringArray(R.array.types)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = adapter

        val defaultLabel = if (defaultType == "income") getString(R.string.type_income) else getString(R.string.type_expense)
        val pos = types.indexOfFirst { it.equals(defaultLabel, ignoreCase = true) }.coerceAtLeast(0)
        binding.spinnerType.setSelection(pos)
    }

    private fun setupCategorySpinner() {
        val labels = resources.getStringArray(R.array.category_labels)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        binding.etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
        binding.etDate.setOnClickListener {
            val picker = DatePickerDialog(this,
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    binding.etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            picker.show()
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener { saveTransaction() }

        binding.btnDelete.setOnClickListener {
            currentTransaction?.let { t ->
                lifecycleScope.launch {
                    viewModel.deleteTransaction(t)
                    runOnUiThread { Toast.makeText(this@EditTransactionActivity, getString(R.string.delete), Toast.LENGTH_SHORT).show() }
                    finish()
                }
            }
        }
    }

    private fun saveTransaction() {
        val amountText = binding.etAmount.text.toString().replace(',', '.')
        val amount = amountText.toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            Toast.makeText(this, getString(R.string.amount), Toast.LENGTH_SHORT).show()
            return
        }

        // КРИТИЧЕСКИ ВАЖНО: Округляем до 2 знаков ПЕРЕД записью в БД
        // Это предотвращает накопление погрешностей при суммировании
        val roundedAmount = (amount * 100).roundToLong() / 100.0

        val typesArr = resources.getStringArray(R.array.types)
        val selectedTypeLabel = binding.spinnerType.selectedItem.toString()
        val typeValue = if (selectedTypeLabel.equals(getString(R.string.type_income), ignoreCase = true)) "income" else "expense"

        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val categoryKey = resources.getStringArray(R.array.category_keys)[categoryIndex]

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(binding.etDate.text.toString()) ?: Date()

        val desc = binding.etDescription.text.toString().ifEmpty { null }

        val tx = Transaction(
            uid = currentTransaction?.uid ?: UUID.randomUUID().toString(),
            amount = roundedAmount,
            type = typeValue,
            category = categoryKey,
            date = date,
            description = desc
        )

        lifecycleScope.launch {
            if (currentTransaction == null) {
                viewModel.insertTransaction(tx)
                runOnUiThread { Toast.makeText(this@EditTransactionActivity, getString(R.string.save), Toast.LENGTH_SHORT).show() }
            } else {
                viewModel.updateTransaction(tx)
                runOnUiThread { Toast.makeText(this@EditTransactionActivity, getString(R.string.edit), Toast.LENGTH_SHORT).show() }
            }
            finish()
        }
    }

    private suspend fun loadTransaction(uid: String) {
        val t = viewModel.getTransaction(uid) ?: return
        currentTransaction = t
        runOnUiThread {
            // Форматируем amount с 2 знаками после запятой для отображения
            binding.etAmount.setText(String.format(Locale.US, "%.2f", t.amount))
            val types = resources.getStringArray(R.array.types)
            val typeLabel = if (t.type == "income") getString(R.string.type_income) else getString(R.string.type_expense)
            val typePos = types.indexOfFirst { it.equals(typeLabel, ignoreCase = true) }.coerceAtLeast(0)
            binding.spinnerType.setSelection(typePos)
            val keys = resources.getStringArray(R.array.category_keys)
            val catPos = keys.indexOf(t.category).coerceAtLeast(0)
            binding.spinnerCategory.setSelection(catPos)
            binding.etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(t.date))
            binding.etDescription.setText(t.description ?: "")
        }
    }
}