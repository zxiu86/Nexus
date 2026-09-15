package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import android.util.Patterns
import com.example.data.model.AdminAnnouncement
import com.example.data.model.AuthResult
import com.example.data.model.CloudUserData
import com.example.data.model.NexusUser
import com.example.data.model.ReadingHistoryEntry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(private val context: Context) {

    companion object {
        private const val TAG = "NexusAuthRepository"
        // The personal verified Admin Email linked to the app owner
        const val PRIMARY_ADMIN_EMAIL = "alsaid66900@gmail.com"

        private const val PREFS_NAME = "nexus_auth_prefs"
        private const val KEY_LAST_SYNC = "last_cloud_sync_time"
        private const val KEY_SAVED_USER_NAME = "saved_user_display_name"
        private const val KEY_SAVED_USER_EMAIL = "saved_user_email"
        private const val KEY_SAVED_USER_UID = "saved_user_uid"
        private const val KEY_SAVED_USER_PHOTO = "saved_user_photo"
        private const val KEY_SAVED_IS_ADMIN = "saved_is_admin"
        private const val KEY_REGISTERED_ACCOUNTS = "nexus_registered_accounts_json"
        private const val KEY_CLOUD_BACKUP_PREFIX = "nexus_cloud_data_"

        // Official Web Client ID and Secret provided for Nexus Firebase Auth
        const val DEFAULT_WEB_CLIENT_ID = "830681771668-r7044r7huaje8j1cusj0b1q456gc7ji2.apps.googleusercontent.com"
        const val DEFAULT_WEB_CLIENT_SECRET = "zaid^_^0110-_-zaid"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUserFlow = MutableStateFlow<NexusUser?>(null)
    val currentUserFlow: StateFlow<NexusUser?> = _currentUserFlow.asStateFlow()

    private val _isFirebaseConfigured = MutableStateFlow(false)
    val isFirebaseConfigured: StateFlow<Boolean> = _isFirebaseConfigured.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(prefs.getLong(KEY_LAST_SYNC, 0L))
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        initializeFirebaseSafely()
        restoreCachedUser()
    }

    private fun initializeFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            _isFirebaseConfigured.value = true
            Log.d(TAG, "Firebase successfully initialized for Nexus App")

            // Listen for Firebase Auth state changes
            firebaseAuth?.addAuthStateListener { auth ->
                val fbUser = auth.currentUser
                if (fbUser != null) {
                    val nexusUser = mapFirebaseUser(fbUser)
                    _currentUserFlow.value = nexusUser
                    cacheUser(nexusUser)
                    Log.d(TAG, "User state updated via Firebase: ${nexusUser.email}, isAdmin: ${nexusUser.isAdmin}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization notice: ${e.message}. Real Local/Cloud Auth fallback is active.")
            _isFirebaseConfigured.value = false
        }
    }

    private fun mapFirebaseUser(fbUser: FirebaseUser): NexusUser {
        val email = fbUser.email.orEmpty().trim()
        val isOwnerAdmin = isEmailAdmin(email)

        return NexusUser(
            uid = fbUser.uid,
            email = email,
            displayName = fbUser.displayName.takeIf { !it.isNullOrBlank() }
                ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
            photoUrl = fbUser.photoUrl?.toString(),
            isAdmin = isOwnerAdmin,
            createdAt = fbUser.metadata?.creationTimestamp ?: System.currentTimeMillis()
        )
    }

    /**
     * Checks if email qualifies for protected Admin privileges.
     * The admin is strictly tied to the verified personal account: alsaid66900@gmail.com
     */
    fun isEmailAdmin(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val cleanEmail = email.trim().lowercase()
        return cleanEmail == PRIMARY_ADMIN_EMAIL.lowercase()
    }

    private fun cacheUser(user: NexusUser) {
        prefs.edit()
            .putString(KEY_SAVED_USER_UID, user.uid)
            .putString(KEY_SAVED_USER_EMAIL, user.email)
            .putString(KEY_SAVED_USER_NAME, user.displayName)
            .putString(KEY_SAVED_USER_PHOTO, user.photoUrl)
            .putBoolean(KEY_SAVED_IS_ADMIN, user.isAdmin)
            .apply()
    }

    private fun clearCachedUser() {
        prefs.edit()
            .remove(KEY_SAVED_USER_UID)
            .remove(KEY_SAVED_USER_EMAIL)
            .remove(KEY_SAVED_USER_NAME)
            .remove(KEY_SAVED_USER_PHOTO)
            .remove(KEY_SAVED_IS_ADMIN)
            .apply()
    }

    private fun restoreCachedUser() {
        val uid = prefs.getString(KEY_SAVED_USER_UID, null)
        val email = prefs.getString(KEY_SAVED_USER_EMAIL, null)
        if (!uid.isNullOrBlank() && !email.isNullOrBlank()) {
            val name = prefs.getString(KEY_SAVED_USER_NAME, email.substringBefore("@")) ?: ""
            val photo = prefs.getString(KEY_SAVED_USER_PHOTO, null)
            val isAdmin = isEmailAdmin(email)
            _currentUserFlow.value = NexusUser(
                uid = uid,
                email = email,
                displayName = name,
                photoUrl = photo,
                isAdmin = isAdmin
            )
        }
    }

    // ==========================================
    // Local Account Store (SHA-256 Security)
    // ==========================================

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((password + salt).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getRegisteredAccounts(): JSONObject {
        val raw = prefs.getString(KEY_REGISTERED_ACCOUNTS, "{}") ?: "{}"
        return try {
            JSONObject(raw)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun saveRegisteredAccounts(accounts: JSONObject) {
        prefs.edit().putString(KEY_REGISTERED_ACCOUNTS, accounts.toString()).apply()
    }

    // ==========================================
    // Authentication Operations
    // ==========================================

    suspend fun signInWithEmail(email: String, pass: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = pass.trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext AuthResult.Error("صيغة البريد الإلكتروني غير صحيحة")
        }
        if (cleanPass.isBlank()) {
            return@withContext AuthResult.Error("يرجى إدخال كلمة المرور")
        }

        val auth = firebaseAuth
        if (auth != null) {
            try {
                val result = auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
                val fbUser = result.user
                if (fbUser != null) {
                    val nexusUser = mapFirebaseUser(fbUser)
                    _currentUserFlow.value = nexusUser
                    cacheUser(nexusUser)
                    return@withContext AuthResult.Success(nexusUser)
                }
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseAuth signIn failed, checking local store: ${e.message}")
            }
        }

        // Real persistent local store fallback
        val accounts = getRegisteredAccounts()
        if (!accounts.has(cleanEmail)) {
            // Special handling for the Master Admin: allow instant initial setup if admin
            if (isEmailAdmin(cleanEmail) && cleanPass.length >= 6) {
                val salt = UUID.randomUUID().toString().take(8)
                val passHash = hashPassword(cleanPass, salt)
                val adminUid = "admin_nexus_${System.currentTimeMillis()}"
                val accObj = JSONObject().apply {
                    put("uid", adminUid)
                    put("email", cleanEmail)
                    put("displayName", "المشرف العام (Admin)")
                    put("salt", salt)
                    put("passHash", passHash)
                    put("createdAt", System.currentTimeMillis())
                }
                accounts.put(cleanEmail, accObj)
                saveRegisteredAccounts(accounts)

                val adminUser = NexusUser(
                    uid = adminUid,
                    email = cleanEmail,
                    displayName = "المشرف العام (Admin)",
                    isAdmin = true
                )
                _currentUserFlow.value = adminUser
                cacheUser(adminUser)
                return@withContext AuthResult.Success(adminUser)
            }
            return@withContext AuthResult.Error("البريد الإلكتروني غير مسجل مسبقاً، يرجى إنشاء حساب جديد")
        }

        val accObj = accounts.getJSONObject(cleanEmail)
        val salt = accObj.optString("salt", "")
        val expectedHash = accObj.optString("passHash", "")
        val inputHash = hashPassword(cleanPass, salt)

        if (inputHash != expectedHash) {
            return@withContext AuthResult.Error("كلمة المرور غير صحيحة، يرجى التأكد وإعادة المحاولة")
        }

        val uid = accObj.optString("uid", "user_${System.currentTimeMillis()}")
        val displayName = accObj.optString("displayName", cleanEmail.substringBefore("@"))
        val photoUrl = accObj.optString("photoUrl", "").takeIf { it.isNotBlank() }
        val isAdmin = isEmailAdmin(cleanEmail)

        val nexusUser = NexusUser(
            uid = uid,
            email = cleanEmail,
            displayName = displayName,
            photoUrl = photoUrl,
            isAdmin = isAdmin,
            createdAt = accObj.optLong("createdAt", System.currentTimeMillis())
        )

        _currentUserFlow.value = nexusUser
        cacheUser(nexusUser)
        AuthResult.Success(nexusUser)
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = pass.trim()
        val cleanName = displayName.trim().ifBlank { cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() } }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext AuthResult.Error("صيغة البريد الإلكتروني غير صحيحة")
        }
        if (cleanPass.length < 6) {
            return@withContext AuthResult.Error("كلمة المرور يجب أن لا تقل عن 6 أحرف أو أرقام")
        }

        val accounts = getRegisteredAccounts()
        if (accounts.has(cleanEmail)) {
            return@withContext AuthResult.Error("هذا البريد الإلكتروني مسجل بالفعل، يمكنك تسجيل الدخول مباشرة")
        }

        var registeredUid = "nexus_uid_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        val auth = firebaseAuth
        if (auth != null) {
            try {
                val result = auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
                val fbUser = result.user
                if (fbUser != null) {
                    registeredUid = fbUser.uid
                    try {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(cleanName)
                            .build()
                        fbUser.updateProfile(profileUpdates).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to update Firebase profile name", e)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseAuth signUp warning: ${e.message}")
            }
        }

        // Store account permanently in local registry
        val salt = UUID.randomUUID().toString().take(8)
        val passHash = hashPassword(cleanPass, salt)
        val accObj = JSONObject().apply {
            put("uid", registeredUid)
            put("email", cleanEmail)
            put("displayName", cleanName)
            put("salt", salt)
            put("passHash", passHash)
            put("createdAt", System.currentTimeMillis())
        }
        accounts.put(cleanEmail, accObj)
        saveRegisteredAccounts(accounts)

        val nexusUser = NexusUser(
            uid = registeredUid,
            email = cleanEmail,
            displayName = cleanName,
            isAdmin = isEmailAdmin(cleanEmail),
            createdAt = System.currentTimeMillis()
        )

        _currentUserFlow.value = nexusUser
        cacheUser(nexusUser)
        AuthResult.Success(nexusUser)
    }

    suspend fun signInWithGoogleCredential(idToken: String): AuthResult = withContext(Dispatchers.IO) {
        if (idToken.isBlank()) {
            return@withContext AuthResult.Error("لم يتم استلام رمز اعتماد Google")
        }

        // Parse token JWT payload to extract user profile
        val parsedInfo = parseGoogleIdTokenPayload(idToken)

        val auth = firebaseAuth
        if (auth != null) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val fbUser = result.user
                if (fbUser != null) {
                    val nexusUser = mapFirebaseUser(fbUser)
                    _currentUserFlow.value = nexusUser
                    cacheUser(nexusUser)
                    return@withContext AuthResult.Success(nexusUser)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firebase credential sign-in error, using parsed token info: ${e.message}")
            }
        }

        if (parsedInfo != null && parsedInfo.email.isNotBlank()) {
            val isOwnerAdmin = isEmailAdmin(parsedInfo.email)
            val nexusUser = NexusUser(
                uid = parsedInfo.sub.ifBlank { "google_${UUID.randomUUID().toString().take(12)}" },
                email = parsedInfo.email,
                displayName = parsedInfo.name.ifBlank { parsedInfo.email.substringBefore("@") },
                photoUrl = parsedInfo.picture.takeIf { it.isNotBlank() },
                isAdmin = isOwnerAdmin,
                createdAt = System.currentTimeMillis()
            )
            _currentUserFlow.value = nexusUser
            cacheUser(nexusUser)
            return@withContext AuthResult.Success(nexusUser)
        }

        AuthResult.Error("تعذر استخراج بيانات حساب Google، يرجى المحاولة لاحقاً")
    }

    private data class GoogleTokenInfo(
        val sub: String,
        val email: String,
        val name: String,
        val picture: String
    )

    private fun parseGoogleIdTokenPayload(idToken: String): GoogleTokenInfo? {
        return try {
            val parts = idToken.split(".")
            if (parts.size >= 2) {
                val payloadBytes = Base64.decode(
                    parts[1],
                    Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                )
                val jsonString = String(payloadBytes, Charsets.UTF_8)
                val json = JSONObject(jsonString)
                val sub = json.optString("sub", "")
                val email = json.optString("email", "")
                val name = json.optString("name", json.optString("given_name", ""))
                val picture = json.optString("picture", "")
                GoogleTokenInfo(sub = sub, email = email, name = name, picture = picture)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding Google ID token payload", e)
            null
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext Result.failure(Exception("صيغة البريد الإلكتروني غير صحيحة"))
        }

        val auth = firebaseAuth
        if (auth != null) {
            try {
                auth.sendPasswordResetEmail(cleanEmail).await()
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseAuth password reset notice: ${e.message}")
            }
        }

        // Check local store
        val accounts = getRegisteredAccounts()
        if (accounts.has(cleanEmail)) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("البريد الإلكتروني غير مسجل مسبقاً في التطبيق"))
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out error: ${e.message}")
        }
        _currentUserFlow.value = null
        clearCachedUser()
    }

    // ==========================================
    // Cloud Sync Operations (Firestore & Persistent Backup)
    // ==========================================

    suspend fun syncDataToCloud(
        favorites: Set<String>,
        readLater: Set<String>,
        history: List<ReadingHistoryEntry>,
        readChapters: Map<String, Set<Int>>
    ): Boolean = withContext(Dispatchers.IO) {
        val currentUser = _currentUserFlow.value ?: return@withContext false
        _isSyncing.value = true

        try {
            // 1. Save locally per user UID to guarantee data survival
            val backupKey = KEY_CLOUD_BACKUP_PREFIX + currentUser.uid
            val backupJson = JSONObject().apply {
                put("favorites", org.json.JSONArray(favorites.toList()))
                put("readLater", org.json.JSONArray(readLater.toList()))
                val historyArr = org.json.JSONArray()
                history.take(100).forEach { entry ->
                    val obj = JSONObject().apply {
                        put("mangaId", entry.mangaId)
                        put("mangaTitle", entry.mangaTitle)
                        put("mangaCover", entry.mangaCover ?: "")
                        put("chapterNumber", entry.chapterNumber)
                        put("chapterTitle", entry.chapterTitle)
                        put("pageNumber", entry.pageNumber)
                        put("totalPages", entry.totalPages)
                        put("timestamp", entry.timestamp)
                    }
                    historyArr.put(obj)
                }
                put("history", historyArr)
                val chaptersObj = JSONObject()
                readChapters.forEach { (mId, set) ->
                    chaptersObj.put(mId, org.json.JSONArray(set.toList()))
                }
                put("readChapters", chaptersObj)
                put("lastSynced", System.currentTimeMillis())
            }
            prefs.edit().putString(backupKey, backupJson.toString()).apply()

            // 2. Sync to Firestore if available
            val db = firestore
            if (db != null) {
                val historyData = history.take(100).map { entry ->
                    mapOf(
                        "mangaId" to entry.mangaId,
                        "mangaTitle" to entry.mangaTitle,
                        "mangaCover" to (entry.mangaCover ?: ""),
                        "chapterNumber" to entry.chapterNumber,
                        "chapterTitle" to entry.chapterTitle,
                        "pageNumber" to entry.pageNumber,
                        "totalPages" to entry.totalPages,
                        "timestamp" to entry.timestamp
                    )
                }
                val readChaptersData = readChapters.mapValues { it.value.toList() }
                val cloudPayload = hashMapOf(
                    "uid" to currentUser.uid,
                    "email" to currentUser.email,
                    "displayName" to currentUser.displayName,
                    "favorites" to favorites.toList(),
                    "readLater" to readLater.toList(),
                    "history" to historyData,
                    "readChapters" to readChaptersData,
                    "lastSynced" to System.currentTimeMillis()
                )
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("nexus_cloud_data")
                    .document("user_library")
                    .set(cloudPayload, SetOptions.merge())
                    .await()
            }

            val now = System.currentTimeMillis()
            _lastSyncTimestamp.value = now
            prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
            Log.d(TAG, "Cloud & local backup sync successful for user: ${currentUser.email}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync data to cloud", e)
            false
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun fetchCloudUserData(): CloudUserData? = withContext(Dispatchers.IO) {
        val currentUser = _currentUserFlow.value ?: return@withContext null

        // Try Firestore first
        val db = firestore
        if (db != null) {
            try {
                val doc = db.collection("users")
                    .document(currentUser.uid)
                    .collection("nexus_cloud_data")
                    .document("user_library")
                    .get()
                    .await()

                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val favs = (doc.get("favorites") as? List<String>) ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    val later = (doc.get("readLater") as? List<String>) ?: emptyList()

                    @Suppress("UNCHECKED_CAST")
                    val rawHistory = (doc.get("history") as? List<Map<String, Any>>) ?: emptyList()
                    val parsedHistory = rawHistory.mapNotNull { map ->
                        try {
                            ReadingHistoryEntry(
                                mangaId = map["mangaId"] as? String ?: return@mapNotNull null,
                                mangaTitle = map["mangaTitle"] as? String ?: "",
                                mangaCover = (map["mangaCover"] as? String)?.takeIf { it.isNotBlank() },
                                chapterNumber = (map["chapterNumber"] as? Number)?.toInt() ?: 1,
                                chapterTitle = map["chapterTitle"] as? String ?: "",
                                pageNumber = (map["pageNumber"] as? Number)?.toInt() ?: 1,
                                totalPages = (map["totalPages"] as? Number)?.toInt() ?: 1,
                                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    @Suppress("UNCHECKED_CAST")
                    val rawChapters = (doc.get("readChapters") as? Map<String, List<Long>>) ?: emptyMap()
                    val parsedChapters = rawChapters.mapValues { (_, v) -> v.map { it.toInt() } }
                    val lastSynced = doc.getLong("lastSynced") ?: System.currentTimeMillis()

                    return@withContext CloudUserData(
                        favorites = favs,
                        readLater = later,
                        history = parsedHistory,
                        readChapters = parsedChapters,
                        lastSynced = lastSynced
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore fetch notice: ${e.message}, falling back to local user store")
            }
        }

        // Restore from local user backup
        val backupKey = KEY_CLOUD_BACKUP_PREFIX + currentUser.uid
        val rawBackup = prefs.getString(backupKey, null) ?: return@withContext null

        try {
            val json = JSONObject(rawBackup)
            val favsArr = json.optJSONArray("favorites") ?: org.json.JSONArray()
            val favs = (0 until favsArr.length()).map { favsArr.getString(it) }

            val laterArr = json.optJSONArray("readLater") ?: org.json.JSONArray()
            val later = (0 until laterArr.length()).map { laterArr.getString(it) }

            val histArr = json.optJSONArray("history") ?: org.json.JSONArray()
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

            CloudUserData(
                favorites = favs,
                readLater = later,
                history = parsedHistory,
                readChapters = parsedChapters,
                lastSynced = lastSynced
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse local cloud backup", e)
            null
        }
    }

    // ==========================================
    // Admin Broadcast & Announcements (Protected)
    // ==========================================

    suspend fun postAdminAnnouncement(
        title: String,
        message: String,
        priority: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUserFlow.value
        if (user == null || !user.isAdmin) {
            return@withContext Result.failure(SecurityException("غير مصرح: صلاحية المشرف مطلوبة لنشر الإعلانات."))
        }

        val db = firestore
        if (db != null) {
            try {
                val announcement = hashMapOf(
                    "title" to title.trim(),
                    "message" to message.trim(),
                    "priority" to priority,
                    "timestamp" to System.currentTimeMillis(),
                    "active" to true,
                    "authorEmail" to user.email
                )

                db.collection("nexus_announcements")
                    .document("active_broadcast")
                    .set(announcement)
                    .await()

                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                Log.w(TAG, "Firestore post announcement notice: ${e.message}")
            }
        }

        // Local announcement fallback
        val annJson = JSONObject().apply {
            put("id", "local_ann_${System.currentTimeMillis()}")
            put("title", title.trim())
            put("message", message.trim())
            put("priority", priority)
            put("timestamp", System.currentTimeMillis())
            put("active", true)
            put("authorEmail", user.email)
        }
        prefs.edit().putString("nexus_local_announcement", annJson.toString()).apply()
        Result.success(Unit)
    }

    suspend fun dismissAdminAnnouncement(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUserFlow.value
        if (user == null || !user.isAdmin) {
            return@withContext Result.failure(SecurityException("غير مصرح"))
        }

        val db = firestore
        if (db != null) {
            try {
                db.collection("nexus_announcements")
                    .document("active_broadcast")
                    .update("active", false)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore dismiss announcement notice: ${e.message}")
            }
        }

        prefs.edit().remove("nexus_local_announcement").apply()
        Result.success(Unit)
    }

    fun observeActiveAnnouncement(): Flow<AdminAnnouncement?> = callbackFlow {
        val db = firestore
        if (db == null) {
            val localRaw = prefs.getString("nexus_local_announcement", null)
            if (localRaw != null) {
                try {
                    val obj = JSONObject(localRaw)
                    if (obj.optBoolean("active", false)) {
                        trySend(
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
                        trySend(null)
                    }
                } catch (e: Exception) {
                    trySend(null)
                }
            } else {
                trySend(null)
            }
            close()
            return@callbackFlow
        }

        val registration = db.collection("nexus_announcements")
            .document("active_broadcast")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val active = snapshot.getBoolean("active") ?: false
                if (!active) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val announcement = AdminAnnouncement(
                    id = snapshot.id,
                    title = snapshot.getString("title").orEmpty(),
                    message = snapshot.getString("message").orEmpty(),
                    priority = snapshot.getString("priority") ?: "info",
                    timestamp = snapshot.getLong("timestamp") ?: System.currentTimeMillis(),
                    active = true,
                    authorEmail = snapshot.getString("authorEmail").orEmpty()
                )
                trySend(announcement)
            }

        awaitClose { registration.remove() }
    }
}
