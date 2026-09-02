package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.startapp.sdk.adsbase.StartAppAd

/**
 * Start.io Ad Helper for Interstitial (بياني) and Fullscreen Ads.
 * Handles showing interstitial ads safely upon entering chapters or tapping "Next Chapter".
 */
object StartIoAdManager {
    private const val TAG = "StartIoAdManager"

    /**
     * Displays an interstitial (إعلان بياني) ad safely.
     * Called when entering a chapter or clicking "Next Chapter".
     */
    fun showInterstitial(context: Context) {
        try {
            if (context is Activity) {
                StartAppAd.showAd(context)
            } else {
                val startAppAd = StartAppAd(context)
                startAppAd.showAd()
            }
            Log.d(TAG, "Start.io Interstitial Ad displayed successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Could not show Start.io Interstitial Ad: ${e.message}")
        }
    }
}
