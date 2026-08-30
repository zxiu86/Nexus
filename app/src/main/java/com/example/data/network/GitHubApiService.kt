package com.example.data.network

import com.example.data.model.ChapterDetailDto
import com.example.data.model.GitHubReleaseDto
import com.example.data.model.SeriesInfoDto
import com.example.data.model.WorkDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiService {

    /**
     * Fetches data/works.json as raw Map<String, WorkDto> from repository
     */
    @GET("repos/{owner}/{repo}/contents/data/works.json")
    @Headers("Accept: application/vnd.github.v3.raw")
    suspend fun getWorksRaw(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("ref") branch: String = "main"
    ): Response<ResponseBody>

    /**
     * Fetches data/{seriesSlug}/info.json as raw SeriesInfoDto
     */
    @GET("repos/{owner}/{repo}/contents/data/{seriesSlug}/info.json")
    @Headers("Accept: application/vnd.github.v3.raw")
    suspend fun getSeriesInfoRaw(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("seriesSlug") seriesSlug: String,
        @Query("ref") branch: String = "main"
    ): Response<ResponseBody>

    /**
     * Fetches data/{seriesSlug}/{chapter}.json as raw ChapterDetailDto
     */
    @GET("repos/{owner}/{repo}/contents/data/{seriesSlug}/{chapter}.json")
    @Headers("Accept: application/vnd.github.v3.raw")
    suspend fun getChapterDetailRaw(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("seriesSlug") seriesSlug: String,
        @Path("chapter") chapterNumber: Int,
        @Query("ref") branch: String = "main"
    ): Response<ResponseBody>

    /**
     * Generic content fetcher for any path in repo with raw and json headers
     */
    @GET("repos/{owner}/{repo}/contents/{path}")
    @Headers("Accept: application/vnd.github.v3.raw, application/vnd.github.raw+json, application/json")
    suspend fun getContentRaw(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Query("ref") branch: String = "main"
    ): Response<ResponseBody>

    /**
     * Gets the latest release info for in-app update checks
     */
    @GET("repos/{owner}/{repo}/releases/latest")
    @Headers("Accept: application/vnd.github.v3+json")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GitHubReleaseDto>
}
