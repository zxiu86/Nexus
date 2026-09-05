package com.example.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * - Tab 1: البحث (Search)
 * - Tab 2: المفضلة (Favorites)
 * - Tab 3: السجل (History)
 * - Tab 4: الإعدادات (Settings - includes Downloads and Updates)
 */
@Composable
fun NexusBottomFooterBar(
    selectedTab: Int,
    favoritesCount: Int,
    downloadedCount: Int = 0,
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
                .padding(horizontal = 6.dp, vertical = 6.dp),
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
                icon = Icons.Default.Search,
                label = "البحث",
                isSelected = selectedTab == 1,
                badgeCount = null,
                onClick = { onTabSelected(1) },
                testTag = "footer_tab_search"
            )

            FooterNavItem(
                icon = Icons.Default.Favorite,
                label = "المفضلة",
                isSelected = selectedTab == 2,
                badgeCount = if (favoritesCount > 0) favoritesCount else null,
                badgeColor = NexusOrange,
                onClick = { onTabSelected(2) },
                testTag = "footer_tab_favorites"
            )

            FooterNavItem(
                icon = Icons.Default.History,
                label = "السجل",
                isSelected = selectedTab == 3,
                badgeCount = null,
                onClick = { onTabSelected(3) },
                testTag = "footer_tab_history"
            )

            FooterNavItem(
                icon = Icons.Default.Settings,
                label = "الإعدادات",
                isSelected = selectedTab == 4,
                hasDot = hasUpdate,
                onClick = { onTabSelected(4) },
                testTag = "footer_tab_settings"
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
 * TAB 1: FAVORITES & READ LATER VIEW (المفضلة والمشاهدة لاحقاً)
 * =========================================================================
 */
@Composable
fun FavoritesTabContent(
    favoriteList: List<MangaItem>,
    readLaterList: List<MangaItem> = emptyList(),
    selectedSubTab: Int = 0,
    onSubTabSelected: (Int) -> Unit = {},
    onMangaClick: (String) -> Unit,
    onChapterClick: (String, Int) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleReadLater: (String) -> Unit = {},
    onExploreHome: () -> Unit
) {
    val currentList = if (selectedSubTab == 0) favoriteList else readLaterList
    val isFavoritesTab = selectedSubTab == 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("favorites_tab_container")
    ) {
        // Sub-Tab Switcher: المفضلة / المشاهدة لاحقاً
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(SurfaceCard, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Favorites Sub-tab
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selectedSubTab == 0) NexusOrangeDark else Color.Transparent,
                border = if (selectedSubTab == 0) BorderStroke(1.dp, NexusOrange.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSubTabSelected(0) }
                    .testTag("subtab_favorites")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (selectedSubTab == 0) NexusOrangeLight else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "المفضلة (${favoriteList.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSubTab == 0) TextPrimary else TextSecondary,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            // Read Later Sub-tab
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selectedSubTab == 1) NexusGoldDark else Color.Transparent,
                border = if (selectedSubTab == 1) BorderStroke(1.dp, NexusGold.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSubTabSelected(1) }
                    .testTag("subtab_read_later")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = if (selectedSubTab == 1) NexusGoldLight else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "المشاهدة لاحقاً (${readLaterList.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSubTab == 1) TextPrimary else TextSecondary,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }

        if (currentList.isEmpty()) {
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
                            border = BorderStroke(
                                1.dp,
                                if (isFavoritesTab) NexusOrange.copy(alpha = 0.4f) else NexusGold.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isFavoritesTab) Icons.Default.FavoriteBorder else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (isFavoritesTab) NexusOrange else NexusGold,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isFavoritesTab) "قائمة المفضلة فارغة" else "قائمة المشاهدة لاحقاً فارغة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        )

                        Text(
                            text = if (isFavoritesTab)
                                "لم تقم بإضافة أي مانهوا أو مانغا إلى المفضلة بعد. انقر على أيقونة القلب في أي عمل للوصول إليه بسرعة هنا."
                            else
                                "لم تقم بحفظ أي عمل للمشاهدة لاحقاً. انقر على أيقونة الإشارة المرجعية في تفاصيل العمل لحفظه هنا.",
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
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
                                text = if (isFavoritesTab) "الأعمال المفضلة (${currentList.size})" else "قائمة المشاهدة لاحقاً (${currentList.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = if (isFavoritesTab) "تنبيهات وتحديثات الفصول فور نزولها" else "أعمال تم حفظها لقراءتها لاحقاً",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isFavoritesTab) NexusOrangeDark else NexusGoldDark,
                            border = BorderStroke(1.dp, if (isFavoritesTab) NexusOrange.copy(alpha = 0.5f) else NexusGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavoritesTab) Icons.Default.Favorite else Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = if (isFavoritesTab) NexusOrangeLight else NexusGoldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${currentList.size} عمل",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFavoritesTab) NexusOrangeLight else NexusGoldLight
                                    )
                                )
                            }
                        }
                    }
                }

                items(currentList, key = { it.id }) { manga ->
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
                            // Thumbnail with Badge
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

                                // New Chapter Notification Badge on Favorites
                                if (isFavoritesTab) {
                                    Surface(
                                        shape = RoundedCornerShape(bottomStart = 8.dp),
                                        color = BadgeNew,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = "محدث",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                fontSize = 8.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // Details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (manga.genres.isNotEmpty()) {
                                    Text(
                                        text = manga.genres.take(2).joinToString(" ، "),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NexusGoldLight,
                                            fontSize = 11.sp
                                        )
                                    )
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

                            // Toggle / Remove Button
                            IconButton(
                                onClick = {
                                    if (isFavoritesTab) {
                                        onToggleFavorite(manga.id)
                                    } else {
                                        onToggleReadLater(manga.id)
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceVariantDark)
                            ) {
                                Icon(
                                    imageVector = if (isFavoritesTab) Icons.Default.Favorite else Icons.Default.Bookmark,
                                    contentDescription = "إزالة",
                                    tint = if (isFavoritesTab) NexusOrange else NexusGold,
                                    modifier = Modifier.size(20.dp)
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
                        text = "يمكنك تحميل الفصول مسبقاً لقراءتها في أي وقت بدون إنترنت مع حفظ آمن وتصفح فائق السرعة.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )

                    // Storage Badge
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
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = NexusGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "حفظ محلي آمن 📥",
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
                        text = "الخادم السحابي: خوادم نكسوس السحابية (متصل ونشط)",
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

        // What's New in v1.8.4 Header
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
                    text = "شرح مميزات وتحديثات الإصدار v${com.example.BuildConfig.VERSION_NAME}:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                )
            }
        }

        // Feature 1: Smooth direct update
        item {
            FeatureHighlightCard(
                icon = Icons.Default.SystemUpdate,
                iconTint = NexusGold,
                title = "1. تثبيت وتحديث سلس ومباشر",
                description = "حل مشكلة فك الحزمة وإتاحة تنزيل التحديثات وتثبيتها بنقرة واحدة مباشرة من داخل التطبيق، أو اختيار التحميل المباشر عبر المتصفح حسب رغبتك."
            )
        }

        // Feature 2: High Speed & Quality
        item {
            FeatureHighlightCard(
                icon = Icons.Default.AutoAwesome,
                iconTint = NexusOrange,
                title = "2. سرعة فائقة في فتح الفصول",
                description = "تحسين شامل لسرعة جلب وتحميل الصفحات وعرضها بأعلى دقة ووضوح مع استهلاك اقتصادي للبيانات والذاكرة."
            )
        }

        // Feature 3: Immersive Reading Mode
        item {
            FeatureHighlightCard(
                icon = Icons.Default.Visibility,
                iconTint = NexusGoldLight,
                title = "3. وضع القراءة المغمور (شاشة كاملة)",
                description = "قراءة على كامل الشاشة بدون أي إطارات مشتتة مع دعم التقريب بإصبعين (Pinch-to-Zoom) والتحريك الانسيابي وتثبيت شريط التحكم بنقرة واحدة."
            )
        }

        // Feature 4: Offline Reading
        item {
            FeatureHighlightCard(
                icon = Icons.Default.CloudDownload,
                iconTint = NexusGold,
                title = "4. قراءة بدون إنترنت (أوفلاين)",
                description = "إمكانية تنزيل الفصول مسبقاً وتصفحها في أي وقت بدون اتصال بالإنترنت مع إدارة فورية وسلسة للفصول المحفوظة."
            )
        }

        // Feature 5: Reading History & Resume
        item {
            FeatureHighlightCard(
                icon = Icons.Default.History,
                iconTint = NexusOrange,
                title = "5. استئناف فوري وحفظ دقيق للتقدم",
                description = "حفظ تلقائي لآخر صفحة وفصل قرأته مع زر المتابعة الفورية للعودة مباشرة من حيث توقفت، بالإضافة إلى قائمة المفضلة السريعة."
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

/**
 * Upgraded & High-Tech Search Tab with animated query suggestions, multi-tier filters,
 * sort controls, view layout switcher (Grid/List), and responsive animations.
 */
@Composable
fun SearchTabContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    categories: List<String>,
    searchResults: List<MangaItem>,
    onMangaClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    favorites: Set<String>,
    modifier: Modifier = Modifier
) {
    var selectedTypeFilter by remember { mutableStateOf("الكل") } // الكل, مانهوا, مانجا, مانها
    var selectedSortOption by remember { mutableIntStateOf(0) } // 0: الأعلى تقييماً, 1: الأكثر فصولاً, 2: الأحدث, 3: الأبجدي
    var isGridView by remember { mutableStateOf(true) }

    val popularSearchTags = remember {
        listOf("سولو ليفلينج", "ون بيس", "برج الإله", "الرجل الخارق", "أكشن", "فنتازيا", "رومانسي", "سحر", "غموض")
    }

    val typeFilters = listOf("الكل", "مانهوا (كوري)", "مانجا (ياباني)", "مانها (صيني)")
    val sortOptions = listOf("الأعلى تقييماً ⭐", "الأكثر فصولاً 📚", "الأحدث نزولاً 🆕", "أ - ي 🔤")

    // Filter and Sort Processing
    val processedResults = remember(searchResults, selectedTypeFilter, selectedSortOption) {
        var list = searchResults
        if (selectedTypeFilter != "الكل") {
            when (selectedTypeFilter) {
                "مانهوا (كوري)" -> list = list.filter { it.type == MangaType.MANHWA }
                "مانجا (ياباني)" -> list = list.filter { it.type == MangaType.MANGA }
                "مانها (صيني)" -> list = list.filter { it.type == MangaType.MANHUA }
            }
        }
        when (selectedSortOption) {
            0 -> list.sortedByDescending { it.rating }
            1 -> list.sortedByDescending { it.totalChaptersCount }
            2 -> list.sortedByDescending { it.id }
            3 -> list.sortedBy { it.titleAr }
            else -> list
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Modern Floating Search Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                color = NexusOrangeDark,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = NexusOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "البحث والاستكشاف الذكي",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                )
                            )
                        }

                        // Grid / List Toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceElevated, RoundedCornerShape(10.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isGridView) NexusOrange else Color.Transparent,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable { isGridView = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "عرض شبكي",
                                        tint = if (isGridView) BackgroundDark else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isGridView) NexusOrange else Color.Transparent,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable { isGridView = false }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = "عرض قائمة",
                                        tint = if (!isGridView) BackgroundDark else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "ابحث بالاسم، المؤلف، النوع أو التصنيف...",
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = if (searchQuery.isNotBlank()) NexusOrange else TextSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح البحث",
                                        tint = NexusOrange
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexusOrange,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            cursorColor = NexusOrange,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Popular Suggestion Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "الأكثر بحثاً:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(popularSearchTags) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceElevated.copy(alpha = 0.8f),
                                    modifier = Modifier.clickable { onSearchQueryChange(tag) }
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NexusGoldLight,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Section: Categories
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "التصنيفات والأنواع",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelect(category) },
                            label = {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BackgroundDark else TextSecondary
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NexusOrange,
                                containerColor = SurfaceCard
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) NexusOrange else SurfaceElevated,
                                selectedBorderColor = NexusOrange,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Type & Sort Filters Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(typeFilters) { type ->
                        val isSelected = type == selectedTypeFilter
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NexusGold else SurfaceCard,
                            border = BorderStroke(1.dp, if (isSelected) NexusGold else SurfaceElevated),
                            modifier = Modifier.clickable { selectedTypeFilter = type }
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BackgroundDark else TextSecondary,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sort Options Selector
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sortOptions.indices.toList()) { index ->
                    val isSelected = selectedSortOption == index
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) SurfaceElevated else SurfaceDark,
                        border = BorderStroke(1.dp, if (isSelected) NexusOrange else Color.Transparent),
                        modifier = Modifier.clickable { selectedSortOption = index }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = sortOptions[index],
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NexusOrange else TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Results Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الأعمال المطابقة (${processedResults.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                )

                if (selectedCategory != "الكل" || searchQuery.isNotBlank() || selectedTypeFilter != "الكل") {
                    TextButton(
                        onClick = {
                            onCategorySelect("الكل")
                            onSearchQueryChange("")
                            selectedTypeFilter = "الكل"
                        }
                    ) {
                        Text(
                            text = "إعادة ضبط الفلاتر",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NexusOrange,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        if (processedResults.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, SurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SurfaceElevated,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = "لم يتم العثور على أي نتائج مطابقة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        )
                        Text(
                            text = "جرب البحث باسم مختلف أو قم بتغيير الفلاتر والتصنيفات",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextTertiary,
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            if (isGridView) {
                // GRID VIEW (2-column cards)
                val chunkedResults = processedResults.chunked(2)
                items(chunkedResults, key = { it.joinToString("-") { m -> m.id } }) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        for (manga in pair) {
                            Box(modifier = Modifier.weight(1f)) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                    border = BorderStroke(1.dp, SurfaceElevated),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onMangaClick(manga.id) }
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(190.dp)
                                        ) {
                                            NexusMangaImage(
                                                imageUrl = manga.coverUrl,
                                                fallbackRes = manga.coverRes,
                                                contentDescription = manga.titleAr,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )

                                            // Favorite button
                                            val isFav = favorites.contains(manga.id)
                                            IconButton(
                                                onClick = { onToggleFavorite(manga.id) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(32.dp)
                                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "المفضلة",
                                                    tint = if (isFav) NexusOrange else Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Rating badge
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color.Black.copy(alpha = 0.75f),
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = NexusGold,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = manga.rating.toString(),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                }
                                            }

                                            // Type badge
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = NexusOrangeDark.copy(alpha = 0.9f),
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(6.dp)
                                            ) {
                                                Text(
                                                    text = manga.type.labelAr,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = NexusOrange,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = manga.titleAr.ifEmpty { manga.titleEn },
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    fontSize = 13.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = "${manga.totalChaptersCount} فصلاً • ${manga.genres.firstOrNull() ?: manga.type.labelAr}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // LIST VIEW (Detailed rows)
                items(processedResults, key = { it.id }) { manga ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, SurfaceElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMangaClick(manga.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cover Image
                            Box(
                                modifier = Modifier
                                    .size(width = 75.dp, height = 100.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                NexusMangaImage(
                                    imageUrl = manga.coverUrl,
                                    fallbackRes = manga.coverRes,
                                    contentDescription = manga.titleAr,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Details column
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NexusOrange.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = manga.type.labelAr,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NexusOrange,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "★ ${manga.rating}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NexusGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Text(
                                    text = manga.titleAr.ifEmpty { manga.titleEn },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = manga.synopsis,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${manga.totalChaptersCount} فصلاً",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                                    )
                                    Text(
                                        text = manga.genres.take(2).joinToString("، "),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NexusGoldLight,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            // Favorite Icon button
                            val isFav = favorites.contains(manga.id)
                            IconButton(onClick = { onToggleFavorite(manga.id) }) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "المفضلة",
                                    tint = if (isFav) NexusOrange else TextSecondary
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
 * Professional, fully featured Settings Tab Content.
 * Features:
 * 1. Storage & Downloads Management (Detailed storage quota, batch delete, cache clean).
 * 2. Reader & Display Preferences (Reading mode, image quality, keep screen on, volume keys scroll).
 * 3. Updates & Changelog (In-app updates v1.9.1, cloud checks, auto-sync).
 * 4. About & Community (Nexus Manga Studio team, GitHub, contact).
 */
@Composable
fun SettingsTabContent(
    uiState: HomeUiState,
    onReadChapter: (String, Int) -> Unit,
    onDeleteDownload: (String, Int) -> Unit,
    onTriggerUpdate: () -> Unit,
    onCheckCloudUpdates: () -> Unit,
    onRefreshData: () -> Unit,
    onExploreHome: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    var settingsCategory by remember { mutableIntStateOf(0) } // 0: التخزين والتحميلات, 1: القراءة والعرض, 2: التحديثات, 3: حول التطبيق
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearDownloadsDialog by remember { mutableStateOf(false) }
    var cacheCleanedSuccess by remember { mutableStateOf(false) }

    // User preference local toggles
    var readerMode by remember { mutableIntStateOf(0) } // 0: ويبتون طولي, 1: أفقي RTL, 2: أفقي LTR
    var imageQuality by remember { mutableIntStateOf(0) } // 0: عالية الدقة HD, 1: متوازنة, 2: موفر البيانات
    var keepScreenOn by remember { mutableStateOf(true) }
    var volumeScroll by remember { mutableStateOf(false) }
    var doubleTapZoom by remember { mutableStateOf(true) }
    var autoSyncUpdates by remember { mutableStateOf(true) }
    var wifiOnlyDownloads by remember { mutableStateOf(false) }

    val categoryTabs = listOf(
        "التحميلات والتخزين" to Icons.Default.Storage,
        "تفضيلات القارئ" to Icons.AutoMirrored.Filled.MenuBook,
        "التحديثات v1.9.1" to Icons.Default.AutoAwesome,
        "حول التطبيق" to Icons.Default.Info
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Categories Segmented Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryTabs.indices.toList()) { index ->
                val (title, icon) = categoryTabs[index]
                val isSelected = settingsCategory == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) NexusOrange else SurfaceDark,
                    border = BorderStroke(1.dp, if (isSelected) NexusOrange else SurfaceElevated),
                    modifier = Modifier.clickable { settingsCategory = index }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) BackgroundDark else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BackgroundDark else TextSecondary
                            )
                        )
                        if (index == 2 && uiState.updateInfo.updateAvailable) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                        }
                    }
                }
            }
        }

        when (settingsCategory) {
            0 -> {
                // TAB 0: DOWNLOADS & STORAGE MANAGEMENT
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Storage Quota Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceElevated),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Storage,
                                                    contentDescription = null,
                                                    tint = NexusGold,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "مساحة التخزين المستهلكة",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            )
                                            Text(
                                                text = "${uiState.downloadedChapters.size} فصول محملة محلياً",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NexusGold.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = uiState.formattedTotalStorage,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                color = NexusGold
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Storage meter
                                LinearProgressIndicator(
                                    progress = {
                                        (uiState.downloadedChapters.size.toFloat() / 50f).coerceIn(0.05f, 1f)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = NexusGold,
                                    trackColor = SurfaceElevated
                                )

                                HorizontalDivider(color = SurfaceElevated)

                                // Quick actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onClearCache()
                                            cacheCleanedSuccess = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusOrange),
                                        border = BorderStroke(1.dp, NexusOrange.copy(alpha = 0.5f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (cacheCleanedSuccess) Icons.Default.Check else Icons.Default.CleaningServices,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (cacheCleanedSuccess) "تم التنظيف ✓" else "مسح الكاش",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    if (uiState.downloadedChapters.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = { showClearDownloadsDialog = true },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                                            border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "حذف الكل",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Download Over Wi-Fi Toggle Card
                    item {
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "التحميل عبر الواي فاي فقط",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "توفير باقة بيانات الجوال عند تنزيل الفصول",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextTertiary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Switch(
                                    checked = wifiOnlyDownloads,
                                    onCheckedChange = { wifiOnlyDownloads = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = BackgroundDark,
                                        checkedTrackColor = NexusOrange,
                                        uncheckedTrackColor = SurfaceElevated
                                    )
                                )
                            }
                        }
                    }

                    // Downloaded Chapters Section Header
                    item {
                        Text(
                            text = "الفصول المحملة للقراءة أوفلاين (${uiState.downloadedChapters.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (uiState.downloadedChapters.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                border = BorderStroke(1.dp, SurfaceElevated),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Text(
                                        text = "لا توجد فصول محملة حالياً",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        )
                                    )
                                    Text(
                                        text = "يمكنك تحميل أي فصل بضغطة زر من صفحة تفاصيل العمل لقراءته بأي وقت بدون إنترنت",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextTertiary,
                                            fontSize = 11.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.downloadedChapters, key = { "${it.mangaId}_${it.chapterNumber}" }) { chapter ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                border = BorderStroke(1.dp, SurfaceElevated),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onReadChapter(chapter.mangaId, chapter.chapterNumber) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = NexusGoldDark,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDone,
                                                contentDescription = null,
                                                tint = NexusGold,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chapter.mangaTitle,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "الفصل ${chapter.chapterNumber} • ${chapter.formattedSize} • متاح أوفلاين",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NexusGoldLight,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteDownload(chapter.mangaId, chapter.chapterNumber) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "حذف التنزيل",
                                            tint = Color(0xFFEF5350),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // TAB 1: READER & DISPLAY PREFERENCES
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Reading Direction Selector
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceElevated),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = null,
                                        tint = NexusOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "نمط واتجاه القراءة الافتراضي",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                }

                                val readerModes = listOf("ويب تون عمودي (مستمر)", "أفقي (من اليمين لليسار)", "أفقي (من اليسار لليمين)")
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    readerModes.forEachIndexed { idx, modeName ->
                                        val isSelected = readerMode == idx
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) NexusOrangeDark else SurfaceDark,
                                            border = BorderStroke(1.dp, if (isSelected) NexusOrange else SurfaceElevated),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { readerMode = idx }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = modeName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = if (isSelected) TextPrimary else TextSecondary,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = NexusOrange,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Image Quality Selector
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceElevated),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = NexusGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "جودة تحميل صور الفصول",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                }

                                val qualities = listOf("عالية الدقة الأصلية (HD)", "متوازنة (الموصى بها)", "موفر البيانات (سريعة)")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    qualities.forEachIndexed { idx, qTitle ->
                                        val isSelected = imageQuality == idx
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) NexusGold else SurfaceDark,
                                            border = BorderStroke(1.dp, if (isSelected) NexusGold else SurfaceElevated),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { imageQuality = idx }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = qTitle,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isSelected) BackgroundDark else TextSecondary,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 10.sp
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reader Switches Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceElevated),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DisplaySettings,
                                        contentDescription = null,
                                        tint = NexusOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "ميزات إضافية للقارئ",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                }

                                // Keep Screen On
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "إبقاء الشاشة مفعلة",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "منع إغلاق الشاشة تلقائياً أثناء قراءة الفصول",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextTertiary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    Switch(
                                        checked = keepScreenOn,
                                        onCheckedChange = { keepScreenOn = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = BackgroundDark,
                                            checkedTrackColor = NexusOrange,
                                            uncheckedTrackColor = SurfaceElevated
                                        )
                                    )
                                }

                                HorizontalDivider(color = SurfaceElevated)

                                // Volume Keys Scroll
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تقليب الصفحات بأزرار الصوت",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "استخدام أزرار رفع وخفض الصوت للتنقل بين الصفحات",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextTertiary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    Switch(
                                        checked = volumeScroll,
                                        onCheckedChange = { volumeScroll = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = BackgroundDark,
                                            checkedTrackColor = NexusOrange,
                                            uncheckedTrackColor = SurfaceElevated
                                        )
                                    )
                                }

                                HorizontalDivider(color = SurfaceElevated)

                                // Double Tap Zoom
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تكبير سريع بالنقر المزدوج",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "تكبير وتصغير صفحات المانجا بنقرتين متتاليتين",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextTertiary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    Switch(
                                        checked = doubleTapZoom,
                                        onCheckedChange = { doubleTapZoom = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = BackgroundDark,
                                            checkedTrackColor = NexusOrange,
                                            uncheckedTrackColor = SurfaceElevated
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // TAB 2: UPDATES & CHANGELOG (v1.9.1)
                UpdatesTabContent(
                    uiState = uiState,
                    onTriggerUpdate = onTriggerUpdate,
                    onCheckCloudUpdates = onCheckCloudUpdates,
                    onRefreshData = onRefreshData
                )
            }
            3 -> {
                // TAB 3: ABOUT & COMMUNITY
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // App Branding Banner
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceElevated),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = NexusOrangeDark,
                                    border = BorderStroke(2.dp, NexusOrange),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = NexusOrange,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Nexus Manga Reader",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NexusGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "الإصدار الرسمي v${com.example.BuildConfig.VERSION_NAME} (Build 30)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NexusGold
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = "تطبيق قراءة المانجا والمانهوا والمانها الأول باللغة العربية. تم تصميمه لتقديم تجربة فائقة السرعة، استقرار تام، ودعم قراءة كامل بدون إنترنت.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Tech & Features Specs Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, SurfaceElevated),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "المواصفات والتقنيات المبني بها",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )

                                val specs = listOf(
                                    "واجهة المستخدم" to "Jetpack Compose (Material 3)",
                                    "محرك القراءة" to "محسن للقراءة السريعة والأوفلاين",
                                    "التخزين المحلي" to "Room Database & File Cache",
                                    "التحديثات السحابية" to "GitHub Releases Auto-Sync"
                                )

                                specs.forEach { (label, value) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                        )
                                        Text(
                                            text = value,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NexusOrange
                                            )
                                        )
                                    }
                                    HorizontalDivider(color = SurfaceElevated.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear Downloads Confirmation Dialog
    if (showClearDownloadsDialog) {
        AlertDialog(
            onDismissRequest = { showClearDownloadsDialog = false },
            title = {
                Text(
                    text = "حذف جميع التحميلات؟",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف كافة الفصول المحملة محلياً وتحرير مساحة التخزين؟",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.downloadedChapters.forEach {
                            onDeleteDownload(it.mangaId, it.chapterNumber)
                        }
                        showClearDownloadsDialog = false
                    }
                ) {
                    Text(
                        text = "نعم، حذف الكل",
                        color = Color(0xFFEF5350),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDownloadsDialog = false }) {
                    Text("إلغاء")
                }
            },
            containerColor = SurfaceCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

