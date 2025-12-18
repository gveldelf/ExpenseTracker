package ru.arhipov.expensetracker.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    private const val PREFS = "settings"
    private const val KEY_LANG = "lang" // values: "ru", "en"

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, Locale.getDefault().language) ?: Locale.getDefault().language
    }

    fun setLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    fun applyLocale(context: Context): Context {
        val lang = getSavedLanguage(context)
        return updateContextLocale(context, Locale(if (lang.isNullOrEmpty()) Locale.getDefault().language else lang))
    }

    private fun updateContextLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val res = context.resources
        val conf = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocale(locale)
            val ctx = context.createConfigurationContext(conf)
            return ctx
        } else {
            conf.locale = locale
            res.updateConfiguration(conf, res.displayMetrics)
            return context
        }
    }
}
