package com.example.ui.screens.details

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onBatchIndexChange: (Int) -> Unit,
    onNextBatch: () -> Unit,
    onPreviousBatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val manga = uiState.manga

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
            // Top Navigation Bar (With Return to Home Button)
            item {
                DetailsTopAppBar(
                    onNavigateHome = onNavigateHome,
                    isFavorite = uiState.isFavorite,
                    onToggleFavorite = onToggleFavorite
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

            // Section Title: Chapters List Header with Batch Range Info
            item {
                ChaptersHeaderSection(
                    totalChapters = manga.totalChaptersCount,
                    rangeText = uiState.currentBatchRangeText,
                    currentBatch = uiState.currentBatchIndex + 1,
                    totalBatches = uiState.totalBatches
                )
            }

            // 30-Chapter Batch List (كل دفعة 30 فصل)
            val chapters = uiState.currentBatchChapters
            items(
                items = chapters,
                key = { it.id }
            ) { chapter ->
                ChapterListItem(
                    chapter = chapter,
                    isRead = chapter.number <= uiState.lastReadChapterNumber,
                    onClick = { onChapterClick(chapter.number) }
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
    onToggleFavorite: () -> Unit
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

        // Favorite Toggle Button with Glowing Pill
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .size(42.dp)
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
                modifier = Modifier.size(22.dp)
            )
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
            // Work Cover Image with glowing border and tag
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(0.70f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        BorderStroke(
                            1.5.dp,
                            Brush.verticalGradient(
                                colors = listOf(NexusGold, NexusOrange)
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .shadow(12.dp, RoundedCornerShape(14.dp))
            ) {
                NexusMangaImage(
                    imageUrl = manga.coverUrl,
                    fallbackRes = manga.coverRes,
                    contentDescription = manga.titleAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
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
 * Chapters List Header with batch indicator
 */
@Composable
fun ChaptersHeaderSection(
    totalChapters: Int,
    rangeText: String,
    currentBatch: Int,
    totalBatches: Int
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

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = NexusGoldDark,
            border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.4f))
        ) {
            Text(
                text = "دفعة $currentBatch من $totalBatches",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NexusGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
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
    onClick: () -> Unit
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
            if (isRead) SurfaceElevated.copy(alpha = 0.4f) else SurfaceElevated
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
                            if (isRead) SurfaceElevated else NexusGoldDark
                        )
                        .border(
                            0.5.dp,
                            if (isRead) Color.Transparent else NexusGold.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${chapter.number}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (isRead) TextSecondary else NexusGold,
                            fontSize = 13.sp
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (chapter.isNew) {
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
            }
        }
    }
}

/**
 * Pagination Controls at the bottom:
 * - Switcher buttons for previous/next 30 chapters
 * - Direct batch chips for jumping between batches
 * - Return to Home button
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = NexusGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "التبديل بين مجموعات الفصول (30 فصل)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                )
            }

            // Direct Batch Selection Chips: [1-30], [31-60], [61-90]
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(totalBatches) { batchIdx ->
                    val isSelected = batchIdx == currentBatchIndex
                    val startNum = batchIdx * batchSize + 1
                    val endNum = ((batchIdx + 1) * batchSize).coerceAtMost(totalChapters)
                    val label = "$startNum - $endNum"

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NexusGold else SurfaceVariantDark,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NexusOrange else SurfaceElevated
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectBatch(batchIdx) }
                            .testTag("batch_chip_$batchIdx")
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) BackgroundDark else TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Prev 30 & Next 30 Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "الـ 30 فصل السابقة"
                Button(
                    onClick = onPreviousBatch,
                    enabled = currentBatchIndex > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("prev_batch_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceVariantDark,
                        contentColor = TextPrimary,
                        disabledContainerColor = SurfaceDark,
                        disabledContentColor = TextTertiary
                    ),
                    border = BorderStroke(1.dp, SurfaceElevated)
                ) {
                    Text(
                        text = "◄ 30 فصل السابقة",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // "الـ 30 فصل التالية"
                Button(
                    onClick = onNextBatch,
                    enabled = currentBatchIndex + 1 < totalBatches,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("next_batch_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NexusGold,
                        contentColor = BackgroundDark,
                        disabledContainerColor = SurfaceDark,
                        disabledContentColor = TextTertiary
                    )
                ) {
                    Text(
                        text = "30 فصل التالية ►",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
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
}
