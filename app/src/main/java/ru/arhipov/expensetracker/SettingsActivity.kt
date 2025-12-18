package ru.arhipov.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import ru.arhipov.expensetracker.databinding.ActivitySettingsBinding
import ru.arhipov.expensetracker.util.CurrencyUtil
import ru.arhipov.expensetracker.util.LocaleHelper
import android.content.Context

class SettingsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleHelper.wrapContext(it) })
    }

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLanguageButtons()
        setupCurrencySpinner()
        setupSave()
    }

    private fun setupLanguageButtons() {
        val savedLang = LocaleHelper.getSavedLanguage(this)
        binding.rbEnglish.isChecked = savedLang.startsWith("en")
        binding.rbRussian.isChecked = savedLang.startsWith("ru")

        binding.rbEnglish.setOnClickListener {
            binding.rbEnglish.isChecked = true
            binding.rbRussian.isChecked = false
        }
        binding.rbRussian.setOnClickListener {
            binding.rbRussian.isChecked = true
            binding.rbEnglish.isChecked = false
        }
    }

    private fun setupCurrencySpinner() {
        val currencies = listOf("RUB", "USD", "EUR")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter

        val current = CurrencyUtil.getSelectedCurrency(this)
        val index = currencies.indexOf(current).coerceAtLeast(0)
        binding.spinnerCurrency.setSelection(index)
    }

    private fun setupSave() {
        binding.btnSaveSettings.setOnClickListener {
            val lang = if (binding.rbEnglish.isChecked) "en" else "ru"
            LocaleHelper.setLanguage(this, lang)

            val currency = binding.spinnerCurrency.selectedItem.toString()
            CurrencyUtil.setSelectedCurrency(this, currency)

            // Применить locale и перезапустить activity stack, чтобы изменения применились немедленно
            LocaleHelper.applyLocale(this)

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
