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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

/**
 * Real user authentication and per-user data persistence repository for Nexus.
 *
 * File & Folder Architecture:
 * - Local storage: user/user.json (auto-created if missing)
 * - Remote GitHub storage: user/user.json on github/zxiu86/Data (auto-created and queried)
 * - Organized structure:
 *   {
 *      "meta": { "version": "2.0.0", "updatedAt": ... },
 *      "users": {
 *         "zxiuzaid": { ... "data": { "favorites": [...], "readLater": [...], "history": [...] } },
 *         "another_user": { ... }
 *      }
 *   }
 */
class AuthRepository(private val context: Context) {

    companion object {
        private const val TAG = "NexusAuthRepository"

        // Master Admin Credentials
        const val MASTER_ADMIN_USERNAME = "zxiuzaid"
        const val MASTER_ADMIN_PASSWORD = "za/id/20/10"
        const val PRIMARY_ADMIN_EMAIL = "alsaid66900@gmail.com"

        // Local & Remote File Paths
        const val USER_DIR_NAME = "user"
        const val USER_FILE_NAME = "user.json"
        const val GITHUB_USER_FILE_PATH = "user/user.json"

        private const val PREFS_NAME = "nexus_symbolic_auth_prefs"
        private const val KEY_LAST_SYNC = "last_cloud_sync_time"
        private const val KEY_CURRENT_USER_JSON = "current_active_user_json"
        private const val KEY_LOCAL_ANNOUNCEMENT = "nexus_local_announcement"

        const val DEFAULT_WEB_CLIENT_ID = "830681771668-r7044r7huaje8j1cusj0b1q456gc7ji2.apps.googleusercontent.com"
        const val DEFAULT_WEB_CLIENT_SECRET = "zaid^_^0110-_-zaid"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val userDir: File = File(context.filesDir, USER_DIR_NAME)
    private val userJsonFile: File = File(userDir, USER_FILE_NAME)

    private val _currentUserFlow = MutableStateFlow<NexusUser?>(null)
    val currentUserFlow: StateFlow<NexusUser?> = _currentUserFlow.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val isFirebaseConfigured: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(prefs.getLong(KEY_LAST_SYNC, 0L))
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        ensureUserDirectoryAndFile()
        restoreCachedUser()
        coroutineScope.launch {
            syncWithRemoteGitHubUserFileIfAvailable()
        }
    }

    // =========================================================================
    // Directory & File Management (Auto-creation of user/user.json)
    // =========================================================================

    /**
     * Checks if directory 'user' and file 'user/user.json' exist; if not,
     * automatically creates the directory and file with master admin seeded.
     */
    @Synchronized
    private fun ensureUserDirectoryAndFile(): JSONObject {
        try {
            if (!userDir.exists()) {
                val created = userDir.mkdirs()
                Log.d(TAG, "Created user directory: $created at ${userDir.absolutePath}")
            }

            if (!userJsonFile.exists() || userJsonFile.length() == 0L) {
                val defaultDb = createDefaultUserJsonDatabase()
                writeUserJsonToDisk(defaultDb)
                return defaultDb
            }

            // File exists: read and validate
            val content = userJsonFile.readText(Charsets.UTF_8)
            val json = try {
                JSONObject(content)
            } catch (e: Exception) {
                Log.w(TAG, "Invalid user.json file on disk, reinitializing...")
                createDefaultUserJsonDatabase()
            }

            // Ensure 'users' object and master admin exist
            if (!json.has("users")) {
                json.put("users", JSONObject())
            }
            ensureMasterAdminInJson(json)
            writeUserJsonToDisk(json)
            return json
        } catch (e: Exception) {
            Log.e(TAG, "Error in ensureUserDirectoryAndFile: ${e.message}", e)
            return createDefaultUserJsonDatabase()
        }
    }

    private fun createDefaultUserJsonDatabase(): JSONObject {
        val root = JSONObject()
        val meta = JSONObject().apply {
            put("app", "Nexus Manga")
            put("version", "2.0.1")
            put("format", "nexus_user_database")
            put("createdAt", System.currentTimeMillis())
            put("updatedAt", System.currentTimeMillis())
        }
        root.put("meta", meta)

        val users = JSONObject()
        root.put("users", users)

        ensureMasterAdminInJson(root)
        return root
    }

    private fun ensureMasterAdminInJson(root: JSONObject) {
        val users = root.optJSONObject("users") ?: JSONObject().also { root.put("users", it) }
        val adminKey = MASTER_ADMIN_USERNAME.lowercase()

        if (!users.has(adminKey)) {
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
                put("data", JSONObject().apply {
                    put("favorites", JSONArray())
                    put("readLater", JSONArray())
                    put("history", JSONArray())
                    put("readChapters", JSONObject())
                    put("lastSynced", System.currentTimeMillis())
                })
            }
            users.put(adminKey, adminObj)
        }
    }

    @Synchronized
    private fun writeUserJsonToDisk(json: JSONObject) {
        try {
            if (!userDir.exists()) userDir.mkdirs()
            val tempFile = File(userDir, "user.json.tmp")
            FileOutputStream(tempFile).use { fos ->
                fos.write(json.toString(2).toByteArray(Charsets.UTF_8))
                fos.flush()
            }
            if (tempFile.exists()) {
                if (userJsonFile.exists()) userJsonFile.delete()
                tempFile.renameTo(userJsonFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing user.json to disk: ${e.message}", e)
        }
    }

    /**
     * Reads the current users database from user/user.json
     */
    @Synchronized
    fun getUsersDatabase(): JSONObject {
        return try {
            if (!userJsonFile.exists()) {
                ensureUserDirectoryAndFile()
            } else {
                val content = userJsonFile.readText(Charsets.UTF_8)
                val obj = JSONObject(content)
                if (!obj.has("users")) {
                    obj.put("users", JSONObject())
                }
                ensureMasterAdminInJson(obj)
                obj
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading user.json, recreating: ${e.message}")
            ensureUserDirectoryAndFile()
        }
    }

    /**
     * Saves changes to user/user.json and triggers remote GitHub sync
     */
    @Synchronized
    fun saveUsersDatabase(db: JSONObject) {
        try {
            val meta = db.optJSONObject("meta") ?: JSONObject().also { db.put("meta", it) }
            meta.put("updatedAt", System.currentTimeMillis())
            writeUserJsonToDisk(db)

            // Trigger background GitHub sync if token is available
            coroutineScope.launch {
                pushUserJsonToGitHub(db)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving users database: ${e.message}", e)
        }
    }

    // =========================================================================
    // Remote GitHub Sync for user/user.json & users/users.json
    // =========================================================================

    private suspend fun syncWithRemoteGitHubUserFileIfAvailable() = withContext(Dispatchers.IO) {
        try {
            val owner = GitHubNetworkModule.getConfiguredOwner()
            val repo = GitHubNetworkModule.getDataRepo()
            val branch = GitHubNetworkModule.getConfiguredBranch()

            // 1. Try direct raw URLs first (faster and bypasses rate limits)
            val directRawUrls = listOf(
                "https://raw.githubusercontent.com/$owner/$repo/$branch/$GITHUB_USER_FILE_PATH",
                "https://raw.githubusercontent.com/$owner/$repo/$branch/users/users.json"
            )

            var remoteContent: String? = null
            for (rawUrl in directRawUrls) {
                val fetched = GitHubNetworkModule.fetchDirectRaw(rawUrl, forceFresh = true)
                if (!fetched.isNullOrBlank() && fetched.trim().startsWith("{")) {
                    remoteContent = fetched
                    break
                }
            }

            // 2. If raw not found, try GitHub API
            if (remoteContent == null) {
                val response = GitHubNetworkModule.apiService.getContentRaw(owner, repo, GITHUB_USER_FILE_PATH, branch)
                if (response.isSuccessful && response.body() != null) {
                    val rawStr = response.body()!!.string()
                    val decoded = decodeGitHubContent(rawStr)
                    if (decoded.isNotBlank() && decoded.trim().startsWith("{")) {
                        remoteContent = decoded
                    }
                } else if (response.code() == 404) {
                    // Remote file does not exist yet; create it on GitHub repository
                    Log.d(TAG, "user/user.json does not exist on GitHub, pushing initial file...")
                    val localJson = getUsersDatabase()
                    pushUserJsonToGitHub(localJson)
                    return@withContext
                }
            }

            if (!remoteContent.isNullOrBlank()) {
                val remoteJson = JSONObject(remoteContent)
                val localJson = getUsersDatabase()
                val merged = mergeUsersDatabases(localJson, remoteJson)
                writeUserJsonToDisk(merged)
                Log.d(TAG, "Successfully synced user/user.json from GitHub ($owner/$repo on branch $branch)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Remote GitHub sync for user/user.json: ${e.message}")
        }
    }

    private suspend fun pushUserJsonToGitHub(json: JSONObject) = withContext(Dispatchers.IO) {
        val token = GitHubNetworkModule.getActiveToken()
        if (token.isEmpty()) {
            Log.w(TAG, "GitHub token is empty, skipping remote push for user/user.json")
            return@withContext
        }

        try {
            val jsonStr = json.toString(2)
            val commitMsg = "[Nexus 2.0.1] Update user accounts database"

            // Primary path: user/user.json
            val res1 = GitHubNetworkModule.pushOrUpdateFileToGitHub(
                path = GITHUB_USER_FILE_PATH,
                contentString = jsonStr,
                commitMessage = commitMsg
            )

            // Secondary path: users/users.json for dual compatibility
            val res2 = GitHubNetworkModule.pushOrUpdateFileToGitHub(
                path = "users/users.json",
                contentString = jsonStr,
                commitMessage = commitMsg
            )

            if (res1.isSuccess || res2.isSuccess) {
                Log.d(TAG, "Successfully committed user accounts to GitHub repo")
            } else {
                Log.w(TAG, "Notice: GitHub push for user/user.json: ${res1.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote GitHub push failed: ${e.message}")
        }
    }

    suspend fun forceSyncUsersWithGitHub(): Result<String> = withContext(Dispatchers.IO) {
        val token = GitHubNetworkModule.getActiveToken()
        if (token.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("رمز الوصول (GitHub Token) غير مضبوط. يرجى ضبط الرمز من لوحة المشرف لإتمام المزامنة السحابية."))
        }

        try {
            val localJson = getUsersDatabase()
            val usersCount = localJson.optJSONObject("users")?.length() ?: 0
            val jsonStr = localJson.toString(2)
            val commitMsg = "[Nexus 2.0.1] Force sync user database ($usersCount users)"

            val res = GitHubNetworkModule.pushOrUpdateFileToGitHub(
                path = GITHUB_USER_FILE_PATH,
                contentString = jsonStr,
                commitMessage = commitMsg
            )

            // Also mirror to users/users.json
            GitHubNetworkModule.pushOrUpdateFileToGitHub(
                path = "users/users.json",
                contentString = jsonStr,
                commitMessage = commitMsg
            )

            if (res.isSuccess) {
                Result.success("تمت مزامنة بيانات المستخدمين بنجاح مع GitHub ($usersCount مستخدمين مسجلين).")
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("فشل رفع ملف user/user.json إلى GitHub"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decodeGitHubContent(rawContent: String): String {
        val trimmed = rawContent.trim()
        if (trimmed.startsWith("{") && trimmed.contains("\"content\"") && trimmed.contains("\"encoding\"")) {
            try {
                val jsonObject = JSONObject(trimmed)
                if (jsonObject.optString("encoding") == "base64") {
                    val base64Content = jsonObject.optString("content").replace("\n", "").replace("\r", "").replace(" ", "")
                    val decodedBytes = android.util.Base64.decode(base64Content, android.util.Base64.DEFAULT)
                    return String(decodedBytes, Charsets.UTF_8)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed base64 decode for github content: ${e.message}")
            }
        }
        return rawContent
    }

    private fun mergeUsersDatabases(local: JSONObject, remote: JSONObject): JSONObject {
        val localUsers = local.optJSONObject("users") ?: JSONObject()
        val remoteUsers = remote.optJSONObject("users") ?: JSONObject()

        val rKeys = remoteUsers.keys()
        while (rKeys.hasNext()) {
            val key = rKeys.next()
            if (!localUsers.has(key)) {
                localUsers.put(key, remoteUsers.getJSONObject(key))
            } else {
                val localU = localUsers.getJSONObject(key)
                val remoteU = remoteUsers.getJSONObject(key)
                val lSync = localU.optJSONObject("data")?.optLong("lastSynced", 0L) ?: 0L
                val rSync = remoteU.optJSONObject("data")?.optLong("lastSynced", 0L) ?: 0L
                if (rSync > lSync) {
                    localUsers.put(key, remoteU)
                }
            }
        }
        local.put("users", localUsers)
        ensureMasterAdminInJson(local)
        return local
    }

    // =========================================================================
    // Security & Hashing Helpers
    // =========================================================================

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((password + salt).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
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
    // Core Symbolic Auth Operations (Username & Password with user/user.json)
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

        // 2. Query user/user.json database
        val db = getUsersDatabase()
        val usersObj = db.optJSONObject("users") ?: JSONObject()

        if (!usersObj.has(key)) {
            return@withContext AuthResult.Error("اسم المستخدم غير مسجل مسبقاً، يرجى إنشاء حساب جديد")
        }

        val userObj = usersObj.getJSONObject(key)
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

        // 2. Prevent duplicate usernames in user/user.json
        val db = getUsersDatabase()
        val usersObj = db.optJSONObject("users") ?: JSONObject().also { db.put("users", it) }

        if (usersObj.has(key)) {
            return@withContext AuthResult.Error("اسم المستخدم مسجل مسبقاً، يرجى اختيار اسم آخر أو تسجيل الدخول")
        }

        // 3. Register new user into user/user.json
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
            put("data", JSONObject().apply {
                put("favorites", JSONArray())
                put("readLater", JSONArray())
                put("history", JSONArray())
                put("readChapters", JSONObject())
                put("lastSynced", System.currentTimeMillis())
            })
        }

        usersObj.put(key, newUserObj)
        saveUsersDatabase(db)

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
    // Saved in user/user.json without data loss
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
            val db = getUsersDatabase()
            val usersObj = db.optJSONObject("users") ?: JSONObject().also { db.put("users", it) }

            val targetUserObj = if (usersObj.has(userKey)) {
                usersObj.getJSONObject(userKey)
            } else {
                JSONObject().apply {
                    put("uid", user.uid)
                    put("username", user.username)
                    put("displayName", user.displayName)
                    put("email", user.email)
                    put("isAdmin", user.isAdmin)
                    put("createdAt", user.createdAt)
                    usersObj.put(userKey, this)
                }
            }

            val dataObj = JSONObject().apply {
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

            targetUserObj.put("data", dataObj)
            saveUsersDatabase(db)

            prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
            _lastSyncTimestamp.value = System.currentTimeMillis()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed syncing user data to user/user.json: ${e.message}", e)
            false
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun fetchCloudUserData(): CloudUserData? = withContext(Dispatchers.IO) {
        val user = _currentUserFlow.value ?: return@withContext null
        val userKey = user.username.lowercase().ifBlank { user.uid }

        try {
            val db = getUsersDatabase()
            val usersObj = db.optJSONObject("users") ?: return@withContext null
            if (!usersObj.has(userKey)) return@withContext null

            val userObj = usersObj.getJSONObject(userKey)
            val dataObj = userObj.optJSONObject("data") ?: return@withContext null

            val favsArr = dataObj.optJSONArray("favorites") ?: JSONArray()
            val favs = (0 until favsArr.length()).map { favsArr.getString(it) }

            val laterArr = dataObj.optJSONArray("readLater") ?: JSONArray()
            val later = (0 until laterArr.length()).map { laterArr.getString(it) }

            val histArr = dataObj.optJSONArray("history") ?: JSONArray()
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

            val chapObj = dataObj.optJSONObject("readChapters") ?: JSONObject()
            val parsedChapters = mutableMapOf<String, List<Int>>()
            val keys = chapObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = chapObj.getJSONArray(k)
                val list = (0 until arr.length()).map { arr.getInt(it) }
                parsedChapters[k] = list
            }

            val lastSynced = dataObj.optLong("lastSynced", System.currentTimeMillis())

            return@withContext CloudUserData(
                favorites = favs,
                readLater = later,
                history = parsedHistory,
                readChapters = parsedChapters,
                lastSynced = lastSynced
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error querying user data from user/user.json: ${e.message}")
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

        // Push announcement to GitHub repo (data/announcements.json)
        try {
            GitHubNetworkModule.pushOrUpdateFileToGitHub(
                path = "data/announcements.json",
                contentString = annJson.toString(2),
                commitMessage = "[Nexus 2.0.1] Admin broadcast: $title"
            )
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Announcement saved locally, GitHub push error: ${e.message}")
        }

        Result.success(Unit)
    }

    suspend fun dismissAdminAnnouncement(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUserFlow.value
        if (user == null || !user.isAdmin) {
            return@withContext Result.failure(SecurityException("غير مصرح"))
        }
        prefs.edit().remove(KEY_LOCAL_ANNOUNCEMENT).apply()

        try {
            val emptyObj = JSONObject().apply {
                put("active", false)
                put("timestamp", System.currentTimeMillis())
            }
            GitHubNetworkModule.pushOrUpdateFileToGitHub(
                path = "data/announcements.json",
                contentString = emptyObj.toString(2),
                commitMessage = "[Nexus 2.0.1] Dismiss broadcast announcement"
            )
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Local announcement dismissed, GitHub dismiss error: ${e.message}")
        }

        Result.success(Unit)
    }

    fun observeActiveAnnouncement(): Flow<AdminAnnouncement?> = flow {
        // 1. Emit local cached announcement first
        val localRaw = prefs.getString(KEY_LOCAL_ANNOUNCEMENT, null)
        var hasEmitted = false
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
                    hasEmitted = true
                }
            } catch (_: Exception) {}
        }

        // 2. Fetch remote announcement from GitHub
        try {
            val owner = GitHubNetworkModule.getConfiguredOwner()
            val repo = GitHubNetworkModule.getDataRepo()
            val branch = GitHubNetworkModule.getConfiguredBranch()
            val remoteUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/data/announcements.json"
            val remoteContent = GitHubNetworkModule.fetchDirectRaw(remoteUrl, forceFresh = true)
            if (!remoteContent.isNullOrBlank()) {
                val remoteObj = JSONObject(remoteContent)
                if (remoteObj.optBoolean("active", false)) {
                    val announcement = AdminAnnouncement(
                        id = remoteObj.optString("id", "ann_1"),
                        title = remoteObj.optString("title", ""),
                        message = remoteObj.optString("message", ""),
                        priority = remoteObj.optString("priority", "info"),
                        timestamp = remoteObj.optLong("timestamp", System.currentTimeMillis()),
                        active = true,
                        authorEmail = remoteObj.optString("authorEmail", "")
                    )
                    prefs.edit().putString(KEY_LOCAL_ANNOUNCEMENT, remoteObj.toString()).apply()
                    emit(announcement)
                    return@flow
                } else {
                    prefs.edit().remove(KEY_LOCAL_ANNOUNCEMENT).apply()
                    emit(null)
                    return@flow
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Notice: Fetching remote announcements: ${e.message}")
        }

        if (!hasEmitted) {
            emit(null)
        }
    }
}

