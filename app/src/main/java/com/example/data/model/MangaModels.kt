package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class MangaType(val labelAr: String) {
    MANHWA("مانهوا كورية"),
    MANHUA("مانها صينية");

    companion object {
        fun fromString(value: String?): MangaType {
            if (value == null) return MANHWA
            val normalized = value.trim().lowercase()
            return if (normalized.contains("مانها") || normalized.contains("manhua")) {
                MANHUA
            } else {
                MANHWA
            }
        }
    }
}

data class ChapterPage(
    val pageNumber: Int,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val caption: String = ""
)

data class Chapter(
    val id: String,
    val mangaId: String,
    val number: Int,
    val title: String,
    val releaseDate: String,
    val isNew: Boolean = false,
    val pagesCount: Int = 0,
    val pages: List<ChapterPage> = emptyList()
)

data class MangaItem(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val type: MangaType,
    val coverUrl: String? = null,
    val coverRes: Int? = null,
    val bannerUrl: String? = null,
    val bannerRes: Int? = null,
    val synopsis: String,
    val author: String,
    val artist: String,
    val scanlationTeam: String = "فريق نكسوس للترجمة",
    val translator: String = "Nexus Team",
    val cleaner: String = "Nexus Studio",
    val typesetter: String = "Nexus Studio",
    val rating: Float = 4.9f,
    val views: String = "1.0M",
    val status: String = "مستمر",
    val genres: List<String> = emptyList(),
    val totalChaptersCount: Int = 0,
    val chapters: List<Chapter> = emptyList()
) {
    val latestThreeChapters: List<Chapter>
        get() = chapters.sortedByDescending { it.number }.take(3)
}

// ----------------------------------------------------
// DTOs matching the user's GitHub Repository architecture
// ----------------------------------------------------

@JsonClass(generateAdapter = true)
data class WorkDto(
    val slug: String? = null,
    val id: String? = null,
    val title: String? = null,
    val name: String? = null,
    val cover: String? = null,
    val thumbnail: String? = null,
    val image: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val type: String? = null,
    val author: String? = null,
    val artist: String? = null,
    val genres: List<String>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class SeriesInfoDto(
    val status: String? = "مستمر",
    val rating: Double? = 4.9,
    val views: String? = "1.0M",
    val chapters: List<ChapterSummaryDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChapterSummaryDto(
    val number: Int = 1,
    val title: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "is_new") val isNew: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class ChapterDetailDto(
    val series: String? = null,
    val chapter: Int = 1,
    val title: String? = null,
    @Json(name = "total_images") val totalImages: Int? = null,
    val images: List<String>? = emptyList(),
    val pages: List<String>? = emptyList()
)

// ----------------------------------------------------
// GitHub Release & In-App Updater DTOs
// ----------------------------------------------------

@JsonClass(generateAdapter = true)
data class GitHubReleaseAsset(
    val name: String? = null,
    @Json(name = "browser_download_url") val browserDownloadUrl: String? = null,
    @Json(name = "content_type") val contentType: String? = null,
    val size: Long? = 0L
)

@JsonClass(generateAdapter = true)
data class GitHubReleaseDto(
    @Json(name = "tag_name") val tagName: String? = null,
    val name: String? = null,
    val body: String? = null,
    @Json(name = "published_at") val publishedAt: String? = null,
    val assets: List<GitHubReleaseAsset>? = emptyList()
)

data class AppUpdateState(
    val isChecking: Boolean = false,
    val updateAvailable: Boolean = false,
    val latestVersion: String = "",
    val currentVersion: String = "1.3",
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadedApkPath: String? = null,
    val errorMessage: String? = null
)
