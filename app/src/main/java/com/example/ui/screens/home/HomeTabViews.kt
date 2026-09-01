package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadedChapter
import com.example.data.model.MangaItem
import com.example.data.model.MangaType
import com.example.data.model.ReadingHistoryEntry
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
import com.example.ui.viewmodel.HomeUiState

/**
 * Modern Nexus Bottom Footer Navigation Bar:
 * - Tab 0: الرئيسية (Home)
 * - Tab 1: المفضلة (Favorites)
 * - Tab 2: السجل (History)
 * - Tab 3: التحميلات (Downloads)
 * - Tab 4: التحديثات (Updates)
 */
@Composable
fun NexusBottomFooterBar(
    selectedTab: Int,
    favoritesCount: Int,
    downloadedCount: Int,
    hasUpdate: Boolean,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("nexus_bottom_footer_bar"),
        color = SurfaceDark.copy(alpha = 0.98f),
        border = BorderStroke(0.5.dp, SurfaceElevated),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterNavItem(
                icon = Icons.Default.Explore,
                label = "الرئيسية",
                isSelected = selectedTab == 0,
                badgeCount = null,
                onClick = { onTabSelected(0) },
                testTag = "footer_tab_home"
            )

            FooterNavItem(
                icon = Icons.Default.Favorite,
                label = "المفضلة",
                isSelected = selectedTab == 1,
                badgeCount = if (favoritesCount > 0) favoritesCount else null,
                badgeColor = NexusOrange,
                onClick = { onTabSelected(1) },
                testTag = "footer_tab_favorites"
            )

            FooterNavItem(
                icon = Icons.Default.History,
                label = "السجل",
                isSelected = selectedTab == 2,
                badgeCount = null,
                onClick = { onTabSelected(2) },
                testTag = "footer_tab_history"
            )

            FooterNavItem(
                icon = Icons.Default.CloudDownload,
                label = "التحميلات",
                isSelected = selectedTab == 3,
                badgeCount = if (downloadedCount > 0) downloadedCount else null,
                badgeColor = NexusGold,
                onClick = { onTabSelected(3) },
                testTag = "footer_tab_downloads"
            )

            FooterNavItem(
                icon = Icons.Default.AutoAwesome,
                label = "التحديثات",
                isSelected = selectedTab == 4,
                hasDot = hasUpdate,
                onClick = { onTabSelected(4) },
                testTag = "footer_tab_updates"
            )
        }
    }
}

@Composable
private fun FooterNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int? = null,
    badgeColor: Color = NexusGold,
    hasDot: Boolean = false,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        label = "footer_item_scale"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) NexusGold.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (isSelected) NexusGold.copy(alpha = 0.4f) else Color.Transparent
        ),
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) NexusGoldLight else TextTertiary,
                    modifier = Modifier.size(22.dp)
                )

                if (badgeCount != null) {
                    Surface(
                        shape = CircleShape,
                        color = badgeColor,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (badgeCount > 9) "9+" else "$badgeCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BackgroundDark
                                )
                            )
                        }
                    }
                } else if (hasDot) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NexusOrange)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) NexusGoldLight else TextTertiary
                )
            )
        }
    }
}

/**
 * =========================================================================
 * TAB 1: FAVORITES VIEW (قسم المفضلة المطور)
 * =========================================================================
 */
@Composable
fun FavoritesTabContent(
    favoriteList: List<MangaItem>,
    onMangaClick: (String) -> Unit,
    onChapterClick: (String, Int) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onExploreHome: () -> Unit
) {
    if (favoriteList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SurfaceVariantDark,
                        border = BorderStroke(1.dp, NexusOrange.copy(alpha = 0.4f)),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = NexusOrange,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "قائمة المفضلة فارغة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = "لم تقم بإضافة أي مانهوا أو مانغا إلى المفضلة بعد. انقر على أيقونة القلب في أي عمل للوصول إليه بسرعة هنا.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )

                    Button(
                        onClick = onExploreHome,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexusGold,
                            contentColor = BackgroundDark
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("استكشف الأعمال الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("favorites_tab_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الأعمال المفضلة (${favoriteList.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "تم حفظها محلياً للوصول الفوري",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NexusOrangeDark,
                        border = BorderStroke(1.dp, NexusOrange.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = NexusOrangeLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${favoriteList.size} عمل",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NexusOrangeLight
                                )
                            )
                        }
                    }
                }
            }

            items(favoriteList, key = { it.id }) { manga ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onMangaClick(manga.id) }
                        .testTag("favorite_card_${manga.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .size(width = 68.dp, height = 92.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, NexusGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        ) {
                            NexusMangaImage(
                                imageUrl = manga.coverUrl,
                                fallbackRes = manga.coverRes,
                                contentDescription = manga.titleAr,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (manga.type == MangaType.MANHWA) NexusGold else NexusOrange
                                ) {
                                    Text(
                                        text = manga.type.labelAr,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BackgroundDark,
                                            fontSize = 9.sp
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = NexusGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${manga.rating}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NexusGold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Text(
                                text = manga.titleAr,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = manga.genres.take(3).joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${manga.chapters.size} فصول متوفرة",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NexusGoldLight,
                                        fontSize = 11.sp
                                    )
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SurfaceVariantDark,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onChapterClick(manga.id, 1) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = NexusGold,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "اقرأ الفصل 1",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NexusGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Remove Favorite Button
                        IconButton(
                            onClick = { onToggleFavorite(manga.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "إزالة",
                                tint = NexusOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * =========================================================================
 * TAB 2: READING HISTORY VIEW (سجل القراءة الذكي)
 * =========================================================================
 */
@Composable
fun HistoryTabContent(
    historyList: List<ReadingHistoryEntry>,
    onContinueReading: (String, Int) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onExploreHome: () -> Unit
) {
    if (historyList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SurfaceVariantDark,
                        border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.4f)),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "سجل القراءة فارغ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = "عند قراءتك لأي فصل في التطبيق، سيتم حفظ الفصول والصفحة التي توقفت عندها تلقائياً هنا لتتمكن من المتابعة بنقرة واحدة.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )

                    Button(
                        onClick = onExploreHome,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexusGold,
                            contentColor = BackgroundDark
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("ابدأ القراءة الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("history_tab_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "سجل القراءة الأخير",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "يتذكر الفصول والصفحة التي توقفت عندها",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }

                    OutlinedButton(
                        onClick = onClearAllHistory,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SurfaceElevated),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextTertiary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text("مسح الكل", fontSize = 11.sp)
                        }
                    }
                }
            }

            items(historyList, key = { it.mangaId }) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onContinueReading(item.mangaId, item.chapterNumber) }
                        .testTag("history_card_${item.mangaId}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cover
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 86.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, NexusGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        ) {
                            NexusMangaImage(
                                imageUrl = item.mangaCover,
                                fallbackRes = null,
                                contentDescription = item.mangaTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.mangaTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NexusGoldDark
                                ) {
                                    Text(
                                        text = "الفصل ${item.chapterNumber}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NexusGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                if (item.totalPages > 0) {
                                    Text(
                                        text = "صفحة ${item.pageNumber} من ${item.totalPages}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
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
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = item.timestampFormatted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextTertiary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Action Buttons: Continue & Delete
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { onContinueReading(item.mangaId, item.chapterNumber) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NexusGold,
                                    contentColor = BackgroundDark
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("متابعة", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            IconButton(
                                onClick = { onDeleteHistoryItem(item.mangaId) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف من السجل",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * =========================================================================
 * TAB 3: DOWNLOADS VIEW (التحميلات والقراءة بدون إنترنت المحمية)
 * =========================================================================
 */
@Composable
fun DownloadsTabContent(
    downloadedList: List<DownloadedChapter>,
    totalStorageFormatted: String,
    onReadChapter: (String, Int) -> Unit,
    onDeleteDownload: (String, Int) -> Unit,
    onExploreHome: () -> Unit
) {
    if (downloadedList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SurfaceVariantDark,
                        border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.4f)),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "لا توجد فصول محملة بعد",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = "يمكنك تحميل الفصول مسبقاً لقراءتها في أي وقت بدون إنترنت. جميع الملفات مشفرة ومحمية داخل التطبيق لمنع تسريبها.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )

                    // Security Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BackgroundDark,
                        border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = NexusGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "حماية أمنية وتشفير محلي 🔒",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NexusGoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onExploreHome,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexusGold,
                            contentColor = BackgroundDark
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("تصفح الفصول للتحميل", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("downloads_tab_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Storage Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NexusGoldDark,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DownloadDone,
                                        contentDescription = null,
                                        tint = NexusGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "الفصول المحملة (${downloadedList.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "المساحة المشغولة: $totalStorageFormatted",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NexusGoldLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BackgroundDark,
                            border = BorderStroke(0.5.dp, SurfaceElevated)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = NexusGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "محمي",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NexusGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            items(downloadedList, key = { "${it.mangaId}-${it.chapterNumber}" }) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onReadChapter(item.mangaId, item.chapterNumber) }
                        .testTag("download_card_${item.mangaId}_${item.chapterNumber}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cover
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 86.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, NexusGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        ) {
                            NexusMangaImage(
                                imageUrl = item.mangaCover,
                                fallbackRes = null,
                                contentDescription = item.mangaTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.mangaTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NexusGoldDark
                                ) {
                                    Text(
                                        text = "الفصل ${item.chapterNumber}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NexusGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Text(
                                    text = "${item.totalPages} صفحة • ${item.formattedSize}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BadgeSuccess,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "جاهز للقراءة بدون إنترنت",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = BadgeSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Read & Delete
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { onReadChapter(item.mangaId, item.chapterNumber) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NexusGold,
                                    contentColor = BackgroundDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("اقرأ", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            IconButton(
                                onClick = { onDeleteDownload(item.mangaId, item.chapterNumber) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف التحميل",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * =========================================================================
 * TAB 4: UPDATES & CHANGELOG VIEW (قسم التحديثات ومميزات v1.6 بالتفصيل)
 * =========================================================================
 */
@Composable
fun UpdatesTabContent(
    uiState: HomeUiState,
    onTriggerUpdate: () -> Unit,
    onCheckCloudUpdates: () -> Unit,
    onRefreshData: () -> Unit
) {
    val updateInfo = uiState.updateInfo

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("updates_tab_content"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Version Hero Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(listOf(NexusGold, NexusOrange, SurfaceElevated))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(NexusGold, NexusOrange))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "تطبيق Nexus Manga",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NexusGold,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = "الإصدار الحالي v${updateInfo.currentVersion}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BackgroundDark
                                )
                            )
                        }

                        if (updateInfo.updateAvailable) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NexusOrange
                            ) {
                                Text(
                                    text = "تحديث جديد v${updateInfo.latestVersion} 🚀",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BackgroundDark
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = "مستودع السحابة: github.com/zxiu86/Nexus (متصل ونشط)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )

                    HorizontalDivider(color = SurfaceElevated)

                    // Action Buttons for Updates & Cloud Sync
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onCheckCloudUpdates,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexusGoldDark,
                                contentColor = NexusGold
                            ),
                            border = BorderStroke(1.dp, NexusGold),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("فحص التحديثات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (updateInfo.updateAvailable) {
                            Button(
                                onClick = onTriggerUpdate,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NexusOrange,
                                    contentColor = BackgroundDark
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("تنزيل الـ APK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // What's New in v1.6 Header
        item {
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
                Text(
                    text = "شرح مميزات وتحديثات الإصدار v1.6.1:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                )
            }
        }

        // Feature 0: Package Signature & Conflict Resolution
        item {
            FeatureHighlightCard(
                icon = Icons.Default.CheckCircle,
                iconTint = NexusGold,
                title = "1. توحيد الحزمة والتوقيع (حل تعارض الحزم)",
                description = "تم تحديث معرفات البناء والتوقيع لضمان التثبيت المباشر. (تنبيه: إذا ظهرت رسالة 'تعارض الحزمة' عند التثبيت لأول مرة، يرجى حذف النسخة التجريبية القديمة لمرة واحدة وتثبيت هذا الإصدار الموحد)."
            )
        }

        // Feature 1: Offline Reading with Content Protection
        item {
            FeatureHighlightCard(
                icon = Icons.Default.Lock,
                iconTint = NexusGold,
                title = "2. القراءة بدون اتصال مع الحماية الأمنية",
                description = "يمكنك الآن تحميل أي فصل بضغطة زر وتخزينه محلياً لقراءته لاحقاً بدون إنترنت. يتم تشفير وحفظ صفحات الفصول في مجلدات التطبيق الخاصة المعزولة داخلياً لمنع استخراجها أو تسريبها خارج التطبيق."
            )
        }

        // Feature 2: Immersive Full Screen Mode
        item {
            FeatureHighlightCard(
                icon = Icons.Default.Visibility,
                iconTint = NexusOrange,
                title = "3. وضع القراءة المغمور (Immersive Mode)",
                description = "إخفاء تلقائي لشريط الحالة (Status Bar) وشريط التنقل السفلي أثناء القراءة لمنحك تجربة شاشة كاملة 100% بدون أي تشتيت، مع نقرة واحدة على الشاشة للتبديل بين إظهار وإخفاء القوائم والأزرار."
            )
        }

        // Feature 3: Pinch to Zoom & Pan
        item {
            FeatureHighlightCard(
                icon = Icons.Default.ZoomIn,
                iconTint = NexusGoldLight,
                title = "4. التكبير والتصغير التفاعلي (Pinch-to-Zoom)",
                description = "دعم كامل للتقريب بإصبعين والتحريك بسلاسة فائقة للتكبير على التفاصيل والنصوص الصغيرة داخل صفحات المانهوا والمانغا دون فقدان الدقة."
            )
        }

        // Feature 4: Anti-Screenshot Security
        item {
            FeatureHighlightCard(
                icon = Icons.Default.Security,
                iconTint = NexusOrangeLight,
                title = "5. الحماية الأمنية لمنع التصوير (FLAG_SECURE)",
                description = "حماية حقوق صانعي العمل والمترجمين من خلال منع لقطات الشاشة وتسجيل الفيديو أثناء قراءة الفصول لحماية المحتوى الحصري داخل التطبيق."
            )
        }

        // Feature 5: Progress Tracking & History
        item {
            FeatureHighlightCard(
                icon = Icons.Default.History,
                iconTint = NexusGold,
                title = "6. سجل القراءة وتتبع التقدم الذكي",
                description = "تحديد الفصول المقروءة تلقائياً، وتخزين آخر صفحة تم الوصول إليها مع التاريخ والوقت، وزر المتابعة السريعة لاستئناف القراءة فوراً."
            )
        }

        // Feature 6: Bottom Navigation Bar (Footer)
        item {
            FeatureHighlightCard(
                icon = Icons.Default.Explore,
                iconTint = NexusOrange,
                title = "7. شريط التنقل السفلي الفاخر (Footer)",
                description = "شريط تنقل أنيق وسلس في أسفل الشاشة يتيح لك التبديل السريع بين: الرئيسية، المفضلة، السجل، التحميلات، وقائمة التحديثات."
            )
        }
    }
}

@Composable
private fun FeatureHighlightCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}
