package ru.arhipov.expensetracker

import android.app.Application
import ru.arhipov.expensetracker.util.LocaleHelper

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // это сделано, чтобы применялся сохранённый язык при старте приложения(по данному вопросу консультировался у ИИ)
        LocaleHelper.applyLocale(this)
    }
}
