package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.AdminAnnouncement
import com.example.data.model.AuthResult
import com.example.data.model.CloudUserData
import com.example.data.model.NexusUser
import com.example.data.model.ReadingHistoryEntry
import com.example.data.network.GitHubNetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

/**
 * Lightweight Symbolic Authentication & Per-User Data Persistence Repository for Nexus.
 *
 * Requirements:
 * 1. Simple Username & Password registration/login without heavy external OAuth friction.
 * 2. Strict prevention of duplicate usernames during registration.
 * 3. Master Admin credentials:
 *    - Username: zxiuzaid
 *    - Password: za/id/20/10
 *    - Grants verified full Admin privileges (Admin Dashboard, announcements, report resolution).
 * 4. Per-user isolated library persistence (Favorites, Read Later, History, Read Chapters)
 *    associated with github/zxiu86/Data repository and local cache.
 */
class AuthRepository(private val context: Context) {

    companion object {
        private const val TAG = "NexusAuthRepository"

        // Master Admin Credentials
        const val MASTER_ADMIN_USERNAME = "zxiuzaid"
        const val MASTER_ADMIN_PASSWORD = "za/id/20/10"
        const val PRIMARY_ADMIN_EMAIL = "alsaid66900@gmail.com"

        private const val PREFS_NAME = "nexus_symbolic_auth_prefs"
        private const val KEY_LAST_SYNC = "last_cloud_sync_time"
        private const val KEY_CURRENT_USER_JSON = "current_active_user_json"
        private const val KEY_REGISTERED_USERS = "registered_users_database_v2"
        private const val KEY_USER_DATA_PREFIX = "user_data_store_"
        private const val KEY_LOCAL_ANNOUNCEMENT = "nexus_local_announcement"

        const val DEFAULT_WEB_CLIENT_ID = "830681771668-r7044r7huaje8j1cusj0b1q456gc7ji2.apps.googleusercontent.com"
        const val DEFAULT_WEB_CLIENT_SECRET = "zaid^_^0110-_-zaid"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUserFlow = MutableStateFlow<NexusUser?>(null)
    val currentUserFlow: StateFlow<NexusUser?> = _currentUserFlow.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val isFirebaseConfigured: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(prefs.getLong(KEY_LAST_SYNC, 0L))
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    init {
        ensureMasterAdminExists()
        restoreCachedUser()
    }

    // =========================================================================
    // Security & Hashing Helpers
    // =========================================================================

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((password + salt).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getRegisteredUsersDb(): JSONObject {
        val raw = prefs.getString(KEY_REGISTERED_USERS, "{}") ?: "{}"
        return try {
            JSONObject(raw)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun saveRegisteredUsersDb(db: JSONObject) {
        prefs.edit().putString(KEY_REGISTERED_USERS, db.toString()).apply()
    }

    /**
     * Guarantees the Master Admin account 'zxiuzaid' with password 'za/id/20/10'
     * is always initialized in the database.
     */
    private fun ensureMasterAdminExists() {
        val db = getRegisteredUsersDb()
        val adminKey = MASTER_ADMIN_USERNAME.lowercase()
        if (!db.has(adminKey)) {
            val salt = "nexus_admin_salt_2026"
            val passHash = hashPassword(MASTER_ADMIN_PASSWORD, salt)
            val adminObj = JSONObject().apply {
                put("uid", "admin_zxiuzaid_master")
                put("username", MASTER_ADMIN_USERNAME)
                put("displayName", "zxiuzaid (المشرف العام)")
                put("email", PRIMARY_ADMIN_EMAIL)
                put("salt", salt)
                put("passHash", passHash)
                put("isAdmin", true)
                put("createdAt", System.currentTimeMillis())
            }
            db.put(adminKey, adminObj)
            saveRegisteredUsersDb(db)
        }
    }

    private fun cacheUser(user: NexusUser) {
        val obj = JSONObject().apply {
            put("uid", user.uid)
            put("username", user.username)
            put("displayName", user.displayName)
            put("email", user.email)
            put("photoUrl", user.photoUrl ?: "")
            put("isAdmin", user.isAdmin)
            put("createdAt", user.createdAt)
        }
        prefs.edit().putString(KEY_CURRENT_USER_JSON, obj.toString()).apply()
    }

    private fun clearCachedUser() {
        prefs.edit().remove(KEY_CURRENT_USER_JSON).apply()
    }

    private fun restoreCachedUser() {
        val raw = prefs.getString(KEY_CURRENT_USER_JSON, null) ?: return
        try {
            val obj = JSONObject(raw)
            val username = obj.optString("username", "")
            if (username.isNotBlank()) {
                val user = NexusUser(
                    uid = obj.optString("uid", "user_$username"),
                    username = username,
                    displayName = obj.optString("displayName", username),
                    email = obj.optString("email", "$username@nexus.local"),
                    photoUrl = obj.optString("photoUrl", "").takeIf { it.isNotBlank() },
                    isAdmin = obj.optBoolean("isAdmin", false) || isUsernameAdmin(username),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
                _currentUserFlow.value = user
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed restoring cached user: ${e.message}")
        }
    }

    fun isUsernameAdmin(username: String?): Boolean {
        if (username.isNullOrBlank()) return false
        val clean = username.trim().lowercase()
        return clean == MASTER_ADMIN_USERNAME.lowercase() || clean == "alsaid66900@gmail.com"
    }

    fun isEmailAdmin(email: String?): Boolean = isUsernameAdmin(email)

    // =========================================================================
    // Core Symbolic Auth Operations (Username & Password)
    // =========================================================================

    suspend fun signInWithUsername(usernameInput: String, passwordInput: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUsername = usernameInput.trim()
        val cleanPassword = passwordInput.trim()

        if (cleanUsername.isBlank()) {
            return@withContext AuthResult.Error("يرجى إدخال اسم المستخدم")
        }
        if (cleanPassword.isBlank()) {
            return@withContext AuthResult.Error("يرجى إدخال كلمة المرور")
        }

        val key = cleanUsername.lowercase()

        // 1. Check Master Admin Credentials
        if (key == MASTER_ADMIN_USERNAME.lowercase()) {
            if (cleanPassword == MASTER_ADMIN_PASSWORD) {
                val adminUser = NexusUser(
                    uid = "admin_zxiuzaid_master",
                    username = MASTER_ADMIN_USERNAME,
                    displayName = "zxiuzaid (المشرف العام)",
                    email = PRIMARY_ADMIN_EMAIL,
                    isAdmin = true,
                    createdAt = System.currentTimeMillis()
                )
                _currentUserFlow.value = adminUser
                cacheUser(adminUser)
                return@withContext AuthResult.Success(adminUser)
            } else {
                return@withContext AuthResult.Error("كلمة المرور غير صحيحة لحساب المشرف")
            }
        }

        // 2. Check General User Registry
        val db = getRegisteredUsersDb()
        if (!db.has(key)) {
            return@withContext AuthResult.Error("اسم المستخدم غير مسجل مسبقاً، يرجى إنشاء حساب جديد")
        }

        val userObj = db.getJSONObject(key)
        val salt = userObj.optString("salt", "")
        val expectedHash = userObj.optString("passHash", "")
        val inputHash = hashPassword(cleanPassword, salt)

        if (inputHash != expectedHash) {
            return@withContext AuthResult.Error("كلمة المرور غير صحيحة، يرجى التأكد وإعادة المحاولة")
        }

        val uid = userObj.optString("uid", "user_${UUID.randomUUID().toString().take(10)}")
        val actualUsername = userObj.optString("username", cleanUsername)
        val displayName = userObj.optString("displayName", actualUsername)
        val email = userObj.optString("email", "$actualUsername@nexus.local")
        val isAdmin = userObj.optBoolean("isAdmin", false) || isUsernameAdmin(actualUsername)

        val nexusUser = NexusUser(
            uid = uid,
            username = actualUsername,
            displayName = displayName,
            email = email,
            isAdmin = isAdmin,
            createdAt = userObj.optLong("createdAt", System.currentTimeMillis())
        )

        _currentUserFlow.value = nexusUser
        cacheUser(nexusUser)
        AuthResult.Success(nexusUser)
    }

    suspend fun signUpWithUsername(usernameInput: String, passwordInput: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanUsername = usernameInput.trim()
        val cleanPassword = passwordInput.trim()

        if (cleanUsername.isBlank()) {
            return@withContext AuthResult.Error("يرجى إدخال اسم المستخدم")
        }
        if (cleanUsername.length < 3) {
            return@withContext AuthResult.Error("اسم المستخدم يجب أن يتكون من 3 أحرف على الأقل")
        }
        if (cleanPassword.length < 4) {
            return@withContext AuthResult.Error("كلمة المرور يجب أن لا تقل عن 4 خانات")
        }

        val key = cleanUsername.lowercase()

        // 1. Admin username reservation
        if (key == MASTER_ADMIN_USERNAME.lowercase()) {
            if (cleanPassword == MASTER_ADMIN_PASSWORD) {
                val adminUser = NexusUser(
                    uid = "admin_zxiuzaid_master",
                    username = MASTER_ADMIN_USERNAME,
                    displayName = "zxiuzaid (المشرف العام)",
                    email = PRIMARY_ADMIN_EMAIL,
                    isAdmin = true,
                    createdAt = System.currentTimeMillis()
                )
                _currentUserFlow.value = adminUser
                cacheUser(adminUser)
                return@withContext AuthResult.Success(adminUser)
            } else {
                return@withContext AuthResult.Error("اسم المشرف محجوز. يرجى إدخال كلمة المرور الصحيحة لحساب المشرف أو اختيار اسم آخر.")
            }
        }

        // 2. Prevent duplicate usernames
        val db = getRegisteredUsersDb()
        if (db.has(key)) {
            return@withContext AuthResult.Error("اسم المستخدم مسجل مسبقاً، يرجى اختيار اسم آخر أو تسجيل الدخول")
        }

        // 3. Register new user
        val salt = UUID.randomUUID().toString().take(8)
        val passHash = hashPassword(cleanPassword, salt)
        val uid = "nexus_uid_${UUID.randomUUID().toString().replace("-", "").take(14)}"

        val newUserObj = JSONObject().apply {
            put("uid", uid)
            put("username", cleanUsername)
            put("displayName", cleanUsername)
            put("email", "$cleanUsername@nexus.local")
            put("salt", salt)
            put("passHash", passHash)
            put("isAdmin", false)
            put("createdAt", System.currentTimeMillis())
        }

        db.put(key, newUserObj)
        saveRegisteredUsersDb(db)

        val newUser = NexusUser(
            uid = uid,
            username = cleanUsername,
            displayName = cleanUsername,
            email = "$cleanUsername@nexus.local",
            isAdmin = false,
            createdAt = System.currentTimeMillis()
        )

        _currentUserFlow.value = newUser
        cacheUser(newUser)
        AuthResult.Success(newUser)
    }

    // Compatibility wrappers for existing calls
    suspend fun signInWithEmail(emailOrUser: String, pass: String): AuthResult =
        signInWithUsername(emailOrUser, pass)

    suspend fun signUpWithEmail(emailOrUser: String, pass: String, displayName: String = ""): AuthResult =
        signUpWithUsername(if (displayName.isNotBlank()) displayName else emailOrUser, pass)

    suspend fun signInWithGoogleCredential(idToken: String): AuthResult = withContext(Dispatchers.IO) {
        AuthResult.Error("تم استبدال تسجيل الدخول بالنظام الرمزي المباشر (اسم المستخدم وكلمة المرور).")
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    fun signOut() {
        _currentUserFlow.value = null
        clearCachedUser()
    }

    // =========================================================================
    // Per-User Library Persistence (Favorites, Read Later, History, Read Chapters)
    // Saved in local storage and synced to GitHub zxiu86/Data
    // =========================================================================

    suspend fun syncDataToCloud(
        favorites: Set<String>,
        readLater: Set<String>,
        history: List<ReadingHistoryEntry>,
        readChapters: Map<String, Set<Int>>
    ): Boolean = withContext(Dispatchers.IO) {
        val user = _currentUserFlow.value ?: return@withContext false
        val userKey = user.username.lowercase().ifBlank { user.uid }
        _isSyncing.value = true

        try {
            val userPayload = JSONObject().apply {
                put("username", user.username)
                put("uid", user.uid)
                put("isAdmin", user.isAdmin)
                put("favorites", JSONArray(favorites.toList()))
                put("readLater", JSONArray(readLater.toList()))

                val histArr = JSONArray()
                history.take(60).forEach { entry ->
                    val hObj = JSONObject().apply {
                        put("mangaId", entry.mangaId)
                        put("mangaTitle", entry.mangaTitle)
                        put("mangaCover", entry.mangaCover ?: "")
                        put("chapterNumber", entry.chapterNumber)
                        put("chapterTitle", entry.chapterTitle)
                        put("pageNumber", entry.pageNumber)
                        put("totalPages", entry.totalPages)
                        put("timestamp", entry.timestamp)
                    }
                    histArr.put(hObj)
                }
                put("history", histArr)

                val chapObj = JSONObject()
                readChapters.forEach { (mId, set) ->
                    chapObj.put(mId, JSONArray(set.toList()))
                }
                put("readChapters", chapObj)
                put("lastSynced", System.currentTimeMillis())
            }

            // 1. Save to local per-user storage
            val storageKey = KEY_USER_DATA_PREFIX + userKey
            prefs.edit()
                .putString(storageKey, userPayload.toString())
                .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
                .apply()

            _lastSyncTimestamp.value = System.currentTimeMillis()

            // 2. If GitHub token is configured, sync to github/zxiu86/Data under data/users/{username}.json
            val token = GitHubNetworkModule.getActiveToken()
            if (token.isNotEmpty()) {
                try {
                    val owner = GitHubNetworkModule.getConfiguredOwner()
                    val repo = GitHubNetworkModule.getDataRepo()
                    val branch = GitHubNetworkModule.getConfiguredBranch()
                    val filePath = "data/users/$userKey.json"

                    val metaResp = GitHubNetworkModule.apiService.getFileMetadata(owner, repo, filePath, branch)
                    val sha = if (metaResp.isSuccessful && metaResp.body() != null) {
                        val metaStr = metaResp.body()!!.string()
                        JSONObject(metaStr).optString("sha")
                    } else null

                    val base64Content = android.util.Base64.encodeToString(
                        userPayload.toString(2).toByteArray(Charsets.UTF_8),
                        android.util.Base64.NO_WRAP
                    )

                    val commitBody = JSONObject().apply {
                        put("message", "Sync user data for ${user.username}")
                        put("content", base64Content)
                        if (!sha.isNullOrBlank()) {
                            put("sha", sha)
                        }
                        put("branch", branch)
                    }

                    val requestBody = commitBody.toString()
                        .toRequestBody("application/json; charset=utf-8".toMediaType())

                    GitHubNetworkModule.apiService.updateFileContent(owner, repo, filePath, requestBody)
                } catch (e: Exception) {
                    Log.w(TAG, "GitHub remote sync notice: ${e.message}")
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing user data: ${e.message}", e)
            false
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun fetchCloudUserData(): CloudUserData? = withContext(Dispatchers.IO) {
        val user = _currentUserFlow.value ?: return@withContext null
        val userKey = user.username.lowercase().ifBlank { user.uid }

        // 1. Check local per-user storage first
        val storageKey = KEY_USER_DATA_PREFIX + userKey
        val raw = prefs.getString(storageKey, null)
        if (!raw.isNullOrBlank()) {
            try {
                val json = JSONObject(raw)
                val favsArr = json.optJSONArray("favorites") ?: JSONArray()
                val favs = (0 until favsArr.length()).map { favsArr.getString(it) }

                val laterArr = json.optJSONArray("readLater") ?: JSONArray()
                val later = (0 until laterArr.length()).map { laterArr.getString(it) }

                val histArr = json.optJSONArray("history") ?: JSONArray()
                val parsedHistory = (0 until histArr.length()).mapNotNull { i ->
                    val obj = histArr.getJSONObject(i)
                    ReadingHistoryEntry(
                        mangaId = obj.optString("mangaId"),
                        mangaTitle = obj.optString("mangaTitle"),
                        mangaCover = obj.optString("mangaCover").takeIf { it.isNotBlank() },
                        chapterNumber = obj.optInt("chapterNumber", 1),
                        chapterTitle = obj.optString("chapterTitle", ""),
                        pageNumber = obj.optInt("pageNumber", 1),
                        totalPages = obj.optInt("totalPages", 1),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                }

                val chapObj = json.optJSONObject("readChapters") ?: JSONObject()
                val parsedChapters = mutableMapOf<String, List<Int>>()
                val keys = chapObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val arr = chapObj.getJSONArray(k)
                    val list = (0 until arr.length()).map { arr.getInt(it) }
                    parsedChapters[k] = list
                }

                val lastSynced = json.optLong("lastSynced", System.currentTimeMillis())

                return@withContext CloudUserData(
                    favorites = favs,
                    readLater = later,
                    history = parsedHistory,
                    readChapters = parsedChapters,
                    lastSynced = lastSynced
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing local user data: ${e.message}")
            }
        }

        null
    }

    // =========================================================================
    // Admin Broadcast & Announcements
    // =========================================================================

    suspend fun postAdminAnnouncement(
        title: String,
        message: String,
        priority: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUserFlow.value
        if (user == null || !user.isAdmin) {
            return@withContext Result.failure(SecurityException("غير مصرح: صلاحية المشرف مطلوبة لنشر الإعلانات."))
        }

        val annJson = JSONObject().apply {
            put("id", "ann_${System.currentTimeMillis()}")
            put("title", title.trim())
            put("message", message.trim())
            put("priority", priority)
            put("timestamp", System.currentTimeMillis())
            put("active", true)
            put("authorEmail", user.displayName)
        }
        prefs.edit().putString(KEY_LOCAL_ANNOUNCEMENT, annJson.toString()).apply()
        Result.success(Unit)
    }

    suspend fun dismissAdminAnnouncement(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUserFlow.value
        if (user == null || !user.isAdmin) {
            return@withContext Result.failure(SecurityException("غير مصرح"))
        }
        prefs.edit().remove(KEY_LOCAL_ANNOUNCEMENT).apply()
        Result.success(Unit)
    }

    fun observeActiveAnnouncement(): Flow<AdminAnnouncement?> = flow {
        val localRaw = prefs.getString(KEY_LOCAL_ANNOUNCEMENT, null)
        if (localRaw != null) {
            try {
                val obj = JSONObject(localRaw)
                if (obj.optBoolean("active", false)) {
                    emit(
                        AdminAnnouncement(
                            id = obj.optString("id", "ann_1"),
                            title = obj.optString("title", ""),
                            message = obj.optString("message", ""),
                            priority = obj.optString("priority", "info"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            active = true,
                            authorEmail = obj.optString("authorEmail", "")
                        )
                    )
                } else {
                    emit(null)
                }
            } catch (e: Exception) {
                emit(null)
            }
        } else {
            emit(null)
        }
    }
}
