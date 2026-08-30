package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.data.model.AppUpdateState
import com.example.data.model.Chapter
import com.example.data.model.ChapterDetailDto
import com.example.data.model.ChapterPage
import com.example.data.model.MangaItem
import com.example.data.model.MangaType
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

class MangaRepository(context: Context) {

    private val TAG = "NexusMangaRepository"
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexus_manga_prefs", Context.MODE_PRIVATE)

    private val _favoritesFlow = MutableStateFlow<Set<String>>(emptySet())
    val favoritesFlow: StateFlow<Set<String>> = _favoritesFlow.asStateFlow()

    private val _lastReadFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
    val lastReadFlow: StateFlow<Map<String, Int>> = _lastReadFlow.asStateFlow()

    private val _allMangaFlow = MutableStateFlow<List<MangaItem>>(getDefaultMangaList())
    val allMangaFlow: StateFlow<List<MangaItem>> = _allMangaFlow.asStateFlow()

    // Dynamic chapter cache with loaded images
    private val loadedChaptersCache = mutableMapOf<String, Chapter>()
    private val cacheDir = context.cacheDir

    init {
        loadPreferences()
        loadMangaFromDiskCache()
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
            Log.w(TAG, "Moshi works parsing error: ${e.message}, using JSONObject fallback")
        }

        if (resultMap.isNotEmpty()) return resultMap

        // Strategy 2: org.json.JSONObject fallback (100% resilient)
        try {
            if (cleanJson.startsWith("[")) {
                val jsonArray = org.json.JSONArray(cleanJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i) ?: continue
                    val slug = obj.optString("slug", obj.optString("id", "work_$i"))
                    val genresList = mutableListOf<String>()
                    val gArr = obj.optJSONArray("genres")
                    if (gArr != null) {
                        for (j in 0 until gArr.length()) {
                            genresList.add(gArr.optString(j))
                        }
                    }
                    resultMap[slug] = WorkDto(
                        slug = slug,
                        id = obj.optString("id", slug),
                        title = obj.optString("title", obj.optString("name", slug)),
                        name = obj.optString("name", obj.optString("title", slug)),
                        cover = obj.optString("cover", obj.optString("thumbnail", obj.optString("image", ""))),
                        thumbnail = obj.optString("thumbnail", obj.optString("cover", "")),
                        image = obj.optString("image", obj.optString("cover", "")),
                        summary = obj.optString("summary", obj.optString("description", "")),
                        description = obj.optString("description", obj.optString("summary", "")),
                        type = obj.optString("type", "مانهوا"),
                        author = obj.optString("author", "غير محدد"),
                        artist = obj.optString("artist", "غير محدد"),
                        genres = genresList
                    )
                }
            } else {
                val jsonObj = org.json.JSONObject(cleanJson)
                val keys = jsonObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = jsonObj.optJSONObject(key)
                    if (obj != null) {
                        val slug = obj.optString("slug", key)
                        val genresList = mutableListOf<String>()
                        val gArr = obj.optJSONArray("genres")
                        if (gArr != null) {
                            for (j in 0 until gArr.length()) {
                                genresList.add(gArr.optString(j))
                            }
                        }
                        resultMap[key] = WorkDto(
                            slug = slug,
                            id = obj.optString("id", key),
                            title = obj.optString("title", obj.optString("name", key)),
                            name = obj.optString("name", obj.optString("title", key)),
                            cover = obj.optString("cover", obj.optString("thumbnail", obj.optString("image", ""))),
                            thumbnail = obj.optString("thumbnail", obj.optString("cover", "")),
                            image = obj.optString("image", obj.optString("cover", "")),
                            summary = obj.optString("summary", obj.optString("description", "")),
                            description = obj.optString("description", obj.optString("summary", "")),
                            type = obj.optString("type", "مانهوا"),
                            author = obj.optString("author", "غير محدد"),
                            artist = obj.optString("artist", "غير محدد"),
                            genres = genresList
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "org.json works parsing failed", e)
        }

        return resultMap
    }

    private fun parseSeriesInfoDto(jsonStr: String): SeriesInfoDto? {
        val cleanJson = decodeGitHubContent(jsonStr).trim()
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
            val obj = org.json.JSONObject(cleanJson)
            val chList = mutableListOf<com.example.data.model.ChapterSummaryDto>()
            val arr = obj.optJSONArray("chapters")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val chObj = arr.optJSONObject(i) ?: continue
                    val num = chObj.optInt("number", i + 1)
                    val title = chObj.optString("title", "الفصل $num")
                    val releaseDate = chObj.optString("release_date", chObj.optString("releaseDate", "اليوم"))
                    val isNew = chObj.optBoolean("is_new", chObj.optBoolean("isNew", false))
                    chList.add(
                        com.example.data.model.ChapterSummaryDto(
                            number = num,
                            title = title,
                            releaseDate = releaseDate,
                            isNew = isNew
                        )
                    )
                }
            }
            return SeriesInfoDto(
                status = obj.optString("status", "مستمر"),
                rating = obj.optDouble("rating", 4.9),
                views = obj.optString("views", "1.2M"),
                chapters = chList
            )
        } catch (e: Exception) {
            Log.w(TAG, "org.json series info fallback failed: ${e.message}")
        }
        return null
    }

    private fun parseChapterDetailDto(jsonStr: String, mangaId: String, chapterNumber: Int): ChapterDetailDto? {
        val cleanJson = decodeGitHubContent(jsonStr).trim()
        try {
            val adapter = GitHubNetworkModule.moshi.adapter(ChapterDetailDto::class.java)
            val dto = adapter.fromJson(cleanJson)
            if (dto != null && (!dto.images.isNullOrEmpty() || !dto.pages.isNullOrEmpty())) {
                return dto
            }
        } catch (e: Exception) {
            Log.w(TAG, "Moshi chapter detail parsing error: ${e.message}")
        }

        try {
            val obj = org.json.JSONObject(cleanJson)
            val imagesList = mutableListOf<String>()
            val imgArr = obj.optJSONArray("images") ?: obj.optJSONArray("pages")
            if (imgArr != null) {
                for (i in 0 until imgArr.length()) {
                    val imgUrl = imgArr.optString(i)
                    if (imgUrl.isNotBlank()) imagesList.add(imgUrl)
                }
            }
            return ChapterDetailDto(
                series = obj.optString("series", mangaId),
                chapter = obj.optInt("chapter", chapterNumber),
                title = obj.optString("title", "الفصل $chapterNumber"),
                totalImages = obj.optInt("total_images", imagesList.size),
                images = imagesList,
                pages = imagesList
            )
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

    fun getAllManga(): List<MangaItem> = _allMangaFlow.value.ifEmpty { getDefaultMangaList() }

    fun getHeroFeaturedManga(): List<MangaItem> = getAllManga().take(5)

    fun getMangaById(id: String): MangaItem? {
        return getAllManga().find { it.id == id } ?: getDefaultMangaList().find { it.id == id }
    }

    /**
     * Refresh data from GitHub Data repository (zxiu86/Data):
     * 1. data/works.json
     * 2. data/[series-slug]/info.json (fetched in parallel)
     */
    suspend fun refreshMangaFromGitHub(): Result<List<MangaItem>> = withContext(Dispatchers.IO) {
        try {
            val owner = GitHubNetworkModule.getConfiguredOwner()
            val repo = GitHubNetworkModule.getDataRepo()
            val branch = GitHubNetworkModule.getConfiguredBranch()

            Log.d(TAG, "Fetching works from GitHub Data repo: $owner/$repo (branch: $branch)")

            // Candidate paths for works file
            val candidatePaths = listOf("data/works.json", "works.json", "data/works", "works")
            var worksJsonStr: String? = null

            for (path in candidatePaths) {
                try {
                    val worksResponse = GitHubNetworkModule.apiService.getContentRaw(owner, repo, path, branch)
                    if (worksResponse.isSuccessful && worksResponse.body() != null) {
                        worksJsonStr = worksResponse.body()!!.string()
                        Log.d(TAG, "Successfully retrieved works from API path: $path")
                        break
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Path $path attempt failed: ${e.message}")
                }
            }

            // Fallback: direct raw download from raw.githubusercontent.com
            if (worksJsonStr.isNullOrBlank()) {
                val directRawUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/data/works.json"
                val directResult = GitHubNetworkModule.fetchDirectRaw(directRawUrl)
                if (!directResult.isNullOrBlank()) {
                    worksJsonStr = directResult
                    Log.d(TAG, "Retrieved works via direct raw URL: $directRawUrl")
                }
            }

            if (worksJsonStr.isNullOrBlank()) {
                Log.w(TAG, "Failed to load works.json from $owner/$repo, using cached/default manga")
                val current = _allMangaFlow.value
                return@withContext if (current.isNotEmpty()) Result.success(current) else Result.success(getDefaultMangaList())
            }

            val decodedJson = decodeGitHubContent(worksJsonStr)
            saveWorksToDiskCache(decodedJson)

            val worksMap = parseWorksJson(decodedJson)

            if (worksMap.isEmpty()) {
                Log.w(TAG, "works.json parsed map was empty.")
                return@withContext Result.success(getDefaultMangaList())
            }

            // Fetch info.json for each work in parallel
            val fullMangaList = coroutineScope {
                worksMap.map { (key, workDto) ->
                    async {
                        val slug = workDto.slug ?: workDto.id ?: key
                        val info = fetchSeriesInfoSafely(owner, repo, slug, branch)
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
        branch: String
    ): SeriesInfoDto? = withContext(Dispatchers.IO) {
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
                    saveSeriesInfoToDiskCache(slug, decodedJson)
                    val info = parseSeriesInfoDto(decodedJson)
                    if (info != null) return@withContext info
                }
            } catch (e: Exception) {
                Log.d(TAG, "Series info path $path failed: ${e.message}")
            }
        }

        // Direct raw fallback
        try {
            val directRawUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/data/$slug/info.json"
            val directResult = GitHubNetworkModule.fetchDirectRaw(directRawUrl)
            if (!directResult.isNullOrBlank()) {
                val decodedJson = decodeGitHubContent(directResult)
                saveSeriesInfoToDiskCache(slug, decodedJson)
                val info = parseSeriesInfoDto(decodedJson)
                if (info != null) return@withContext info
            }
        } catch (e: Exception) {
            Log.d(TAG, "Direct raw series info failed: ${e.message}")
        }

        loadSeriesInfoFromDiskCache(slug)
    }

    private fun convertToMangaItem(
        slug: String,
        workDto: WorkDto,
        seriesInfo: SeriesInfoDto?
    ): MangaItem {
        val rawChapters = seriesInfo?.chapters ?: emptyList()
        val chaptersList = if (rawChapters.isNotEmpty()) {
            rawChapters.map { chSummary ->
                Chapter(
                    id = "${slug}_ch_${chSummary.number}",
                    mangaId = slug,
                    number = chSummary.number,
                    title = chSummary.title ?: "الفصل ${chSummary.number}",
                    releaseDate = chSummary.releaseDate ?: "اليوم",
                    isNew = chSummary.isNew ?: false,
                    pagesCount = 0,
                    pages = emptyList()
                )
            }.sortedBy { it.number }
        } else {
            // Generate placeholder chapters if info.json had no chapters list
            (1..30).map { num ->
                Chapter(
                    id = "${slug}_ch_$num",
                    mangaId = slug,
                    number = num,
                    title = "الفصل $num",
                    releaseDate = if (num > 27) "اليوم" else "منذ أسبوع",
                    isNew = num > 27,
                    pagesCount = 8,
                    pages = generateDefaultPagesForChapter(num)
                )
            }
        }

        val typeEnum = MangaType.fromString(workDto.type)
        val title = workDto.title ?: workDto.name ?: slug
        val cover = workDto.cover ?: workDto.thumbnail ?: workDto.image
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
     * Checks remote GitHub data/[series-slug]/[chapter].json first if online,
     * otherwise serves cached or generated pages.
     */
    suspend fun getChapterWithPages(mangaId: String, chapterNumber: Int): Chapter? =
        withContext(Dispatchers.IO) {
            val cacheKey = "${mangaId}_$chapterNumber"
            if (loadedChaptersCache.containsKey(cacheKey)) {
                return@withContext loadedChaptersCache[cacheKey]
            }

            val owner = GitHubNetworkModule.getConfiguredOwner()
            val repo = GitHubNetworkModule.getDataRepo()
            val branch = GitHubNetworkModule.getConfiguredBranch()

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
                        saveChapterDetailToDiskCache(mangaId, chapterNumber, decodedJson)
                        val detail = parseChapterDetailDto(decodedJson, mangaId, chapterNumber)

                        val imageList = if (!detail?.images.isNullOrEmpty()) {
                            detail!!.images!!
                        } else if (!detail?.pages.isNullOrEmpty()) {
                            detail!!.pages!!
                        } else {
                            emptyList()
                        }

                        if (imageList.isNotEmpty()) {
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
                                pagesCount = pages.size,
                                pages = pages
                            )
                            loadedChaptersCache[cacheKey] = chapter
                            return@withContext chapter
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Chapter path $path failed: ${e.message}")
                }
            }

            // Direct raw fallback
            try {
                val directRawUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/data/$mangaId/$chapterNumber.json"
                val directResult = GitHubNetworkModule.fetchDirectRaw(directRawUrl)
                if (!directResult.isNullOrBlank()) {
                    val decodedJson = decodeGitHubContent(directResult)
                    saveChapterDetailToDiskCache(mangaId, chapterNumber, decodedJson)
                    val detail = parseChapterDetailDto(decodedJson, mangaId, chapterNumber)
                    val imageList = detail?.images ?: detail?.pages ?: emptyList()
                    if (imageList.isNotEmpty()) {
                        val pages = imageList.mapIndexed { idx, url ->
                            ChapterPage(pageNumber = idx + 1, imageUrl = url, caption = "صفحة ${idx + 1}")
                        }
                        val chapter = Chapter(
                            id = "${mangaId}_ch_$chapterNumber",
                            mangaId = mangaId,
                            number = chapterNumber,
                            title = detail?.title ?: "الفصل $chapterNumber",
                            releaseDate = "اليوم",
                            pagesCount = pages.size,
                            pages = pages
                        )
                        loadedChaptersCache[cacheKey] = chapter
                        return@withContext chapter
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Direct raw chapter fetch failed: ${e.message}")
            }

            // Check if available in disk cache
            val cachedDetail = loadChapterDetailFromDiskCache(mangaId, chapterNumber)
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
                    pagesCount = pages.size,
                    pages = pages
                )
                loadedChaptersCache[cacheKey] = chapter
                return@withContext chapter
            }

            // Fallback to local chapter representation
            val manga = getMangaById(mangaId)
            val fallbackChapter = manga?.chapters?.find { it.number == chapterNumber }
            if (fallbackChapter != null) {
                val pages = if (fallbackChapter.pages.isNotEmpty()) {
                    fallbackChapter.pages
                } else {
                    generateDefaultPagesForChapter(chapterNumber)
                }
                val fullCh = fallbackChapter.copy(pages = pages, pagesCount = pages.size)
                loadedChaptersCache[cacheKey] = fullCh
                return@withContext fullCh
            }

            null
        }

    /**
     * Checks GitHub Releases for In-App Updates against current version (1.4)
     * Queries repository: zxiu86/Nexus
     */
    suspend fun checkForAppUpdate(): AppUpdateState = withContext(Dispatchers.IO) {
        val currentVersion = "1.4"
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

                    if (downloadUrl.isNotBlank()) {
                        return@withContext AppUpdateState(
                            isChecking = false,
                            updateAvailable = hasNewerVersion,
                            latestVersion = tag.ifEmpty { release.name ?: "1.4" },
                            currentVersion = currentVersion,
                            releaseNotes = release.body ?: "• الربط المباشر مع مستودع البيانات zxiu86/Data.\n• جلب الفصول والمانهوا ديناميكياً باستخدام التوكن السري.\n• فحص التحديثات وتنزيل الـ APK من مستودع zxiu86/Nexus.",
                            downloadUrl = downloadUrl
                        )
                    }
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
                        latestVersion = tag.ifEmpty { release.name ?: "1.4" },
                        currentVersion = currentVersion,
                        releaseNotes = release.body ?: "• الربط المباشر مع مستودع البيانات zxiu86/Data.\n• جلب الفصول والمانهوا ديناميكياً.\n• تحسين أداء القارئ واستقرار التطبيق.",
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
            releaseNotes = "• ربط تلقائي ديناميكي بمستودع البيانات السحابية zxiu86/Data.\n• استخدام التوكن السري لاستدعاء المانهوا والفصول والملفات مباشرة.\n• فحص التحديثات من مستودع zxiu86/Nexus."
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

    // ----------------------------------------------------
    // Built-in Sample Dataset (Ensures instant offline display)
    // ----------------------------------------------------
    companion object {
        private fun getDefaultMangaList(): List<MangaItem> {
            return listOf(
            createMangaItem(
                id = "demonic-emperor",
                titleAr = "سيد الشياطين العائد (Demonic Emperor)",
                titleEn = "Demonic Emperor",
                type = MangaType.MANHUA,
                coverRes = R.drawable.manhua_martial_emperor_1788030575736,
                bannerRes = R.drawable.manhua_martial_emperor_1788030575736,
                synopsis = "تشو ييفان، إمبراطور الشياطين الأسطوري، تعرض للخيانة من تلميذه وقتل بعد عثوره على كتاب التراث السري التساعي. يستيقظ في جسد خادم ضعيف لعائلة لوه المنهارة، ويبدأ معركته للسيطرة على العالم مجدداً.",
                author = "يه شياو (Ye Xiao)",
                artist = "وو وي (Wu Wei)",
                scanlationTeam = "فريق نكسوس للترجمة (Nexus Scans)",
                translator = "المعلم كايزن",
                cleaner = "دارك لورد",
                typesetter = "فانتوم إكس",
                rating = 4.96f,
                views = "2.8M",
                status = "مستمر",
                genres = listOf("أكشن", "خيال", "فنون قتال", "تناسخ", "ذكاء وتخطيط"),
                totalChapters = 90
            ),
            createMangaItem(
                id = "solo-shadow-monarch",
                titleAr = "سيد الظلال المنفرد",
                titleEn = "Solo Shadow Monarch",
                type = MangaType.MANHWA,
                coverRes = R.drawable.manhwa_shadow_monarch_1788030563820,
                bannerRes = R.drawable.manhwa_shadow_monarch_1788030563820,
                synopsis = "في عالم ظهرت فيه بوابات غامضة تربط عالمنا بأبعاد الوحوش، يظهر الصيادون ذوو القدرات الخارقة. سونغ جين وو أضعف صياد من الرتبة E يجد نفسه محاصراً في زنزانة مزدوجة مروعة ليحصل على نظام الترقية المنفرد.",
                author = "تشو غونغ (Chugong)",
                artist = "دو بو ري (DUBU - Redice)",
                scanlationTeam = "فريق نكسوس للترجمة (Nexus Scans)",
                translator = "كايزن العرب",
                cleaner = "أرثر دارك",
                typesetter = "فانتوم إكس",
                rating = 4.95f,
                views = "2.4M",
                status = "مستمر",
                genres = listOf("أكشن", "فانتازيا خيالية", "بوابات", "سحر", "نظام", "مغامرات"),
                totalChapters = 90
            ),
            createMangaItem(
                id = "archmage-returns-4000",
                titleAr = "عودة الساحر الأسطوري بعد 4000 سنة",
                titleEn = "The Great Mage Returns After 4000 Years",
                type = MangaType.MANHWA,
                coverRes = R.drawable.manhwa_archmage_1788030586830,
                bannerRes = R.drawable.manhwa_archmage_1788030586830,
                synopsis = "أعظم ساحر بشري في التاريخ، لوكاس تراومان، تم ختم روحه من قبل الحكام السماويين لمدة 4000 عام في ظلام مطبق. يستيقظ فجأة في جسد فراي بليك، الطالب الفاشل في أكاديمية ويست رود السحرية.",
                author = "بارناكل (Barnacle)",
                artist = "كيم دونغ وون",
                scanlationTeam = "فريق نكسوس للترجمة (Nexus Scans)",
                translator = "سفير السحر",
                cleaner = "ألكيميست",
                typesetter = "مانا بلاست",
                rating = 4.91f,
                views = "1.5M",
                status = "مستمر",
                genres = listOf("سحر وأساطير", "إعادة تجسد", "أكاديمية", "فانتازيا", "قوى عليا"),
                totalChapters = 75
            ),
            createMangaItem(
                id = "tower-of-gods",
                titleAr = "برج الإله والخوارق",
                titleEn = "Tower of Gods & Mysteries",
                type = MangaType.MANHWA,
                coverRes = R.drawable.manhwa_shadow_monarch_1788030563820,
                bannerRes = R.drawable.manhwa_shadow_monarch_1788030563820,
                synopsis = "ما الذي ترغب به؟ المال؟ المجد؟ القوة؟ كل ما تريده ينتظرك في قمة البرج. بام يدخل البرج بحثاً عن راشيل ليكتشف أسراراً تفوق خيال البشر وقوى الشينسو الأسطورية.",
                author = "إس آي يو (SIU)",
                artist = "فريق ناكستر للرسم",
                scanlationTeam = "فريق نكسوس للترجمة (Nexus Scans)",
                translator = "نوفا ستار",
                cleaner = "شينسو",
                typesetter = "كروكس",
                rating = 4.94f,
                views = "3.1M",
                status = "مستمر",
                genres = listOf("مغامرات أسطورية", "برج التحدي", "خوارق", "أكشن وغموض", "أسرار"),
                totalChapters = 80
            ),
            createMangaItem(
                id = "divine-dragon-monarch",
                titleAr = "ملك التنانين الإلهية",
                titleEn = "Divine Dragon Monarch",
                type = MangaType.MANHUA,
                coverRes = R.drawable.manhua_martial_emperor_1788030575736,
                bannerRes = R.drawable.manhua_martial_emperor_1788030575736,
                synopsis = "عالم تسوده سلالات الوحوش الأسطورية والتنانين الإلهية. الشاب لين تيان يوقظ خط دم تنين الفوضى البدائي بعد أن اعتبرته عشيرته عديم الفائدة.",
                author = "تانغ جيا سان شاو",
                artist = "أستوديو فنون الشرق",
                scanlationTeam = "فريق نكسوس للترجمة (Nexus Scans)",
                translator = "روح التنين",
                cleaner = "سكاي لورد",
                typesetter = "بلاك وينغ",
                rating = 4.87f,
                views = "1.2M",
                status = "مستمر",
                genres = listOf("مانها صينية", "تنانين وخوارق", "زراعة خالدة", "سحر قتالي"),
                totalChapters = 65
            ),
            createMangaItem(
                id = "player-returned-10000-years",
                titleAr = "اللاعب الذي عاد بعد 10,000 سنة",
                titleEn = "Player Who Returned After 10,000 Years",
                type = MangaType.MANHWA,
                coverRes = R.drawable.manhwa_archmage_1788030586830,
                bannerRes = R.drawable.manhwa_archmage_1788030586830,
                synopsis = "سقط في الجحيم وعاش هناك لعشرة آلاف عام ملتهمًا الشياطين والملوك حتى أصبح المفترس الأكبر. عندما يعود إلى الأرض أخيرًا، يجد العالم قد تحول إلى بوابات وزنزانات حديثة.",
                author = "نابان (Naban)",
                artist = "أستوديو بيتر",
                scanlationTeam = "فريق نكسوس للترجمة (Nexus Scans)",
                translator = "أوفر لورد",
                cleaner = "ريد فاير",
                typesetter = "إكستريم",
                rating = 4.89f,
                views = "1.6M",
                status = "مستمر",
                genres = listOf("كوميديا سوداء", "أكشن ناري", "شياطين", "عودة بالزمن", "نظام"),
                totalChapters = 60
            )
        )
    }

    private fun createMangaItem(
        id: String,
        titleAr: String,
        titleEn: String,
        type: MangaType,
        coverRes: Int,
        bannerRes: Int,
        synopsis: String,
        author: String,
        artist: String,
        scanlationTeam: String,
        translator: String,
        cleaner: String,
        typesetter: String,
        rating: Float,
        views: String,
        status: String,
        genres: List<String>,
        totalChapters: Int
    ): MangaItem {
        val generatedChapters = (1..totalChapters).map { num ->
            val isLatestThree = num > (totalChapters - 3)
            val releaseTime = when {
                num == totalChapters -> "اليوم"
                num == totalChapters - 1 -> "منذ 3 ساعات"
                num == totalChapters - 2 -> "منذ يوم"
                else -> "2026/08/25"
            }
            Chapter(
                id = "${id}_ch_$num",
                mangaId = id,
                number = num,
                title = "الفصل $num : الفصل $num",
                releaseDate = releaseTime,
                isNew = isLatestThree,
                pagesCount = 8,
                pages = generateDefaultPagesForChapter(num)
            )
        }

        return MangaItem(
            id = id,
            titleAr = titleAr,
            titleEn = titleEn,
            type = type,
            coverRes = coverRes,
            bannerRes = bannerRes,
            synopsis = synopsis,
            author = author,
            artist = artist,
            scanlationTeam = scanlationTeam,
            translator = translator,
            cleaner = cleaner,
            typesetter = typesetter,
            rating = rating,
            views = views,
            status = status,
            genres = genres,
            totalChaptersCount = totalChapters,
            chapters = generatedChapters
        )
    }

    private fun generateDefaultPagesForChapter(chapterNum: Int): List<ChapterPage> {
        val availableImages = listOf(
            R.drawable.comic_panel_action_1788030599626,
            R.drawable.manhwa_shadow_monarch_1788030563820,
            R.drawable.manhua_martial_emperor_1788030575736,
            R.drawable.manhwa_archmage_1788030586830
        )
        return (1..6).map { pageIdx ->
            val img = availableImages[(chapterNum + pageIdx) % availableImages.size]
            ChapterPage(
                pageNumber = pageIdx,
                imageRes = img,
                caption = "صفحة $pageIdx"
            )
        }
    }
    }
}
