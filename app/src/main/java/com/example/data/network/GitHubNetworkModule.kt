package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object GitHubNetworkModule {

    private const val TAG = "NexusGitHubNetwork"
    private const val GITHUB_API_BASE_URL = "https://api.github.com/"
    const val DEFAULT_OWNER = "zxiu86"
    const val DEFAULT_DATA_REPO = "Data"
    const val DEFAULT_APP_REPO = "Nexus"
    const val DEFAULT_BRANCH = "main"
    const val DEFAULT_TOKEN = "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"

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var okHttpCache: Cache? = null

    fun init(context: Context) {
        val httpCacheDirectory = File(context.cacheDir, "nexus_http_cache")
        val cacheSize = 50L * 1024 * 1024 // 50 MB Cache
        okHttpCache = Cache(httpCacheDirectory, cacheSize)
    }

    fun getActiveToken(): String {
        val token = runCatching {
            BuildConfig::class.java.getField("GITHUB_TOKEN").get(null) as? String
        }.getOrNull()?.trim()

        return if (!token.isNullOrEmpty() && token != "placeholder" && token != "null") {
            token
        } else {
            DEFAULT_TOKEN
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
            .header("User-Agent", "Nexus-Manga-App-Android/1.4")
            .header("X-GitHub-Api-Version", "2022-11-28")

        val token = getActiveToken()
        if (token.isNotEmpty()) {
            val authHeader = when {
                token.startsWith("Bearer ") || token.startsWith("token ") -> token
                token.startsWith("ghp_") -> "Bearer $token"
                else -> "Bearer $token"
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
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)

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

    fun getConfiguredOwner(): String {
        val owner = runCatching {
            BuildConfig::class.java.getField("GITHUB_OWNER").get(null) as? String
        }.getOrNull()?.trim()
        return if (!owner.isNullOrEmpty() && owner != "placeholder" && owner != "null") owner else DEFAULT_OWNER
    }

    fun getConfiguredRepo(): String {
        val repo = runCatching {
            BuildConfig::class.java.getField("GITHUB_REPO").get(null) as? String
        }.getOrNull()?.trim()
        return if (!repo.isNullOrEmpty() && repo != "placeholder" && repo != "null") repo else DEFAULT_DATA_REPO
    }

    fun getDataRepo(): String = DEFAULT_DATA_REPO

    fun getAppRepo(): String = DEFAULT_APP_REPO

    fun getConfiguredBranch(): String {
        val branch = runCatching {
            BuildConfig::class.java.getField("GITHUB_BRANCH").get(null) as? String
        }.getOrNull()?.trim()
        return if (!branch.isNullOrEmpty() && branch != "placeholder" && branch != "null") branch else DEFAULT_BRANCH
    }

    /**
     * Direct raw URL fetcher with auth headers for fail-safe retrieval
     */
    fun fetchDirectRaw(url: String): String? {
        return try {
            val token = getActiveToken()
            val requestBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", "Nexus-Manga-App-Android/1.4")

            if (token.isNotEmpty()) {
                val authHeader = if (token.startsWith("token ") || token.startsWith("Bearer ")) token else "token $token"
                requestBuilder.header("Authorization", authHeader)
            }

            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful && response.body != null) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Direct raw fetch failed for $url: ${e.message}")
            null
        }
    }

    fun isGitHubConfigured(): Boolean = true
}

