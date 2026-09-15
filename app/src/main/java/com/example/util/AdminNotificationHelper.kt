package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.UserReport

object AdminNotificationHelper {

    private const val ADMIN_CHANNEL_ID = "nexus_admin_reports_channel"
    private const val USER_CHANNEL_ID = "nexus_user_reports_channel"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Admin Channel (High Importance with Sound & Vibrate)
            val adminChannel = NotificationChannel(
                ADMIN_CHANNEL_ID,
                "إشعارات المشرف - البلاغات الواردة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات خاصة بالمشرف عند ورود طلبات وبلاغات ومشاكل جديدة من المستخدمين"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(adminChannel)

            // User Channel (High Importance for Report Status Updates)
            val userChannel = NotificationChannel(
                USER_CHANNEL_ID,
                "متابعة البلاغات والطلبات",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات قبول أو رفض البلاغات والطلبات المرسلة من طرفك"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(userChannel)
        }
    }

    fun notifyAdminNewReport(context: Context, report: UserReport) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_ADMIN_DASHBOARD", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "📢 بلاغ جديد وارد: ${report.subCategory}"
        val body = buildString {
            if (report.targetTitle.isNotBlank()) append("[${report.targetTitle}] ")
            append(report.details.take(75))
            if (report.details.length > 75) append("...")
            append(" | من: ")
            append(report.userDisplayName.ifBlank { report.userEmail.ifBlank { "مستخدم" } })
        }

        val notification = NotificationCompat.Builder(context, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.nexus_fox_cosmic_icon_1788280684840)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(report.id.hashCode(), notification)
        } catch (_: SecurityException) {
        }
    }

    fun notifyUserReportResult(context: Context, report: UserReport) {
        initChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.id.hashCode() + 100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isApproved = report.isApproved()
        val title = if (isApproved) "✅ تم الموافقة على البلاغ" else "❌ للأسف تم رفض البلاغ"
        val body = buildString {
            append("نوع البلاغ: ${report.subCategory}\n")
            if (report.adminResponseNote.isNotBlank()) {
                append("ملاحظة الإدارة: ${report.adminResponseNote}\n")
            }
            append("التفاصيل: ${report.details.take(50)}")
        }

        val notification = NotificationCompat.Builder(context, USER_CHANNEL_ID)
            .setSmallIcon(R.drawable.nexus_fox_cosmic_icon_1788280684840)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(report.id.hashCode() + 100, notification)
        } catch (_: SecurityException) {
        }
    }
}
