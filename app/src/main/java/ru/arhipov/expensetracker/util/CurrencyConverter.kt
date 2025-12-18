package ru.arhipov.expensetracker.util

import java.util.Currency
import java.util.Locale

object CurrencyConverter {

    // Базовая валюта — RUB
    private const val USD_RATE = 0.011   // 1 RUB ≈ 0.011 USD
    private const val EUR_RATE = 0.010   // 1 RUB ≈ 0.010 EUR

    fun convertFromRub(amount: Double, currencyCode: String): Double {
        return when (currencyCode) {
            "USD" -> amount * USD_RATE
            "EUR" -> amount * EUR_RATE
            else -> amount // RUB
        }
    }

    fun format(amount: Double, currencyCode: String, locale: Locale): String {
        val currency = Currency.getInstance(currencyCode)
        val formatter = java.text.NumberFormat.getCurrencyInstance(locale)
        formatter.currency = currency
        return formatter.format(amount)
    }
}
