package com.nikitaberezhnyj.saycheese.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleUtils {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"

    private val supportedLanguages = setOf("en", "uk")

    fun resolveLanguage(context: Context): String {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)

        val system = Locale.getDefault().language

        return when {
            saved != null && saved in supportedLanguages -> saved
            system in supportedLanguages -> system
            else -> "en"
        }
    }

    fun saveLanguage(context: Context, language: String) {
        require(language in supportedLanguages) {
            "Unsupported language: $language"
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    fun setLocale(context: Context, languageCode: String): Context {
        require(languageCode in supportedLanguages) {
            "Unsupported language: $languageCode"
        }

        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}