package kr.stonecold.zuitweak.common

import android.content.Context
import android.content.res.Configuration
import kr.stonecold.zuitweak.ZuiTweakApplication
import java.util.Locale

@Suppress("unused")
object LanguageUtil {
    private const val PREF_LANGUAGE_KEY = "pref_language"

    val SUPPORTED_LANGUAGES = listOf("ko", "en", "ru")

    private var localizedContext: Context? = null
    private var localizedLanguage: String? = null

    fun setLanguage(language: String) {
        val context = ZuiTweakApplication.appContext
        val languagePreferences = context.getSharedPreferences(Constants.LANGAUGE_PREFS_NAME, Context.MODE_PRIVATE)

        languagePreferences.edit().putString(PREF_LANGUAGE_KEY, language).apply()
        localizedContext = null
    }

    /**
     * Returns the saved language, or null when the user has never picked one.
     * Callers should fall back to the system locale, then to English.
     */
    fun getLanguage(): String? {
        val context = ZuiTweakApplication.appContext
        val languagePreferences = context.getSharedPreferences(Constants.LANGAUGE_PREFS_NAME, Context.MODE_PRIVATE)

        return languagePreferences.getString(PREF_LANGUAGE_KEY, null)?.takeIf { it.isNotEmpty() }
    }

    /** Resolves the effective language: saved choice, else system, else English. */
    fun resolveLanguage(): String {
        getLanguage()?.let { return it }
        val system = try {
            android.content.res.Resources.getSystem().configuration.locales[0].language
        } catch (e: Exception) {
            Locale.getDefault().language
        }
        return if (SUPPORTED_LANGUAGES.contains(system)) system else "en"
    }

    /**
     * Resolves a string in the selected language. The application context keeps the
     * system configuration, so hook titles built outside an Activity would otherwise
     * stay in the system language after switching.
     */
    fun getString(id: Int): String {
        return try {
            val context = ZuiTweakApplication.appContext
            val language = resolveLanguage()
            if (localizedContext == null || localizedLanguage != language) {
                val config = Configuration(context.resources.configuration)
                config.setLocale(Locale(language))
                localizedContext = context.createConfigurationContext(config)
                localizedLanguage = language
            }
            localizedContext!!.getString(id)
        } catch (e: Exception) {
            id.toString()
        }
    }
}
