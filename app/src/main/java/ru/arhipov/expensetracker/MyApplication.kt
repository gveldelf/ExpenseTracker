package ru.arhipov.expensetracker

import android.app.Application
import ru.arhipov.expensetracker.util.LocaleHelper
import ru.arhipov.expensetracker.util.ThemeHelper

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Применяем сохранённый язык при старте приложения
        LocaleHelper.applyLocale(this)

        // Применяем сохраненную тему при запуске приложения
        val savedTheme = ThemeHelper.getSavedTheme(this)
        ThemeHelper.applyTheme(savedTheme)
    }
}