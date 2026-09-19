package com.example.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

data class GitHubConnectionTestResult(
    val success: Boolean,
    val message: String,
    val username: String? = null,
    val canPush: Boolean = false,
    val statusCode: Int = 0
)

object GitHubNetworkModule {

    private const val TAG = "NexusGitHubNetwork"
    private const val GITHUB_API_BASE_URL = "https://api.github.com/"
    private const val PREFS_NAME = "nexus_github_prefs"
    private const val KEY_CUSTOM_TOKEN = "custom_github_token"
    private const val KEY_CUSTOM_OWNER = "custom_github_owner"
    private const val KEY_CUSTOM_REPO = "custom_github_repo"
    private const val KEY_CUSTOM_BRANCH = "custom_github_branch"

    const val DEFAULT_OWNER = "zxiu86"
    const val DEFAULT_DATA_REPO = "Data"
    const val DEFAULT_APP_REPO = "Nexus"
    const val DEFAULT_BRANCH = "main"

    private var sharedPrefs: SharedPreferences? = null
    private var okHttpCache: Cache? = null

    @Volatile
    var lastAuthError: String? = null
        private set

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun init(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val httpCacheDirectory = File(context.cacheDir, "nexus_http_cache")
        val cacheSize = 50L * 1024 * 1024 // 50 MB Cache
        okHttpCache = Cache(httpCacheDirectory, cacheSize)
    }

    fun getActiveToken(): String {
        // 1. Custom token entered by user/admin in SharedPreferences
        val customToken = sharedPrefs?.getString(KEY_CUSTOM_TOKEN, null)?.trim()
        if (!customToken.isNullOrEmpty()) {
            return customToken
        }

        // 2. Token from BuildConfig
        val buildConfigToken = runCatching {
            BuildConfig::class.java.getField("GITHUB_TOKEN").get(null) as? String
        }.getOrNull()?.trim()

        return if (!buildConfigToken.isNullOrEmpty() && buildConfigToken != "placeholder" && buildConfigToken != "null") {
            buildConfigToken
        } else {
            ""
        }
    }

    fun getCustomToken(): String {
        return sharedPrefs?.getString(KEY_CUSTOM_TOKEN, "").orEmpty()
    }

    fun saveCustomCredentials(token: String?, owner: String?, repo: String?, branch: String?) {
        sharedPrefs?.edit()?.apply {
            if (token != null) putString(KEY_CUSTOM_TOKEN, token.trim())
            if (owner != null) putString(KEY_CUSTOM_OWNER, owner.trim())
            if (repo != null) putString(KEY_CUSTOM_REPO, repo.trim())
            if (branch != null) putString(KEY_CUSTOM_BRANCH, branch.trim())
            apply()
        }
    }

    fun clearCustomCredentials() {
        sharedPrefs?.edit()?.clear()?.apply()
    }

    fun getConfiguredOwner(): String {
        val custom = sharedPrefs?.getString(KEY_CUSTOM_OWNER, null)?.trim()
        if (!custom.isNullOrEmpty()) return custom
        val owner = runCatching {
            BuildConfig::class.java.getField("GITHUB_OWNER").get(null) as? String
        }.getOrNull()?.trim()
        return if (!owner.isNullOrEmpty() && owner != "placeholder" && owner != "null") owner else DEFAULT_OWNER
    }

    fun getConfiguredRepo(): String {
        val custom = sharedPrefs?.getString(KEY_CUSTOM_REPO, null)?.trim()
        if (!custom.isNullOrEmpty()) return custom
        val repo = runCatching {
            BuildConfig::class.java.getField("GITHUB_REPO").get(null) as? String
        }.getOrNull()?.trim()
        return if (!repo.isNullOrEmpty() && repo != "placeholder" && repo != "null") repo else DEFAULT_DATA_REPO
    }

    fun getDataRepo(): String = getConfiguredRepo()
    fun getAppRepo(): String = DEFAULT_APP_REPO

    fun getConfiguredBranch(): String {
        val custom = sharedPrefs?.getString(KEY_CUSTOM_BRANCH, null)?.trim()
        if (!custom.isNullOrEmpty()) return custom
        val branch = runCatching {
            BuildConfig::class.java.getField("GITHUB_BRANCH").get(null) as? String
        }.getOrNull()?.trim()
        return if (!branch.isNullOrEmpty() && branch != "placeholder" && branch != "null") branch else DEFAULT_BRANCH
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
            .header("User-Agent", "Nexus-Manga-App-Android/2.0.1")
            .header("X-GitHub-Api-Version", "2022-11-28")

        val token = getActiveToken()
        val isGitHubApi = originalRequest.url.host.contains("api.github.com")

        if (token.isNotEmpty() && isGitHubApi) {
            val authHeader = when {
                token.startsWith("Bearer ") || token.startsWith("token ") -> token
                else -> "Bearer $token"
            }
            builder.header("Authorization", authHeader)
        }

        val response = chain.proceed(builder.build())
        val isWriteMethod = originalRequest.method.uppercase() in listOf("POST", "PUT", "DELETE", "PATCH")

        // If authenticated request failed with 401 Unauthorized or 403 Forbidden:
        if ((response.code == 401 || response.code == 403) && token.isNotEmpty()) {
            val errorBody = runCatching { response.peekBody(1024).string() }.getOrNull()
            lastAuthError = "HTTP ${response.code}: $errorBody"
            Log.w(TAG, "GitHub API call failed: ${originalRequest.method} ${originalRequest.url.encodedPath} -> ${response.code}: $errorBody")

            // FOR WRITE REQUESTS: Do NOT retry unauthenticated! It's impossible to write to GitHub without valid credentials.
            if (isWriteMethod) {
                return@Interceptor response
            }

            // FOR GET REQUESTS ONLY: If public repo, retry WITHOUT Authorization header
            response.close()
            Log.w(TAG, "Retrying GET without Authorization header for public access...")
            val unauthRequest = originalRequest.newBuilder()
                .header("User-Agent", "Nexus-Manga-App-Android/2.0.1")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .removeHeader("Authorization")
                .build()
            return@Interceptor chain.proceed(unauthRequest)
        }

        if (response.isSuccessful) {
            lastAuthError = null
        }

        response
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

    suspend fun testGitHubConnection(
        token: String = getActiveToken(),
        owner: String = getConfiguredOwner(),
        repo: String = getConfiguredRepo()
    ): GitHubConnectionTestResult = withContext(Dispatchers.IO) {
        if (token.isBlank()) {
            return@withContext GitHubConnectionTestResult(
                success = false,
                message = "رمز الوصول (GitHub Token) فارغ. يرجى إدخال رمز وصول شخصي (Personal Access Token) بصلاحية repo.",
                statusCode = 0
            )
        }

        val cleanToken = token.trim()
        val authHeader = when {
            cleanToken.startsWith("Bearer ") || cleanToken.startsWith("token ") -> cleanToken
            else -> "Bearer $cleanToken"
        }

        try {
            // 1. Check user info
            val userReq = Request.Builder()
                .url("https://api.github.com/user")
                .header("Authorization", authHeader)
                .header("User-Agent", "Nexus-Manga-App-Android/2.0.1")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()

            val userResp = okHttpClient.newCall(userReq).execute()
            val userBody = userResp.body?.string().orEmpty()

            if (!userResp.isSuccessful) {
                val errorMsg = when (userResp.code) {
                    401 -> "رمز الوصول غير صالح أو تم إلغاؤه (401 Bad credentials). يرجى توليد رمز جديد من GitHub."
                    403 -> "تم رفض الوصول (403). قد يكون تم تجاوز حد الاستعلامات أو الرمز مقيد."
                    else -> "فشل فحص الحساب (رمز الخطأ: ${userResp.code})"
                }
                return@withContext GitHubConnectionTestResult(
                    success = false,
                    message = errorMsg,
                    statusCode = userResp.code
                )
            }

            val userObj = JSONObject(userBody)
            val username = userObj.optString("login", "مجهول")

            // 2. Check repo access & permissions
            val repoReq = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo")
                .header("Authorization", authHeader)
                .header("User-Agent", "Nexus-Manga-App-Android/2.0.1")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()

            val repoResp = okHttpClient.newCall(repoReq).execute()
            val repoBody = repoResp.body?.string().orEmpty()

            if (!repoResp.isSuccessful) {
                val repoError = when (repoResp.code) {
                    404 -> "المستودع $owner/$repo غير موجود أو الرمز لا يملك صلاحية الوصول إليه."
                    else -> "فشل الوصول للمستودع $owner/$repo (رمز الخطأ: ${repoResp.code})"
                }
                return@withContext GitHubConnectionTestResult(
                    success = false,
                    message = repoError,
                    username = username,
                    statusCode = repoResp.code
                )
            }

            val repoObj = JSONObject(repoBody)
            val permissions = repoObj.optJSONObject("permissions")
            val canPush = permissions?.optBoolean("push", false) ?: false

            if (canPush) {
                GitHubConnectionTestResult(
                    success = true,
                    message = "تم الاتصال بنجاح! الحساب: @$username | المستودع: $owner/$repo | صلاحية الكتابة والمزامنة: مفعلة ✓",
                    username = username,
                    canPush = true,
                    statusCode = 200
                )
            } else {
                GitHubConnectionTestResult(
                    success = false,
                    message = "تم الاتصال بالحساب @$username ولكن الرمز لا يملك صلاحية الكتابة (Push) في $owner/$repo. يرجى تفعيل صلاحية 'repo' للرمز.",
                    username = username,
                    canPush = false,
                    statusCode = 200
                )
            }
        } catch (e: Exception) {
            GitHubConnectionTestResult(
                success = false,
                message = "تعذر الاتصال بالشبكة: ${e.message}",
                statusCode = -1
            )
        }
    }

    /**
     * Unified method to push or update any file on GitHub with proper SHA fetching and error handling
     */
    suspend fun pushOrUpdateFileToGitHub(
        path: String,
        contentString: String,
        commitMessage: String,
        owner: String = getConfiguredOwner(),
        repo: String = getDataRepo(),
        branch: String = getConfiguredBranch()
    ): Result<String> = withContext(Dispatchers.IO) {
        val token = getActiveToken()
        if (token.isBlank()) {
            return@withContext Result.failure(IllegalStateException("رمز الوصول لـ GitHub غير مضبوط. يرجى إدخال رمز صالح من لوحة المشرف لإتمام المزامنة."))
        }

        try {
            // 1. Get SHA if file exists
            var sha: String? = null
            try {
                val metaResp = apiService.getFileMetadata(owner, repo, path, branch)
                if (metaResp.isSuccessful && metaResp.body() != null) {
                    val metaStr = metaResp.body()!!.string()
                    val metaObj = JSONObject(metaStr)
                    sha = metaObj.optString("sha").takeIf { it.isNotBlank() }
                }
            } catch (e: Exception) {
                Log.d(TAG, "File $path might not exist yet: ${e.message}")
            }

            // 2. Prepare payload
            val base64Content = Base64.encodeToString(
                contentString.toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )

            val commitBody = JSONObject().apply {
                put("message", commitMessage)
                put("content", base64Content)
                if (!sha.isNullOrBlank()) {
                    put("sha", sha)
                }
                put("branch", branch)
            }

            val requestBody = commitBody.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val putResp = apiService.updateFileContent(owner, repo, path, requestBody)
            if (putResp.isSuccessful) {
                Log.d(TAG, "Successfully committed $path to $owner/$repo on branch $branch")
                Result.success("تم رفع وحفظ $path في GitHub بنجاح.")
            } else {
                val err = putResp.errorBody()?.string() ?: "Code ${putResp.code()}"
                Log.w(TAG, "Failed committing $path to GitHub: ${putResp.code()} - $err")
                Result.failure(Exception("فشل إرسال البيانات إلى GitHub ($path): كود ${putResp.code()} - $err"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing $path to GitHub", e)
            Result.failure(e)
        }
    }

    fun clearHttpCache() {
        try {
            okHttpCache?.evictAll()
            Log.d(TAG, "OkHttp cache successfully evicted.")
        } catch (e: Exception) {
            Log.w(TAG, "Failed evicting OkHttp cache: ${e.message}")
        }
    }

    /**
     * Direct raw URL fetcher with automatic cache-busting, public fallback and multi-mirror support
     */
    fun fetchDirectRaw(url: String, forceFresh: Boolean = true): String? {
        return try {
            val targetUrl = if (forceFresh) {
                val separator = if (url.contains("?")) "&" else "?"
                "$url${separator}_cb=${System.currentTimeMillis()}"
            } else {
                url
            }

            val requestBuilder = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", "Nexus-Manga-App-Android/2.0.1")

            if (forceFresh) {
                requestBuilder
                    .header("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
            }

            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful && response.body != null) {
                val bodyStr = response.body?.string()
                if (!bodyStr.isNullOrBlank() && !bodyStr.contains("404: Not Found") && !bodyStr.startsWith("<!DOCTYPE html>")) {
                    bodyStr
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Direct raw fetch failed for $url: ${e.message}")
            null
        }
    }

    fun isGitHubConfigured(): Boolean = getActiveToken().isNotBlank()
}

