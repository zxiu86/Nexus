package com.example

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.example.data.network.GitHubNetworkModule

class NexusApp : Application(), ImageLoaderFactory {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        GitHubNetworkModule.init(applicationContext)
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
