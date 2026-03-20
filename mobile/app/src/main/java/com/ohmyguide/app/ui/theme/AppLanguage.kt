package com.ohmyguide.app.ui.theme

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.ohmyguide.app.ui.theme.strings.EnStrings
import com.ohmyguide.app.ui.theme.strings.JaStrings
import com.ohmyguide.app.ui.theme.strings.KoStrings
import com.ohmyguide.app.ui.theme.strings.ZhCnStrings
import com.ohmyguide.app.ui.theme.strings.ZhTwStrings
import java.util.Locale

enum class AppLanguage(val code: String, val strings: AppStrings, val locale: Locale) {
    EN("en", EnStrings, Locale.ENGLISH),
    JA("ja", JaStrings, Locale.JAPANESE),
    ZH_TW("zh-TW", ZhTwStrings, Locale.TRADITIONAL_CHINESE),
    ZH_CN("zh-CN", ZhCnStrings, Locale.SIMPLIFIED_CHINESE),
    KO("ko", KoStrings, Locale.KOREAN);

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code == code } ?: EN
    }
}

val LocalStrings = staticCompositionLocalOf<AppStrings> { EnStrings }

object LanguageManager {
    private const val PREFS_NAME = "omg_prefs"
    private const val KEY_LANGUAGE = "language"

    private val _current = mutableStateOf(AppLanguage.EN)
    val current: State<AppLanguage> = _current

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        _current.value = AppLanguage.fromCode(code)
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, language.code).apply()
        _current.value = language
    }
}
