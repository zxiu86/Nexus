package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object InAppUpdateManager {

    private const val TAG = "InAppUpdateManager"

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
    fun requestInstallPermission(context: Context) {
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
     * Downloads the APK file via DownloadManager or opens the direct download link
     */
    fun startApkDownload(context: Context, downloadUrl: String, versionName: String) {
        try {
            if (downloadUrl.isBlank()) {
                Toast.makeText(context, "رابط التحديث غير متوفر حالياً", Toast.LENGTH_SHORT).show()
                return
            }

            val fileName = "nexus-update-v$versionName.apk"
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Nexus v$versionName")
                setDescription("جاري تحميل تحديث تطبيق Nexus...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/vnd.android.package-archive")
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (downloadManager != null) {
                downloadManager.enqueue(request)
                Toast.makeText(context, "بدأ تحميل التحديث في الخلفية...", Toast.LENGTH_LONG).show()
            } else {
                // Fallback to browser intent if DownloadManager is unavailable
                openDownloadInBrowser(context, downloadUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating APK download", e)
            openDownloadInBrowser(context, downloadUrl)
        }
    }

    fun openDownloadInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح رابط التحديث: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launches Android Package Installer to install the downloaded APK
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "ملف التحديث غير موجود", Toast.LENGTH_SHORT).show()
                return
            }

            if (!canInstallPackages(context)) {
                Toast.makeText(context, "يرجى منح إذن تثبيت التطبيقات من الإعدادات", Toast.LENGTH_LONG).show()
                requestInstallPermission(context)
                return
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            Toast.makeText(context, "حدث خطأ أثناء محاولة التثبيت: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
