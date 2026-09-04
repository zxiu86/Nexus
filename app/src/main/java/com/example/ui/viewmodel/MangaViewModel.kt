package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppUpdateState
import com.example.data.model.Chapter
import com.example.data.model.ChapterDownloadProgress
import com.example.data.model.DownloadedChapter
import com.example.data.model.MangaItem
import com.example.data.model.ReadingHistoryEntry
import com.example.data.repository.MangaRepository
import com.example.util.InAppUpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.random.Random

data class HomeUiState(
    val selectedTab: Int = 0, // 0: Home, 1: Favorites, 2: History, 3: Downloads, 4: Updates
    val heroMangaList: List<MangaItem> = emptyList(),
    val latestMangaGrid: List<MangaItem> = emptyList(),
    val allMangaList: List<MangaItem> = emptyList(),
    val randomDiscoveryList: List<MangaItem> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val readLater: Set<String> = emptySet(),
    val lastReadMap: Map<String, Int> = emptyMap(),
    val downloadedChapters: List<DownloadedChapter> = emptyList(),
    val downloadProgressMap: Map<String, ChapterDownloadProgress> = emptyMap(),
    val readingHistory: List<ReadingHistoryEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "الكل",
    val currentPage: Int = 1,
    val itemsPerPage: Int = 14,
    val favoriteSubTab: Int = 0, // 0: Favorites (المفضلة), 1: Read Later (المشاهدة لاحقاً)
    val isRefreshing: Boolean = false,
    val showUpdateDialog: Boolean = false,
    val updateInfo: AppUpdateState = AppUpdateState(),
    val isAppReady: Boolean = false
) {
    val totalPages: Int
        get() = if (latestMangaGrid.isEmpty()) 1 else (latestMangaGrid.size + itemsPerPage - 1) / itemsPerPage

    val paginatedMangaList: List<MangaItem>
        get() {
            val validPage = currentPage.coerceIn(1, totalPages.coerceAtLeast(1))
            val start = (validPage - 1) * itemsPerPage
            if (start >= latestMangaGrid.size) return emptyList()
            val end = (start + itemsPerPage).coerceAtMost(latestMangaGrid.size)
            return latestMangaGrid.subList(start, end)
        }

    val favoriteMangaList: List<MangaItem>
        get() = allMangaList.filter { favorites.contains(it.id) }

    val readLaterMangaList: List<MangaItem>
        get() = allMangaList.filter { readLater.contains(it.id) }

    val formattedTotalStorage: String
        get() {
            val totalBytes = downloadedChapters.sumOf { it.sizeBytes }
            if (totalBytes <= 0) return "0 MB"
            val mb = totalBytes.toDouble() / (1024 * 1024)
            return if (mb >= 1000) {
                val gb = mb / 1024
                "${DecimalFormat("#.##").format(gb)} GB"
            } else {
                "${DecimalFormat("#.#").format(mb)} MB"
            }
        }
}

data class DetailsUiState(
    val manga: MangaItem? = null,
    val isFavorite: Boolean = false,
    val isReadLater: Boolean = false,
    val lastReadChapterNumber: Int = 1,
    val readChapterNumbers: Set<Int> = emptySet(),
    val userRating: Int = 0,
    val currentBatchIndex: Int = 0, // 0 for chapters 1-30, 1 for 31-60, etc.
    val batchSize: Int = 30,
    val downloadedChapterNumbers: Set<Int> = emptySet(),
    val downloadProgressMap: Map<String, ChapterDownloadProgress> = emptyMap(),
    val isBatchDownloading: Boolean = false
) {
    val totalBatches: Int
        get() {
            val total = manga?.chapters?.size ?: 0
            if (total == 0) return 1
            return (total + batchSize - 1) / batchSize
        }

    val currentBatchChapters: List<Chapter>
        get() {
            val list = manga?.chapters ?: return emptyList()
            val start = currentBatchIndex * batchSize
            val end = (start + batchSize).coerceAtMost(list.size)
            if (start >= list.size) return emptyList()
            return list.subList(start, end)
        }

    val currentBatchRangeText: String
        get() {
            val list = manga?.chapters ?: return ""
            val start = currentBatchIndex * batchSize + 1
            val end = ((currentBatchIndex + 1) * batchSize).coerceAtMost(list.size)
            return "الفصول ($start - $end)"
        }
}

data class ReaderUiState(
    val manga: MangaItem? = null,
    val currentChapter: Chapter? = null,
    val isLoadingPages: Boolean = false,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val isQuickJumpSheetOpen: Boolean = false,
    val currentPageIndex: Int = 0,
    val initialScrollPage: Int = 1,
    val readingProgressText: String = "",
    val hasPreviousChapter: Boolean = false,
    val hasNextChapter: Boolean = false
)

class MangaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MangaRepository(application.applicationContext)

    private val _selectedTab = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("الكل")
    private val _currentPage = MutableStateFlow(1)
    private val _favoriteSubTab = MutableStateFlow(0)
    private val _randomSeed = MutableStateFlow(0L)
    private val _isAppReady = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _showUpdateDialog = MutableStateFlow(false)
    private val _appUpdateState = MutableStateFlow(AppUpdateState())

    private data class DialogState(
        val isRefreshing: Boolean,
        val showUpdate: Boolean,
        val updateInfo: AppUpdateState,
        val isAppReady: Boolean
    )

    private val _dialogStateFlow = combine(
        _isRefreshing,
        _showUpdateDialog,
        _appUpdateState,
        _isAppReady
    ) { isRefreshing, showUpdate, updateInfo, isAppReady ->
        DialogState(isRefreshing, showUpdate, updateInfo, isAppReady)
    }

    private data class FilterState(
        val selectedTab: Int,
        val query: String,
        val category: String,
        val page: Int,
        val favSubTab: Int,
        val randomSeed: Long
    )

    private val _tabSearchCategory = combine(_selectedTab, _searchQuery, _selectedCategory) { tab, query, category ->
        Triple(tab, query, category)
    }
    private val _pageFavRand = combine(_currentPage, _favoriteSubTab, _randomSeed) { page, favSubTab, randSeed ->
        Triple(page, favSubTab, randSeed)
    }

    private val _filterStateFlow = combine(_tabSearchCategory, _pageFavRand) { tsc, pfr ->
        FilterState(
            selectedTab = tsc.first,
            query = tsc.second,
            category = tsc.third,
            page = pfr.first,
            favSubTab = pfr.second,
            randomSeed = pfr.third
        )
    }

    private data class OfflineDataState(
        val downloaded: List<DownloadedChapter>,
        val progressMap: Map<String, ChapterDownloadProgress>,
        val history: List<ReadingHistoryEntry>,
        val readLater: Set<String>,
        val lastReadMap: Map<String, Int>
    )

    private val _offlineDataFlow = combine(
        repository.downloadedChaptersFlow,
        repository.downloadProgressFlow,
        repository.readingHistoryFlow,
        repository.readLaterFlow,
        repository.lastReadFlow
    ) { downloaded, progress, history, readLater, lastReadMap ->
        OfflineDataState(downloaded, progress, history, readLater, lastReadMap)
    }

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.allMangaFlow,
        repository.favoritesFlow,
        _offlineDataFlow,
        _filterStateFlow,
        _dialogStateFlow
    ) { allMangaList, favorites, offlineData, filterState, dialogState ->
        var filteredList = allMangaList
        if (filterState.query.isNotBlank()) {
            filteredList = filteredList.filter {
                it.titleAr.contains(filterState.query, ignoreCase = true) ||
                it.titleEn.contains(filterState.query, ignoreCase = true)
            }
        }
        if (filterState.category != "الكل") {
            val cat = filterState.category
            filteredList = filteredList.filter {
                it.genres.contains(cat) || it.type.labelAr.contains(cat)
            }
        }

        // Random Discovery Selection (changes every 30 minutes or on user refresh)
        val timeBlockSeed = (System.currentTimeMillis() / (30 * 60 * 1000L)) + filterState.randomSeed
        val discoveryItems = if (allMangaList.isNotEmpty()) {
            allMangaList.shuffled(Random(timeBlockSeed)).take(8)
        } else {
            emptyList()
        }

        HomeUiState(
            selectedTab = filterState.selectedTab,
            heroMangaList = allMangaList.take(5),
            latestMangaGrid = filteredList,
            allMangaList = allMangaList,
            randomDiscoveryList = discoveryItems,
            favorites = favorites,
            readLater = offlineData.readLater,
            lastReadMap = offlineData.lastReadMap,
            downloadedChapters = offlineData.downloaded,
            downloadProgressMap = offlineData.progressMap,
            readingHistory = offlineData.history,
            searchQuery = filterState.query,
            selectedCategory = filterState.category,
            currentPage = filterState.page,
            itemsPerPage = 14,
            favoriteSubTab = filterState.favSubTab,
            isRefreshing = dialogState.isRefreshing,
            showUpdateDialog = dialogState.showUpdate,
            updateInfo = dialogState.updateInfo,
            isAppReady = dialogState.isAppReady
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState(
            heroMangaList = repository.getHeroFeaturedManga(),
            latestMangaGrid = repository.getAllManga(),
            allMangaList = repository.getAllManga()
        )
    )

    private val _detailsUiState = MutableStateFlow(DetailsUiState())
    val detailsUiState: StateFlow<DetailsUiState> = _detailsUiState.asStateFlow()

    private val _readerUiState = MutableStateFlow(ReaderUiState())
    val readerUiState: StateFlow<ReaderUiState> = _readerUiState.asStateFlow()

    init {
        // App Preload warmup for smooth entry without stutter
        viewModelScope.launch {
            delay(1200L)
            _isAppReady.value = true
            // Check for updates as soon as app is ready to notify user immediately
            checkForUpdates()
        }

        // Automatically start silent background auto-sync and periodic update checker
        startSilentAutoSyncLoop()
        checkForUpdates()

        // Reactively observe repo changes to keep active details screen updated silently
        viewModelScope.launch {
            combine(
                repository.allMangaFlow,
                repository.downloadedChaptersFlow,
                repository.downloadProgressFlow,
                repository.readLaterFlow,
                repository.readChaptersFlow,
                repository.userRatingsFlow
            ) { args: Array<Any?> ->
                @Suppress("UNCHECKED_CAST")
                val mangaList = args[0] as List<MangaItem>
                @Suppress("UNCHECKED_CAST")
                val downloaded = args[1] as List<DownloadedChapter>
                @Suppress("UNCHECKED_CAST")
                val progressMap = args[2] as Map<String, ChapterDownloadProgress>
                @Suppress("UNCHECKED_CAST")
                val readLater = args[3] as Set<String>
                @Suppress("UNCHECKED_CAST")
                val readChapters = args[4] as Map<String, Set<Int>>
                @Suppress("UNCHECKED_CAST")
                val userRatings = args[5] as Map<String, Int>

                val currentDetailsManga = _detailsUiState.value.manga
                if (currentDetailsManga != null) {
                    val updated = mangaList.find { it.id == currentDetailsManga.id } ?: currentDetailsManga
                    val downloadedNums = downloaded.filter { it.mangaId == currentDetailsManga.id }.map { it.chapterNumber }.toSet()
                    val reads = readChapters[currentDetailsManga.id] ?: repository.getReadChapters(currentDetailsManga.id)
                    val rating = userRatings[currentDetailsManga.id] ?: repository.getUserRating(currentDetailsManga.id)
                    _detailsUiState.value = _detailsUiState.value.copy(
                        manga = updated,
                        isFavorite = repository.isFavorite(currentDetailsManga.id),
                        isReadLater = readLater.contains(currentDetailsManga.id),
                        readChapterNumbers = reads,
                        userRating = rating,
                        downloadedChapterNumbers = downloadedNums,
                        downloadProgressMap = progressMap
                    )
                }
            }.collect {}
        }
    }

    private fun startSilentAutoSyncLoop() {
        viewModelScope.launch {
            repository.refreshMangaFromGitHub(forceFresh = true)
            while (isActive) {
                delay(30_000L)
                try {
                    repository.refreshMangaFromGitHub(forceFresh = true)
                    checkForUpdates()
                } catch (e: Exception) {
                    // Ignore background polling glitches
                }
            }
        }
    }

    fun markAppReady() {
        _isAppReady.value = true
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setHomePage(page: Int) {
        val total = homeUiState.value.totalPages
        _currentPage.value = page.coerceIn(1, total.coerceAtLeast(1))
    }

    fun setFavoriteSubTab(tab: Int) {
        _favoriteSubTab.value = tab
    }

    fun refreshRandomDiscovery() {
        _randomSeed.value = System.currentTimeMillis()
    }

    fun refreshDataFromGitHub(showIndicator: Boolean = false) {
        viewModelScope.launch {
            if (showIndicator) _isRefreshing.value = true
            repository.refreshMangaFromGitHub(forceFresh = true)
            checkForUpdates()
            if (showIndicator) _isRefreshing.value = false
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            val update = repository.checkForAppUpdate()
            _appUpdateState.value = update
            if (update.updateAvailable) {
                _showUpdateDialog.value = true
            }
        }
    }

    fun openUpdateDialog() {
        _showUpdateDialog.value = true
        viewModelScope.launch {
            val update = repository.checkForAppUpdate()
            _appUpdateState.value = update
        }
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }

    fun triggerAppUpdate(context: Context) {
        val update = _appUpdateState.value
        if (update.downloadUrl.isNotBlank()) {
            InAppUpdateManager.startApkDownload(
                context = context,
                downloadUrl = update.downloadUrl,
                versionName = update.latestVersion
            )
        }
        _showUpdateDialog.value = false
    }

    fun toggleFavorite(mangaId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(mangaId)
            if (_detailsUiState.value.manga?.id == mangaId) {
                _detailsUiState.value = _detailsUiState.value.copy(
                    isFavorite = repository.isFavorite(mangaId)
                )
            }
            if (_readerUiState.value.manga?.id == mangaId) {
                _readerUiState.value = _readerUiState.value.copy(
                    isFavorite = repository.isFavorite(mangaId)
                )
            }
        }
    }

    fun toggleReadLater(mangaId: String) {
        viewModelScope.launch {
            repository.toggleReadLater(mangaId)
            if (_detailsUiState.value.manga?.id == mangaId) {
                _detailsUiState.value = _detailsUiState.value.copy(
                    isReadLater = repository.isReadLater(mangaId)
                )
            }
        }
    }

    fun isFavorite(mangaId: String): Boolean = repository.isFavorite(mangaId)
    fun isReadLater(mangaId: String): Boolean = repository.isReadLater(mangaId)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _currentPage.value = 1
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        _currentPage.value = 1
    }

    // --- Offline Download Logic ---
    private var batchDownloadJob: Job? = null

    fun downloadChapter(manga: MangaItem, chapter: Chapter) {
        viewModelScope.launch {
            repository.downloadChapter(manga, chapter)
        }
    }

    fun downloadBatchChapters(manga: MangaItem, chapters: List<Chapter>) {
        batchDownloadJob?.cancel()
        batchDownloadJob = viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isBatchDownloading = true)
            try {
                repository.downloadChaptersBatch(manga, chapters)
            } finally {
                _detailsUiState.value = _detailsUiState.value.copy(isBatchDownloading = false)
            }
        }
    }

    fun stopBatchDownload() {
        batchDownloadJob?.cancel()
        batchDownloadJob = null
        _detailsUiState.value = _detailsUiState.value.copy(isBatchDownloading = false)
    }

    fun deleteDownloadedChapter(mangaId: String, chapterNumber: Int) {
        viewModelScope.launch {
            repository.deleteDownloadedChapter(mangaId, chapterNumber)
        }
    }

    fun isChapterDownloaded(mangaId: String, chapterNumber: Int): Boolean {
        return repository.isChapterDownloaded(mangaId, chapterNumber)
    }

    // --- Reading History & Progress Logic ---
    fun recordReadingProgress(mangaId: String, pageNumber: Int, totalPages: Int) {
        val manga = _readerUiState.value.manga ?: repository.getMangaById(mangaId) ?: return
        val currentChapter = _readerUiState.value.currentChapter ?: return

        repository.recordReadingProgress(
            mangaId = manga.id,
            mangaTitle = manga.titleAr,
            mangaCover = manga.coverUrl,
            chapterNumber = currentChapter.number,
            chapterTitle = currentChapter.title,
            pageNumber = pageNumber,
            totalPages = totalPages
        )
    }

    fun deleteHistoryItem(mangaId: String) {
        viewModelScope.launch {
            repository.deleteReadingHistoryItem(mangaId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllReadingHistory()
        }
    }

    // --- Details Screen Logic ---
    fun loadMangaDetails(mangaId: String) {
        val manga = repository.getMangaById(mangaId) ?: return
        val isFav = repository.isFavorite(mangaId)
        val isReadLater = repository.isReadLater(mangaId)
        val lastRead = repository.getLastReadChapter(mangaId)
        val readNums = repository.getReadChapters(mangaId)
        val userRating = repository.getUserRating(mangaId)
        val initialBatch = ((lastRead - 1) / 30).coerceAtLeast(0)
        val downloadedNums = repository.downloadedChaptersFlow.value
            .filter { it.mangaId == mangaId }
            .map { it.chapterNumber }
            .toSet()

        _detailsUiState.value = DetailsUiState(
            manga = manga,
            isFavorite = isFav,
            isReadLater = isReadLater,
            lastReadChapterNumber = lastRead,
            readChapterNumbers = readNums,
            userRating = userRating,
            currentBatchIndex = initialBatch,
            batchSize = 30,
            downloadedChapterNumbers = downloadedNums,
            downloadProgressMap = repository.downloadProgressFlow.value
        )
    }

    fun markChapterAsRead(mangaId: String, chapterNumber: Int) {
        repository.markChapterAsRead(mangaId, chapterNumber)
        if (_detailsUiState.value.manga?.id == mangaId) {
            _detailsUiState.value = _detailsUiState.value.copy(
                readChapterNumbers = _detailsUiState.value.readChapterNumbers + chapterNumber
            )
        }
    }

    fun setBatchIndex(index: Int) {
        val total = _detailsUiState.value.totalBatches
        if (index in 0 until total) {
            _detailsUiState.value = _detailsUiState.value.copy(currentBatchIndex = index)
        }
    }

    fun nextBatch() {
        val current = _detailsUiState.value.currentBatchIndex
        val total = _detailsUiState.value.totalBatches
        if (current + 1 < total) {
            setBatchIndex(current + 1)
        }
    }

    fun previousBatch() {
        val current = _detailsUiState.value.currentBatchIndex
        if (current > 0) {
            setBatchIndex(current - 1)
        }
    }

    // --- Reader Screen Logic ---
    fun loadChapter(mangaId: String, chapterNumber: Int, forceFresh: Boolean = true) {
        val manga = repository.getMangaById(mangaId) ?: return
        val isDownloaded = repository.isChapterDownloaded(mangaId, chapterNumber)
        val lastSavedPage = repository.getLastReadPage(mangaId, chapterNumber)

        _readerUiState.value = ReaderUiState(
            manga = manga,
            currentChapter = null,
            isLoadingPages = true,
            isFavorite = repository.isFavorite(mangaId),
            isDownloaded = isDownloaded,
            isQuickJumpSheetOpen = false,
            initialScrollPage = lastSavedPage,
            hasPreviousChapter = chapterNumber > 1,
            hasNextChapter = chapterNumber < manga.totalChaptersCount
        )

        viewModelScope.launch {
            val fullChapter = repository.getChapterWithPages(mangaId, chapterNumber, forceFresh = forceFresh)
            _readerUiState.value = _readerUiState.value.copy(
                currentChapter = fullChapter,
                isLoadingPages = false,
                isDownloaded = repository.isChapterDownloaded(mangaId, chapterNumber)
            )

            if (fullChapter != null && !fullChapter.isClosed) {
                repository.recordReadingProgress(
                    mangaId = manga.id,
                    mangaTitle = manga.titleAr,
                    mangaCover = manga.coverUrl,
                    chapterNumber = chapterNumber,
                    chapterTitle = fullChapter.title,
                    pageNumber = lastSavedPage,
                    totalPages = fullChapter.pages.size.coerceAtLeast(1)
                )
            }
        }
    }

    fun goToPreviousChapter() {
        val manga = _readerUiState.value.manga ?: return
        val current = _readerUiState.value.currentChapter ?: return
        if (current.number > 1) {
            loadChapter(manga.id, current.number - 1)
        }
    }

    fun goToNextChapter() {
        val manga = _readerUiState.value.manga ?: return
        val current = _readerUiState.value.currentChapter ?: return
        if (current.number < manga.totalChaptersCount) {
            loadChapter(manga.id, current.number + 1)
        }
    }

    fun setQuickJumpSheetOpen(open: Boolean) {
        _readerUiState.value = _readerUiState.value.copy(isQuickJumpSheetOpen = open)
    }
}
