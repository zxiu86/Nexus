package com.example.ui.screens.details

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.R
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.StartIoAdManager
import com.example.data.model.Chapter
import com.example.data.model.MangaItem
import com.example.data.model.MangaType
import com.example.ui.components.NexusMangaImage
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BadgeNew
import com.example.ui.theme.BadgeSuccess
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
import com.example.ui.viewmodel.DetailsUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(
    uiState: DetailsUiState,
    onNavigateHome: () -> Unit,
    onChapterClick: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleReadLater: () -> Unit = {},
    onBatchIndexChange: (Int) -> Unit,
    onNextBatch: () -> Unit,
    onPreviousBatch: () -> Unit,
    onDownloadChapter: (com.example.data.model.Chapter) -> Unit = {},
    onDownloadBatch: () -> Unit = {},
    onStopBatchDownload: () -> Unit = {},
    onDeleteDownloadedChapter: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val manga = uiState.manga
    val context = LocalContext.current

    // Show Interstitial ad once every time DetailsScreen is opened
    LaunchedEffect(Unit) {
        StartIoAdManager.showInterstitial(context)
    }

    if (manga == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = NexusGoldDark,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = NexusGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text("جاري تحميل تفاصيل العمل...", color = TextSecondary)
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Atmospheric Top Background Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NexusGoldDark.copy(alpha = 0.45f),
                            NexusOrangeDark.copy(alpha = 0.20f),
                            BackgroundDark
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("details_screen_lazy_column"),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Top Navigation Bar (With Return to Home Button, Read Later, and Favorite)
            item {
                DetailsTopAppBar(
                    onNavigateHome = onNavigateHome,
                    isFavorite = uiState.isFavorite,
                    isReadLater = uiState.isReadLater,
                    onToggleFavorite = onToggleFavorite,
                    onToggleReadLater = onToggleReadLater
                )
            }

            // Cover & Title Header Card
            item {
                MangaHeaderCard(manga = manga)
            }

            // Action Buttons: "متابعة القراءة" & "الفصل الأول" (Above chapters)
            item {
                ActionButtonsSection(
                    lastReadChapter = uiState.lastReadChapterNumber,
                    onContinueReading = { onChapterClick(uiState.lastReadChapterNumber) },
                    onFirstChapter = { onChapterClick(1) }
                )
            }

            // Categories / Genres (التصنيفات)
            item {
                CategoriesSection(genres = manga.genres)
            }

            // Short Synopsis / Story (القصة القصيرة)
            item {
                SynopsisSection(synopsis = manga.synopsis)
            }

            // Staff / Production Team (العاملين عليها)
            item {
                StaffCreditsSection(manga = manga)
            }

            // Section Title: Chapters List Header with Batch Range Info & Batch Download button
            item {
                ChaptersHeaderSection(
                    totalChapters = manga.totalChaptersCount,
                    rangeText = uiState.currentBatchRangeText,
                    currentBatch = uiState.currentBatchIndex + 1,
                    totalBatches = uiState.totalBatches,
                    isBatchDownloading = uiState.isBatchDownloading,
                    onDownloadBatch = onDownloadBatch,
                    onStopBatchDownload = onStopBatchDownload
                )
            }

            // 30-Chapter Batch List (كل دفعة 30 فصل)
            val chapters = uiState.currentBatchChapters
            items(
                items = chapters,
                key = { it.id }
            ) { chapter ->
                val isDownloaded = uiState.downloadedChapterNumbers.contains(chapter.number)
                val downloadKey = "${manga.id}_${chapter.number}"
                val downloadProgress = uiState.downloadProgressMap[downloadKey]

                ChapterListItem(
                    chapter = chapter,
                    isRead = chapter.number <= uiState.lastReadChapterNumber,
                    isDownloaded = isDownloaded,
                    downloadProgress = downloadProgress,
                    onClick = { onChapterClick(chapter.number) },
                    onDownload = { onDownloadChapter(chapter) },
                    onDeleteDownload = { onDeleteDownloadedChapter(chapter.number) }
                )
            }

            // Pagination Switcher Controls at the bottom (زر التبديل بين الـ 30 فصل)
            item {
                BatchPaginationControl(
                    currentBatchIndex = uiState.currentBatchIndex,
                    totalBatches = uiState.totalBatches,
                    batchSize = uiState.batchSize,
                    totalChapters = manga.totalChaptersCount,
                    onSelectBatch = onBatchIndexChange,
                    onPreviousBatch = onPreviousBatch,
                    onNextBatch = onNextBatch,
                    onNavigateHome = onNavigateHome
                )
            }
        }
    }
}

@Composable
fun DetailsTopAppBar(
    onNavigateHome: () -> Unit,
    isFavorite: Boolean,
    isReadLater: Boolean = false,
    onToggleFavorite: () -> Unit,
    onToggleReadLater: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Return to Home Button (زر العودة للصفحة الرئيسية)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceCard.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.35f)),
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable { onNavigateHome() }
                .testTag("details_back_home_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "العودة للرئيسية",
                    tint = NexusGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "الرئيسية",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NexusGoldLight
                    )
                )
            }
        }

        // Screen Title Pill
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = SurfaceVariantDark.copy(alpha = 0.7f),
            border = BorderStroke(0.5.dp, SurfaceElevated)
        ) {
            Text(
                text = "تفاصيل العمل",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Action Buttons: Read Later Bookmark & Favorite
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Read Later Bookmark Button (المشاهدة لاحقاً)
            IconButton(
                onClick = onToggleReadLater,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isReadLater) NexusGoldDark else SurfaceCard)
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isReadLater) NexusGold else SurfaceElevated
                        ),
                        CircleShape
                    )
                    .testTag("details_read_later_button")
            ) {
                Icon(
                    imageVector = if (isReadLater) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "المشاهدة لاحقاً",
                    tint = if (isReadLater) NexusGold else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Favorite Toggle Button with Glowing Pill
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isFavorite) NexusOrangeDark else SurfaceCard)
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isFavorite) NexusOrange else SurfaceElevated
                        ),
                        CircleShape
                    )
                    .testTag("details_fav_button")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "المفضلة",
                    tint = if (isFavorite) NexusOrangeLight else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 3D Rotating Manga Cover with Boxcover Aura on back
 * Rotates around itself every 3 seconds
 * Front face: Manga cover image with type badge
 * Back face: Boxcover halo aura (R.drawable.img_boxcover_bg) with cosmic emblem
 */
@Composable
fun Rotating3DCoverCard(
    manga: MangaItem,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "manga_cover_flip_infinite")
    val rotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_y_anim"
    )

    // Back face is visible between 90 and 270 degrees of Y rotation
    val isBackFace = rotationY in 90f..270f

    Box(
        modifier = modifier
            .width(120.dp)
            .aspectRatio(0.70f)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 14f * density
            }
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(
                    1.6.dp,
                    Brush.verticalGradient(
                        colors = listOf(NexusGold, NexusOrange, NexusGoldLight)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .shadow(14.dp, RoundedCornerShape(16.dp))
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        if (!isBackFace) {
            // FRONT FACE: Manga Cover
            Box(modifier = Modifier.fillMaxSize()) {
                NexusMangaImage(
                    imageUrl = manga.coverUrl,
                    fallbackRes = manga.coverRes,
                    contentDescription = manga.titleAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom gradient shading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    BackgroundDark.copy(alpha = 0.5f)
                                )
                            )
                        )
                )

                // Type Badge overlay (مانهوا / مانها / مانجا)
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    color = if (manga.type == MangaType.MANHWA) NexusGold else NexusOrange,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = manga.type.labelAr,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = BackgroundDark
                        )
                    )
                }
            }
        } else {
            // BACK FACE: Boxcover Halo (R.drawable.img_boxcover_bg)
            // Compensate with 180° rotation so content isn't mirrored horizontally
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.rotationY = 180f }
                    .background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_boxcover_bg),
                    contentDescription = "هالة العمل boxcover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Atmospheric radial aura glow overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NexusGold.copy(alpha = 0.25f),
                                    NexusOrangeDark.copy(alpha = 0.45f),
                                    BackgroundDark.copy(alpha = 0.65f)
                                )
                            )
                        )
                )

                // Golden crest insignia in the center of the halo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BackgroundDark.copy(alpha = 0.85f),
                        border = BorderStroke(1.2.dp, NexusGold),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "NEXUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = NexusGold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "هالة العمل",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NexusGoldLight,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MangaHeaderCard(manga: MangaItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                colors = listOf(
                    NexusGold.copy(alpha = 0.5f),
                    NexusOrange.copy(alpha = 0.2f),
                    SurfaceElevated
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 3D Rotating Work Cover with Boxcover Aura on back
            Rotating3DCoverCard(manga = manga)

            // Manga Metadata Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = manga.titleAr,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = manga.titleEn,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextTertiary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Rating & Views Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rating Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NexusGoldDark,
                        border = BorderStroke(0.8.dp, NexusGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${manga.rating}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NexusGold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Views Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceVariantDark,
                        border = BorderStroke(0.5.dp, SurfaceElevated)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = NexusOrangeLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = manga.views,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Status & Total Chapters Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariantDark,
                    border = BorderStroke(1.dp, SurfaceElevated),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (manga.status.contains("مستمر")) BadgeSuccess else NexusGold)
                        )
                        Text(
                            text = "${manga.status} • ${manga.totalChaptersCount} فصل متاح",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NexusGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Action Buttons placed prominently above the content:
 * - "متابعة القراءة" (Continue Reading with Gradient Fill)
 * - "الفصل الأول" (First Chapter with Outline)
 */
@Composable
fun ActionButtonsSection(
    lastReadChapter: Int,
    onContinueReading: () -> Unit,
    onFirstChapter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // "متابعة القراءة" Button (Primary Golden-Orange Gradient)
        Surface(
            modifier = Modifier
                .weight(1.2f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onContinueReading() }
                .testTag("continue_reading_button"),
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(NexusGold, NexusOrange)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "متابعة القراءة (فصل $lastReadChapter)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = BackgroundDark,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // "الفصل الأول" Button (Secondary Outlined Card)
        Surface(
            modifier = Modifier
                .weight(0.8f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onFirstChapter() }
                .testTag("first_chapter_button"),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceCard,
            border = BorderStroke(1.5.dp, NexusOrange)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = NexusOrangeLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "الفصل الأول",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NexusOrangeLight,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

/**
 * Categories / Genres Section (التصنيفات)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesSection(genres: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(3.5.dp, 15.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NexusGold)
            )
            Text(
                text = "التصنيفات والأنواع",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            genres.forEach { genre ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = genre,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Short Story / Synopsis Section (القصة القصيرة)
 */
@Composable
fun SynopsisSection(synopsis: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(3.5.dp, 15.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NexusGold)
                )
                Text(
                    text = "نبذة عن القصة",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                )
            }

            Text(
                text = synopsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 22.sp,
                    fontSize = 13.sp
                ),
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NexusGoldDark,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = !expanded }
                ) {
                    Text(
                        text = if (expanded) "عرض أقل ▲" else "قراءة المزيد ▼",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Staff & Team Section (العاملين عليها)
 */
@Composable
fun StaffCreditsSection(manga: MangaItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = NexusGoldDark,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = NexusGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "فريق الإنتاج والترجمة",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StaffInfoRow(icon = Icons.Default.Edit, label = "المؤلف", value = manga.author)
                StaffInfoRow(icon = Icons.Default.Brush, label = "الرسام", value = manga.artist)
                StaffInfoRow(icon = Icons.Default.Translate, label = "فريق الترجمة", value = manga.scanlationTeam)
                StaffInfoRow(icon = Icons.Default.Person, label = "المترجم", value = manga.translator)
                StaffInfoRow(icon = Icons.Default.AutoAwesome, label = "التبييض والتحرير", value = "${manga.cleaner} / ${manga.typesetter}")
            }
        }
    }
}

@Composable
fun StaffInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceVariantDark,
        border = BorderStroke(0.5.dp, SurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NexusGold,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            )
        }
    }
}

/**
 * Chapters List Header with batch indicator and batch download action
 */
@Composable
fun ChaptersHeaderSection(
    totalChapters: Int,
    rangeText: String,
    currentBatch: Int,
    totalBatches: Int,
    isBatchDownloading: Boolean = false,
    onDownloadBatch: () -> Unit = {},
    onStopBatchDownload: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(3.5.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NexusGold)
            )
            Column {
                Text(
                    text = "قائمة الفصول",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = "عرض 30 فصلاً بالدفعة ($rangeText)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NexusGoldLight,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Batch Download or Stop Button (تنزيل الدفعة المتعدد مع زر التوقيف)
            if (isBatchDownloading) {
                // Downloading Status Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NexusOrangeDark,
                    border = BorderStroke(1.dp, NexusOrange),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NexusOrangeLight,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "جاري التنزيل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NexusOrangeLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Stop Batch Download Button (زر إيقاف التنزيل المتعدد)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF3B1515),
                    border = BorderStroke(1.dp, Color(0xFFEF5350)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onStopBatchDownload() }
                        .testTag("stop_batch_download_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.StopCircle,
                            contentDescription = "إيقاف تنزيل الدفعة",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "إيقاف",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFF8A80),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onDownloadBatch() }
                        .testTag("batch_download_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "تنزيل الدفعة كاملة",
                            tint = NexusGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "تنزيل الدفعة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NexusGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NexusGoldDark,
                border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "دفعة $currentBatch من $totalBatches",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NexusGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/**
 * Individual Chapter Item in the 30-chapter list
 */
@Composable
fun ChapterListItem(
    chapter: Chapter,
    isRead: Boolean,
    isDownloaded: Boolean = false,
    downloadProgress: com.example.data.model.ChapterDownloadProgress? = null,
    onClick: () -> Unit,
    onDownload: () -> Unit = {},
    onDeleteDownload: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("details_chapter_${chapter.number}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) SurfaceDark.copy(alpha = 0.8f) else SurfaceCard
        ),
        border = BorderStroke(
            0.8.dp,
            if (isDownloaded) NexusGold.copy(alpha = 0.6f)
            else if (isRead) SurfaceElevated.copy(alpha = 0.4f)
            else SurfaceElevated
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Chapter Number Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isDownloaded) NexusGoldDark
                            else if (isRead) SurfaceElevated
                            else NexusGoldDark
                        )
                        .border(
                            0.5.dp,
                            if (isDownloaded) NexusGold
                            else if (isRead) Color.Transparent
                            else NexusGold.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${chapter.number}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (isDownloaded) NexusGold else if (isRead) TextSecondary else NexusGold,
                            fontSize = 13.sp
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isRead) FontWeight.Medium else FontWeight.Bold,
                                color = if (isRead) TextSecondary else TextPrimary,
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isDownloaded) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NexusGoldDark,
                                border = BorderStroke(0.5.dp, NexusGold.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "محمّل 🔒",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NexusGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = chapter.releaseDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextTertiary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (chapter.isClosed) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF3E2723),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFFFFB74D),
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "مغلق للصيانة",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB74D)
                                )
                            )
                        }
                    }
                } else if (chapter.isNew) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BadgeNew
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "جديد",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                if (isRead) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تمت القراءة",
                        tint = BadgeSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Download Button / Status Icon
                if (chapter.isClosed) {
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "تنبيه إغلاق الفصل",
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (downloadProgress != null && !downloadProgress.isCompleted && !downloadProgress.isFailed) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { downloadProgress.progress },
                            modifier = Modifier.size(22.dp),
                            color = NexusGold,
                            strokeWidth = 2.5.dp,
                            trackColor = SurfaceElevated
                        )
                    }
                } else if (isDownloaded) {
                    IconButton(
                        onClick = onDeleteDownload,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "تم التحميل بأمان",
                            tint = BadgeSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "تحميل الفصل بدون إنترنت",
                            tint = NexusGoldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern, Flexible & Practical Chapter Batch Pagination Switcher
 * - Elegant batch carousel with auto-centering
 * - Quick jump dialog with batch matrix
 * - Direct chapter number search & jump
 * - Rich previous/next buttons showing actual chapter ranges
 * - Batch progress tracking indicator
 */
@Composable
fun BatchPaginationControl(
    currentBatchIndex: Int,
    totalBatches: Int,
    batchSize: Int,
    totalChapters: Int,
    onSelectBatch: (Int) -> Unit,
    onPreviousBatch: () -> Unit,
    onNextBatch: () -> Unit,
    onNavigateHome: () -> Unit
) {
    var showQuickJumpDialog by remember { mutableStateOf(false) }
    var chapterInputText by remember { mutableStateOf("") }
    val carouselListState = rememberLazyListState()

    // Smoothly scroll the active batch card into view whenever batch changes
    LaunchedEffect(currentBatchIndex) {
        if (totalBatches > 0) {
            carouselListState.animateScrollToItem((currentBatchIndex - 1).coerceAtLeast(0))
        }
    }

    // Calculations for Previous and Next range labels
    val prevBatchStart = if (currentBatchIndex > 0) (currentBatchIndex - 1) * batchSize + 1 else 1
    val prevBatchEnd = if (currentBatchIndex > 0) (currentBatchIndex * batchSize).coerceAtMost(totalChapters) else 1

    val nextBatchStart = if (currentBatchIndex + 1 < totalBatches) (currentBatchIndex + 1) * batchSize + 1 else 1
    val nextBatchEnd = if (currentBatchIndex + 1 < totalBatches) ((currentBatchIndex + 2) * batchSize).coerceAtMost(totalChapters) else totalChapters

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                colors = listOf(
                    NexusGold.copy(alpha = 0.45f),
                    SurfaceElevated,
                    NexusOrange.copy(alpha = 0.25f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title, Batch Counter Badge & Quick Jump Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NexusGoldDark,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "مجموعات الفصول",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "الدفعة ${currentBatchIndex + 1} من $totalBatches (30 فصلاً لكل دفعة)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Quick Jump Trigger Button (الانتقال السريع)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceVariantDark,
                    border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showQuickJumpDialog = true }
                        .testTag("batch_quick_jump_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "عرض كل الدفعات",
                            tint = NexusGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "تصفح الدفعات",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NexusGoldLight,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Modern Carousel of Rich Batch Cards
            LazyRow(
                state = carouselListState,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(totalBatches) { batchIdx ->
                    val isSelected = batchIdx == currentBatchIndex
                    val startNum = batchIdx * batchSize + 1
                    val endNum = ((batchIdx + 1) * batchSize).coerceAtMost(totalChapters)
                    val chaptersCount = (endNum - startNum + 1).coerceAtLeast(1)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) NexusGold else SurfaceVariantDark,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) NexusOrange else SurfaceElevated
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectBatch(batchIdx) }
                            .testTag("batch_card_$batchIdx")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "مجموعة ${batchIdx + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) BackgroundDark.copy(alpha = 0.8f) else NexusGold,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = "$startNum - $endNum",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) BackgroundDark else TextPrimary,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = "$chaptersCount فصلاً",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) BackgroundDark.copy(alpha = 0.7f) else TextTertiary,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }

            // Direct Chapter Search & Instant Jump Row (انتقل لأي فصل برقم الفصل)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceVariantDark.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, SurfaceElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NexusGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "انتقال لفصل محدد:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                    OutlinedTextField(
                        value = chapterInputText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                chapterInputText = input
                            }
                        },
                        placeholder = {
                            Text(
                                text = "مثال: 45",
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val chNum = chapterInputText.toIntOrNull()
                                if (chNum != null && chNum > 0) {
                                    val targetBatch = ((chNum - 1) / batchSize).coerceIn(0, totalBatches - 1)
                                    onSelectBatch(targetBatch)
                                    chapterInputText = ""
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("direct_chapter_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexusGold,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NexusGold,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            val chNum = chapterInputText.toIntOrNull()
                            if (chNum != null && chNum > 0) {
                                val targetBatch = ((chNum - 1) / batchSize).coerceIn(0, totalBatches - 1)
                                onSelectBatch(targetBatch)
                                chapterInputText = ""
                            }
                        },
                        enabled = chapterInputText.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexusGold,
                            contentColor = BackgroundDark,
                            disabledContainerColor = SurfaceElevated,
                            disabledContentColor = TextTertiary
                        ),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("direct_chapter_go_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "انتقال",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Batch Progress Indicator Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val progress = if (totalBatches > 1) {
                    (currentBatchIndex.toFloat()) / (totalBatches - 1).toFloat()
                } else 1f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NexusGold,
                    trackColor = SurfaceElevated
                )
            }

            // Prev Batch & Next Batch Ergonomic Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "◄ السابقة" with range label
                Button(
                    onClick = onPreviousBatch,
                    enabled = currentBatchIndex > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("prev_batch_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceVariantDark,
                        contentColor = TextPrimary,
                        disabledContainerColor = SurfaceDark,
                        disabledContentColor = TextTertiary
                    ),
                    border = BorderStroke(1.dp, if (currentBatchIndex > 0) NexusGold.copy(alpha = 0.4f) else SurfaceElevated)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "◄ السابقة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentBatchIndex > 0) {
                            Text(
                                text = "($prevBatchStart - $prevBatchEnd)",
                                fontSize = 9.sp,
                                color = NexusGoldLight
                            )
                        }
                    }
                }

                // "التالية ►" with range label
                Button(
                    onClick = onNextBatch,
                    enabled = currentBatchIndex + 1 < totalBatches,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("next_batch_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NexusGold,
                        contentColor = BackgroundDark,
                        disabledContainerColor = SurfaceDark,
                        disabledContentColor = TextTertiary
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "التالية ►",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (currentBatchIndex + 1 < totalBatches) {
                            Text(
                                text = "($nextBatchStart - $nextBatchEnd)",
                                fontSize = 9.sp,
                                color = BackgroundDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color = SurfaceElevated
            )

            // Return to Home Button (زر العودة للصفحة الرئيسية)
            OutlinedButton(
                onClick = onNavigateHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("details_footer_home_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NexusGoldLight
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = NexusGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "العودة للصفحة الرئيسية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    // Quick Jump Dialog Matrix Modal
    if (showQuickJumpDialog) {
        AlertDialog(
            onDismissRequest = { showQuickJumpDialog = false },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = NexusGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "الانتقال السريع لمجموعة فصول",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                        )
                    }
                    IconButton(
                        onClick = { showQuickJumpDialog = false },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "اختر الدفعة المطلوبة للقفز إليها مباشرة:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )

                    // Scrollable Grid of batches
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(totalBatches) { idx ->
                            val isSelected = idx == currentBatchIndex
                            val startNum = idx * batchSize + 1
                            val endNum = ((idx + 1) * batchSize).coerceAtMost(totalChapters)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) NexusGoldDark else SurfaceVariantDark,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) NexusGold else SurfaceElevated
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectBatch(idx)
                                        showQuickJumpDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "مجموعة ${idx + 1}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) NexusGold else TextPrimary,
                                                fontSize = 13.sp
                                            )
                                        )
                                        Text(
                                            text = "الفصول من $startNum إلى $endNum",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = NexusGold
                                        ) {
                                            Text(
                                                text = "النشطة حالياً",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Black,
                                                    color = BackgroundDark,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQuickJumpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
                ) {
                    Text("إغلاق", color = TextPrimary)
                }
            }
        )
    }
}
