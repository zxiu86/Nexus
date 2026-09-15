package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
        private const val KEY_SAVED_IS_ADMIN = "saved_is_admin"

        // Official Web Client ID for Google Sign-In provided for Nexus
        const val DEFAULT_WEB_CLIENT_ID = "830681771668-r7044r7huaje8j1cusj0b1q456gc7ji2.apps.googleusercontent.com"
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

            // Listen for Auth changes
            firebaseAuth?.addAuthStateListener { auth ->
                val fbUser = auth.currentUser
                if (fbUser != null) {
                    val nexusUser = mapFirebaseUser(fbUser)
                    _currentUserFlow.value = nexusUser
                    cacheUser(nexusUser)
                    Log.d(TAG, "User state updated: ${nexusUser.email}, isAdmin: ${nexusUser.isAdmin}")
                } else {
                    _currentUserFlow.value = null
                    clearCachedUser()
                    Log.d(TAG, "User signed out")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization warning (google-services.json might be missing): ${e.message}")
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
     * The admin is strictly tied to the verified personal account, not a hardcoded bypass.
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
            .putBoolean(KEY_SAVED_IS_ADMIN, user.isAdmin)
            .apply()
    }

    private fun clearCachedUser() {
        prefs.edit()
            .remove(KEY_SAVED_USER_UID)
            .remove(KEY_SAVED_USER_EMAIL)
            .remove(KEY_SAVED_USER_NAME)
            .remove(KEY_SAVED_IS_ADMIN)
            .apply()
    }

    private fun restoreCachedUser() {
        val uid = prefs.getString(KEY_SAVED_USER_UID, null)
        val email = prefs.getString(KEY_SAVED_USER_EMAIL, null)
        if (!uid.isNullOrBlank() && !email.isNullOrBlank()) {
            val name = prefs.getString(KEY_SAVED_USER_NAME, email.substringBefore("@")) ?: ""
            val isAdmin = isEmailAdmin(email)
            _currentUserFlow.value = NexusUser(
                uid = uid,
                email = email,
                displayName = name,
                isAdmin = isAdmin
            )
        }
    }

    // ==========================================
    // Authentication Operations
    // ==========================================

    suspend fun signInWithEmail(email: String, pass: String): AuthResult = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Firebase غير مهيأ. تأكد من توفر خدمات جوجل.")
        try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val fbUser = result.user ?: return@withContext AuthResult.Error("فشل تسجيل الدخول: لا يوجد مستخدم")
            val nexusUser = mapFirebaseUser(fbUser)
            _currentUserFlow.value = nexusUser
            cacheUser(nexusUser)
            AuthResult.Success(nexusUser)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail error", e)
            val arabicMsg = when {
                e.message?.contains("no user record", ignoreCase = true) == true -> "البريد الإلكتروني غير مسجل مسبقاً"
                e.message?.contains("password is invalid", ignoreCase = true) == true ||
                        e.message?.contains("wrong-password", ignoreCase = true) == true -> "كلمة المرور غير صحيحة"
                e.message?.contains("invalid-email", ignoreCase = true) == true -> "صيغة البريد الإلكتروني غير صحيحة"
                e.message?.contains("network", ignoreCase = true) == true -> "تعذر الاتصال بالشبكة، تحقق من اتصالك"
                else -> e.localizedMessage ?: "حدث خطأ أثناء تسجيل الدخول"
            }
            AuthResult.Error(arabicMsg)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): AuthResult = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Firebase غير مهيأ.")
        try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val fbUser = result.user ?: return@withContext AuthResult.Error("فشل إنشاء الحساب")

            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                fbUser.updateProfile(profileUpdates).await()
            }

            val nexusUser = mapFirebaseUser(fbUser).copy(displayName = displayName.ifBlank { fbUser.displayName.orEmpty() })
            _currentUserFlow.value = nexusUser
            cacheUser(nexusUser)
            AuthResult.Success(nexusUser)
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithEmail error", e)
            val arabicMsg = when {
                e.message?.contains("email-already-in-use", ignoreCase = true) == true -> "هذا البريد الإلكتروني مسجل بالفعل، يمكنك تسجيل الدخول"
                e.message?.contains("weak-password", ignoreCase = true) == true -> "كلمة المرور ضعيفة، يجب أن لا تقل عن 6 أحرف"
                e.message?.contains("invalid-email", ignoreCase = true) == true -> "البريد الإلكتروني غير صالح"
                else -> e.localizedMessage ?: "حدث خطأ أثناء إنشاء الحساب"
            }
            AuthResult.Error(arabicMsg)
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): AuthResult = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Firebase غير مهيأ.")
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val fbUser = result.user ?: return@withContext AuthResult.Error("فشل تسجيل الدخول بحساب Google")
            val nexusUser = mapFirebaseUser(fbUser)
            _currentUserFlow.value = nexusUser
            cacheUser(nexusUser)
            AuthResult.Success(nexusUser)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In with credential failed", e)
            AuthResult.Error(e.localizedMessage ?: "فشل تسجيل الدخول بحساب Google")
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext Result.failure(Exception("Firebase غير مهيأ"))
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
    // Cloud Sync Operations (Firestore)
    // ==========================================

    suspend fun syncDataToCloud(
        favorites: Set<String>,
        readLater: Set<String>,
        history: List<ReadingHistoryEntry>,
        readChapters: Map<String, Set<Int>>
    ): Boolean = withContext(Dispatchers.IO) {
        val currentUser = _currentUserFlow.value ?: return@withContext false
        val db = firestore ?: return@withContext false

        _isSyncing.value = true
        try {
            // Convert history to serializable map list
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

            // Convert readChapters to string keys and int lists
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

            val now = System.currentTimeMillis()
            _lastSyncTimestamp.value = now
            prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
            Log.d(TAG, "Cloud sync successful for user: ${currentUser.email}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync data to Firestore", e)
            false
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun fetchCloudUserData(): CloudUserData? = withContext(Dispatchers.IO) {
        val currentUser = _currentUserFlow.value ?: return@withContext null
        val db = firestore ?: return@withContext null

        try {
            val doc = db.collection("users")
                .document(currentUser.uid)
                .collection("nexus_cloud_data")
                .document("user_library")
                .get()
                .await()

            if (!doc.exists()) return@withContext null

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

            CloudUserData(
                favorites = favs,
                readLater = later,
                history = parsedHistory,
                readChapters = parsedChapters,
                lastSynced = lastSynced
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch cloud user data", e)
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
            return@withContext Result.failure(SecurityException("غير مصرح: صلاحية الأدمن مطلوبة لنشر الإعلانات."))
        }

        val db = firestore ?: return@withContext Result.failure(Exception("Firestore غير متوفر"))

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

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post announcement", e)
            Result.failure(e)
        }
    }

    suspend fun dismissAdminAnnouncement(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUserFlow.value
        if (user == null || !user.isAdmin) {
            return@withContext Result.failure(SecurityException("غير مصرح"))
        }

        val db = firestore ?: return@withContext Result.failure(Exception("Firestore غير متوفر"))

        try {
            db.collection("nexus_announcements")
                .document("active_broadcast")
                .update("active", false)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeActiveAnnouncement(): Flow<AdminAnnouncement?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
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
