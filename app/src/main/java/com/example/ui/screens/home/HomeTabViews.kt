package com.example.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Layers
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
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
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
import com.example.ui.theme.HarmattanFontFamily
import com.example.ui.theme.ThemePalettes
import com.example.ui.theme.HarmattanTypography
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundAmoled
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BadgeNew
import com.example.ui.theme.BadgeSuccess
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusGoldDark
import com.example.ui.theme.NexusGoldLight
import com.example.ui.theme.NexusOrange
import com.example.ui.theme.NexusOrangeDark
import com.example.ui.theme.NexusOrangeLight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.example.ui.theme.NexusBluePrimary
import com.example.ui.theme.NexusRedPrimary
import com.example.ui.theme.NexusMarineBluePrimary
import com.example.ui.theme.NexusCherryBlossomPrimary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.HomeUiState
import com.example.util.AppVersionConfig

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
    val accentPrimary = MaterialTheme.colorScheme.primary
    val accentSecondary = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("nexus_bottom_footer_bar")
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            border = BorderStroke(
                1.2.dp,
                Brush.horizontalGradient(
                    listOf(
                        accentPrimary.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        accentSecondary.copy(alpha = 0.45f)
                    )
                )
            ),
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
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
                    badgeColor = accentPrimary,
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
}

@Composable
private fun FooterNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    hasDot: Boolean = false,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val accentPrimary = MaterialTheme.colorScheme.primary
    val onAccentPrimary = MaterialTheme.colorScheme.onPrimary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "footer_item_scale"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) accentPrimary.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (isSelected) accentPrimary.copy(alpha = 0.45f) else Color.Transparent
        ),
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Glowing mini indicator line on top of active item
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentPrimary)
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }

            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) accentPrimary else unselectedColor,
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
                                    color = onAccentPrimary
                                )
                            )
                        }
                    }
                } else if (hasDot) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentPrimary)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accentPrimary else unselectedColor
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
    val accentPrimary = MaterialTheme.colorScheme.primary
    val accentSecondary = MaterialTheme.colorScheme.secondary

    var showSidePopup by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("") }
    var popupItemTitle by remember { mutableStateOf("") }
    var popupIsFav by remember { mutableStateOf(true) }

    LaunchedEffect(showSidePopup) {
        if (showSidePopup) {
            delay(2200)
            showSidePopup = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("favorites_tab_container")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Sub-Tab Switcher: المفضلة / المشاهدة لاحقاً
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Favorites Sub-tab
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selectedSubTab == 0) accentPrimary.copy(alpha = 0.2f) else Color.Transparent,
                border = if (selectedSubTab == 0) BorderStroke(1.dp, accentPrimary.copy(alpha = 0.5f)) else null,
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
                        tint = if (selectedSubTab == 0) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "المفضلة (${favoriteList.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSubTab == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            // Read Later Sub-tab
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selectedSubTab == 1) accentSecondary.copy(alpha = 0.2f) else Color.Transparent,
                border = if (selectedSubTab == 1) BorderStroke(1.dp, accentSecondary.copy(alpha = 0.5f)) else null,
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
                        tint = if (selectedSubTab == 1) accentSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "المشاهدة لاحقاً (${readLaterList.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSubTab == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isFavoritesTab) Color(0xFFE53935).copy(alpha = 0.4f) else accentPrimary.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isFavoritesTab) Icons.Default.FavoriteBorder else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (isFavoritesTab) Color(0xFFE53935) else accentPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isFavoritesTab) "قائمة المفضلة فارغة" else "قائمة المشاهدة لاحقاً فارغة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp
                            )
                        )

                        Text(
                            text = if (isFavoritesTab)
                                "لم تقم بإضافة أي مانهوا أو مانغا إلى المفضلة بعد. انقر على أيقونة القلب في أي عمل للوصول إليه بسرعة هنا."
                            else
                                "لم تقم بحفظ أي عمل للمشاهدة لاحقاً. انقر على أيقونة الإشارة المرجعية في تفاصيل العمل لحفظه هنا.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        )

                        Button(
                            onClick = onExploreHome,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentPrimary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
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
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = if (isFavoritesTab) "تنبيهات وتحديثات الفصول فور نزولها" else "أعمال تم حفظها لقراءتها لاحقاً",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isFavoritesTab) Color(0xFFE53935).copy(alpha = 0.2f) else accentPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (isFavoritesTab) Color(0xFFE53935).copy(alpha = 0.5f) else accentPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavoritesTab) Icons.Default.Favorite else Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = if (isFavoritesTab) Color(0xFFE53935) else accentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${currentList.size} عمل",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFavoritesTab) Color(0xFFE53935) else accentPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                items(currentList, key = { it.id }) { manga ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                                    .border(1.dp, accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            ) {
                                NexusMangaImage(
                                    imageUrl = manga.coverUrl,
                                    fallbackRes = manga.coverRes,
                                    contentDescription = manga.titleAr,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                 // New Chapter Notification Badge on Favorites (only if it has recent new chapters)
                                val hasRecentChapter = manga.chapters.any { com.example.util.ChapterDateUtils.isChapterNew(it.releaseDate, it.isNew) }
                                if (isFavoritesTab && hasRecentChapter) {
                                    Surface(
                                        shape = RoundedCornerShape(bottomStart = 8.dp),
                                        color = accentPrimary,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = "محدث",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.onPrimary,
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
                                            color = accentPrimary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Text(
                                    text = manga.titleAr,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = manga.genres.take(3).joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                            color = accentPrimary,
                                            fontSize = 11.sp
                                        )
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
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
                                                tint = accentPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "اقرأ الفصل 1",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = accentPrimary,
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
                                    popupItemTitle = manga.titleAr
                                    if (isFavoritesTab) {
                                        popupIsFav = true
                                        popupMessage = "تمت الإزالة من المفضلة 💔"
                                        onToggleFavorite(manga.id)
                                    } else {
                                        popupIsFav = false
                                        popupMessage = "تمت الإزالة من المشاهدة لاحقاً"
                                        onToggleReadLater(manga.id)
                                    }
                                    showSidePopup = true
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = if (isFavoritesTab) Icons.Default.Favorite else Icons.Default.Bookmark,
                                    contentDescription = "إزالة",
                                    tint = if (isFavoritesTab) Color(0xFFE53935) else accentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Side Popup Notification Banner
    AnimatedVisibility(
        visible = showSidePopup,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(280)
        ) + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 16.dp, end = 12.dp)
            .zIndex(99f)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            border = BorderStroke(
                1.5.dp,
                if (popupIsFav) Color(0xFFE53935) else accentPrimary
            ),
            shadowElevation = 10.dp,
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Small Logo / Badge
                Surface(
                    shape = CircleShape,
                    color = if (popupIsFav) Color(0xFFE53935).copy(alpha = 0.18f) else accentPrimary.copy(alpha = 0.18f),
                    border = BorderStroke(
                        1.dp,
                        if (popupIsFav) Color(0xFFE53935) else accentPrimary
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (popupIsFav) Icons.Default.FavoriteBorder else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (popupIsFav) Color(0xFFE53935) else accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = popupMessage,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = popupItemTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 150.dp)
                    )
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
    val accentPrimary = MaterialTheme.colorScheme.primary
    val accentSecondary = MaterialTheme.colorScheme.secondary

    if (historyList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = accentPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "سجل القراءة فارغ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = "عند قراءتك لأي فصل في التطبيق، سيتم حفظ الفصول والصفحة التي توقفت عندها تلقائياً هنا لتتمكن من المتابعة بنقرة واحدة.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )

                    Button(
                        onClick = onExploreHome,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "يتذكر الفصول والصفحة التي توقفت عندها",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    OutlinedButton(
                        onClick = onClearAllHistory,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                                .border(1.dp, accentPrimary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
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
                                    color = MaterialTheme.colorScheme.onSurface,
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
                                    color = accentPrimary.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, accentPrimary.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "الفصل ${item.chapterNumber}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = accentPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                if (item.totalPages > 0) {
                                    Text(
                                        text = "صفحة ${item.pageNumber} من ${item.totalPages}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = item.timestampFormatted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                                    containerColor = accentPrimary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
    val accentPrimary = MaterialTheme.colorScheme.primary

    if (downloadedList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = accentPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "لا توجد فصول محملة بعد",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = "يمكنك تحميل الفصول مسبقاً لقراءتها في أي وقت بدون إنترنت مع حفظ آمن وتصفح فائق السرعة.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )

                    // Storage Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "حفظ محلي آمن 📥",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = accentPrimary,
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
                            containerColor = accentPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.4f)),
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
                                color = accentPrimary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DownloadDone,
                                        contentDescription = null,
                                        tint = accentPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "الفصول المحملة (${downloadedList.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "المساحة المشغولة: $totalStorageFormatted",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = accentPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = accentPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "محمي",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = accentPrimary,
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                                .border(1.dp, accentPrimary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
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
                                    color = MaterialTheme.colorScheme.onSurface,
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
                                    color = accentPrimary.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, accentPrimary.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "الفصل ${item.chapterNumber}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = accentPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Text(
                                    text = "${item.totalPages} صفحة • ${item.formattedSize}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    containerColor = accentPrimary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
    var isGridView by remember { mutableStateOf(true) }
    var sideToastMessage by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    LaunchedEffect(sideToastMessage) {
        if (sideToastMessage != null) {
            kotlinx.coroutines.delay(2000L)
            sideToastMessage = null
        }
    }

    // When no search or category filter is active, display the top 5 newest works.
    // When searching or filtering by category, display all matching results.
    val displayedResults = remember(searchResults, searchQuery, selectedCategory) {
        if (searchQuery.isBlank() && selectedCategory == "الكل") {
            searchResults.take(5)
        } else {
            searchResults
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Modern Floating Search Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.2.dp,
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        )
                    ),
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
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "البحث والاستكشاف الذكي",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp
                                    )
                                )
                            }

                            // Grid / List Toggle
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isGridView) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable { isGridView = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.GridView,
                                            contentDescription = "عرض شبكي",
                                            tint = if (isGridView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (!isGridView) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable { isGridView = false }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.List,
                                            contentDescription = "عرض قائمة",
                                            tint = if (!isGridView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    text = "ابحث بالاسم، المؤلف، أو التصنيف...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = if (searchQuery.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "مسح البحث",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
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
                        text = if (searchQuery.isNotBlank() || selectedCategory != "الكل") "نتائج البحث" else "أحدث الأعمال المضافة",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    )

                    if (selectedCategory != "الكل" || searchQuery.isNotBlank()) {
                        TextButton(
                            onClick = {
                                onCategorySelect("الكل")
                                onSearchQueryChange("")
                            }
                        ) {
                            Text(
                                text = "إعادة ضبط البحث",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            if (displayedResults.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
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
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = "لم يتم العثور على أي نتائج مطابقة",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "جرب البحث باسم مختلف أو قم باختيار تصنيف آخر",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                if (isGridView) {
                    // GRID VIEW (2-column cards) - Without work type labels, with aesthetic gradient border & small favorite button
                    val chunkedResults = displayedResults.chunked(2)
                    items(chunkedResults, key = { it.joinToString("-") { m -> m.id } }) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            for (manga in pair) {
                                Box(modifier = Modifier.weight(1f)) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(
                                            1.2.dp,
                                            Brush.verticalGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                )
                                            )
                                        ),
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

                                                // Compact Favorite Button with Side Toast
                                                val isFav = favorites.contains(manga.id)
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isFav) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.60f),
                                                    border = BorderStroke(
                                                        0.8.dp,
                                                        if (isFav) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.35f)
                                                    ),
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(6.dp)
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .clickable {
                                                            val willBeFav = !isFav
                                                            onToggleFavorite(manga.id)
                                                            sideToastMessage = Pair(
                                                                if (willBeFav) "تمت الإضافة إلى المفضلة" else "تمت الإزالة من المفضلة",
                                                                willBeFav
                                                            )
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                            contentDescription = "المفضلة",
                                                            tint = if (isFav) MaterialTheme.colorScheme.primary else Color.White,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
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
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 13.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                val extraInfo = if (manga.genres.isNotEmpty()) " • ${manga.genres.first()}" else ""
                                                Text(
                                                    text = "${manga.totalChaptersCount} فصلاً$extraInfo",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    // LIST VIEW (Detailed rows) - Without work type labels, with aesthetic gradient border & small favorite button
                    items(displayedResults, key = { it.id }) { manga ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.2.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                )
                            ),
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

                                // Details column (Work type removed completely)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = manga.titleAr.ifEmpty { manga.titleEn },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = manga.synopsis,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        )
                                        if (manga.genres.isNotEmpty()) {
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                                            )
                                            Text(
                                                text = manga.genres.take(2).joinToString("، "),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                // Compact Favorite Icon Button with Side Toast
                                val isFav = favorites.contains(manga.id)
                                Surface(
                                    shape = CircleShape,
                                    color = if (isFav) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (isFav) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            val willBeFav = !isFav
                                            onToggleFavorite(manga.id)
                                            sideToastMessage = Pair(
                                                if (willBeFav) "تمت الإضافة إلى المفضلة" else "تمت الإزالة من المفضلة",
                                                willBeFav
                                            )
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "المفضلة",
                                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sleek side popup / toast on add & remove from favorites
        AnimatedVisibility(
            visible = sideToastMessage != null,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        ) {
            sideToastMessage?.let { (msg, isAdded) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAdded) Color(0xFF1B3022) else Color(0xFF331E23),
                    border = BorderStroke(
                        1.dp,
                        if (isAdded) Color(0xFF4CAF50).copy(alpha = 0.75f) else Color(0xFFE57373).copy(alpha = 0.75f)
                    ),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Icon(
                            imageVector = if (isAdded) Icons.Default.Favorite else Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = if (isAdded) Color(0xFF81C784) else Color(0xFFFF8A80),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.5.sp
                            )
                        )
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
    onUpdateReaderMode: (Int) -> Unit = {},
    onUpdateImageQuality: (Int) -> Unit = {},
    onUpdateKeepScreenOn: (Boolean) -> Unit = {},
    onUpdateWifiOnlyDownloads: (Boolean) -> Unit = {},
    onUpdateAutoSyncUpdates: (Boolean) -> Unit = {},
    onUpdateThemeMode: (Int) -> Unit = {},
    onUpdateBackgroundStyle: (Int) -> Unit = {},
    onUpdateAccentColor: (Int) -> Unit = {},
    onUpdateCardAnimationEnabled: (Boolean) -> Unit = {},
    onUpdatePreventChapterCache: (Boolean) -> Unit = {},
    onDeleteAllDownloads: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showClearDownloadsDialog by remember { mutableStateOf(false) }
    var cacheCleanedSuccess by remember { mutableStateOf(false) }
    var expandedDownloadsList by remember { mutableStateOf(false) }
    var showMoreAccentOptions by remember { mutableStateOf(false) }

    // Read real user settings from AppSettings
    val appSettings = uiState.appSettings
    val readerMode = appSettings.readerMode
    val imageQuality = appSettings.imageQuality
    val keepScreenOn = appSettings.keepScreenOn
    val wifiOnlyDownloads = appSettings.wifiOnlyDownloads
    val autoSyncUpdates = appSettings.autoSyncUpdates
    val themeMode = appSettings.themeMode
    val backgroundStyle = appSettings.backgroundStyle
    val accentColor = appSettings.accentColor
    val cardAnimationEnabled = appSettings.cardAnimationEnabled
    val preventChapterCache = appSettings.preventChapterCache

    val currentThemePreset = ThemePalettes.getPresetById(accentColor)
    val accentPrimary = currentThemePreset.primaryColor

    MaterialTheme(typography = HarmattanTypography) {
        CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = HarmattanFontFamily)) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("settings_tab_content"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        // =========================================================================
        // HEADER: App Identity Card (v1.9.3)
        // =========================================================================
        item(key = "settings_hero_header") {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(
                        listOf(accentPrimary.copy(alpha = 0.6f), MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = accentPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = accentPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "إعدادات التطبيق",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentPrimary.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = AppVersionConfig.getSettingsVersionLabel(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = accentPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "تخصيص كامل للمظهر، محرك القراءة، الذاكرة المؤقتة والتنزيلات",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SECTION 1: المظهر والثيمات والألوان (Themes & Appearance)
        // =========================================================================
        item(key = "section_theme_header") {
            SettingsSectionHeader(
                title = "المظهر والثيمات والألوان",
                icon = Icons.Default.Palette,
                accent = accentPrimary
            )
        }

        item(key = "section_theme_card") {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. الوضع العام (داكن / فاتح / حسب النظام)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "وضع الثيم العام",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf(
                                Triple(1, "داكن", Icons.Default.DarkMode),
                                Triple(2, "فاتح", Icons.Default.LightMode),
                                Triple(0, "حسب النظام", Icons.Default.BrightnessAuto)
                            )

                            modes.forEach { (modeId, modeTitle, modeIcon) ->
                                val isSelected = themeMode == modeId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) accentPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 0.5.dp,
                                        if (isSelected) accentPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onUpdateThemeMode(modeId) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = modeIcon,
                                            contentDescription = modeTitle,
                                            tint = if (isSelected) accentPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = modeTitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) accentPrimary else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 2. نمط لون الخلفية (أسود كامل AMOLED / الافتراضي / أبيض)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "نمط لون الخلفية",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "اختر درجة لون خلفية التطبيق المناسبة لشاشتك",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val bgOptions = listOf(
                                Triple(1, "أسود كامل (AMOLED)", Color(0xFF000000)),
                                Triple(0, "الافتراضي (داكن)", Color(0xFF101114)),
                                Triple(2, "أبيض ناصع", Color(0xFFFFFFFF))
                            )

                            bgOptions.forEach { (bgId, bgTitle, bgColor) ->
                                val isSelected = backgroundStyle == bgId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) accentPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 0.5.dp,
                                        if (isSelected) accentPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onUpdateBackgroundStyle(bgId) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = bgColor,
                                            border = BorderStroke(1.dp, if (bgId == 2) Color.LightGray else Color.Gray),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            if (isSelected) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = if (bgId == 2) Color.Black else Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = bgTitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) accentPrimary else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 10.sp
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 3. الألوان الأساسية الفاخرة (Solid Color Palettes)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الألوان الأساسية الفاخرة",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "لون أساسي موحد",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val solidPresets = ThemePalettes.SOLID_PRESETS.chunked(4)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            solidPresets.forEach { rowPresets ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowPresets.forEach { preset ->
                                        val isSelected = accentColor == preset.id
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) preset.primaryColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(
                                                if (isSelected) 1.6.dp else 0.5.dp,
                                                if (isSelected) preset.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { onUpdateAccentColor(preset.id) }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = preset.primaryColor,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    if (isSelected) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = if (preset.id == 4) Color.Black else Color.White,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = preset.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isSelected) preset.primaryColor else MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 10.sp
                                                    ),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 4. تدرجات الألوان التجميلية المتعددة (Multi-Color Gradient Presets)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "تدرجات الألوان التجميلية المتعددة",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "تدرجات ساحرة متدرجة ومتناغمة في كامل أرجاء التطبيق",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "✨ جديد 1.9.9",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val gradientPresets = ThemePalettes.GRADIENT_PRESETS.chunked(3)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            gradientPresets.forEach { rowPresets ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowPresets.forEach { preset ->
                                        val isSelected = accentColor == preset.id
                                        val gradBrush = Brush.linearGradient(preset.gradientColors)
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(
                                                if (isSelected) 2.dp else 0.8.dp,
                                                if (isSelected) gradBrush else Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)))
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { onUpdateAccentColor(preset.id) }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp, 22.dp)
                                                        .clip(RoundedCornerShape(11.dp))
                                                        .background(gradBrush),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = preset.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 10.sp
                                                    ),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 5. ميزة أنيميشن وتموجات الألوان لبطاقات أحدث عملين فقط (للثيمات متعددة الألوان)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (ThemePalettes.isMultiColorTheme(accentColor))
                                    accentPrimary.copy(alpha = 0.08f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (ThemePalettes.isMultiColorTheme(accentColor)) accentPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (ThemePalettes.isMultiColorTheme(accentColor)) accentPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "تموجات ألوان أحدث عملين",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (ThemePalettes.isMultiColorTheme(accentColor)) accentPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (ThemePalettes.isMultiColorTheme(accentColor)) "نشط للثيم الحالي" else "للثيمات المتعددة فقط",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (ThemePalettes.isMultiColorTheme(accentColor)) accentPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Text(
                                text = "تأثير تموجات ألوان حية ومتحركة على بطاقات أول عملين فقط في القائمة عند اختيار ثيم متعدد الألوان.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.5.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }

                        Switch(
                            checked = cardAnimationEnabled,
                            onCheckedChange = { onUpdateCardAnimationEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentPrimary
                            )
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SECTION 2: محرك القراءة وإيماءات اللمس (Reader & Gestures)
        // =========================================================================
        item(key = "section_reader_header") {
            SettingsSectionHeader(
                title = "محرك القراءة وإيماءات اللمس",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                accent = accentPrimary
            )
        }

        item(key = "section_reader_card") {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Feature Highlight: التكبير والتصغير بالسحب بإصبعين (Pinch to Zoom)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = accentPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accentPrimary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "التكبير والتصغير بإصبعين (Pinch-to-Zoom)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = BadgeSuccess
                                    ) {
                                        Text(
                                            text = "مفعّل ✓",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "اسحب بإصبعين للتكبير والتصغير بحرية مع إمكانية تحريك وتمرير الصفحة بسلاسة فائقة داخل أي فصل.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.5.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        }
                    }

                    // 1. نمط القراءة (ويب تون / أفقي يمين / أفقي يسار)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "اتجاه ونمط القراءة الافتراضي",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        val readerModes = listOf(
                            0 to "ويب تون عمودي مستمر",
                            1 to "أفقي (يمين لليسار RTL)",
                            2 to "أفقي (يسار ليمين LTR)"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            readerModes.forEach { (modeId, modeTitle) ->
                                val isSelected = readerMode == modeId
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) accentPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) accentPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onUpdateReaderMode(modeId) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = modeTitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
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

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 2. جودة تحميل صور الفصول
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "جودة صور الفصول",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        val qualities = listOf(
                            0 to "عالية الدقة HD",
                            1 to "متوازنة (الموصى بها)",
                            2 to "موفر البيانات"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            qualities.forEach { (qId, qTitle) ->
                                val isSelected = imageQuality == qId
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) accentPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) accentPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onUpdateImageQuality(qId) }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = qTitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
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

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 3. مفتاح إبقاء الشاشة مفعلة أثناء القراءة
                    SettingsSwitchRow(
                        title = "إبقاء الشاشة مفعلة أثناء القراءة",
                        subtitle = "منع إغلاق الشاشة تلقائياً أثناء تصفح فصول المانجا والمانهوا",
                        checked = keepScreenOn,
                        onCheckedChange = onUpdateKeepScreenOn,
                        accent = accentPrimary
                    )
                }
            }
        }

        // =========================================================================
        // SECTION 3: سياسة الكاش والتخزين (Cache & Storage Policy)
        // =========================================================================
        item(key = "section_cache_header") {
            SettingsSectionHeader(
                title = "التخزين والذاكرة المؤقتة (الكاش)",
                icon = Icons.Default.Storage,
                accent = accentPrimary
            )
        }

        item(key = "section_cache_card") {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // مساحة التخزين المستهلكة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مساحة التخزين المستهلكة",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "${uiState.downloadedChapters.size} فصول محملة محلياً للأوفلاين",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = uiState.formattedTotalStorage,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = accentPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = {
                            (uiState.downloadedChapters.size.toFloat() / 50f).coerceIn(0.04f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentPrimary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // 🚀 منع الفصول من التخزين في الكاش الثابت (User Request)
                    SettingsSwitchRow(
                        title = "منع إدخال الفصول في الكاش الثابت",
                        subtitle = "مجرد الخروج من الفصل يتم حذف وتفريغ كل صور الفصل تلقائياً من الذاكرة المؤقتة للحفاظ على سرعة الهاتف وتوفير المساحة",
                        checked = preventChapterCache,
                        onCheckedChange = onUpdatePreventChapterCache,
                        accent = accentPrimary
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // أزرار تنظيف الكاش وحذف التنزيلات
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
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentPrimary),
                            border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (cacheCleanedSuccess) Icons.Default.Check else Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (cacheCleanedSuccess) "تم التنظيف ✓" else "مسح الكاش الآن",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (uiState.downloadedChapters.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showClearDownloadsDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                                border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.6f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حذف كل التحميلات",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SECTION 4: التنزيلات والشبكة (Downloads & Network)
        // =========================================================================
        item(key = "section_downloads_header") {
            SettingsSectionHeader(
                title = "التنزيلات والشبكة",
                icon = Icons.Default.CloudDownload,
                accent = accentPrimary
            )
        }

        item(key = "section_downloads_card") {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SettingsSwitchRow(
                        title = "التحميل عبر الواي فاي فقط (Wi-Fi Only)",
                        subtitle = "توفير باقة بيانات الجوال ومنع تنزيل الفصول إلا عند الاتصال بشبكة واي فاي",
                        checked = wifiOnlyDownloads,
                        onCheckedChange = onUpdateWifiOnlyDownloads,
                        accent = accentPrimary
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // إدارة الفصول المحملة
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { expandedDownloadsList = !expandedDownloadsList }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "الفصول المحملة محلياً (${uiState.downloadedChapters.size})",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (uiState.downloadedChapters.isEmpty()) "لا توجد فصول محملة حالياً" else "انقر لعرض وتصفح الفصول",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = if (expandedDownloadsList) Icons.Default.SwapVert else Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (expandedDownloadsList && uiState.downloadedChapters.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.downloadedChapters.forEach { chapter ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = chapter.mangaTitle,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "الفصل ${chapter.chapterNumber} • ${chapter.formattedSize}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = accentPrimary,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                        }

                                        Button(
                                            onClick = { onReadChapter(chapter.mangaId, chapter.chapterNumber) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = accentPrimary,
                                                contentColor = Color.Black
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("اقرأ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        IconButton(
                                            onClick = { onDeleteDownload(chapter.mangaId, chapter.chapterNumber) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "حذف",
                                                tint = Color(0xFFEF5350),
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
        }

        // =========================================================================
        // SECTION 5: التحديثات السحابية (Updates & Version 1.9.3)
        // =========================================================================
        item(key = "section_updates_header") {
            SettingsSectionHeader(
                title = "تحديثات التطبيق والإصدار",
                icon = Icons.Default.AutoAwesome,
                accent = accentPrimary
            )
        }

        item(key = "section_updates_card") {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = AppVersionConfig.getCurrentVersionBadge(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (uiState.updateInfo.updateAvailable) "يتوفر تحديث جديد!" else "أنت تستخدم أحدث إصدار مستقر رسمي",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (uiState.updateInfo.updateAvailable) accentPrimary else BadgeSuccess,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Button(
                            onClick = onCheckCloudUpdates,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentPrimary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("فحص التحديثات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsSwitchRow(
                        title = "البحث التلقائي عن التحديثات السحابية",
                        subtitle = "فحص تلقائي للإصدارات الجديدة فور توفرها على GitHub Releases",
                        checked = autoSyncUpdates,
                        onCheckedChange = onUpdateAutoSyncUpdates,
                        accent = accentPrimary
                    )

                    // مميزات الإصدار الجديد Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = AppVersionConfig.getChangelogHeader(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = accentPrimary
                                )
                            )
                            AppVersionConfig.CURRENT_CHANGELOG_FEATURES.forEach { logItem ->
                                Text(
                                    text = "• $logItem",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SECTION 6: حول التطبيق (About)
        // =========================================================================
        item(key = "section_about_header") {
            SettingsSectionHeader(
                title = "حول التطبيق والمطورين",
                icon = Icons.Default.Info,
                accent = accentPrimary
            )
        }

        item(key = "section_about_card") {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Nexus Manga Reader",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "تطبيق قراءة المانجا والمانهوا الأول المصمم بأحدث تقنيات Jetpack Compose و Material 3 لتقديم تجربة قراءة فائقة السرعة، أداء سلس، ودعم كامل للقراءة بدون إنترنت.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = AppVersionConfig.getSettingsFullDetails(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = accentPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
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
                        onDeleteAllDownloads()
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
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Reusable Section Header for universal settings design
 */
@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = accent.copy(alpha = 0.15f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.5.sp
            )
        )
    }
}

/**
 * Reusable Switch Row for universal settings design
 */
@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = accent,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

