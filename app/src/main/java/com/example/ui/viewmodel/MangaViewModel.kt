package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppUpdateState
import com.example.data.model.Chapter
import com.example.data.model.MangaItem
import com.example.data.repository.MangaRepository
import com.example.util.InAppUpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val heroMangaList: List<MangaItem> = emptyList(),
    val latestMangaGrid: List<MangaItem> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedCategory: String = "الكل",
    val isRefreshing: Boolean = false,
    val showUpdateDialog: Boolean = false,
    val updateInfo: AppUpdateState = AppUpdateState()
)

data class DetailsUiState(
    val manga: MangaItem? = null,
    val isFavorite: Boolean = false,
    val lastReadChapterNumber: Int = 1,
    val currentBatchIndex: Int = 0, // 0 for chapters 1-30, 1 for 31-60, etc.
    val batchSize: Int = 30
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
    val isQuickJumpSheetOpen: Boolean = false,
    val readingProgressText: String = "",
    val hasPreviousChapter: Boolean = false,
    val hasNextChapter: Boolean = false
)

class MangaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MangaRepository(application.applicationContext)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("الكل")
    private val _isRefreshing = MutableStateFlow(false)
    private val _showUpdateDialog = MutableStateFlow(false)
    private val _appUpdateState = MutableStateFlow(AppUpdateState())

    private data class FilterAndDialogState(
        val query: String,
        val category: String,
        val isRefreshing: Boolean,
        val showUpdate: Boolean,
        val updateInfo: AppUpdateState
    )

    private val _filterAndDialogFlow = combine(
        _searchQuery,
        _selectedCategory,
        _isRefreshing,
        _showUpdateDialog,
        _appUpdateState
    ) { query, category, isRefreshing, showUpdate, updateInfo ->
        FilterAndDialogState(query, category, isRefreshing, showUpdate, updateInfo)
    }

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.allMangaFlow,
        repository.favoritesFlow,
        _filterAndDialogFlow
    ) { allMangaList, favorites, filterState ->
        var filteredList = allMangaList
        if (filterState.query.isNotBlank()) {
            filteredList = filteredList.filter {
                it.titleAr.contains(filterState.query, ignoreCase = true) ||
                it.titleEn.contains(filterState.query, ignoreCase = true)
            }
        }
        if (filterState.category != "الكل") {
            filteredList = filteredList.filter {
                it.genres.contains(filterState.category) || it.type.labelAr.contains(filterState.category)
            }
        }

        HomeUiState(
            heroMangaList = allMangaList.take(5),
            latestMangaGrid = filteredList,
            favorites = favorites,
            searchQuery = filterState.query,
            selectedCategory = filterState.category,
            isRefreshing = filterState.isRefreshing,
            showUpdateDialog = filterState.showUpdate,
            updateInfo = filterState.updateInfo
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState(
            heroMangaList = repository.getHeroFeaturedManga(),
            latestMangaGrid = repository.getAllManga()
        )
    )

    private val _detailsUiState = MutableStateFlow(DetailsUiState())
    val detailsUiState: StateFlow<DetailsUiState> = _detailsUiState.asStateFlow()

    private val _readerUiState = MutableStateFlow(ReaderUiState())
    val readerUiState: StateFlow<ReaderUiState> = _readerUiState.asStateFlow()

    init {
        // Automatically sync with GitHub repository & check for updates on startup
        refreshDataFromGitHub()
        checkForUpdates()
    }

    fun refreshDataFromGitHub() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshMangaFromGitHub()
            _isRefreshing.value = false
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

    fun isFavorite(mangaId: String): Boolean = repository.isFavorite(mangaId)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    // --- Details Screen Logic ---
    fun loadMangaDetails(mangaId: String) {
        val manga = repository.getMangaById(mangaId) ?: return
        val isFav = repository.isFavorite(mangaId)
        val lastRead = repository.getLastReadChapter(mangaId)
        val initialBatch = ((lastRead - 1) / 30).coerceAtLeast(0)

        _detailsUiState.value = DetailsUiState(
            manga = manga,
            isFavorite = isFav,
            lastReadChapterNumber = lastRead,
            currentBatchIndex = initialBatch,
            batchSize = 30
        )
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
    fun loadChapter(mangaId: String, chapterNumber: Int) {
        val manga = repository.getMangaById(mangaId) ?: return
        repository.saveLastRead(mangaId, chapterNumber)

        _readerUiState.value = ReaderUiState(
            manga = manga,
            currentChapter = null,
            isLoadingPages = true,
            isFavorite = repository.isFavorite(mangaId),
            isQuickJumpSheetOpen = false,
            hasPreviousChapter = chapterNumber > 1,
            hasNextChapter = chapterNumber < manga.totalChaptersCount
        )

        viewModelScope.launch {
            val fullChapter = repository.getChapterWithPages(mangaId, chapterNumber)
            _readerUiState.value = _readerUiState.value.copy(
                currentChapter = fullChapter,
                isLoadingPages = false
            )
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
