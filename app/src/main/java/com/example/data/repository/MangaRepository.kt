package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.data.model.AppUpdateState
import com.example.data.model.Chapter
import com.example.data.model.ChapterDetailDto
import com.example.data.model.ChapterDownloadProgress
import com.example.data.model.ChapterPage
import com.example.data.model.DownloadedChapter
import com.example.data.model.MangaItem
import com.example.data.model.MangaType
import com.example.data.model.ReadingHistoryEntry
import com.example.data.model.SeriesInfoDto
import com.example.data.model.WorkDto
import com.example.data.network.GitHubNetworkModule
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class MangaRepository(private val context: Context) {

    private val TAG = "NexusMangaRepository"
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexus_manga_prefs", Context.MODE_PRIVATE)

    private val _favoritesFlow = MutableStateFlow<Set<String>>(emptySet())
    val favoritesFlow: StateFlow<Set<String>> = _favoritesFlow.asStateFlow()

    private val _lastReadFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
    val lastReadFlow: StateFlow<Map<String, Int>> = _lastReadFlow.asStateFlow()

    private val _allMangaFlow = MutableStateFlow<List<MangaItem>>(emptyList())
    val allMangaFlow: StateFlow<List<MangaItem>> = _allMangaFlow.asStateFlow()

    // Offline Downloads State
    private val _downloadedChaptersFlow = MutableStateFlow<List<DownloadedChapter>>(emptyList())
    val downloadedChaptersFlow: StateFlow<List<DownloadedChapter>> = _downloadedChaptersFlow.asStateFlow()

    private val _downloadProgressFlow = MutableStateFlow<Map<String, ChapterDownloadProgress>>(emptyMap())
    val downloadProgressFlow: StateFlow<Map<String, ChapterDownloadProgress>> = _downloadProgressFlow.asStateFlow()

    // Reading History State
    private val _readingHistoryFlow = MutableStateFlow<List<ReadingHistoryEntry>>(emptyList())
    val readingHistoryFlow: StateFlow<List<ReadingHistoryEntry>> = _readingHistoryFlow.asStateFlow()

    // Dynamic chapter cache with loaded images
    private val loadedChaptersCache = mutableMapOf<String, Chapter>()
    private val cacheDir = context.cacheDir
    private val secureStorageDir = File(context.filesDir, "secure_chapters")

    init {
        if (!secureStorageDir.exists()) {
            secureStorageDir.mkdirs()
        }
        loadPreferences()
        loadMangaFromDiskCache()
        loadDownloadedChaptersManifest()
        loadReadingHistoryFromDisk()
    }

    private fun decodeGitHubContent(rawContent: String): String {
        val trimmed = rawContent.trim()
        if (trimmed.startsWith("{") && trimmed.contains("\"content\"") && trimmed.contains("\"encoding\"")) {
            try {
                val jsonObject = org.json.JSONObject(trimmed)
                if (jsonObject.optString("encoding") == "base64") {
                    val base64Content = jsonObject.optString("content").replace("\n", "").replace("\r", "").replace(" ", "")
                    val decodedBytes = android.util.Base64.decode(base64Content, android.util.Base64.DEFAULT)
                    return String(decodedBytes, Charsets.UTF_8)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Base64 decode fallback failed: ${e.message}")
            }
        }
        return rawContent
    }

    private fun parseWorksJson(jsonStr: String): Map<String, WorkDto> {
        val cleanJson = decodeGitHubContent(jsonStr).trim()
        val resultMap = mutableMapOf<String, WorkDto>()
        if (cleanJson.isBlank() || cleanJson.startsWith("<!DOCTYPE") || cleanJson.contains("404: Not Found")) {
            return emptyMap()
        }

        // Strategy 1: Moshi parsing
        try {
            if (cleanJson.startsWith("[")) {
                val listType = Types.newParameterizedType(List::class.java, WorkDto::class.java)
                val listAdapter = GitHubNetworkModule.moshi.adapter<List<WorkDto>>(listType)
                val list = listAdapter.fromJson(cleanJson) ?: emptyList()
                for (w in list) {
                    val slug = w.slug ?: w.id ?: w.title ?: w.name ?: "work_${w.hashCode()}"
                    resultMap[slug] = w
                }
            } else {
                val mapType = Types.newParameterizedType(Map::class.java, String::class.java, WorkDto::class.java)
                val mapAdapter = GitHubNetworkModule.moshi.adapter<Map<String, WorkDto>>(mapType)
                val map = mapAdapter.fromJson(cleanJson) ?: emptyMap()
                resultMap.putAll(map)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Moshi works parsing error: ${e.message}, attempting org.json fallback")
        }

        if (resultMap.isNotEmpty()) return resultMap

        // Strategy 2: org.json.JSONObject fallback (100% resilient)
        try {
            if (cleanJson.startsWith("[")) {
                val jsonArray = org.json.JSONArray(cleanJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i) ?: continue
                    val slug = obj.optString("slug", obj.optString("id", "work_$i"))
                    resultMap[slug] = parseWorkDtoFromJsonObject(slug, obj)
                }
            } else {
                val jsonObj = org.json.JSONObject(cleanJson)
                val nestedArray = jsonObj.optJSONArray("works") ?: jsonObj.optJSONArray("data") ?: jsonObj.optJSONArray("items")
                if (nestedArray != null) {
                    for (i in 0 until nestedArray.length()) {
                        val obj = nestedArray.optJSONObject(i) ?: continue
                        val slug = obj.optString("slug", obj.optString("id", "work_$i"))
                        resultMap[slug] = parseWorkDtoFromJsonObject(slug, obj)
                    }
                } else {
                    val targetObj = jsonObj.optJSONObject("works") ?: jsonObj.optJSONObject("data") ?: jsonObj
                    val keys = targetObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = targetObj.optJSONObject(key)
                        if (obj != null) {
                            val slug = obj.optString("slug", key)
                            resultMap[key] = parseWorkDtoFromJsonObject(slug, obj)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "org.json works parsing failed", e)
        }

        return resultMap
    }

    private fun parseWorkDtoFromJsonObject(slug: String, obj: org.json.JSONObject): WorkDto {
        val genresList = mutableListOf<String>()
        val gArr = obj.optJSONArray("genres") ?: obj.optJSONArray("categories") ?: obj.optJSONArray("tags")
        if (gArr != null) {
            for (j in 0 until gArr.length()) {
                val g = gArr.optString(j)
                if (g.isNotBlank()) genresList.add(g)
            }
        }
        val rawCover = obj.optString("cover", obj.optString("thumbnail", obj.optString("image", obj.optString("banner", ""))))
        return WorkDto(
            slug = slug,
            id = obj.optString("id", slug),
            title = obj.optString("title", obj.optString("name", slug)),
            name = obj.optString("name", obj.optString("title", slug)),
            cover = sanitizeImageUrl(rawCover, slug),
            thumbnail = sanitizeImageUrl(rawCover, slug),
            image = sanitizeImageUrl(rawCover, slug),
            summary = obj.optString("summary", obj.optString("description", obj.optString("synopsis", ""))),
            description = obj.optString("description", obj.optString("summary", obj.optString("synopsis", ""))),
            type = obj.optString("type", "مانهوا"),
            author = obj.optString("author", "غير محدد"),
            artist = obj.optString("artist", "غير محدد"),
            genres = if (genresList.isNotEmpty()) genresList else listOf("مانها", "أكشن")
        )
    }

    private fun sanitizeImageUrl(url: String?, slug: String?): String? {
        if (url.isNullOrBlank()) return null
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/") || url.startsWith("file:")) return url
        val owner = GitHubNetworkModule.getConfiguredOwner()
        val repo = GitHubNetworkModule.getDataRepo()
        val branch = GitHubNetworkModule.getConfiguredBranch()
        val cleanPath = url.removePrefix("/")
        return if (cleanPath.startsWith("data/")) {
            "https://raw.githubusercontent.com/$owner/$repo/$branch/$cleanPath"
        } else if (!slug.isNullOrBlank()) {
            "https://raw.githubusercontent.com/$owner/$repo/$branch/data/$slug/$cleanPath"
        } else {
            "https://raw.githubusercontent.com/$owner/$repo/$branch/$cleanPath"
        }
    }

    private fun parseSeriesInfoDto(jsonStr: String): SeriesInfoDto? {
        val cleanJson = decodeGitHubContent(jsonStr).trim()
        if (cleanJson.isBlank() || cleanJson.startsWith("<!DOCTYPE") || cleanJson.contains("404: Not Found")) {
            return null
        }

        try {
            val adapter = GitHubNetworkModule.moshi.adapter(SeriesInfoDto::class.java)
            val info = adapter.fromJson(cleanJson)
            if (info != null && (!info.chapters.isNullOrEmpty() || info.status != null)) {
                return info
            }
        } catch (e: Exception) {
            Log.w(TAG, "Moshi series info parsing error: ${e.message}")
        }

        try {
            val chList = mutableListOf<com.example.data.model.ChapterSummaryDto>()

            if (cleanJson.startsWith("[")) {
                val arr = org.json.JSONArray(cleanJson)
                for (i in 0 until arr.length()) {
                    val chObj = arr.optJSONObject(i) ?: continue
                    val num = chObj.optInt("number", chObj.optInt("chapter", i + 1))
                    val title = chObj.optString("title", "الفصل $num")
                    val releaseDate = chObj.optString("release_date", chObj.optString("releaseDate", "اليوم"))
                    val isNew = chObj.optBoolean("is_new", chObj.optBoolean("isNew", false))
                    val isClosed = chObj.optBoolean("is_closed", chObj.optBoolean("isClosed", false))
                    chList.add(com.example.data.model.ChapterSummaryDto(num, title, releaseDate, isNew, isClosed))
                }
                return SeriesInfoDto(status = "مستمر", rating = 4.9, views = "1.2M", chapters = chList)
            } else {
                val obj = org.json.JSONObject(cleanJson)
                val arr = obj.optJSONArray("chapters") ?: obj.optJSONArray("chapter_list")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val chObj = arr.optJSONObject(i) ?: continue
                        val num = chObj.optInt("number", chObj.optInt("chapter", i + 1))
                        val title = chObj.optString("title", "الفصل $num")
                        val releaseDate = chObj.optString("release_date", chObj.optString("releaseDate", "اليوم"))
                        val isNew = chObj.optBoolean("is_new", chObj.optBoolean("isNew", false))
                        val isClosed = chObj.optBoolean("is_closed", chObj.optBoolean("isClosed", false))
                        chList.add(com.example.data.model.ChapterSummaryDto(num, title, releaseDate, isNew, isClosed))
                    }
                }
                return SeriesInfoDto(
                    status = obj.optString("status", "مستمر"),
                    rating = obj.optDouble("rating", 4.9),
                    views = obj.optString("views", "1.2M"),
                    chapters = chList
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "org.json series info fallback failed: ${e.message}")
        }
        return null
    }

    private fun parseChapterDetailDto(jsonStr: String, mangaId: String, chapterNumber: Int): ChapterDetailDto? {
        val cleanJson = decodeGitHubContent(jsonStr).trim()
        if (cleanJson.isBlank() || cleanJson.startsWith("<!DOCTYPE") || cleanJson.contains("404: Not Found")) {
            return null
        }

        try {
            val adapter = GitHubNetworkModule.moshi.adapter(ChapterDetailDto::class.java)
            val dto = adapter.fromJson(cleanJson)
            if (dto != null) {
                if (dto.isClosed == true) {
                    return dto
                }
                if (!dto.images.isNullOrEmpty() || !dto.pages.isNullOrEmpty()) {
                    val resolvedImages = (dto.images ?: dto.pages ?: emptyList()).mapNotNull { sanitizeImageUrl(it, mangaId) }
                    return dto.copy(images = resolvedImages, pages = resolvedImages)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Moshi chapter detail parsing error: ${e.message}")
        }

        try {
            val imagesList = mutableListOf<String>()

            if (cleanJson.startsWith("[")) {
                val arr = org.json.JSONArray(cleanJson)
                for (i in 0 until arr.length()) {
                    val rawUrl = arr.optString(i)
                    val resolved = sanitizeImageUrl(rawUrl, mangaId)
                    if (!resolved.isNullOrBlank()) imagesList.add(resolved)
                }
                return ChapterDetailDto(
                    series = mangaId,
                    chapter = chapterNumber,
                    title = "الفصل $chapterNumber",
                    totalImages = imagesList.size,
                    images = imagesList,
                    pages = imagesList
                )
            } else {
                val obj = org.json.JSONObject(cleanJson)
                val isClosed = obj.optBoolean("is_closed", obj.optBoolean("isClosed", false))
                val imgArr = obj.optJSONArray("images") ?: obj.optJSONArray("pages") ?: obj.optJSONArray("urls")
                if (imgArr != null) {
                    for (i in 0 until imgArr.length()) {
                        val imgUrl = imgArr.optString(i)
                        val resolved = sanitizeImageUrl(imgUrl, mangaId)
                        if (!resolved.isNullOrBlank()) imagesList.add(resolved)
                    }
                }
                return ChapterDetailDto(
                    series = obj.optString("series", mangaId),
                    chapter = obj.optInt("chapter", chapterNumber),
                    title = obj.optString("title", "الفصل $chapterNumber"),
                    isClosed = isClosed,
                    totalImages = if (isClosed) 0 else obj.optInt("total_images", imagesList.size),
                    images = if (isClosed) emptyList() else imagesList,
                    pages = if (isClosed) emptyList() else imagesList
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "org.json chapter detail fallback failed: ${e.message}")
        }
        return null
    }

    private fun loadMangaFromDiskCache() {
        try {
            val cacheFile = java.io.File(cacheDir, "nexus_works_cache.json")
            if (cacheFile.exists() && cacheFile.length() > 0) {
                val json = cacheFile.readText()
                val worksMap = parseWorksJson(json)
                if (worksMap.isNotEmpty()) {
                    val restoredList = worksMap.map { (key, workDto) ->
                        val slug = workDto.slug ?: workDto.id ?: key
                        val info = loadSeriesInfoFromDiskCache(slug)
                        convertToMangaItem(slug, workDto, info)
                    }
                    if (restoredList.isNotEmpty()) {
                        _allMangaFlow.value = restoredList
                        Log.d(TAG, "Restored ${restoredList.size} works from disk cache.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading works disk cache: ${e.message}")
        }
    }

    private fun saveWorksToDiskCache(worksJsonStr: String) {
        try {
            val cacheFile = java.io.File(cacheDir, "nexus_works_cache.json")
            cacheFile.writeText(worksJsonStr)
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving works to disk cache: ${e.message}")
        }
    }

    private fun saveSeriesInfoToDiskCache(slug: String, infoJson: String) {
        try {
            val cacheFile = java.io.File(cacheDir, "nexus_series_${slug}_cache.json")
            cacheFile.writeText(infoJson)
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving series info disk cache: ${e.message}")
        }
    }

    private fun loadSeriesInfoFromDiskCache(slug: String): SeriesInfoDto? {
        return try {
            val cacheFile = java.io.File(cacheDir, "nexus_series_${slug}_cache.json")
            if (cacheFile.exists() && cacheFile.length() > 0) {
                val json = cacheFile.readText()
                parseSeriesInfoDto(json)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun saveChapterDetailToDiskCache(mangaId: String, chapterNumber: Int, json: String) {
        try {
            val cacheFile = java.io.File(cacheDir, "nexus_ch_${mangaId}_${chapterNumber}.json")
            cacheFile.writeText(json)
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving chapter detail cache: ${e.message}")
        }
    }

    private fun loadChapterDetailFromDiskCache(mangaId: String, chapterNumber: Int): ChapterDetailDto? {
        return try {
            val cacheFile = java.io.File(cacheDir, "nexus_ch_${mangaId}_${chapterNumber}.json")
            if (cacheFile.exists() && cacheFile.length() > 0) {
                val json = cacheFile.readText()
                parseChapterDetailDto(json, mangaId, chapterNumber)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun loadPreferences() {
        val favs = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        _favoritesFlow.value = favs

        val allKeys = prefs.all
        val readMap = mutableMapOf<String, Int>()
        for ((k, v) in allKeys) {
            if (k.startsWith("last_read_") && v is Int) {
                val mangaId = k.removePrefix("last_read_")
                readMap[mangaId] = v
            }
        }
        _lastReadFlow.value = readMap
    }

    fun toggleFavorite(mangaId: String) {
        val current = _favoritesFlow.value.toMutableSet()
        if (current.contains(mangaId)) {
            current.remove(mangaId)
        } else {
            current.add(mangaId)
        }
        _favoritesFlow.value = current
        prefs.edit().putStringSet("favorites", current).apply()
    }

    fun isFavorite(mangaId: String): Boolean {
        return _favoritesFlow.value.contains(mangaId)
    }

    fun saveLastRead(mangaId: String, chapterNumber: Int) {
        val current = _lastReadFlow.value.toMutableMap()
        current[mangaId] = chapterNumber
        _lastReadFlow.value = current
        prefs.edit().putInt("last_read_$mangaId", chapterNumber).apply()
    }

    fun getLastReadChapter(mangaId: String): Int {
        return _lastReadFlow.value[mangaId] ?: 1
    }

    fun getAllManga(): List<MangaItem> = _allMangaFlow.value

    fun getHeroFeaturedManga(): List<MangaItem> = getAllManga().take(5)

    fun getMangaById(id: String): MangaItem? {
        return _allMangaFlow.value.find { it.id == id }
    }

    // =========================================================================
    // SECURE OFFLINE DOWNLOADS (حماية المحتوى والتنزيل المشفر داخل التطبيق)
    // =========================================================================

    private fun loadDownloadedChaptersManifest() {
        try {
            val manifestFile = File(context.filesDir, "secure_downloads_manifest.json")
            if (manifestFile.exists() && manifestFile.length() > 0) {
                val json = manifestFile.readText()
                val listType = Types.newParameterizedType(List::class.java, DownloadedChapter::class.java)
                val adapter = GitHubNetworkModule.moshi.adapter<List<DownloadedChapter>>(listType)
                val list = adapter.fromJson(json) ?: emptyList()
                // Verify that files still exist on disk
                val validList = list.filter { item ->
                    item.localImagePaths.isNotEmpty() && File(item.localImagePaths.first()).exists()
                }
                _downloadedChaptersFlow.value = validList
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading downloaded chapters manifest: ${e.message}")
        }
    }

    private fun saveDownloadedChaptersManifest() {
        try {
            val manifestFile = File(context.filesDir, "secure_downloads_manifest.json")
            val listType = Types.newParameterizedType(List::class.java, DownloadedChapter::class.java)
            val adapter = GitHubNetworkModule.moshi.adapter<List<DownloadedChapter>>(listType)
            val json = adapter.toJson(_downloadedChaptersFlow.value)
            manifestFile.writeText(json)
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving downloaded chapters manifest: ${e.message}")
        }
    }

    fun isChapterDownloaded(mangaId: String, chapterNumber: Int): Boolean {
        return _downloadedChaptersFlow.value.any { it.mangaId == mangaId && it.chapterNumber == chapterNumber }
    }

    fun getDownloadedChapter(mangaId: String, chapterNumber: Int): DownloadedChapter? {
        return _downloadedChaptersFlow.value.find { it.mangaId == mangaId && it.chapterNumber == chapterNumber }
    }

    suspend fun downloadChapter(manga: MangaItem, chapter: Chapter): Result<DownloadedChapter> = withContext(Dispatchers.IO) {
        val downloadKey = "${manga.id}_${chapter.number}"
        try {
            // Update progress: starting
            updateDownloadProgress(manga.id, chapter.number, currentStep = 0, totalSteps = 1, progress = 0.05f)

            // Step 1: Ensure we have chapter pages
            val fullChapter = if (chapter.pages.isNotEmpty()) {
                chapter
            } else {
                getChapterWithPages(manga.id, chapter.number) ?: throw IllegalStateException("تعذر جلب صفحات الفصل للتنزيل")
            }

            if (fullChapter.pages.isEmpty()) {
                throw IllegalStateException("الفصل لا يحتوي على صفحات للتحميل")
            }

            val chapterDir = File(secureStorageDir, "${manga.id}_ch_${chapter.number}")
            if (!chapterDir.exists()) {
                chapterDir.mkdirs()
            }

            val totalPages = fullChapter.pages.size
            val localPaths = mutableListOf<String>()
            var totalBytes = 0L

            // Step 2: Download each page securely into app's private filesDir
            for ((index, page) in fullChapter.pages.withIndex()) {
                val imageUrl = page.imageUrl
                if (imageUrl.isNullOrBlank()) continue

                val targetFile = File(chapterDir, "page_${String.format("%03d", index + 1)}.nexus")
                
                // If not already downloaded, fetch from network
                if (!targetFile.exists() || targetFile.length() == 0L) {
                    val request = Request.Builder().url(imageUrl).build()
                    val response = GitHubNetworkModule.okHttpClient.newCall(request).execute()
                    if (!response.isSuccessful || response.body == null) {
                        throw IllegalStateException("فشل تنزيل الصفحة ${index + 1} (${response.code})")
                    }

                    val bytes = response.body!!.bytes()
                    FileOutputStream(targetFile).use { fos ->
                        fos.write(bytes)
                    }
                }

                totalBytes += targetFile.length()
                localPaths.add(targetFile.absolutePath)

                // Update progress
                val currentProgress = 0.1f + (0.9f * (index + 1).toFloat() / totalPages.toFloat())
                updateDownloadProgress(
                    manga.id,
                    chapter.number,
                    currentStep = index + 1,
                    totalSteps = totalPages,
                    progress = currentProgress
                )
            }

            val downloadedChapter = DownloadedChapter(
                mangaId = manga.id,
                mangaTitle = manga.titleAr,
                mangaCover = manga.coverUrl,
                chapterNumber = chapter.number,
                chapterTitle = chapter.title,
                totalPages = localPaths.size,
                downloadedAt = System.currentTimeMillis(),
                sizeBytes = totalBytes,
                localImagePaths = localPaths
            )

            // Update state & manifest
            val currentList = _downloadedChaptersFlow.value.filterNot { it.mangaId == manga.id && it.chapterNumber == chapter.number }.toMutableList()
            currentList.add(downloadedChapter)
            _downloadedChaptersFlow.value = currentList
            saveDownloadedChaptersManifest()

            // Finish download progress
            updateDownloadProgress(manga.id, chapter.number, currentStep = totalPages, totalSteps = totalPages, progress = 1.0f, isCompleted = true)

            Result.success(downloadedChapter)
        } catch (e: Exception) {
            Log.e(TAG, "Download chapter error: ${e.message}", e)
            updateDownloadProgress(manga.id, chapter.number, isFailed = true, error = e.message ?: "فشل التحميل")
            Result.failure(e)
        }
    }

    private fun updateDownloadProgress(
        mangaId: String,
        chapterNumber: Int,
        currentStep: Int = 0,
        totalSteps: Int = 0,
        progress: Float = 0f,
        isCompleted: Boolean = false,
        isFailed: Boolean = false,
        error: String? = null
    ) {
        val key = "${mangaId}_$chapterNumber"
        val map = _downloadProgressFlow.value.toMutableMap()
        if (isCompleted || isFailed) {
            map[key] = ChapterDownloadProgress(
                mangaId = mangaId,
                chapterNumber = chapterNumber,
                currentStep = currentStep,
                totalSteps = totalSteps,
                progress = progress,
                isCompleted = isCompleted,
                isFailed = isFailed,
                error = error
            )
        } else {
            map[key] = ChapterDownloadProgress(
                mangaId = mangaId,
                chapterNumber = chapterNumber,
                currentStep = currentStep,
                totalSteps = totalSteps,
                progress = progress,
                isCompleted = false,
                isFailed = false,
                error = null
            )
        }
        _downloadProgressFlow.value = map
    }

    suspend fun deleteDownloadedChapter(mangaId: String, chapterNumber: Int) = withContext(Dispatchers.IO) {
        try {
            val chapterDir = File(secureStorageDir, "${mangaId}_ch_${chapterNumber}")
            if (chapterDir.exists()) {
                chapterDir.deleteRecursively()
            }
            val currentList = _downloadedChaptersFlow.value.filterNot { it.mangaId == mangaId && it.chapterNumber == chapterNumber }
            _downloadedChaptersFlow.value = currentList
            saveDownloadedChaptersManifest()

            val progressMap = _downloadProgressFlow.value.toMutableMap()
            progressMap.remove("${mangaId}_$chapterNumber")
            _downloadProgressFlow.value = progressMap
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting downloaded chapter", e)
        }
    }

    fun getTotalDownloadedSizeBytes(): Long {
        return _downloadedChaptersFlow.value.sumOf { it.sizeBytes }
    }

    // =========================================================================
    // READING HISTORY & PROGRESS TRACKING (سجل القراءة وتتبع الصفحة)
    // =========================================================================

    private fun loadReadingHistoryFromDisk() {
        try {
            val historyFile = File(context.filesDir, "nexus_reading_history.json")
            if (historyFile.exists() && historyFile.length() > 0) {
                val json = historyFile.readText()
                val listType = Types.newParameterizedType(List::class.java, ReadingHistoryEntry::class.java)
                val adapter = GitHubNetworkModule.moshi.adapter<List<ReadingHistoryEntry>>(listType)
                val list = adapter.fromJson(json) ?: emptyList()
                _readingHistoryFlow.value = list.sortedByDescending { it.timestamp }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading reading history: ${e.message}")
        }
    }

    private fun saveReadingHistoryToDisk() {
        try {
            val historyFile = File(context.filesDir, "nexus_reading_history.json")
            val listType = Types.newParameterizedType(List::class.java, ReadingHistoryEntry::class.java)
            val adapter = GitHubNetworkModule.moshi.adapter<List<ReadingHistoryEntry>>(listType)
            val json = adapter.toJson(_readingHistoryFlow.value)
            historyFile.writeText(json)
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving reading history: ${e.message}")
        }
    }

    fun recordReadingProgress(
        mangaId: String,
        mangaTitle: String,
        mangaCover: String?,
        chapterNumber: Int,
        chapterTitle: String,
        pageNumber: Int,
        totalPages: Int
    ) {
        // 1. Update last read chapter preference
        saveLastRead(mangaId, chapterNumber)

        // 2. Save last page read for this chapter in prefs
        prefs.edit().putInt("last_page_${mangaId}_$chapterNumber", pageNumber).apply()

        // 3. Update Reading History list
        val entry = ReadingHistoryEntry(
            mangaId = mangaId,
            mangaTitle = mangaTitle,
            mangaCover = mangaCover,
            chapterNumber = chapterNumber,
            chapterTitle = chapterTitle,
            pageNumber = pageNumber,
            totalPages = totalPages,
            timestamp = System.currentTimeMillis()
        )

        val currentList = _readingHistoryFlow.value.filterNot { it.mangaId == mangaId }.toMutableList()
        currentList.add(0, entry)
        _readingHistoryFlow.value = currentList.take(50) // Keep top 50 recent items
        saveReadingHistoryToDisk()
    }

    fun getLastReadPage(mangaId: String, chapterNumber: Int): Int {
        return prefs.getInt("last_page_${mangaId}_$chapterNumber", 1)
    }

    fun deleteReadingHistoryItem(mangaId: String) {
        val currentList = _readingHistoryFlow.value.filterNot { it.mangaId == mangaId }
        _readingHistoryFlow.value = currentList
        saveReadingHistoryToDisk()
    }

    fun clearAllReadingHistory() {
        _readingHistoryFlow.value = emptyList()
        saveReadingHistoryToDisk()
    }

    /**
     * Refresh data from GitHub Data repository (zxiu86/Data):
     * 1. data/works.json
     * 2. data/[series-slug]/info.json (fetched in parallel)
     * Utilizes aggressive cache-busting to bypass CDN delay immediately!
     */
    suspend fun refreshMangaFromGitHub(forceFresh: Boolean = true): Result<List<MangaItem>> = withContext(Dispatchers.IO) {
        try {
            val owner = GitHubNetworkModule.getConfiguredOwner()
            val repo = GitHubNetworkModule.getDataRepo()
            val branch = GitHubNetworkModule.getConfiguredBranch()

            if (forceFresh) {
                GitHubNetworkModule.clearHttpCache()
                loadedChaptersCache.clear()
            }

            Log.d(TAG, "Fetching works fresh from GitHub Data repo: $owner/$repo (branch: $branch, forceFresh: $forceFresh)")

            var worksJsonStr: String? = null

            // Direct raw URLs with cache-busting for sub-second live updates
            val directUrls = listOf(
                "https://raw.githubusercontent.com/$owner/$repo/$branch/data/works.json",
                "https://raw.githubusercontent.com/$owner/$repo/$branch/works.json",
                "https://cdn.jsdelivr.net/gh/$owner/$repo@$branch/data/works.json",
                "https://cdn.jsdelivr.net/gh/$owner/$repo@$branch/works.json"
            )

            for (url in directUrls) {
                val directResult = GitHubNetworkModule.fetchDirectRaw(url, forceFresh = forceFresh)
                if (!directResult.isNullOrBlank()) {
                    val candidateMap = parseWorksJson(directResult)
                    if (candidateMap.isNotEmpty()) {
                        worksJsonStr = directResult
                        Log.d(TAG, "Successfully retrieved fresh works from: $url")
                        break
                    }
                }
            }

            // Fallback: GitHub API endpoint with multiple path candidates
            if (worksJsonStr.isNullOrBlank()) {
                val candidatePaths = listOf("data/works.json", "works.json", "data/works", "works")
                for (path in candidatePaths) {
                    try {
                        val worksResponse = GitHubNetworkModule.apiService.getContentRaw(owner, repo, path, branch)
                        if (worksResponse.isSuccessful && worksResponse.body() != null) {
                            val candidate = worksResponse.body()!!.string()
                            val candidateMap = parseWorksJson(candidate)
                            if (candidateMap.isNotEmpty()) {
                                worksJsonStr = candidate
                                Log.d(TAG, "Successfully retrieved works from API path: $path")
                                break
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "API Path $path attempt failed: ${e.message}")
                    }
                }
            }

            if (worksJsonStr.isNullOrBlank()) {
                Log.w(TAG, "Failed to load works.json from $owner/$repo, using cached manga")
                val current = _allMangaFlow.value
                return@withContext Result.success(current)
            }

            val decodedJson = decodeGitHubContent(worksJsonStr)
            saveWorksToDiskCache(decodedJson)

            val worksMap = parseWorksJson(decodedJson)

            if (worksMap.isEmpty()) {
                Log.w(TAG, "works.json parsed map was empty.")
                return@withContext Result.success(_allMangaFlow.value)
            }

            // Fetch info.json for each work in parallel with cache-busting
            val fullMangaList = coroutineScope {
                worksMap.map { (key, workDto) ->
                    async {
                        val slug = workDto.slug ?: workDto.id ?: key
                        val info = fetchSeriesInfoSafely(owner, repo, slug, branch, forceFresh = forceFresh)
                        convertToMangaItem(slug, workDto, info)
                    }
                }.awaitAll()
            }

            if (fullMangaList.isNotEmpty()) {
                _allMangaFlow.value = fullMangaList
                Log.d(TAG, "Successfully loaded and updated ${fullMangaList.size} works from GitHub!")
            }
            Result.success(fullMangaList)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing data from GitHub", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchSeriesInfoSafely(
        owner: String,
        repo: String,
        slug: String,
        branch: String,
        forceFresh: Boolean = true
    ): SeriesInfoDto? = withContext(Dispatchers.IO) {
        val directUrls = listOf(
            "https://raw.githubusercontent.com/$owner/$repo/$branch/data/$slug/info.json",
            "https://raw.githubusercontent.com/$owner/$repo/$branch/$slug/info.json",
            "https://cdn.jsdelivr.net/gh/$owner/$repo@$branch/data/$slug/info.json",
            "https://cdn.jsdelivr.net/gh/$owner/$repo@$branch/$slug/info.json"
        )

        for (url in directUrls) {
            try {
                val directResult = GitHubNetworkModule.fetchDirectRaw(url, forceFresh = forceFresh)
                if (!directResult.isNullOrBlank()) {
                    val decodedJson = decodeGitHubContent(directResult)
                    val info = parseSeriesInfoDto(decodedJson)
                    if (info != null) {
                        saveSeriesInfoToDiskCache(slug, decodedJson)
                        return@withContext info
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Direct mirror for info.json failed: $url (${e.message})")
            }
        }

        val candidatePaths = listOf(
            "data/$slug/info.json",
            "$slug/info.json",
            "data/$slug.json",
            "$slug.json",
            "data/$slug/details.json"
        )

        for (path in candidatePaths) {
            try {
                val response = GitHubNetworkModule.apiService.getContentRaw(owner, repo, path, branch)
                if (response.isSuccessful && response.body() != null) {
                    val rawJson = response.body()!!.string()
                    val decodedJson = decodeGitHubContent(rawJson)
                    val info = parseSeriesInfoDto(decodedJson)
                    if (info != null) {
                        saveSeriesInfoToDiskCache(slug, decodedJson)
                        return@withContext info
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Series info API path $path failed: ${e.message}")
            }
        }

        loadSeriesInfoFromDiskCache(slug)
    }

    private fun convertToMangaItem(
        slug: String,
        workDto: WorkDto,
        seriesInfo: SeriesInfoDto?
    ): MangaItem {
        val rawChapters = seriesInfo?.chapters ?: emptyList()
        val chaptersList = rawChapters.map { chSummary ->
            val isClosed = chSummary.isClosed ?: false
            Chapter(
                id = "${slug}_ch_${chSummary.number}",
                mangaId = slug,
                number = chSummary.number,
                title = chSummary.title ?: "الفصل ${chSummary.number}",
                releaseDate = if (isClosed) "تحت الصيانة" else (chSummary.releaseDate ?: "اليوم"),
                isNew = chSummary.isNew ?: false,
                isClosed = isClosed,
                pagesCount = if (isClosed) 1 else 0,
                pages = if (isClosed) listOf(
                    ChapterPage(
                        pageNumber = 1,
                        imageRes = com.example.R.drawable.chapter_closed_notice_1788280703973,
                        caption = "هذا الفصل تحت الصيانة وإعادة الترجمة"
                    )
                ) else emptyList()
            )
        }.sortedBy { it.number }

        val typeEnum = MangaType.fromString(workDto.type)
        val title = workDto.title ?: workDto.name ?: slug
        val cover = sanitizeImageUrl(workDto.cover ?: workDto.thumbnail ?: workDto.image, slug)
        val summary = workDto.summary ?: workDto.description ?: "لا يوجد وصف متوفر للعمل حالياً."

        return MangaItem(
            id = slug,
            titleAr = title,
            titleEn = title,
            type = typeEnum,
            coverUrl = cover,
            bannerUrl = cover,
            synopsis = summary,
            author = workDto.author ?: "غير محدد",
            artist = workDto.artist ?: "غير محدد",
            scanlationTeam = "فريق نكسوس للترجمة (Nexus Scans)",
            rating = (seriesInfo?.rating ?: 4.9).toFloat(),
            views = seriesInfo?.views ?: "1.2M",
            status = seriesInfo?.status ?: "مستمر",
            genres = workDto.genres ?: listOf("مانها", "أكشن"),
            totalChaptersCount = chaptersList.size,
            chapters = chaptersList
        )
    }

    /**
     * Retrieves chapter pages.
     * 1. Checks if chapter is downloaded locally in private storage -> serves offline immediately!
     * 2. Checks memory cache
     * 3. Checks remote GitHub data/[series-slug]/[chapter].json
     */
    suspend fun getChapterWithPages(mangaId: String, chapterNumber: Int): Chapter? =
        withContext(Dispatchers.IO) {
            val cacheKey = "${mangaId}_$chapterNumber"

            // 1. Check offline downloaded chapters first
            val downloaded = getDownloadedChapter(mangaId, chapterNumber)
            if (downloaded != null && downloaded.localImagePaths.isNotEmpty()) {
                val pages = downloaded.localImagePaths.mapIndexed { idx, path ->
                    ChapterPage(pageNumber = idx + 1, imageUrl = path, caption = "صفحة ${idx + 1}")
                }
                val localChapter = Chapter(
                    id = "${mangaId}_ch_$chapterNumber",
                    mangaId = mangaId,
                    number = chapterNumber,
                    title = downloaded.chapterTitle.ifBlank { "الفصل $chapterNumber" },
                    releaseDate = "محفوظ بالجهاز",
                    pagesCount = pages.size,
                    pages = pages
                )
                loadedChaptersCache[cacheKey] = localChapter
                return@withContext localChapter
            }

            // 2. Check memory cache
            if (loadedChaptersCache.containsKey(cacheKey)) {
                return@withContext loadedChaptersCache[cacheKey]
            }

            val owner = GitHubNetworkModule.getConfiguredOwner()
            val repo = GitHubNetworkModule.getDataRepo()
            val branch = GitHubNetworkModule.getConfiguredBranch()

            val directUrls = listOf(
                "https://raw.githubusercontent.com/$owner/$repo/$branch/data/$mangaId/$chapterNumber.json",
                "https://raw.githubusercontent.com/$owner/$repo/$branch/$mangaId/$chapterNumber.json",
                "https://raw.githubusercontent.com/$owner/$repo/$branch/data/$mangaId/chapters/$chapterNumber.json",
                "https://cdn.jsdelivr.net/gh/$owner/$repo@$branch/data/$mangaId/$chapterNumber.json",
                "https://cdn.jsdelivr.net/gh/$owner/$repo@$branch/$mangaId/$chapterNumber.json"
            )

            for (url in directUrls) {
                try {
                    val directResult = GitHubNetworkModule.fetchDirectRaw(url, forceFresh = true)
                    if (!directResult.isNullOrBlank()) {
                        val decodedJson = decodeGitHubContent(directResult)
                        val detail = parseChapterDetailDto(decodedJson, mangaId, chapterNumber)
                        if (detail?.isClosed == true) {
                            saveChapterDetailToDiskCache(mangaId, chapterNumber, decodedJson)
                            val chapter = Chapter(
                                id = "${mangaId}_ch_$chapterNumber",
                                mangaId = mangaId,
                                number = chapterNumber,
                                title = detail.title ?: "الفصل $chapterNumber",
                                releaseDate = "تحت الصيانة",
                                isClosed = true,
                                pagesCount = 1,
                                pages = listOf(
                                    ChapterPage(
                                        pageNumber = 1,
                                        imageRes = com.example.R.drawable.chapter_closed_notice_1788280703973,
                                        caption = "هذا الفصل تحت الصيانة وإعادة الترجمة"
                                    )
                                )
                            )
                            loadedChaptersCache[cacheKey] = chapter
                            return@withContext chapter
                        }
                        val imageList = detail?.images ?: detail?.pages ?: emptyList()
                        if (imageList.isNotEmpty()) {
                            saveChapterDetailToDiskCache(mangaId, chapterNumber, decodedJson)
                            val pages = imageList.mapIndexed { idx, imgUrl ->
                                ChapterPage(pageNumber = idx + 1, imageUrl = imgUrl, caption = "صفحة ${idx + 1}")
                            }
                            val chapter = Chapter(
                                id = "${mangaId}_ch_$chapterNumber",
                                mangaId = mangaId,
                                number = chapterNumber,
                                title = detail?.title ?: "الفصل $chapterNumber",
                                releaseDate = "اليوم",
                                isClosed = false,
                                pagesCount = pages.size,
                                pages = pages
                            )
                            loadedChaptersCache[cacheKey] = chapter
                            return@withContext chapter
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Direct raw chapter mirror failed: $url (${e.message})")
                }
            }

            val candidatePaths = listOf(
                "data/$mangaId/$chapterNumber.json",
                "$mangaId/$chapterNumber.json",
                "data/$mangaId/chapters/$chapterNumber.json",
                "$mangaId/chapters/$chapterNumber.json",
                "data/$mangaId/${String.format("%02d", chapterNumber)}.json",
                "$mangaId/${String.format("%02d", chapterNumber)}.json"
            )

            for (path in candidatePaths) {
                try {
                    val response = GitHubNetworkModule.apiService.getContentRaw(owner, repo, path, branch)
                    if (response.isSuccessful && response.body() != null) {
                        val rawJson = response.body()!!.string()
                        val decodedJson = decodeGitHubContent(rawJson)
                        val detail = parseChapterDetailDto(decodedJson, mangaId, chapterNumber)

                        if (detail?.isClosed == true) {
                            saveChapterDetailToDiskCache(mangaId, chapterNumber, decodedJson)
                            val chapter = Chapter(
                                id = "${mangaId}_ch_$chapterNumber",
                                mangaId = mangaId,
                                number = chapterNumber,
                                title = detail.title ?: "الفصل $chapterNumber",
                                releaseDate = "تحت الصيانة",
                                isClosed = true,
                                pagesCount = 1,
                                pages = listOf(
                                    ChapterPage(
                                        pageNumber = 1,
                                        imageRes = com.example.R.drawable.chapter_closed_notice_1788280703973,
                                        caption = "هذا الفصل تحت الصيانة وإعادة الترجمة"
                                    )
                                )
                            )
                            loadedChaptersCache[cacheKey] = chapter
                            return@withContext chapter
                        }

                        val imageList = if (!detail?.images.isNullOrEmpty()) {
                            detail!!.images!!
                        } else if (!detail?.pages.isNullOrEmpty()) {
                            detail!!.pages!!
                        } else {
                            emptyList()
                        }

                        if (imageList.isNotEmpty()) {
                            saveChapterDetailToDiskCache(mangaId, chapterNumber, decodedJson)
                            val pages = imageList.mapIndexed { idx, url ->
                                ChapterPage(
                                    pageNumber = idx + 1,
                                    imageUrl = url,
                                    caption = "صفحة ${idx + 1}"
                                )
                            }
                            val chapter = Chapter(
                                id = "${mangaId}_ch_$chapterNumber",
                                mangaId = mangaId,
                                number = chapterNumber,
                                title = detail?.title ?: "الفصل $chapterNumber",
                                releaseDate = "اليوم",
                                isClosed = false,
                                pagesCount = pages.size,
                                pages = pages
                            )
                            loadedChaptersCache[cacheKey] = chapter
                            return@withContext chapter
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Chapter API path $path failed: ${e.message}")
                }
            }

            // Check if available in disk cache
            val cachedDetail = loadChapterDetailFromDiskCache(mangaId, chapterNumber)
            if (cachedDetail?.isClosed == true) {
                val chapter = Chapter(
                    id = "${mangaId}_ch_$chapterNumber",
                    mangaId = mangaId,
                    number = chapterNumber,
                    title = cachedDetail.title ?: "الفصل $chapterNumber",
                    releaseDate = "تحت الصيانة",
                    isClosed = true,
                    pagesCount = 1,
                    pages = listOf(
                        ChapterPage(
                            pageNumber = 1,
                            imageRes = com.example.R.drawable.chapter_closed_notice_1788280703973,
                            caption = "هذا الفصل تحت الصيانة وإعادة الترجمة"
                        )
                    )
                )
                loadedChaptersCache[cacheKey] = chapter
                return@withContext chapter
            }

            val cachedImages = if (!cachedDetail?.images.isNullOrEmpty()) {
                cachedDetail!!.images!!
            } else if (!cachedDetail?.pages.isNullOrEmpty()) {
                cachedDetail!!.pages!!
            } else {
                emptyList()
            }

            if (cachedImages.isNotEmpty()) {
                val pages = cachedImages.mapIndexed { idx, url ->
                    ChapterPage(
                        pageNumber = idx + 1,
                        imageUrl = url,
                        caption = "صفحة ${idx + 1}"
                    )
                }
                val chapter = Chapter(
                    id = "${mangaId}_ch_$chapterNumber",
                    mangaId = mangaId,
                    number = chapterNumber,
                    title = cachedDetail?.title ?: "الفصل $chapterNumber",
                    releaseDate = "اليوم",
                    isClosed = false,
                    pagesCount = pages.size,
                    pages = pages
                )
                loadedChaptersCache[cacheKey] = chapter
                return@withContext chapter
            }

            // Fallback to local chapter representation if cached
            val manga = getMangaById(mangaId)
            val fallbackChapter = manga?.chapters?.find { it.number == chapterNumber }
            if (fallbackChapter != null && (fallbackChapter.pages.isNotEmpty() || fallbackChapter.isClosed)) {
                val finalChapter = if (fallbackChapter.isClosed && fallbackChapter.pages.isEmpty()) {
                    fallbackChapter.copy(
                        pagesCount = 1,
                        pages = listOf(
                            ChapterPage(
                                pageNumber = 1,
                                imageRes = com.example.R.drawable.chapter_closed_notice_1788280703973,
                                caption = "هذا الفصل تحت الصيانة وإعادة الترجمة"
                            )
                        )
                    )
                } else fallbackChapter
                loadedChaptersCache[cacheKey] = finalChapter
                return@withContext finalChapter
            }

            null
        }

    /**
     * Checks GitHub Releases for In-App Updates against current version (1.7.1)
     * Queries repository: zxiu86/Nexus
     */
    suspend fun checkForAppUpdate(): AppUpdateState = withContext(Dispatchers.IO) {
        val currentVersion = "1.7.1"
        val v16Changelog = "✨ مميزات وتحديثات الإصدار v1.7.1:\n" +
                "• 🖋️ تشغيل وتضمين الخط العربي الأميري (Amiri Font): يعمل الآن بشكل فوري ومباشر أوفلاين على كافة واجهات ونصوص التطبيق.\n" +
                "• 🚀 سلاسة تامة وإلغاء اللاج والتأخير: تحسين سرعة التنقل وتقليب الصفحات داخل القارئ وتجربة تصفح سريعة 60/120 إطاراً في الثانية.\n" +
                "• ⚡ التحديث الفوري وتجاوز الكاش (Cache Busting): تحميل مباشر وتلقائي لأحدث الأعمال والفصول المرفوعة على جيت هوب بدون أي انتظار أو تأخير.\n" +
                "• 🔄 زر التحديث السريع بالهيدر: إمكانية إجبار التطبيق على مزامنة البيانات السحابية فوراً بنقرة واحدة.\n" +
                "• 🔒 القراءة بدون اتصال والتنزيل المشفر: إمكانية تحميل الفصول وقراءتها بدون إنترنت مع حماية كاملة للمحتوى.\n" +
                "• 🛡️ حماية المحتوى والخصوصية: منع لقطات الشاشة وتسجيل الفيديو داخل قارئ الفصول لحفظ حقوق الأعمال.\n" +
                "• 🔍 وضع القراءة المغمور والتكبير التفاعلي: شاشة كاملة 100% مع دعم التقريب والتحريك باللمس.\n" +
                "• 📊 سجل القراءة وتتبع التقدم التلقائي: حفظ موضع القراءة والصفحة بدقة مع إمكانية المتابعة الفورية.\n" +
                "• 🧭 شريط تنقل سفلي فاخر: تبديل سريع بين الرئيسية، المفضلة، السجل، التحميلات، والتحديثات."

        try {
            val owner = GitHubNetworkModule.getConfiguredOwner()
            val appRepo = GitHubNetworkModule.getAppRepo() // "Nexus"

            // 1. Try getLatestRelease from Nexus repo
            try {
                val response = GitHubNetworkModule.apiService.getLatestRelease(owner, appRepo)
                if (response.isSuccessful && response.body() != null) {
                    val release = response.body()!!
                    val tag = release.tagName?.removePrefix("v")?.trim() ?: ""
                    val apkAsset = release.assets?.find {
                        it.name?.endsWith(".apk", ignoreCase = true) == true ||
                                it.contentType?.contains("android.package-archive") == true ||
                                it.contentType?.contains("octet-stream") == true
                    }

                    val hasNewerVersion = isVersionGreater(tag, currentVersion)
                    val downloadUrl = apkAsset?.browserDownloadUrl ?: release.assets?.firstOrNull()?.browserDownloadUrl ?: ""

                    return@withContext AppUpdateState(
                        isChecking = false,
                        updateAvailable = hasNewerVersion && downloadUrl.isNotBlank(),
                        latestVersion = tag.ifEmpty { release.name ?: currentVersion },
                        currentVersion = currentVersion,
                        releaseNotes = if (release.body.isNullOrBlank()) v16Changelog else "${release.body}\n\n$v16Changelog",
                        downloadUrl = downloadUrl
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Latest release check failed: ${e.message}")
            }

            // 2. Try getAllReleases from Nexus repo
            try {
                val allResponse = GitHubNetworkModule.apiService.getAllReleases(owner, appRepo)
                if (allResponse.isSuccessful && !allResponse.body().isNullOrEmpty()) {
                    val release = allResponse.body()!!.first()
                    val tag = release.tagName?.removePrefix("v")?.trim() ?: ""
                    val apkAsset = release.assets?.find {
                        it.name?.endsWith(".apk", ignoreCase = true) == true ||
                                it.contentType?.contains("android.package-archive") == true
                    }
                    val hasNewerVersion = isVersionGreater(tag, currentVersion)
                    val downloadUrl = apkAsset?.browserDownloadUrl ?: ""

                    return@withContext AppUpdateState(
                        isChecking = false,
                        updateAvailable = hasNewerVersion && downloadUrl.isNotBlank(),
                        latestVersion = tag.ifEmpty { release.name ?: currentVersion },
                        currentVersion = currentVersion,
                        releaseNotes = if (release.body.isNullOrBlank()) v16Changelog else "${release.body}\n\n$v16Changelog",
                        downloadUrl = downloadUrl
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "All releases check failed: ${e.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed: ${e.message}")
        }

        AppUpdateState(
            isChecking = false,
            updateAvailable = false,
            currentVersion = currentVersion,
            latestVersion = currentVersion,
            releaseNotes = v16Changelog
        )
    }

    private fun isVersionGreater(remoteVersion: String, currentVersion: String): Boolean {
        if (remoteVersion.isBlank()) return false
        val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until length) {
            val remote = remoteParts.getOrElse(i) { 0 }
            val current = currentParts.getOrElse(i) { 0 }
            if (remote > current) return true
            if (remote < current) return false
        }
        return false
    }
}

