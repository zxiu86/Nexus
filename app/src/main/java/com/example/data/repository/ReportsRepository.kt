package com.example.data.repository

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.data.model.ReportCategory
import com.example.data.model.ReportStatus
import com.example.data.model.ReportSubCategory
import com.example.data.model.UserReport
import com.example.data.network.GitHubNetworkModule
import com.example.util.AdminNotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class ReportsRepository(
    private val context: Context,
    private val authRepository: AuthRepository
) {

    companion object {
        private const val TAG = "NexusReportsRepo"
        private const val CACHE_FILE_NAME = "nexus_reports_cache.json"
        private const val REPORTS_GITHUB_PATH = "data/reports.json"
        const val CACHE_DELAY_MILLIS = 30 * 60 * 1000L // 30 minutes
    }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, UserReport::class.java)
    private val jsonAdapter = moshi.adapter<List<UserReport>>(listType)

    // User's own local & submitted reports
    private val _userReportsFlow = MutableStateFlow<List<UserReport>>(emptyList())
    val userReportsFlow: StateFlow<List<UserReport>> = _userReportsFlow.asStateFlow()

    // All incoming reports for Admin
    private val _allIncomingReportsFlow = MutableStateFlow<List<UserReport>>(emptyList())
    val allIncomingReportsFlow: StateFlow<List<UserReport>> = _allIncomingReportsFlow.asStateFlow()

    // Side notification popup event for user when report is Approved or Rejected
    private val _userSideNotificationFlow = MutableSharedFlow<UserReport>(extraBufferCapacity = 5)
    val userSideNotificationFlow: SharedFlow<UserReport> = _userSideNotificationFlow.asSharedFlow()

    private var firestore: FirebaseFirestore? = null
    private val notifiedAdminReportIds = mutableSetOf<String>()
    private val notifiedUserReportIds = mutableSetOf<String>()

    init {
        loadReportsFromLocalCache()
        initFirestore()
        startPeriodicSync()
    }

    private fun initFirestore() {
        try {
            firestore = FirebaseFirestore.getInstance()
            listenToFirestoreReports()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore init for reports: ${e.message}")
        }
    }

    private fun loadReportsFromLocalCache() {
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            if (file.exists()) {
                val json = file.readText(Charsets.UTF_8)
                val list = jsonAdapter.fromJson(json) ?: emptyList()
                _userReportsFlow.value = list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached reports", e)
        }
    }

    private fun saveReportsToLocalCache(reports: List<UserReport>) {
        _userReportsFlow.value = reports
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            val json = jsonAdapter.toJson(reports)
            file.writeText(json, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cached reports", e)
        }
    }

    private fun startPeriodicSync() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    checkAndDispatchScheduledReports()
                    refreshReportsFromGitHubAndFirestore()
                } catch (e: Exception) {
                    Log.w(TAG, "Periodic reports sync error: ${e.message}")
                }
                kotlinx.coroutines.delay(60_000L) // check every minute
            }
        }
    }

    // ==========================================
    // Report Submission by User
    // ==========================================

    suspend fun submitReport(
        category: ReportCategory,
        subCategory: ReportSubCategory,
        targetTitle: String,
        chapterNumber: String,
        details: String
    ): UserReport = withContext(Dispatchers.IO) {
        val user = authRepository.currentUserFlow.value
        val now = System.currentTimeMillis()
        val report = UserReport(
            userId = user?.uid.orEmpty(),
            userEmail = user?.email.orEmpty(),
            userDisplayName = user?.displayName.orEmpty(),
            category = category.arabicTitle,
            subCategory = subCategory.arabicTitle,
            targetTitle = targetTitle.trim(),
            chapterNumber = chapterNumber.trim(),
            details = details.trim(),
            createdAt = now,
            scheduledSendTime = now,
            isDispatched = true,
            status = ReportStatus.PENDING.name
        )

        val updated = listOf(report) + _userReportsFlow.value.filter { it.id != report.id }
        saveReportsToLocalCache(updated)
        Log.d(TAG, "Report saved locally and dispatching immediately to remote: ${report.id}")
        dispatchReportToRemote(report)
        report
    }

    suspend fun forceDispatchNow(reportId: String) = withContext(Dispatchers.IO) {
        val list = _userReportsFlow.value.toMutableList()
        val index = list.indexOfFirst { it.id == reportId }
        if (index != -1) {
            val report = list[index]
            val dispatchedReport = report.copy(isDispatched = true)
            list[index] = dispatchedReport
            saveReportsToLocalCache(list)
            dispatchReportToRemote(dispatchedReport)
        }
    }

    private suspend fun checkAndDispatchScheduledReports() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val list = _userReportsFlow.value.toMutableList()
        var changed = false

        for (i in list.indices) {
            val report = list[i]
            if (!report.isDispatched && now >= report.scheduledSendTime) {
                Log.d(TAG, "30 minutes elapsed for report ${report.id}, dispatching to GitHub and Firestore...")
                val dispatched = report.copy(isDispatched = true)
                list[i] = dispatched
                changed = true
                dispatchReportToRemote(dispatched)
            }
        }

        if (changed) {
            saveReportsToLocalCache(list)
        }
    }

    private suspend fun dispatchReportToRemote(report: UserReport) {
        // 1. Dispatch to Firestore
        try {
            firestore?.collection("reports")?.document(report.id)?.set(report, SetOptions.merge())?.await()
            Log.d(TAG, "Dispatched report ${report.id} to Firestore successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore dispatch report ${report.id} error: ${e.message}")
        }

        // 2. Dispatch to GitHub repo zxiu86/Data/data/reports.json
        try {
            val existingReports = fetchReportsFromGitHubRaw().toMutableList()
            val existingIdx = existingReports.indexOfFirst { it.id == report.id }
            if (existingIdx != -1) {
                existingReports[existingIdx] = report
            } else {
                existingReports.add(0, report)
            }
            writeReportsToGitHub(existingReports)
        } catch (e: Exception) {
            Log.w(TAG, "GitHub reports.json commit error: ${e.message}")
        }
    }

    // ==========================================
    // GitHub data/reports.json Sync
    // ==========================================

    private suspend fun fetchReportsFromGitHubRaw(): List<UserReport> = withContext(Dispatchers.IO) {
        try {
            val owner = GitHubNetworkModule.getConfiguredOwner()
            val repo = GitHubNetworkModule.getDataRepo()
            val branch = GitHubNetworkModule.getConfiguredBranch()

            val rawUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/$REPORTS_GITHUB_PATH"
            val json = GitHubNetworkModule.fetchDirectRaw(rawUrl, forceFresh = true)
            if (!json.isNullOrBlank()) {
                return@withContext jsonAdapter.fromJson(json) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchReportsFromGitHubRaw error: ${e.message}")
        }
        emptyList()
    }

    private suspend fun writeReportsToGitHub(reports: List<UserReport>): Boolean = withContext(Dispatchers.IO) {
        val jsonStr = jsonAdapter.toJson(reports)
        val res = GitHubNetworkModule.pushOrUpdateFileToGitHub(
            path = REPORTS_GITHUB_PATH,
            contentString = jsonStr,
            commitMessage = "[Nexus 2.0.1] Sync reports database (${reports.size} items)"
        )
        if (res.isSuccess) {
            Log.d(TAG, "Successfully committed reports to GitHub: $REPORTS_GITHUB_PATH")
            true
        } else {
            Log.w(TAG, "Notice: Reports commit to GitHub: ${res.exceptionOrNull()?.message}")
            false
        }
    }

    suspend fun forceSyncReportsWithGitHub(): Result<String> = withContext(Dispatchers.IO) {
        val token = GitHubNetworkModule.getActiveToken()
        if (token.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("رمز الوصول (GitHub Token) غير مضبوط. يرجى ضبط الرمز من لوحة المشرف لإتمام المزامنة السحابية."))
        }

        try {
            refreshReportsFromGitHubAndFirestore()
            val list = _allIncomingReportsFlow.value
            val success = writeReportsToGitHub(list)
            if (success) {
                Result.success("تمت مزامنة البلاغات بنجاح مع GitHub (${list.size} بلاغ).")
            } else {
                Result.failure(Exception("فشل إرسال ملف data/reports.json إلى GitHub. تحقق من صلاحية الرمز."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // Realtime Firestore Listener & Notifications
    // ==========================================

    private fun listenToFirestoreReports() {
        val fs = firestore ?: return
        fs.collection("reports")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Reports listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = mutableListOf<UserReport>()
                    for (doc in snapshot.documents) {
                        try {
                            val r = doc.toObject(UserReport::class.java)
                            if (r != null) list.add(r)
                        } catch (_: Exception) {}
                    }
                    val sorted = list.sortedByDescending { it.createdAt }
                    _allIncomingReportsFlow.value = sorted
                    processReportUpdates(sorted)
                }
            }
    }

    suspend fun refreshReportsFromGitHubAndFirestore() = withContext(Dispatchers.IO) {
        val ghReports = fetchReportsFromGitHubRaw()
        if (ghReports.isNotEmpty()) {
            val merged = (_allIncomingReportsFlow.value + ghReports)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt }
            _allIncomingReportsFlow.value = merged
            processReportUpdates(merged)
        }
    }

    private fun processReportUpdates(reports: List<UserReport>) {
        val currentUser = authRepository.currentUserFlow.value

        // 1. If current user is Admin: check for incoming pending reports & send phone notification
        if (currentUser?.isAdmin == true) {
            for (r in reports) {
                if (r.isPending() && !notifiedAdminReportIds.contains(r.id)) {
                    notifiedAdminReportIds.add(r.id)
                    AdminNotificationHelper.notifyAdminNewReport(context, r)
                }
            }
        }

        // 2. If current user submitted a report that was reviewed by admin
        val userReports = _userReportsFlow.value.toMutableList()
        var cacheUpdated = false

        for (remoteReport in reports) {
            val localIdx = userReports.indexOfFirst { it.id == remoteReport.id }
            if (localIdx != -1) {
                val local = userReports[localIdx]
                if (local.status != remoteReport.status) {
                    userReports[localIdx] = remoteReport
                    cacheUpdated = true

                    // If status became Approved or Rejected and user not notified yet
                    if ((remoteReport.isApproved() || remoteReport.isRejected()) && !notifiedUserReportIds.contains(remoteReport.id)) {
                        notifiedUserReportIds.add(remoteReport.id)
                        AdminNotificationHelper.notifyUserReportResult(context, remoteReport)
                        CoroutineScope(Dispatchers.Main).launch {
                            _userSideNotificationFlow.emit(remoteReport)
                        }
                    }
                }
            }
        }

        if (cacheUpdated) {
            saveReportsToLocalCache(userReports)
        }
    }

    // ==========================================
    // Admin Actions (Approve / Reject)
    // ==========================================

    suspend fun approveReport(reportId: String, adminNote: String = ""): Boolean = withContext(Dispatchers.IO) {
        updateReportStatus(reportId, ReportStatus.APPROVED.name, adminNote.ifBlank { "تمت الموافقة من قِبل المشرف" })
    }

    suspend fun rejectReport(reportId: String, adminNote: String = ""): Boolean = withContext(Dispatchers.IO) {
        updateReportStatus(reportId, ReportStatus.REJECTED.name, adminNote.ifBlank { "للأسف تم رفض البلاغ" })
    }

    private suspend fun updateReportStatus(reportId: String, newStatus: String, note: String): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        var success = false

        // 1. Update Firestore
        try {
            val updates = mapOf(
                "status" to newStatus,
                "adminResponseNote" to note,
                "reviewedAt" to now
            )
            firestore?.collection("reports")?.document(reportId)?.update(updates)?.await()
            success = true
            Log.d(TAG, "Updated report $reportId in Firestore to $newStatus")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore status update error: ${e.message}")
        }

        // 2. Update GitHub reports.json
        try {
            val list = _allIncomingReportsFlow.value.toMutableList()
            val idx = list.indexOfFirst { it.id == reportId }
            if (idx != -1) {
                list[idx] = list[idx].copy(status = newStatus, adminResponseNote = note, reviewedAt = now)
                _allIncomingReportsFlow.value = list
                writeReportsToGitHub(list)
                success = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "GitHub status update error: ${e.message}")
        }

        success
    }

    fun dismissSideNotification(reportId: String) {
        val list = _userReportsFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == reportId }
        if (idx != -1) {
            list[idx] = list[idx].copy(isUserNotified = true)
            saveReportsToLocalCache(list)
        }
    }
}
