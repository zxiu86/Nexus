package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONObject

/**
 * Bounding box with normalized coordinates (0.0 to 1.0)
 * Allows flawless scaling across any display resolution and screen density.
 */
@JsonClass(generateAdapter = true)
data class NormalizedBoundingBox(
    @Json(name = "x") val x: Float,
    @Json(name = "y") val y: Float,
    @Json(name = "width") val width: Float,
    @Json(name = "height") val height: Float
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("x", Math.round(x * 10000.0) / 10000.0)
            put("y", Math.round(y * 10000.0) / 10000.0)
            put("width", Math.round(width * 10000.0) / 10000.0)
            put("height", Math.round(height * 10000.0) / 10000.0)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): NormalizedBoundingBox {
            return NormalizedBoundingBox(
                x = obj.optDouble("x", 0.0).toFloat(),
                y = obj.optDouble("y", 0.0).toFloat(),
                width = obj.optDouble("width", 0.0).toFloat(),
                height = obj.optDouble("height", 0.0).toFloat()
            )
        }
    }
}

/**
 * Coordinate data for a single page in a chapter
 */
@JsonClass(generateAdapter = true)
data class PageWatermarkData(
    @Json(name = "page_number") val pageNumber: Int,
    @Json(name = "boxes") val boxes: List<NormalizedBoundingBox> = emptyList(),
    @Json(name = "original_text") val originalText: String = "olympustaff.com",
    @Json(name = "replacement_text") val replacementText: String = "تطبيق Nexus"
) {
    fun toJsonObject(): JSONObject {
        val arr = JSONArray()
        for (b in boxes) {
            arr.put(b.toJsonObject())
        }
        return JSONObject().apply {
            put("page_number", pageNumber)
            put("original_text", originalText)
            put("replacement_text", replacementText)
            put("boxes", arr)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): PageWatermarkData {
            val pNum = obj.optInt("page_number", obj.optInt("page", 1))
            val orig = obj.optString("original_text", obj.optString("text", "olympustaff.com"))
            val repl = obj.optString("replacement_text", "تطبيق Nexus")
            val boxList = mutableListOf<NormalizedBoundingBox>()
            val boxArr = obj.optJSONArray("boxes") ?: obj.optJSONArray("rects")
            if (boxArr != null) {
                for (i in 0 until boxArr.length()) {
                    val bObj = boxArr.optJSONObject(i) ?: continue
                    boxList.add(NormalizedBoundingBox.fromJsonObject(bObj))
                }
            }
            return PageWatermarkData(
                pageNumber = pNum,
                boxes = boxList,
                originalText = orig,
                replacementText = repl
            )
        }
    }
}

/**
 * Root coordinate file DTO mirroring tree structure: coordinates/{seriesSlug}/{chapter}.json
 */
@JsonClass(generateAdapter = true)
data class ChapterCoordinatesDto(
    @Json(name = "series_slug") val seriesSlug: String,
    @Json(name = "chapter_number") val chapterNumber: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "pages") val pages: List<PageWatermarkData> = emptyList(),
    @Json(name = "version") val version: Int = 1,
    @Json(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    fun getPageData(pageNumber: Int): PageWatermarkData? {
        return pages.find { it.pageNumber == pageNumber }
    }

    fun hasWatermarks(): Boolean {
        return pages.any { it.boxes.isNotEmpty() }
    }

    fun toJsonString(indent: Int = 2): String {
        val root = JSONObject().apply {
            put("series_slug", seriesSlug)
            put("chapter_number", chapterNumber)
            put("total_pages", totalPages)
            put("version", version)
            put("updated_at", updatedAt)
            val pagesArr = JSONArray()
            for (p in pages) {
                pagesArr.put(p.toJsonObject())
            }
            put("pages", pagesArr)
        }
        return root.toString(indent)
    }

    companion object {
        fun fromJsonString(jsonStr: String): ChapterCoordinatesDto? {
            return try {
                val clean = jsonStr.trim()
                if (clean.isBlank() || clean.startsWith("<!DOCTYPE") || clean.contains("404: Not Found")) {
                    return null
                }
                val obj = JSONObject(clean)
                val slug = obj.optString("series_slug", obj.optString("series", ""))
                val chNum = obj.optInt("chapter_number", obj.optInt("chapter", 1))
                val totalPages = obj.optInt("total_pages", obj.optInt("pages_count", 0))
                val ver = obj.optInt("version", 1)
                val updated = obj.optLong("updated_at", System.currentTimeMillis())
                val pagesList = mutableListOf<PageWatermarkData>()
                val pArr = obj.optJSONArray("pages")
                if (pArr != null) {
                    for (i in 0 until pArr.length()) {
                        val pObj = pArr.optJSONObject(i) ?: continue
                        pagesList.add(PageWatermarkData.fromJsonObject(pObj))
                    }
                }
                ChapterCoordinatesDto(
                    seriesSlug = slug,
                    chapterNumber = chNum,
                    totalPages = totalPages,
                    pages = pagesList,
                    version = ver,
                    updatedAt = updated
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
