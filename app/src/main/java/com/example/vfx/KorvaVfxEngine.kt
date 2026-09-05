package com.example.vfx

import android.content.Context
import android.util.Log

/**
 * Engine wrapper for Korva VFX and libkorva_vfx.so native core.
 * Manages loading the native library and reading infocover_vfx.korv asset.
 */
object KorvaVfxEngine {

    private const val TAG = "KorvaVfxEngine"
    private const val ASSET_FILE = "infocover_vfx.korv"
    private const val NATIVE_LIB_NAME = "korva_vfx"

    private var _isNativeCoreLoaded = false
    val isNativeCoreLoaded: Boolean
        get() = _isNativeCoreLoaded

    init {
        try {
            System.loadLibrary(NATIVE_LIB_NAME)
            _isNativeCoreLoaded = true
            Log.d(TAG, "lib$NATIVE_LIB_NAME.so successfully loaded.")
        } catch (_: UnsatisfiedLinkError) {
            _isNativeCoreLoaded = false
            Log.i(TAG, "lib$NATIVE_LIB_NAME.so not found yet. Using optimized Compose VFX rendering.")
        } catch (e: Exception) {
            _isNativeCoreLoaded = false
            Log.w(TAG, "Exception while loading lib$NATIVE_LIB_NAME.so: ${e.message}")
        }
    }

    /**
     * Reads the configuration from infocover_vfx.korv asset file.
     */
    fun loadVfxAsset(context: Context): String {
        return try {
            context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.w(TAG, "infocover_vfx.korv not found in assets: ${e.message}")
            ""
        }
    }
}
