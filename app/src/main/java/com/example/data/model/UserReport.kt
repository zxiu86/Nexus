package com.example.data.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import java.util.UUID

enum class ReportCategory(val arabicTitle: String) {
    REQUEST("طلب"),
    ISSUE("مشاكل")
}

enum class ReportSubCategory(val arabicTitle: String, val category: ReportCategory) {
    // Requests (طلب)
    ADD_FEATURE("إضافة ميزة", ReportCategory.REQUEST),
    ADD_WORK("إضافة عمل", ReportCategory.REQUEST),

    // Issues (مشاكل)
    BROKEN_FEATURE("ميزات لا تعمل", ReportCategory.ISSUE),
    BROKEN_CHAPTER("فصول لا تعمل", ReportCategory.ISSUE)
}

enum class ReportStatus(val arabicLabel: String) {
    PENDING("قيد المراجعة"),
    APPROVED("تم الموافقة"),
    REJECTED("تم الرفض")
}

@Keep
@JsonClass(generateAdapter = true)
data class UserReport(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val userEmail: String = "",
    val userDisplayName: String = "",
    val category: String = ReportCategory.REQUEST.arabicTitle,
    val subCategory: String = ReportSubCategory.ADD_FEATURE.arabicTitle,
    val targetTitle: String = "",
    val chapterNumber: String = "",
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val scheduledSendTime: Long = System.currentTimeMillis() + (30 * 60 * 1000L), // 30 minutes cache delay
    val isDispatched: Boolean = false,
    val status: String = ReportStatus.PENDING.name,
    val adminResponseNote: String = "",
    val reviewedAt: Long = 0L,
    val isUserNotified: Boolean = false
) {
    fun isApproved(): Boolean = status.equals(ReportStatus.APPROVED.name, ignoreCase = true) || status == "تم الموافقة"
    fun isRejected(): Boolean = status.equals(ReportStatus.REJECTED.name, ignoreCase = true) || status == "تم الرفض"
    fun isPending(): Boolean = !isApproved() && !isRejected()
}
