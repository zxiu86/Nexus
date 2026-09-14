package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Base64
import android.util.Log
import com.example.data.model.Chapter
import com.example.data.model.ChapterCoordinatesDto
import com.example.data.model.MangaItem
import com.example.data.model.NormalizedBoundingBox
import com.example.data.model.PageWatermarkData
import com.example.data.network.GitHubNetworkModule
import com.example.data.repository.CoordinatesRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

object WatermarkCleanerBot {

    private const val TAG = "WatermarkCleanerBot"
    private val botScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isRunning = AtomicBoolean(false)
    private val processingMutex = Mutex()

    private val TARGET_KEYWORDS = listOf(
        "olympustaff.com",
        "olympustaff",
        "olympus",
        "olympus-scans",
        "olympusscan",
        "olympus staff",
        "olympustaf",
        "olympus.com"
    )

    /**
     * Starts the automated background watermark cleaning bot.
     * Scans manga works and chapters, detects watermark coordinates,
     * saves them locally, and pushes coordinates/{seriesSlug}/{chapter}.json to GitHub.
     */
    fun startBackgroundBot(context: Context, works: List<MangaItem>) {
        if (works.isEmpty()) return
        if (!isRunning.compareAndSet(false, true)) {
            Log.d(TAG, "Watermark cleaner bot is already running.")
            return
        }

        botScope.launch {
            try {
                Log.d(TAG, "Starting automated Watermark Cleaner Bot for ${works.size} series...")
                val coordsRepo = CoordinatesRepository.getInstance(context)

                for (work in works) {
                    val slug = work.id
                    if (slug.isBlank()) continue

                    val chapters = work.chapters.sortedByDescending { it.number }
                    for (chapter in chapters) {
                        try {
                            processChapterIfNeeded(context, coordsRepo, slug, chapter)
                            // Gentle pause between chapters to keep CPU/Battery usage minimal
                            delay(500L)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error in bot cleaning chapter ${chapter.number} of $slug: ${e.message}")
                        }
                    }
                }
                Log.d(TAG, "Watermark Cleaner Bot finished full scan cycle.")
            } catch (e: Exception) {
                Log.w(TAG, "Bot background execution error: ${e.message}")
            } finally {
                isRunning.set(false)
            }
        }
    }

    /**
     * Cleans and detects coordinates for a single chapter on-demand.
     */
    suspend fun processSingleChapterOnDemand(
        context: Context,
        seriesSlug: String,
        chapter: Chapter
    ): ChapterCoordinatesDto? = withContext(Dispatchers.IO) {
        val coordsRepo = CoordinatesRepository.getInstance(context)
        val existing = coordsRepo.getCoordinates(seriesSlug, chapter.number)
        if (existing != null) return@withContext existing

        processChapter(context, coordsRepo, seriesSlug, chapter)
    }

    private suspend fun processChapterIfNeeded(
        context: Context,
        coordsRepo: CoordinatesRepository,
        seriesSlug: String,
        chapter: Chapter
    ) {
        // Check if coordinates already exist locally or on GitHub
        val existing = coordsRepo.getCoordinates(seriesSlug, chapter.number)
        if (existing != null) {
            return
        }

        processChapter(context, coordsRepo, seriesSlug, chapter)
    }

    private suspend fun processChapter(
        context: Context,
        coordsRepo: CoordinatesRepository,
        seriesSlug: String,
        chapter: Chapter
    ): ChapterCoordinatesDto? = processingMutex.withLock {
        val pages = chapter.pages
        if (pages.isEmpty()) return null

        val pageResults = mutableListOf<PageWatermarkData>()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            for ((index, page) in pages.withIndex()) {
                val pNum = page.pageNumber.takeIf { it > 0 } ?: (index + 1)
                val imageUrl = page.imageUrl
                if (imageUrl.isNullOrBlank()) continue

                val boxes = analyzeImageForWatermarks(imageUrl, recognizer)
                if (boxes.isNotEmpty()) {
                    pageResults.add(
                        PageWatermarkData(
                            pageNumber = pNum,
                            boxes = boxes,
                            originalText = "olympustaff.com",
                            replacementText = "تطبيق Nexus"
                        )
                    )
                }
                // Yield to prevent any UI stutter
                delay(80L)
            }
        } finally {
            try {
                recognizer.close()
            } catch (e: Exception) {
                // Ignore recognizer close error
            }
        }

        val dto = ChapterCoordinatesDto(
            seriesSlug = seriesSlug,
            chapterNumber = chapter.number,
            totalPages = pages.size,
            pages = pageResults,
            version = 1,
            updatedAt = System.currentTimeMillis()
        )

        // Save locally to cache immediately
        coordsRepo.saveToCache(dto)

        // Upload to GitHub repository under coordinates/{seriesSlug}/{chapter}.json
        if (dto.hasWatermarks()) {
            uploadCoordinatesToGitHub(seriesSlug, chapter.number, dto)
        }

        return dto
    }

    /**
     * Downloads image stream with safe downsampling and runs ML Kit Text Recognition.
     * Guaranteed zero memory leaks with strict Bitmap recycling.
     */
    private suspend fun analyzeImageForWatermarks(
        imageUrl: String,
        recognizer: com.google.mlkit.vision.text.TextRecognizer
    ): List<NormalizedBoundingBox> = withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        try {
            bitmap = downloadScaledBitmap(imageUrl, maxDimension = 1200)
            if (bitmap == null) return@withContext emptyList()

            val imgWidth = bitmap.width.toFloat()
            val imgHeight = bitmap.height.toFloat()
            if (imgWidth <= 0 || imgHeight <= 0) return@withContext emptyList()

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = recognizer.process(inputImage).await()

            val matchedBoxes = mutableListOf<NormalizedBoundingBox>()

            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    val lineText = line.text.lowercase().replace(" ", "").replace("-", "").replace("_", "")
                    val isMatch = TARGET_KEYWORDS.any { kw ->
                        val cleanKw = kw.replace(" ", "").replace("-", "").replace("_", "")
                        lineText.contains(cleanKw) || cleanKw.contains(lineText) && lineText.length >= 7
                    }

                    if (isMatch) {
                        val rect = line.boundingBox ?: block.boundingBox
                        if (rect != null) {
                            val normBox = normalizeAndPadRect(rect, imgWidth, imgHeight)
                            matchedBoxes.add(normBox)
                        }
                    }
                }
            }

            matchedBoxes
        } catch (e: Exception) {
            Log.w(TAG, "Watermark detection failed for $imageUrl: ${e.message}")
            emptyList()
        } finally {
            bitmap?.recycle()
        }
    }

    /**
     * Expands the bounding box slightly (padding) to ensure complete coverage of the watermark
     * and normalizes to 0.0 - 1.0.
     */
    private fun normalizeAndPadRect(rect: Rect, imgWidth: Float, imgHeight: Float): NormalizedBoundingBox {
        val padX = (rect.width() * 0.12f).coerceAtLeast(10f)
        val padY = (rect.height() * 0.20f).coerceAtLeast(8f)

        val left = (rect.left - padX).coerceAtLeast(0f)
        val top = (rect.top - padY).coerceAtLeast(0f)
        val right = (rect.right + padX).coerceAtMost(imgWidth)
        val bottom = (rect.bottom + padY).coerceAtMost(imgHeight)

        val normX = left / imgWidth
        val normY = top / imgHeight
        val normW = (right - left) / imgWidth
        val normH = (bottom - top) / imgHeight

        return NormalizedBoundingBox(
            x = normX.coerceIn(0f, 1f),
            y = normY.coerceIn(0f, 1f),
            width = normW.coerceIn(0.01f, 1f),
            height = normH.coerceIn(0.01f, 1f)
        )
    }

    /**
     * Efficiently downloads and decodes an image to a safe memory footprint.
     */
    private fun downloadScaledBitmap(imageUrl: String, maxDimension: Int): Bitmap? {
        return try {
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", "Nexus-Manga-App-Android/1.9.7")
                .build()

            val response = GitHubNetworkModule.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) return null

            val bytes = response.body!!.bytes()
            if (bytes.isEmpty()) return null

            // First decode bounds only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            val origWidth = options.outWidth
            val origHeight = options.outHeight
            if (origWidth <= 0 || origHeight <= 0) return null

            var sampleSize = 1
            while ((origWidth / sampleSize) > maxDimension || (origHeight / sampleSize) > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
                inMutable = false
            }

            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        } catch (e: Exception) {
            Log.w(TAG, "Bitmap download decode error: ${e.message}")
            null
        }
    }

    /**
     * Pushes the computed coordinates file to GitHub under coordinates/{seriesSlug}/{chapter}.json
     */
    private suspend fun uploadCoordinatesToGitHub(
        seriesSlug: String,
        chapterNumber: Int,
        dto: ChapterCoordinatesDto
    ) = withContext(Dispatchers.IO) {
        val token = GitHubNetworkModule.getActiveToken()
        if (token.isEmpty()) {
            Log.d(TAG, "No GitHub token configured. Coordinates saved locally.")
            return@withContext
        }

        try {
            val owner = GitHubNetworkModule.getConfiguredOwner()
            val repo = GitHubNetworkModule.getDataRepo()
            val branch = GitHubNetworkModule.getConfiguredBranch()
            val filePath = "coordinates/$seriesSlug/$chapterNumber.json"

            val jsonContent = dto.toJsonString(2)
            val base64Content = Base64.encodeToString(jsonContent.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

            // Step 1: Check if file already exists to get SHA
            var sha: String? = null
            val metaResp = GitHubNetworkModule.apiService.getFileMetadata(owner, repo, filePath, branch)
            if (metaResp.isSuccessful && metaResp.body() != null) {
                val metaStr = metaResp.body()!!.string()
                val metaObj = JSONObject(metaStr)
                sha = metaObj.optString("sha", null)
            }

            // Step 2: Build Commit Request
            val commitBody = JSONObject().apply {
                put("message", "Add cleaned watermark coordinates for $seriesSlug chapter $chapterNumber [Nexus Bot]")
                put("content", base64Content)
                put("branch", branch)
                if (!sha.isNullOrBlank()) {
                    put("sha", sha)
                }
            }

            val requestBody = commitBody.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val updateResp = GitHubNetworkModule.apiService.updateFileContent(owner, repo, filePath, requestBody)
            if (updateResp.isSuccessful) {
                Log.d(TAG, "✅ Successfully committed coordinates to GitHub: $filePath")
            } else {
                Log.w(TAG, "GitHub upload coordinates returned code: ${updateResp.code()}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed uploading coordinates to GitHub for $seriesSlug ch $chapterNumber: ${e.message}")
        }
    }
}
