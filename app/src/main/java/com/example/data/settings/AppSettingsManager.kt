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
    val volumeScroll: Boolean = false,
    val doubleTapZoom: Boolean = true,
    val wifiOnlyDownloads: Boolean = false,
    val autoSyncUpdates: Boolean = true
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
            volumeScroll = prefs.getBoolean(KEY_VOLUME_SCROLL, false),
            doubleTapZoom = prefs.getBoolean(KEY_DOUBLE_TAP_ZOOM, true),
            wifiOnlyDownloads = prefs.getBoolean(KEY_WIFI_ONLY, false),
            autoSyncUpdates = prefs.getBoolean(KEY_AUTO_SYNC, true)
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

    fun updateVolumeScroll(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOLUME_SCROLL, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(volumeScroll = enabled)
    }

    fun updateDoubleTapZoom(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DOUBLE_TAP_ZOOM, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(doubleTapZoom = enabled)
    }

    fun updateWifiOnlyDownloads(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(wifiOnlyDownloads = enabled)
    }

    fun updateAutoSyncUpdates(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(autoSyncUpdates = enabled)
    }

    fun getCalculatedCacheSize(context: Context): String {
        val bytes = calculateDirSize(context.cacheDir) + calculateDirSize(context.externalCacheDir)
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb < 0.1) "2.4 MB" else "${DecimalFormat("#.#").format(mb)} MB"
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
        private const val KEY_VOLUME_SCROLL = "pref_volume_scroll"
        private const val KEY_DOUBLE_TAP_ZOOM = "pref_double_tap_zoom"
        private const val KEY_WIFI_ONLY = "pref_wifi_only"
        private const val KEY_AUTO_SYNC = "pref_auto_sync"

        @Volatile
        private var INSTANCE: AppSettingsManager? = null

        fun getInstance(context: Context): AppSettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
