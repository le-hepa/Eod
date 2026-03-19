package com.gomgom.eod.core.common

import android.content.Context
import android.content.res.Configuration
import java.util.Locale


object AppLanguageManager {

    fun ensureDefaultLanguage(context: Context) {
        val prefs = context.getSharedPreferences("eod_settings", Context.MODE_PRIVATE)
        val saved = prefs.getString("app_language", null) ?: "ko"
        applyLanguage(context, saved)
    }

    fun applyKor(context: Context) {
        saveLanguage(context, "ko")
        applyLanguage(context, "ko")
    }

    fun applyEng(context: Context) {
        saveLanguage(context, "en")
        applyLanguage(context, "en")
    }

    fun currentLanguageTag(context: Context): String {
        val prefs = context.getSharedPreferences("eod_settings", Context.MODE_PRIVATE)
        return prefs.getString("app_language", "ko") ?: "ko"
    }

    private fun saveLanguage(context: Context, languageTag: String) {
        context.getSharedPreferences("eod_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language", languageTag)
            .apply()
    }

    private fun applyLanguage(context: Context, languageTag: String) {
        val locale = Locale(languageTag)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}