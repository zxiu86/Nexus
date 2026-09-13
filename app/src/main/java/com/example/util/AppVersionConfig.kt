package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "1.9.5"
    const val VERSION_CODE = 35
    const val BUILD_CODENAME = "Nexus Nova"
    const val RELEASE_CHANNEL = "النسخة الرسمية المستقرة"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v1.9.4"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME"

    /**
     * Build identifier: "Build 34"
     */
    fun getFormattedVersionCode(): String = "Build $VERSION_CODE"

    /**
     * Label displayed on the splash / preload screen before the main page opens
     */
    fun getSplashVersionLabel(): String = "الإصدار $VERSION_NAME • تهيئة المحتوى والمستودع"

    /**
     * Badge text shown in settings card
     */
    fun getSettingsVersionLabel(): String = "v$VERSION_NAME"

    /**
     * Full footer/copyright string in settings:
     * "الإصدار الرسمي v1.9.4 (Build 34) • فريق Nexus"
     */
    fun getSettingsFullDetails(): String =
        "الإصدار الرسمي v$VERSION_NAME (Build $VERSION_CODE) • $AUTHOR_CREDITS"

    /**
     * Current version label in the update status section
     */
    fun getCurrentVersionBadge(): String = "الإصدار الحالي: v$VERSION_NAME"

    /**
     * Changelog header text
     */
    fun getChangelogHeader(): String = "جديد الإصدار v$VERSION_NAME:"

    /**
     * Highlights of this release
     */
    val CURRENT_CHANGELOG_FEATURES = listOf(
        "تلوين ديناميكي شامل لكل أزرار التطبيق، التحديدات، الإشارات، النصوص التمييزية، والفوتر وفق اللون التجميلي المختار.",
        "تصميم عصري وفخم بالكامل للشريط السفلي (الفوتر) بمؤشرات ضوئية تفاعلية وتأثير عائم متناسق.",
        "إزالة حقل البحث من الواجهة الرئيسية لتبسيط الشاشة والاعتماد على تبويب الاستكشاف المتخصص.",
        "ملف موحد ودوال مركزية للتحكم برقم وتفاصيل إصدار التطبيق في كافة أجزاء النظام تلقائياً."
    )
}
