package com.example.util

import android.app.DownloadManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object InAppUpdateManager {

    private const val TAG = "InAppUpdateManager"
    private const val PREFS_NAME = "nexus_update_prefs"
    private const val KEY_PENDING_APK_PATH = "pending_apk_path"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun showToast(context: Context, message: String, isLong: Boolean = false) {
        mainHandler.post {
            Toast.makeText(
                context.applicationContext,
                message,
                if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Checks if the app has permission to install packages from unknown sources (Android 8.0+)
     */
    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens system settings to allow installing apps from this source
     */
    fun requestInstallPermission(context: Context, pendingApkFile: File? = null) {
        if (pendingApkFile != null && pendingApkFile.exists()) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_PENDING_APK_PATH, pendingApkFile.absolutePath).apply()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open install permissions settings", e)
            }
        }
    }

    /**
     * Checks if there is a pending APK waiting to be installed after permission is granted
     */
    fun checkPendingInstall(context: Context) {
        if (!canInstallPackages(context)) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pendingPath = prefs.getString(KEY_PENDING_APK_PATH, null) ?: return
        val apkFile = File(pendingPath)
        if (apkFile.exists() && isValidApkZip(apkFile)) {
            prefs.edit().remove(KEY_PENDING_APK_PATH).apply()
            installApk(context, apkFile)
        }
    }

    /**
     * Checks if the file is a valid Zip/APK file by checking its magic header (PK\x03\x04)
     */
    fun isValidApkZip(file: File): Boolean {
        if (!file.exists() || file.length() < 1024 * 1024) return false
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(4)
                val read = fis.read(header)
                read == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
                        (header[2] == 0x03.toByte() || header[2] == 0x05.toByte() || header[2] == 0x07.toByte()) &&
                        (header[3] == 0x04.toByte() || header[3] == 0x06.toByte() || header[3] == 0x08.toByte())
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resolves the best directory to store the APK for installation.
     * Prefers getExternalFilesDir(DIRECTORY_DOWNLOADS) to ensure Android's PackageInstaller
     * has full cross-process read access without SELinux denials.
     */
    private fun getUpdateStorageDir(context: Context): File {
        val extDownloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (extDownloads != null && (extDownloads.exists() || extDownloads.mkdirs())) {
            return extDownloads
        }
        val extCache = context.externalCacheDir
        if (extCache != null && (extCache.exists() || extCache.mkdirs())) {
            return extCache
        }
        val internalUpdates = File(context.filesDir, "app_updates")
        if (internalUpdates.exists() || internalUpdates.mkdirs()) {
            return internalUpdates
        }
        return context.cacheDir
    }

    /**
     * Downloads the APK file directly with verification and automatic launch of the installer.
     * If any error occurs, it gracefully falls back to the browser download.
     */
    fun startApkDownload(context: Context, downloadUrl: String, versionName: String) {
        if (downloadUrl.isBlank()) {
            showToast(context, "رابط التحديث غير متوفر حالياً")
            return
        }

        val appContext = context.applicationContext
        showToast(appContext, "جاري تنزيل التحديث (v$versionName)... يرجى الانتظار", true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updateDir = getUpdateStorageDir(appContext)
                val targetApk = File(updateDir, "nexus_v$versionName.apk")
                val tempApk = File(updateDir, "nexus_v$versionName.apk.tmp")

                if (tempApk.exists()) tempApk.delete()
                if (targetApk.exists()) targetApk.delete()

                val request = Request.Builder()
                    .url(downloadUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; Nexus-Updater)")
                    .header("Accept", "*/*")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful || response.body == null) {
                    throw IllegalStateException("HTTP ${response.code}: ${response.message}")
                }

                val body = response.body!!
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(tempApk)

                val buffer = ByteArray(64 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                try {
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                    }
                    outputStream.flush()
                } finally {
                    try { outputStream.close() } catch (_: Exception) {}
                    try { inputStream.close() } catch (_: Exception) {}
                    try { body.close() } catch (_: Exception) {}
                }

                // Verify file size and header
                if (!isValidApkZip(tempApk)) {
                    tempApk.delete()
                    throw IllegalStateException("الملف المنزل غير مكتمل أو تالف (${totalBytesRead / 1024} KB)")
                }

                // Atomic rename to final APK file
                if (!tempApk.renameTo(targetApk)) {
                    tempApk.copyTo(targetApk, overwrite = true)
                    tempApk.delete()
                }

                // Make readable
                targetApk.setReadable(true, false)

                // Verify APK integrity using Android's PackageManager
                val packageInfo = appContext.packageManager.getPackageArchiveInfo(targetApk.absolutePath, PackageManager.GET_ACTIVITIES)
                if (packageInfo == null) {
                    Log.w(TAG, "packageArchiveInfo is null for downloaded APK, trying without flags")
                    val fallbackInfo = appContext.packageManager.getPackageArchiveInfo(targetApk.absolutePath, 0)
                    if (fallbackInfo == null) {
                        targetApk.delete()
                        withContext(Dispatchers.Main) {
                            showToast(appContext, "الحزمة غير متوافقة أو تالفة، جاري فتح التحميل عبر المتصفح...", true)
                            openDownloadInBrowser(appContext, downloadUrl)
                        }
                        return@launch
                    }
                }

                Log.d(TAG, "APK successfully verified: ${targetApk.absolutePath} (${targetApk.length()} bytes)")

                withContext(Dispatchers.Main) {
                    showToast(appContext, "اكتمل التنزيل بنجاح! جاري التثبيت...")
                    installApk(appContext, targetApk)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Direct download failed, falling back to browser", e)
                withContext(Dispatchers.Main) {
                    showToast(appContext, "جاري فتح رابط التحديث في المتصفح...", true)
                    openDownloadInBrowser(appContext, downloadUrl)
                }
            }
        }
    }

    /**
     * Launches Android Package Installer to install the verified APK
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                showToast(context, "ملف التحديث غير موجود")
                return
            }

            if (!isValidApkZip(apkFile)) {
                showToast(context, "ملف التحديث تالف أو غير مكتمل، يرجى إعادة التحميل", true)
                return
            }

            if (!canInstallPackages(context)) {
                showToast(context, "يرجى منح إذن تثبيت التطبيقات من الإعدادات للمتابعة", true)
                requestInstallPermission(context, apkFile)
                return
            }

            apkFile.setReadable(true, false)

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                clipData = ClipData.newRawUri("nexus_apk", apkUri)
            }

            // Grant URI read permission explicitly to package installer packages
            val knownInstallers = listOf(
                "com.google.android.packageinstaller",
                "com.android.packageinstaller",
                "com.google.android.apps.packageinstaller",
                "com.samsung.android.packageinstaller"
            )
            for (pkg in knownInstallers) {
                try {
                    context.grantUriPermission(pkg, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }

            val resolvedActivities = context.packageManager.queryIntentActivities(
                installIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resolvedActivities) {
                val pkg = resolveInfo.activityInfo.packageName
                try {
                    context.grantUriPermission(pkg, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            showToast(context, "تعذر فتح مثبت الحزم: ${e.message}", true)
        }
    }

    /**
     * Alternative: Downloads directly via Android's native system DownloadManager
     */
    fun downloadViaSystemDownloadManager(context: Context, downloadUrl: String, versionName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Nexus Update v$versionName")
                setDescription("تنزيل تحديث تطبيق نكسوس")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "nexus_v$versionName.apk")
                setMimeType("application/vnd.android.package-archive")
            }
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            showToast(context, "تم بدء التنزيل عبر مدير تنزيلات النظام. تابعه من لوحة الإشعارات", true)
        } catch (e: Exception) {
            Log.e(TAG, "DownloadManager failed", e)
            openDownloadInBrowser(context, downloadUrl)
        }
    }

    /**
     * Opens the direct download link in the external web browser
     */
    fun openDownloadInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            showToast(context, "تعذر فتح المتصفح: ${e.message}")
        }
    }
}
