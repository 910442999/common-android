package com.yuanquan.common.utils

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.text.TextUtils
import androidx.core.content.edit
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "LanguagePrefs"
    private const val KEY_LANGUAGE = "key_language"
    private const val KEY_COUNTRY = "key_country"

    /**
     * 设置应用语言
     */
    @JvmStatic
    fun setAppLanguage(context: Context): Context? {
        val locale = this.getAppLanguage(context)
        return this.updateLanguages(context, locale)
    }

    /**
     * 切换语言
     */
    @JvmStatic
    fun changeLanguage(context: Context, locale: Locale) {
        this.saveAppLanguage(context, locale)
        //自行处理重启app逻辑
    }

    /**
     * 获取系统语言
     */
    @JvmStatic
    fun getSystemLanguage(context: Context): Locale {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 在 Android 13 上，不能用 Resources.getSystem() 来获取系统语种了
            // Android 13 上面新增了一个 LocaleManager 的语种管理类
            // 因为如果调用 LocaleManager.setApplicationLocales 会影响获取到的结果不准确
            // 所以应该得用 LocaleManager.getSystemLocales 来获取会比较精准
            val localeManager = context.getSystemService<LocaleManager?>(LocaleManager::class.java)
            if (localeManager != null) {
                return localeManager.systemLocales.get(0)
            }
        }
        //系统级别配置
        val config = Resources.getSystem().configuration
        return this.getLocale(config)
    }

    @JvmStatic
    // 如果需要获取整个系统语言列表（多个偏好设置）
    fun getSystemLanguages(context: Context): List<Locale> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
            val locales = localeManager.systemLocales
            (0 until locales.size()).map { locales[it] }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val config = Resources.getSystem().configuration
            val locales = config.locales
            (0 until locales.size()).map { locales[it] }
        } else {
            // 旧版本只返回默认语言
            listOf(Locale.getDefault())
        }
    }

    /**
     * 获取语种对象(应用级别)
     */
    @JvmStatic
    fun getLocale(context: Context): Locale {
        return getLocale(context.resources.configuration)
    }

    @JvmStatic
    fun getLocale(config: Configuration): Locale {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return config.locales.get(0)
        } else {
            return config.locale
        }
    }

    /**
     * 获取应用语言
     */
    @JvmStatic
    fun getAppLanguage(context: Context): Locale {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val language: String = prefs.getString(KEY_LANGUAGE, "") ?: ""
        val country: String = prefs.getString(KEY_COUNTRY, "") ?: ""
        if (!TextUtils.isEmpty(language) && !TextUtils.isEmpty(country)) {
            return Locale(language, country)
        }
        return this.getSystemLanguage(context)
    }

    @JvmStatic
    fun saveAppLanguage(context: Context, locale: Locale) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_LANGUAGE, locale.language)
                .putString(KEY_COUNTRY, locale.country)
        }
    }

    @JvmStatic
    fun clearAppLanguage(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_LANGUAGE)
                .remove(KEY_COUNTRY)
        }
    }

    @JvmStatic
    private fun updateLanguages(context: Context, locale: Locale): Context? {
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
            return context
        }
    }
}