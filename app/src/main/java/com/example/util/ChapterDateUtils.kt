package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility to determine if a chapter is considered "New".
 * Criteria:
 * 1. If [isNew] flag is false and release date is older than 3 days, it's not new.
 * 2. If release date is "اليوم" (today), "أمس" (yesterday), or under 3 days old (<= 72 hours), it's new.
 * 3. If release date exceeds 3 days (e.g. 4+ days, weeks, months, or timestamp > 3 days), the badge disappears immediately.
 */
object ChapterDateUtils {

    private const val THREE_DAYS_MILLIS = 3 * 24 * 60 * 60 * 1000L

    private val dateFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "d MMMM yyyy",
        "MMMM d, yyyy"
    )

    /**
     * Checks whether a chapter is within the 3-day "New" window.
     *
     * @param releaseDate The release date string from data (e.g. "اليوم", "2026-09-13", "منذ ساعتين", "منذ 4 أيام")
     * @param isNewFlag The boolean `isNew` flag from backend summary
     * @return true if the chapter is considered new and under 3 days old, false if older than 3 days.
     */
    fun isChapterNew(releaseDate: String?, isNewFlag: Boolean): Boolean {
        val rawDate = releaseDate?.trim() ?: ""
        if (rawDate.isEmpty()) {
            return isNewFlag
        }

        val normalized = rawDate.lowercase(Locale.ROOT)

        // If closed for maintenance, don't show new
        if (normalized.contains("صيانة") || normalized.contains("maintenance")) {
            return false
        }

        // Arabic relative time patterns
        if (normalized.contains("اليوم") || normalized.contains("today") ||
            normalized.contains("ساعة") || normalized.contains("ساعات") || normalized.contains("دقيقة") || normalized.contains("دقائق") ||
            normalized.contains("hour") || normalized.contains("minute") || normalized.contains("now") || normalized.contains("الان") || normalized.contains("الآن")
        ) {
            return true
        }

        if (normalized.contains("أمس") || normalized.contains("امس") || normalized.contains("yesterday") ||
            normalized.contains("يوم") || normalized.contains("يومين") || normalized.contains("يومان")
        ) {
            return true
        }

        // Check if explicitly mentioned 3 days:
        // "3 أيام", "3 ايام", "3 days" -> still within 3 days
        if (normalized.contains("3 أيام") || normalized.contains("3 ايام") || normalized.contains("3 days") || normalized.contains("3d")) {
            return true
        }

        // Check if explicitly 4+ days, weeks, months, years -> definitely NOT new
        val dayMatch = Regex("""(?:منذ\s*)?(\d+)\s*(?:أيام|ايام|يوم|days?|d)""").find(normalized)
        if (dayMatch != null) {
            val days = dayMatch.groupValues[1].toIntOrNull()
            if (days != null) {
                return days <= 3
            }
        }

        if (normalized.contains("أسبوع") || normalized.contains("اسبوع") || normalized.contains("week") ||
            normalized.contains("شهر") || normalized.contains("month") ||
            normalized.contains("سنة") || normalized.contains("عام") || normalized.contains("year")
        ) {
            return false
        }

        // Try parsing standard date formats
        val now = System.currentTimeMillis()
        for (pattern in dateFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val parsedDate = sdf.parse(rawDate)
                if (parsedDate != null) {
                    val diff = now - parsedDate.time
                    // If parsed date is in future or within 3 days
                    return diff <= THREE_DAYS_MILLIS
                }
            } catch (_: Exception) {
                // Try next pattern
            }
        }

        // If no date pattern matched, fallback to isNewFlag only if date doesn't indicate old age
        return isNewFlag
    }
}
