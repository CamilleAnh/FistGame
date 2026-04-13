package com.yourname.fruitsort

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "game_prefs"
    private const val KEY_LANG = "selected_language"

    fun setLocale(context: Context, langCode: String): Context {
        saveLanguage(context, langCode)
        return updateResources(context, langCode)
    }

    fun loadLocale(context: Context): Context {
        val lang = getSavedLanguage(context)
        return updateResources(context, lang)
    }

    private fun updateResources(context: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        
        return context.createConfigurationContext(config)
    }

    private fun saveLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, langCode).apply()
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "en") ?: "en"
    }
}
