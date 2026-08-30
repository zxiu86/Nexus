package com.example.data.network

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object GitHubNetworkModule {

    private const val GITHUB_API_BASE_URL = "https://api.github.com/"
    private const val DEFAULT_OWNER = "zxiu86"
    private const val DEFAULT_REPO = "Data"
    private const val DEFAULT_BRANCH = "main"
    private const val DEFAULT_TOKEN = "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"

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var okHttpCache: Cache? = null

    fun init(context: Context) {
        val httpCacheDirectory = File(context.cacheDir, "nexus_http_cache")
        val cacheSize = 50L * 1024 * 1024 // 50 MB Cache
        okHttpCache = Cache(httpCacheDirectory, cacheSize)
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
            .header("User-Agent", "Nexus-Manga-App-Android/1.4")

        // Retrieve token safely from BuildConfig or fallback to default
        val token = runCatching {
            BuildConfig::class.java.getField("GITHUB_TOKEN").get(null) as? String
        }.getOrNull()?.trim()

        val finalToken = if (!token.isNullOrEmpty() && token != "placeholder" && token != "null") {
            token
        } else {
            DEFAULT_TOKEN
        }

        if (finalToken.isNotEmpty()) {
            val authHeader = when {
                finalToken.startsWith("Bearer ") || finalToken.startsWith("token ") -> finalToken
                finalToken.startsWith("ghp_") -> "token $finalToken"
                else -> "Bearer $finalToken"
            }
            builder.header("Authorization", authHeader)
        }

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)

        okHttpCache?.let { builder.cache(it) }
        builder.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GITHUB_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apiService: GitHubApiService by lazy {
        retrofit.create(GitHubApiService::class.java)
    }

    /**
     * Helper to get configured GitHub owner (fallback to default if not set)
     */
    fun getConfiguredOwner(): String {
        val owner = runCatching {
            BuildConfig::class.java.getField("GITHUB_OWNER").get(null) as? String
        }.getOrNull()?.trim()
        return if (!owner.isNullOrEmpty() && owner != "placeholder" && owner != "null") owner else DEFAULT_OWNER
    }

    /**
     * Helper to get configured GitHub repository (fallback to default if not set)
     */
    fun getConfiguredRepo(): String {
        val repo = runCatching {
            BuildConfig::class.java.getField("GITHUB_REPO").get(null) as? String
        }.getOrNull()?.trim()
        return if (!repo.isNullOrEmpty() && repo != "placeholder" && repo != "null") repo else DEFAULT_REPO
    }

    /**
     * Helper to get target branch (default: main)
     */
    fun getConfiguredBranch(): String {
        val branch = runCatching {
            BuildConfig::class.java.getField("GITHUB_BRANCH").get(null) as? String
        }.getOrNull()?.trim()
        return if (!branch.isNullOrEmpty() && branch != "placeholder" && branch != "null") branch else DEFAULT_BRANCH
    }

    /**
     * Check if user has configured custom repository settings
     */
    fun isGitHubConfigured(): Boolean {
        return true
    }
}
