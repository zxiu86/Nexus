package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.ChapterCoordinatesDto
import com.example.data.network.GitHubNetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class CoordinatesRepository(private val context: Context) {

    private val TAG = "CoordinatesRepo"
    private val memoryCache = ConcurrentHashMap<String, ChapterCoordinatesDto>()
    private val diskCacheDir = File(context.cacheDir, "nexus_coords_cache").apply {
        if (!exists()) mkdirs()
    }

    /**
     * Gets coordinates for a specific chapter.
     * 1. Memory cache
     * 2. Local disk cache
     * 3. GitHub repository (coordinates/{seriesSlug}/{chapter}.json)
     */
    suspend fun getCoordinates(seriesSlug: String, chapterNumber: Int): ChapterCoordinatesDto? = withContext(Dispatchers.IO) {
        val cacheKey = "${seriesSlug}_$chapterNumber"
        memoryCache[cacheKey]?.let { return@withContext it }

        // Step 1: Disk Cache
        val diskFile = File(diskCacheDir, "coords_${seriesSlug}_${chapterNumber}.json")
        if (diskFile.exists() && diskFile.length() > 0) {
            try {
                val json = diskFile.readText()
                val parsed = ChapterCoordinatesDto.fromJsonString(json)
                if (parsed != null) {
                    memoryCache[cacheKey] = parsed
                    return@withContext parsed
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading disk cache coords: ${e.message}")
            }
        }

        // Step 2: Fetch from GitHub repository (coordinates/{seriesSlug}/{chapter}.json)
        val remoteCoords = fetchFromGitHub(seriesSlug, chapterNumber)
        if (remoteCoords != null) {
            saveToCache(remoteCoords)
            return@withContext remoteCoords
        }

        null
    }

    private fun fetchFromGitHub(seriesSlug: String, chapterNumber: Int): ChapterCoordinatesDto? {
        val owner = GitHubNetworkModule.getConfiguredOwner()
        val repo = GitHubNetworkModule.getDataRepo()
        val branch = GitHubNetworkModule.getConfiguredBranch()

        // Mirror 1: Direct GitHub raw
        val rawUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/coordinates/$seriesSlug/$chapterNumber.json"
        val rawContent = GitHubNetworkModule.fetchDirectRaw(rawUrl, forceFresh = false)
        if (!rawContent.isNullOrBlank()) {
            val dto = ChapterCoordinatesDto.fromJsonString(rawContent)
            if (dto != null) return dto
        }

        // Mirror 2: GitHub API Content
        return try {
            val response = kotlinx.coroutines.runBlocking {
                GitHubNetworkModule.apiService.getContentRaw(
                    owner = owner,
                    repo = repo,
                    path = "coordinates/$seriesSlug/$chapterNumber.json",
                    branch = branch
                )
            }
            if (response.isSuccessful && response.body() != null) {
                val bodyStr = response.body()!!.string()
                ChapterCoordinatesDto.fromJsonString(bodyStr)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveToCache(dto: ChapterCoordinatesDto) {
        val cacheKey = "${dto.seriesSlug}_${dto.chapterNumber}"
        memoryCache[cacheKey] = dto
        try {
            val diskFile = File(diskCacheDir, "coords_${dto.seriesSlug}_${dto.chapterNumber}.json")
            diskFile.writeText(dto.toJsonString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving coordinates to disk cache: ${e.message}")
        }
    }

    fun clearCache() {
        memoryCache.clear()
        try {
            diskCacheDir.deleteRecursively()
            diskCacheDir.mkdirs()
        } catch (e: Exception) {
            Log.w(TAG, "Failed clearing coords cache: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var instance: CoordinatesRepository? = null

        fun getInstance(context: Context): CoordinatesRepository {
            return instance ?: synchronized(this) {
                instance ?: CoordinatesRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
