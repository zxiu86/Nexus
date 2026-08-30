package com.example.ui.screens.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.Chapter
import com.example.data.model.MangaItem
import androidx.compose.material3.CircularProgressIndicator
import com.example.ui.components.NexusMangaImage
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BadgeNew
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusGoldDark
import com.example.ui.theme.NexusGoldLight
import com.example.ui.theme.NexusOrange
import com.example.ui.theme.NexusOrangeDark
import com.example.ui.theme.NexusOrangeLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.ReaderUiState
import kotlinx.coroutines.launch

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
    modifier: Modifier = Modifier
) {
    val manga = uiState.manga
    val chapter = uiState.currentChapter
    val listState = rememberLazyListState()

    var showControls by remember { mutableStateOf(true) }

    if (manga == null || chapter == null || uiState.isLoadingPages) {
        Box(modifier = modifier.fillMaxSize().background(BackgroundDark), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = NexusGold)
                Text(
                    text = "جاري تحميل صفحات الفصل من السحابة...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }
        return
    }

    // Scroll to top when chapter changes
    LaunchedEffect(chapter.number) {
        listState.scrollToItem(0)
    }

    val totalPages = chapter.pages.size
    val currentVisiblePage by remember {
        derivedStateOf {
            (listState.firstVisibleItemIndex + 1).coerceAtMost(totalPages)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("reader_screen_container")
    ) {
        // Continuous Webtoon Vertical Reader
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                }
                .testTag("reader_lazy_column"),
            contentPadding = PaddingValues(top = 70.dp, bottom = 90.dp)
        ) {
            // Chapter Start Banner
            item {
                ChapterStartBanner(manga = manga, chapter = chapter)
            }

            // Webtoon Continuous Comic Pages
            items(
                items = chapter.pages,
                key = { "${chapter.number}-${it.pageNumber}" }
            ) { page ->
                ComicPageItem(
                    imageUrl = page.imageUrl,
                    pageRes = page.imageRes,
                    pageNumber = page.pageNumber,
                    totalPages = totalPages
                )
            }

            // End of Chapter Action Card
            item {
                ChapterEndCard(
                    manga = manga,
                    currentChapter = chapter,
                    hasNextChapter = uiState.hasNextChapter,
                    hasPreviousChapter = uiState.hasPreviousChapter,
                    onNextChapter = onNextChapter,
                    onPreviousChapter = onPreviousChapter,
                    onOpenQuickJump = { onSetQuickJumpOpen(true) },
                    onNavigateHome = onNavigateHome
                )
            }
        }

        // Top Floating Bar (Header: Chapter name & Work Title + Return to Home button)
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ReaderTopBar(
                mangaTitle = manga.titleAr,
                chapterTitle = "الفصل ${chapter.number}",
                onNavigateHome = onNavigateHome,
                onNavigateBack = onNavigateBackToDetails,
                isFavorite = uiState.isFavorite,
                onToggleFavorite = onToggleFavorite
            )
        }

        // Bottom Floating Navigation Bar (Previous, Quick Jump List, Next)
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderBottomBar(
                currentChapterNumber = chapter.number,
                totalChapters = manga.totalChaptersCount,
                hasPrevious = uiState.hasPreviousChapter,
                hasNext = uiState.hasNextChapter,
                onPreviousChapter = onPreviousChapter,
                onNextChapter = onNextChapter,
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
 * - Return to Home button (زر العودة للرئيسية)
 * - Favorite button
 */
@Composable
fun ReaderTopBar(
    mangaTitle: String,
    chapterTitle: String,
    onNavigateHome: () -> Unit,
    onNavigateBack: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reader_top_bar"),
        color = SurfaceDark.copy(alpha = 0.96f),
        border = BorderStroke(0.5.dp, SurfaceElevated),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Return to Home Button (زر العودة للصفحة الرئيسية)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceVariantDark,
                border = BorderStroke(1.dp, SurfaceElevated),
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
                        tint = NexusGoldLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "الرئيسية",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }

            // Title Header (الفصل واسم العمل)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NexusGoldLight,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mangaTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
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
                        tint = if (isFavorite) NexusOrange else TextSecondary,
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
                        tint = TextPrimary,
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
        color = SurfaceDark.copy(alpha = 0.96f),
        border = BorderStroke(0.5.dp, SurfaceElevated),
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
                        color = NexusGoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = "صفحة $currentPage / $totalPages",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextTertiary,
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
                        containerColor = SurfaceVariantDark,
                        contentColor = TextPrimary,
                        disabledContainerColor = SurfaceElevated.copy(alpha = 0.4f),
                        disabledContentColor = TextTertiary
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text(
                        text = "◄ الفصل السابق",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // "قائمة الفصول السريعة" (Quick Jump Dialog Trigger)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NexusGoldDark,
                    border = BorderStroke(1.dp, NexusGold),
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
                            tint = BackgroundDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "الفصول",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BackgroundDark,
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
                        containerColor = NexusGold,
                        contentColor = BackgroundDark,
                        disabledContainerColor = SurfaceElevated.copy(alpha = 0.4f),
                        disabledContentColor = TextTertiary
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
fun ChapterStartBanner(manga: MangaItem, chapter: Chapter) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = manga.titleAr,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = NexusGoldLight
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "ترجمة: ${manga.scanlationTeam}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextTertiary,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Single Comic Page item in continuous webtoon scroll
 * Uses ContentScale.FillWidth and full unbounded vertical height so manhwa strips never get cropped!
 */
@Composable
fun ComicPageItem(
    imageUrl: String?,
    pageRes: Int?,
    pageNumber: Int,
    totalPages: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        if (!imageUrl.isNullOrBlank() && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "صفحة $pageNumber من $totalPages",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .background(SurfaceCard.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = NexusGold,
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = "جاري تحميل صفحة $pageNumber...",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                error = {
                    if (pageRes != null && pageRes != 0) {
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
                                .background(SurfaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "تعذر تحميل الصفحة $pageNumber",
                                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            )
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
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Subtle Page Number Stamp
        Surface(
            shape = RoundedCornerShape(topStart = 6.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
        ) {
            Text(
                text = "$pageNumber / $totalPages",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.8f),
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
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "✨ نهاية الفصل ${currentChapter.number}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NexusGoldLight
                )
            )

            Text(
                text = "نتمنى لك قراءة ممتعة! لا تنسَ متابعة الفصول القادمة أولاً بأول.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = SurfaceElevated
            )

            if (hasNextChapter) {
                Button(
                    onClick = onNextChapter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NexusGold,
                        contentColor = BackgroundDark
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
                        border = BorderStroke(1.dp, SurfaceElevated),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
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
                    border = BorderStroke(1.dp, NexusOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusOrangeLight)
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
                border = BorderStroke(1.dp, SurfaceElevated),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
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
        containerColor = SurfaceDark,
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
                        color = TextPrimary
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = TextSecondary
                    )
                }
            }

            Text(
                text = "${manga.titleAr} (${manga.totalChaptersCount} فصل متاح)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NexusGoldLight,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Search filter for chapters
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = {
                    Text("اكتب رقم الفصل أو عنوانه...", color = TextTertiary, fontSize = 12.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NexusGoldLight,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = NexusGold,
                    unfocusedBorderColor = SurfaceElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
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
                        color = if (isCurrent) NexusGold else SurfaceCard,
                        border = BorderStroke(1.dp, if (isCurrent) NexusOrange else SurfaceElevated),
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
                                    color = if (isCurrent) BackgroundDark else TextPrimary,
                                    fontSize = 13.sp
                                )
                            )
                            if (ch.isNew) {
                                Text(
                                    text = "NEW",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCurrent) BackgroundDark else BadgeNew
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
