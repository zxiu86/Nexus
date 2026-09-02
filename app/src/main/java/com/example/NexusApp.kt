package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.example.data.network.GitHubNetworkModule
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK

class NexusApp : Application(), ImageLoaderFactory {

    companion object {
        const val STARTAPP_APP_ID = "208548380"

        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        GitHubNetworkModule.init(applicationContext)

        // Initialize Start.io Ads SDK with App ID & COPPA / User Consent
        try {
            StartAppSDK.init(this, STARTAPP_APP_ID, false)
            StartAppSDK.enableReturnAds(false)
            // COPPA Compliance: Set user consent & COPPA flags for Start.io SDK
            StartAppSDK.setUserConsent(this, "pas", System.currentTimeMillis(), true)
            StartAppSDK.setUserConsent(this, "coppa", System.currentTimeMillis(), true)
            StartAppAd.disableSplash()
            Log.d("NexusApp", "Start.io Ads SDK initialized with App ID: $STARTAPP_APP_ID")
        } catch (e: Exception) {
            Log.w("NexusApp", "Failed to init StartApp SDK: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient { GitHubNetworkModule.okHttpClient }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.35)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("nexus_image_cache"))
                    .maxSizeBytes(500L * 1024 * 1024) // 500 MB Disk Cache for instant loading
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Allow aggressive caching of manga pages & covers
            .crossfade(200)
            .allowHardware(true)
            .allowRgb565(true) // Memory-efficient bitmap decoding for faster scrolling
            .build()
    }
}
