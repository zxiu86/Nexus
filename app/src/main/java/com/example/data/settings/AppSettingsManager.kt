package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import coil.Coil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.DecimalFormat

data class AppSettings(
    val readerMode: Int = 0, // 0: Webtoon Vertical, 1: Horizontal RTL, 2: Horizontal LTR
    val imageQuality: Int = 0, // 0: HD, 1: Balanced, 2: Data Saver
    val keepScreenOn: Boolean = true,
    val wifiOnlyDownloads: Boolean = false,
    val autoSyncUpdates: Boolean = true,
    val themeMode: Int = 0, // 0: System, 1: Dark, 2: Light
    val backgroundStyle: Int = 0, // 0: Default, 1: AMOLED Pure Black, 2: Pure White
    val accentColor: Int = 0, // 0: Gold, 1: Royal Blue, 2: Crimson Red, 3: Marine Blue, 4: Cherry Blossom
    val preventChapterCache: Boolean = true // Don't persist chapter images in disk cache, auto-clear on exit
)

class AppSettingsManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("nexus_app_settings", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            readerMode = prefs.getInt(KEY_READER_MODE, 0),
            imageQuality = prefs.getInt(KEY_IMAGE_QUALITY, 0),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            wifiOnlyDownloads = prefs.getBoolean(KEY_WIFI_ONLY, false),
            autoSyncUpdates = prefs.getBoolean(KEY_AUTO_SYNC, true),
            themeMode = prefs.getInt(KEY_THEME_MODE, 0),
            backgroundStyle = prefs.getInt(KEY_BACKGROUND_STYLE, 0),
            accentColor = prefs.getInt(KEY_ACCENT_COLOR, 0),
            preventChapterCache = prefs.getBoolean(KEY_PREVENT_CHAPTER_CACHE, true)
        )
    }

    fun updateReaderMode(mode: Int) {
        prefs.edit().putInt(KEY_READER_MODE, mode).apply()
        _settingsFlow.value = _settingsFlow.value.copy(readerMode = mode)
    }

    fun updateImageQuality(quality: Int) {
        prefs.edit().putInt(KEY_IMAGE_QUALITY, quality).apply()
        _settingsFlow.value = _settingsFlow.value.copy(imageQuality = quality)
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(keepScreenOn = enabled)
    }

    fun updateWifiOnlyDownloads(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(wifiOnlyDownloads = enabled)
    }

    fun updateAutoSyncUpdates(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(autoSyncUpdates = enabled)
    }

    fun updateThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        _settingsFlow.value = _settingsFlow.value.copy(themeMode = mode)
    }

    fun updateBackgroundStyle(style: Int) {
        prefs.edit().putInt(KEY_BACKGROUND_STYLE, style).apply()
        _settingsFlow.value = _settingsFlow.value.copy(backgroundStyle = style)
    }

    fun updateAccentColor(color: Int) {
        prefs.edit().putInt(KEY_ACCENT_COLOR, color).apply()
        _settingsFlow.value = _settingsFlow.value.copy(accentColor = color)
    }

    fun updatePreventChapterCache(prevent: Boolean) {
        prefs.edit().putBoolean(KEY_PREVENT_CHAPTER_CACHE, prevent).apply()
        _settingsFlow.value = _settingsFlow.value.copy(preventChapterCache = prevent)
    }

    fun getCalculatedCacheSize(context: Context): String {
        val bytes = calculateDirSize(context.cacheDir) + calculateDirSize(context.externalCacheDir)
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb < 0.1) "0.5 MB" else "${DecimalFormat("#.#").format(mb)} MB"
    }

    fun clearAllCache(context: Context): String {
        val beforeBytes = calculateDirSize(context.cacheDir) + calculateDirSize(context.externalCacheDir)
        try {
            context.cacheDir.deleteRecursively()
            context.cacheDir.mkdirs()
            context.externalCacheDir?.deleteRecursively()
            context.externalCacheDir?.mkdirs()
            Coil.imageLoader(context).memoryCache?.clear()
            Coil.imageLoader(context).diskCache?.clear()
        } catch (_: Exception) {}
        val mb = beforeBytes.toDouble() / (1024 * 1024)
        return if (mb < 0.1) "تم تنظيف الذاكرة المؤقتة بنجاح" else "تم تحرير ${DecimalFormat("#.#").format(mb)} MB بنجاح"
    }

    fun clearChapterImages(context: Context, urls: List<String>) {
        try {
            val loader = Coil.imageLoader(context)
            urls.forEach { url ->
                loader.memoryCache?.remove(coil.memory.MemoryCache.Key(url))
                loader.diskCache?.remove(url)
            }
        } catch (_: Exception) {}
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) calculateDirSize(f) else f.length()
        }
        return size
    }

    companion object {
        private const val KEY_READER_MODE = "pref_reader_mode"
        private const val KEY_IMAGE_QUALITY = "pref_image_quality"
        private const val KEY_KEEP_SCREEN_ON = "pref_keep_screen_on"
        private const val KEY_WIFI_ONLY = "pref_wifi_only"
        private const val KEY_AUTO_SYNC = "pref_auto_sync"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_BACKGROUND_STYLE = "pref_background_style"
        private const val KEY_ACCENT_COLOR = "pref_accent_color"
        private const val KEY_PREVENT_CHAPTER_CACHE = "pref_prevent_chapter_cache"

        @Volatile
        private var INSTANCE: AppSettingsManager? = null

        fun getInstance(context: Context): AppSettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
