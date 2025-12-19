package ru.arhipov.expensetracker.util

import android.content.Context
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtil {

    private const val PREFS = "settings"
    private const val KEY_CURRENCY = "currency"

    // Примитивные курсы (захардкожены)
    private const val RATE_USD = 0.011   // 1 RUB -> 0.011 USD
    private const val RATE_EUR = 0.010

    fun getSelectedCurrency(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENCY, "RUB") ?: "RUB"
    }

    fun setSelectedCurrency(context: Context, code: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENCY, code).apply()
    }

    fun convertFromRub(amountRub: Double, currencyCode: String): Double {
        return when (currencyCode) {
            "USD" -> amountRub * RATE_USD
            "EUR" -> amountRub * RATE_EUR
            else -> amountRub
        }
    }

    private fun localeForCurrency(currencyCode: String): Locale {
        return when (currencyCode) {
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY
            "RUB" -> Locale("ru", "RU")
            else -> Locale.getDefault()
        }
    }

    fun formatForDisplay(context: Context, amountRub: Double): String {
        val code = getSelectedCurrency(context)
        val converted = convertFromRub(amountRub, code)
        val locale = localeForCurrency(code)
        val nf = NumberFormat.getCurrencyInstance(locale)
        try {
            nf.currency = Currency.getInstance(code)
        } catch (_: Exception) { /* ignore */ }
        return nf.format(converted)
    }

    fun formatNumberForChart(context: Context, amountRub: Double): String {
        val code = getSelectedCurrency(context)
        val converted = convertFromRub(amountRub, code)
        val locale = localeForCurrency(code)
        val nf = NumberFormat.getNumberInstance(locale)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return nf.format(converted)
    }
}
