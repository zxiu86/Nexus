package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.details.DetailsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.reader.ReaderScreen
import com.example.ui.viewmodel.MangaViewModel

object NexusDestinations {
    const val HOME = "home"
    const val DETAILS = "details/{mangaId}"
    const val READER = "reader/{mangaId}/{chapterNumber}"

    fun detailsRoute(mangaId: String) = "details/$mangaId"
    fun readerRoute(mangaId: String, chapterNumber: Int) = "reader/$mangaId/$chapterNumber"
}

@Composable
fun NexusNavGraph(
    navController: NavHostController,
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    // Provide Right-to-Left (RTL) layout direction natively for Arabic interface
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        NavHost(
            navController = navController,
            startDestination = NexusDestinations.HOME,
            modifier = modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(animationSpec = tween(220)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(220)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(180))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(220))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(180)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(220)
                )
            }
        ) {
            // Screen 1: Home Screen (الصفحة الرئيسية)
            composable(NexusDestinations.HOME) {
                val homeState by viewModel.homeUiState.collectAsState()
                val context = androidx.compose.ui.platform.LocalContext.current

                HomeScreen(
                    uiState = homeState,
                    onMangaClick = { mangaId ->
                        navController.navigate(NexusDestinations.detailsRoute(mangaId))
                    },
                    onChapterClick = { mangaId, chapterNumber ->
                        navController.navigate(NexusDestinations.readerRoute(mangaId, chapterNumber))
                    },
                    onToggleFavorite = { mangaId ->
                        viewModel.toggleFavorite(mangaId)
                    },
                    onSearchQueryChange = { query ->
                        viewModel.onSearchQueryChanged(query)
                    },
                    onCategorySelect = { cat ->
                        viewModel.onCategorySelected(cat)
                    },
                    onTabSelected = { tabIdx ->
                        viewModel.selectTab(tabIdx)
                    },
                    onDeleteHistoryItem = { mId ->
                        viewModel.deleteHistoryItem(mId)
                    },
                    onClearAllHistory = {
                        viewModel.clearAllHistory()
                    },
                    onDeleteDownloadedChapter = { mId, chNum ->
                        viewModel.deleteDownloadedChapter(mId, chNum)
                    },
                    onRefresh = {
                        viewModel.refreshDataFromGitHub(showIndicator = true)
                    },
                    onTriggerUpdate = {
                        viewModel.triggerAppUpdate(context)
                    },
                    onOpenUpdatesDialog = {
                        viewModel.openUpdateDialog()
                    },
                    onCheckCloudUpdates = {
                        viewModel.checkForUpdates()
                        viewModel.openUpdateDialog()
                    },
                    onDismissUpdateDialog = {
                        viewModel.dismissUpdateDialog()
                    },
                    onPageChange = { page ->
                        viewModel.setHomePage(page)
                    },
                    onNextPage = {
                        viewModel.setHomePage(homeState.currentPage + 1)
                    },
                    onPrevPage = {
                        viewModel.setHomePage(homeState.currentPage - 1)
                    },
                    onRefreshRandomDiscovery = {
                        viewModel.refreshRandomDiscovery()
                    },
                    onFavSubTabSelected = { tab ->
                        viewModel.setFavoriteSubTab(tab)
                    },
                    onToggleReadLater = { mangaId ->
                        viewModel.toggleReadLater(mangaId)
                    }
                )
            }

            // Screen 2: Details Screen (صفحة التفاصيل)
            composable(
                route = NexusDestinations.DETAILS,
                arguments = listOf(
                    navArgument("mangaId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                val detailsState by viewModel.detailsUiState.collectAsState()

                LaunchedEffect(mangaId) {
                    viewModel.loadMangaDetails(mangaId)
                }

                DetailsScreen(
                    uiState = detailsState,
                    onNavigateHome = {
                        navController.navigate(NexusDestinations.HOME) {
                            popUpTo(NexusDestinations.HOME) { inclusive = true }
                        }
                    },
                    onChapterClick = { chapterNum ->
                        navController.navigate(NexusDestinations.readerRoute(mangaId, chapterNum))
                    },
                    onToggleFavorite = {
                        viewModel.toggleFavorite(mangaId)
                    },
                    onToggleReadLater = {
                        viewModel.toggleReadLater(mangaId)
                    },
                    onBatchIndexChange = { idx ->
                        viewModel.setBatchIndex(idx)
                    },
                    onNextBatch = {
                        viewModel.nextBatch()
                    },
                    onPreviousBatch = {
                        viewModel.previousBatch()
                    },
                    onDownloadChapter = { chapter ->
                        detailsState.manga?.let { m ->
                            viewModel.downloadChapter(m, chapter)
                        }
                    },
                    onDownloadBatch = {
                        detailsState.manga?.let { m ->
                            viewModel.downloadBatchChapters(m, detailsState.currentBatchChapters)
                        }
                    },
                    onDeleteDownloadedChapter = { chNum ->
                        viewModel.deleteDownloadedChapter(mangaId, chNum)
                    }
                )
            }

            // Screen 3: Reader Screen (صفحة القراءة)
            composable(
                route = NexusDestinations.READER,
                arguments = listOf(
                    navArgument("mangaId") { type = NavType.StringType },
                    navArgument("chapterNumber") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                val chapterNumber = backStackEntry.arguments?.getInt("chapterNumber") ?: 1
                val readerState by viewModel.readerUiState.collectAsState()

                LaunchedEffect(mangaId, chapterNumber) {
                    viewModel.loadChapter(mangaId, chapterNumber)
                }

                ReaderScreen(
                    uiState = readerState,
                    onNavigateHome = {
                        navController.navigate(NexusDestinations.HOME) {
                            popUpTo(NexusDestinations.HOME) { inclusive = true }
                        }
                    },
                    onNavigateBackToDetails = {
                        navController.popBackStack()
                    },
                    onPreviousChapter = {
                        viewModel.goToPreviousChapter()
                    },
                    onNextChapter = {
                        viewModel.goToNextChapter()
                    },
                    onSelectChapter = { num ->
                        viewModel.loadChapter(mangaId, num)
                    },
                    onToggleFavorite = {
                        viewModel.toggleFavorite(mangaId)
                    },
                    onSetQuickJumpOpen = { open ->
                        viewModel.setQuickJumpSheetOpen(open)
                    },
                    onRecordPageProgress = { page, total ->
                        viewModel.recordReadingProgress(mangaId, page, total)
                    }
                )
            }
        }
    }
}
