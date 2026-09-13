package com.example.ui.screens.reader

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import coil.Coil
import kotlin.math.roundToInt
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.Chapter
import com.example.data.model.MangaItem
import com.example.ui.components.StartIoBannerAd
import com.example.util.StartIoAdManager
import com.example.ui.theme.BadgeNew
import com.example.ui.viewmodel.ReaderUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    uiState: ReaderUiState,
    onNavigateHome: () -> Unit,
    onNavigateBackToDetails: () -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onSelectChapter: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onSetQuickJumpOpen: (Boolean) -> Unit,
    onRecordPageProgress: (page: Int, total: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manga = uiState.manga
    val chapter = uiState.currentChapter
    val listState = rememberLazyListState()

    var showControls by remember { mutableStateOf(false) }

    // 🔒 SCREEN SECURITY (FLAG_SECURE) & IMMERSIVE FULL-SCREEN
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        // Prevent screen capture / screenshots to protect intellectual property
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        // Set Immersive Mode
        val insetsController = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        insetsController?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // 🚀 منع التطبيق من إدخال الفصول في الكاش الثابت:
    // مجرد القارئ يخرج من الفصل ينحذف كل صور الفصل من الكاش أو التخزين المؤقت فوراً
    DisposableEffect(chapter?.number) {
        onDispose {
            try {
                if (chapter != null && !uiState.isDownloaded) {
                    val imageLoader = Coil.imageLoader(context)
                    chapter.pages.forEach { page ->
                        page.imageUrl?.let { url ->
                            if (!url.startsWith("/")) {
                                imageLoader.memoryCache?.remove(coil.memory.MemoryCache.Key(url))
                                imageLoader.diskCache?.remove(url)
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // 💡 إبقاء الشاشة مفعلة أثناء القراءة (Keep Screen On)
    val keepScreenOn = uiState.appSettings.keepScreenOn
    DisposableEffect(keepScreenOn) {
        val window = (context as? Activity)?.window
        if (keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Toggle system bars visibility alongside showControls
    LaunchedEffect(showControls) {
        val window = (context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        if (showControls) {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Zoom & Pan transformation state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    if (manga == null || chapter == null || uiState.isLoadingPages) {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "جاري تجهيز صفحات الفصل...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
        return
    }

    val totalPages = chapter.pages.size
    val currentVisiblePage by remember {
        derivedStateOf {
            (listState.firstVisibleItemIndex + 1).coerceAtMost(totalPages)
        }
    }

    // Trigger Interstitial Ad immediately whenever entering any chapter
    LaunchedEffect(Unit) {
        StartIoAdManager.showInterstitial(context)
    }

    // Restore saved page position & Show Interstitial Ad upon chapter transition
    LaunchedEffect(chapter.number) {
        scale = 1f
        offset = Offset.Zero
        // Trigger Interstitial Ad upon opening chapter
        StartIoAdManager.showInterstitial(context)

        if (uiState.initialScrollPage > 1 && uiState.initialScrollPage <= totalPages) {
            listState.scrollToItem(uiState.initialScrollPage - 1)
        } else {
            listState.scrollToItem(0)
        }
    }

    // Handler for Next Chapter with Interstitial Ad (إعلان بياني بعد كل ضغط على الفصل التالي)
    val handleNextChapter: () -> Unit = {
        StartIoAdManager.showInterstitial(context)
        onNextChapter()
    }

    // Automatically record reading progress as user scrolls
    LaunchedEffect(currentVisiblePage, totalPages) {
        if (totalPages > 0) {
            onRecordPageProgress(currentVisiblePage, totalPages)
        }
    }

    val readerBg = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(readerBg)
            .testTag("reader_screen_container")
    ) {
        // Continuous Webtoon Vertical Reader or Horizontal Pager with Native Smooth 120Hz Scrolling & Pinch-to-Zoom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (!canceled) {
                                val pointerCount = event.changes.size
                                // 🤏 السحب بإصبعين للتكبير والتصغير (Two-finger pinch to zoom)
                                if (pointerCount >= 2 || scale > 1.05f) {
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()

                                    val newScale = (scale * zoomChange).coerceIn(1f, 4f)
                                    if (newScale > 1.01f) {
                                        val maxOffsetX = (newScale - 1f) * size.width * 0.5f
                                        val maxOffsetY = (newScale - 1f) * size.height * 0.5f
                                        offset = Offset(
                                            x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                                            y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
                                        )
                                    } else {
                                        offset = Offset.Zero
                                    }
                                    scale = newScale
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        } while (!canceled && event.changes.any { it.pressed })

                        if (scale <= 1.05f) {
                            scale = 1f
                            offset = Offset.Zero
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showControls = !showControls
                        }
                    )
                }
        ) {
            val readerMode = uiState.appSettings.readerMode
            if (readerMode == 1 || readerMode == 2) {
                // 📖 نمط القراءة الأفقي (تقليب الصفحات يميناً أو يساراً)
                val pagerState = rememberPagerState(
                    initialPage = (uiState.initialScrollPage - 1).coerceIn(0, (totalPages - 1).coerceAtLeast(0)),
                    pageCount = { totalPages }
                )
                LaunchedEffect(pagerState.currentPage) {
                    onRecordPageProgress(pagerState.currentPage + 1, totalPages)
                }
                HorizontalPager(
                    state = pagerState,
                    reverseLayout = (readerMode == 1),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("reader_horizontal_pager")
                ) { pageIdx ->
                    val page = chapter.pages.getOrNull(pageIdx)
                    if (page != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            ComicPageItem(
                                imageUrl = page.imageUrl,
                                pageRes = page.imageRes,
                                pageNumber = page.pageNumber,
                                totalPages = totalPages,
                                isDownloaded = uiState.isDownloaded,
                                imageQuality = uiState.appSettings.imageQuality
                            )
                        }
                    }
                }
            } else {
                // 📜 نمط ويب تون العمودي المستمر (Webtoon Continuous Scroll)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("reader_lazy_column"),
                    contentPadding = PaddingValues(top = if (showControls) 70.dp else 16.dp, bottom = if (showControls) 90.dp else 24.dp)
                ) {
                    // Chapter Start Banner
                    item {
                        ChapterStartBanner(manga = manga, chapter = chapter, isDownloaded = uiState.isDownloaded)
                    }

                    // Webtoon Continuous Comic Pages with Start.io Fixed Banner Ads Between Images
                    itemsIndexed(
                        items = chapter.pages,
                        key = { index, page -> "${chapter.number}-${page.pageNumber}-$index" }
                    ) { index, page ->
                        ComicPageItem(
                            imageUrl = page.imageUrl,
                            pageRes = page.imageRes,
                            pageNumber = page.pageNumber,
                            totalPages = totalPages,
                            isDownloaded = uiState.isDownloaded,
                            imageQuality = uiState.appSettings.imageQuality
                        )

                        // Fixed Start.io Banner Ad between each comic page and the next
                        if (index < chapter.pages.size - 1) {
                            StartIoBannerAd(
                                modifier = Modifier.fillMaxWidth(),
                                adTag = "reader_page_${index + 1}",
                                isInlineReader = true
                            )
                        }
                    }

                    // End of Chapter Action Card
                    item {
                        ChapterEndCard(
                            manga = manga,
                            currentChapter = chapter,
                            hasNextChapter = uiState.hasNextChapter,
                            hasPreviousChapter = uiState.hasPreviousChapter,
                            onNextChapter = handleNextChapter,
                            onPreviousChapter = onPreviousChapter,
                            onOpenQuickJump = { onSetQuickJumpOpen(true) },
                            onNavigateHome = onNavigateHome
                        )
                    }
                }
            }
        }

        // Zoom Level Reset Badge (when zoomed in)
        if (scale > 1f) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
                    .clip(CircleShape)
                    .clickable {
                        scale = 1f
                        offset = Offset.Zero
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "إعادة ضبط التكبير",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${(scale * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Top Floating Bar (Header: Chapter name & Work Title + Return to Home button)
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ReaderTopBar(
                mangaTitle = manga.titleAr,
                chapterTitle = "الفصل ${chapter.number}",
                isDownloaded = uiState.isDownloaded,
                onNavigateHome = onNavigateHome,
                onNavigateBack = onNavigateBackToDetails,
                isFavorite = uiState.isFavorite,
                onToggleFavorite = onToggleFavorite
            )
        }

        // Bottom Floating Navigation Bar (Previous, Quick Jump List, Next)
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderBottomBar(
                currentChapterNumber = chapter.number,
                totalChapters = manga.totalChaptersCount,
                hasPrevious = uiState.hasPreviousChapter,
                hasNext = uiState.hasNextChapter,
                onPreviousChapter = onPreviousChapter,
                onNextChapter = handleNextChapter,
                onOpenQuickJump = { onSetQuickJumpOpen(true) },
                currentPage = currentVisiblePage,
                totalPages = totalPages
            )
        }

        // Quick Jump Modal Bottom Sheet (قائمة للتنقل السريع بين الفصول)
        if (uiState.isQuickJumpSheetOpen) {
            QuickJumpBottomSheet(
                manga = manga,
                currentChapterNumber = chapter.number,
                onSelectChapter = { num ->
                    onSelectChapter(num)
                    onSetQuickJumpOpen(false)
                },
                onDismiss = { onSetQuickJumpOpen(false) }
            )
        }
    }
}

/**
 * Top App Bar for Reader Screen:
 * - Chapter Title + Manga Title
 * - Offline Security badge
 * - Return to Home button
 * - Favorite button
 */
@Composable
fun ReaderTopBar(
    mangaTitle: String,
    chapterTitle: String,
    isDownloaded: Boolean,
    onNavigateHome: () -> Unit,
    onNavigateBack: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reader_top_bar"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                )
            )
        ),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Return to Home Button
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onNavigateHome() }
                    .testTag("reader_home_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "الرئيسية",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "الرئيسية",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Title Header
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isDownloaded) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "محفوظ أوفلاين",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = chapterTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = mangaTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Favorite Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "المفضلة",
                        tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Back to Details
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("reader_back_details_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع للتفاصيل",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Bottom Floating Navigation Bar:
 * - "الفصل السابق" (Previous Chapter)
 * - Quick jump chapter selector modal
 * - "الفصل التالي" (Next Chapter)
 */
@Composable
fun ReaderBottomBar(
    currentChapterNumber: Int,
    totalChapters: Int,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onOpenQuickJump: () -> Unit,
    currentPage: Int,
    totalPages: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reader_bottom_bar"),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                )
            )
        ),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Page HUD / Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فصل $currentChapterNumber من $totalChapters",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = "صفحة $currentPage / $totalPages",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }

            // Controls Row: [Previous Chapter] [Quick Jump Menu] [Next Chapter]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "الفصل السابق"
                Button(
                    onClick = onPreviousChapter,
                    enabled = hasPrevious,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("reader_prev_chapter_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text(
                        text = "◄ الفصل السابق",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // "قائمة الفصول السريعة"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenQuickJump() }
                        .testTag("quick_jump_trigger_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "قائمة الفصول",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "الفصول",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // "الفصل التالي"
                Button(
                    onClick = onNextChapter,
                    enabled = hasNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("reader_next_chapter_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text(
                        text = "الفصل التالي ►",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Chapter Start Banner
 */
@Composable
fun ChapterStartBanner(manga: MangaItem, chapter: Chapter, isDownloaded: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isDownloaded) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "قراءة بدون اتصال (محفوظ محلياً)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Text(
                text = manga.titleAr,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "ترجمة: ${manga.scanlationTeam}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Single Comic Page item in continuous webtoon scroll with instant memory-cached rendering
 */
@Composable
fun ComicPageItem(
    imageUrl: String?,
    pageRes: Int?,
    pageNumber: Int,
    totalPages: Int,
    isDownloaded: Boolean = false,
    imageQuality: Int = 0
) {
    var reloadKey by remember(imageUrl) { mutableIntStateOf(0) }
    var isLoaded by remember(imageUrl, reloadKey) { mutableStateOf(false) }
    var isError by remember(imageUrl, reloadKey) { mutableStateOf(false) }
    var isTimeout by remember(imageUrl, reloadKey) { mutableStateOf(false) }

    LaunchedEffect(imageUrl, reloadKey) {
        if (!imageUrl.isNullOrBlank()) {
            isLoaded = false
            isError = false
            isTimeout = false
            delay(5000L)
            if (!isLoaded) {
                isTimeout = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            key(reloadKey) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        // 🚀 منع التطبيق من إدخال الفصول في الكاش الثابت للقرص:
                        .diskCachePolicy(if (isDownloaded) CachePolicy.ENABLED else CachePolicy.DISABLED)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .apply {
                            if (imageQuality == 2) {
                                bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                            } else if (imageQuality == 0) {
                                bitmapConfig(android.graphics.Bitmap.Config.ARGB_8888)
                            }
                        }
                        .listener(
                            onSuccess = { _, _ ->
                                isLoaded = true
                                isError = false
                                isTimeout = false
                            },
                            onError = { _, _ ->
                                isError = true
                            }
                        )
                        .build(),
                    contentDescription = "صفحة $pageNumber من $totalPages",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // If image fails to load or exceeds 5 seconds, show retry card
            if ((isTimeout || isError) && !isLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isError) "تعذر تحميل الصفحة $pageNumber" else "استغرق تحميل الصفحة $pageNumber أكثر من 5 ثوانٍ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "تحقق من اتصالك بالإنترنت ثم اضغط على زر إعادة المحاولة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                isLoaded = false
                                isError = false
                                isTimeout = false
                                reloadKey++
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إعادة تحميل الصورة",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        } else if (pageRes != null && pageRes != 0) {
            Image(
                painter = painterResource(id = pageRes),
                contentDescription = "صفحة $pageNumber من $totalPages",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Subtle Page Number Stamp
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
        ) {
            Text(
                text = "$pageNumber / $totalPages",
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * End of Chapter Card:
 * Gives prompt to read Next Chapter or Return to Home
 */
@Composable
fun ChapterEndCard(
    manga: MangaItem,
    currentChapter: Chapter,
    hasNextChapter: Boolean,
    hasPreviousChapter: Boolean,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
    onOpenQuickJump: () -> Unit,
    onNavigateHome: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentChapter.isClosed) {
                Text(
                    text = "🔒 الفصل ${currentChapter.number} قيد الصيانة والإعداد",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                )

                Text(
                    text = "هذا الفصل قيد الصيانة أو إعادة التدقيق والرفع حالياً وسيتاح بأفضل جودة قريباً. يمكنك الانتقال للفصول الأخرى أدناه.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                )
            } else {
                Text(
                    text = "✨ نهاية الفصل ${currentChapter.number}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    text = "نتمنى لك قراءة ممتعة! لا تنسَ متابعة الفصول القادمة أولاً بأول.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            )

            if (hasNextChapter) {
                Button(
                    onClick = onNextChapter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "الانتقال إلى الفصل التالي (${currentChapter.number + 1}) ►",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasPreviousChapter) {
                    OutlinedButton(
                        onClick = onPreviousChapter,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("◄ الفصل السابق", fontSize = 11.sp)
                    }
                }

                OutlinedButton(
                    onClick = onOpenQuickJump,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("قائمة الفصول", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Return to Home Button (زر العودة للصفحة الرئيسية)
            OutlinedButton(
                onClick = onNavigateHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("reader_end_home_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("العودة للصفحة الرئيسية", fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Quick Jump Modal Bottom Sheet (قائمة للتنقل بين فصل إلى فصل آخر بشكل سريع)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickJumpBottomSheet(
    manga: MangaItem,
    currentChapterNumber: Int,
    onSelectChapter: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var filterQuery by remember { mutableStateOf("") }

    val filteredChapters = remember(filterQuery, manga.chapters) {
        if (filterQuery.isBlank()) manga.chapters
        else manga.chapters.filter {
            it.number.toString().contains(filterQuery) || it.title.contains(filterQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("quick_jump_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التنقل السريع بين الفصول",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${manga.titleAr} (${manga.totalChaptersCount} فصل متاح)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Search filter for chapters
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = {
                    Text("اكتب رقم الفصل أو عنوانه...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 12.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Grid of Chapter Quick Buttons
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 65.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp)
            ) {
                items(
                    items = filteredChapters,
                    key = { it.id }
                ) { ch ->
                    val isCurrent = ch.number == currentChapterNumber
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isCurrent) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectChapter(ch.number) }
                            .testTag("quick_jump_ch_${ch.number}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${ch.number}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                            )
                            if (ch.isNew) {
                                Text(
                                    text = "NEW",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else BadgeNew
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

