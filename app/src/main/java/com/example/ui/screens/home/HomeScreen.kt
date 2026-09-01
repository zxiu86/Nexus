package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chapter
import com.example.data.model.MangaItem
import com.example.data.model.MangaType
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.FavoritesPopupDialog
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
import com.example.ui.viewmodel.HomeUiState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onMangaClick: (String) -> Unit,
    onChapterClick: (String, Int) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onTabSelected: (Int) -> Unit = {},
    onDeleteHistoryItem: (String) -> Unit = {},
    onClearAllHistory: () -> Unit = {},
    onDeleteDownloadedChapter: (String, Int) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {},
    onTriggerUpdate: () -> Unit = {},
    onOpenUpdatesDialog: () -> Unit = {},
    onCheckCloudUpdates: () -> Unit = {},
    onDismissUpdateDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showFavoritesPopup by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Header Brand (Nexus only)
            NexusHomeTopBar(
                selectedTab = uiState.selectedTab,
                favoritesCount = uiState.favorites.size,
                hasUpdate = uiState.updateInfo.updateAvailable,
                onFavoritesClick = { onTabSelected(1) },
                onUpdateBadgeClick = onTriggerUpdate
            )

            // Dynamic Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (uiState.selectedTab) {
                    0 -> {
                        // Home Tab Content
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("home_screen_lazy_column"),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            // Hero Carousel (Top 5 Featured Works)
                            if (uiState.heroMangaList.isNotEmpty()) {
                                item {
                                    HeroCarouselSection(
                                        heroList = uiState.heroMangaList,
                                        favorites = uiState.favorites,
                                        onMangaClick = onMangaClick,
                                        onChapterClick = onChapterClick,
                                        onToggleFavorite = onToggleFavorite
                                    )
                                }
                            }

                            // Search & Filter Categories
                            item {
                                SearchAndFilterSection(
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = onSearchQueryChange,
                                    selectedCategory = uiState.selectedCategory,
                                    onCategorySelect = onCategorySelect
                                )
                            }

                            // Section Title: أحدث الفصول (Latest Chapters)
                            item {
                                SectionHeaderTitle(
                                    title = "أحدث الفصول المضافة",
                                    subtitle = "تحديثات مستمرة من مستودع البيانات السحابي"
                                )
                            }

                            // 2-Column Grid of Latest Works
                            val items = uiState.latestMangaGrid
                            val chunkedPairs = items.chunked(2)

                            items(
                                items = chunkedPairs,
                                key = { pair -> pair.joinToString("-") { it.id } }
                            ) { pair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    for (manga in pair) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            LatestMangaGridCard(
                                                manga = manga,
                                                isFavorite = uiState.favorites.contains(manga.id),
                                                onMangaClick = { onMangaClick(manga.id) },
                                                onChapterClick = { chNum -> onChapterClick(manga.id, chNum) },
                                                onToggleFavorite = { onToggleFavorite(manga.id) }
                                            )
                                        }
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Favorites Tab Content
                        FavoritesTabContent(
                            favoriteList = uiState.favoriteMangaList,
                            onMangaClick = onMangaClick,
                            onChapterClick = onChapterClick,
                            onToggleFavorite = onToggleFavorite,
                            onExploreHome = { onTabSelected(0) }
                        )
                    }
                    2 -> {
                        // History Tab Content
                        HistoryTabContent(
                            historyList = uiState.readingHistory,
                            onContinueReading = { mangaId, chNum -> onChapterClick(mangaId, chNum) },
                            onDeleteHistoryItem = onDeleteHistoryItem,
                            onClearAllHistory = onClearAllHistory,
                            onExploreHome = { onTabSelected(0) }
                        )
                    }
                    3 -> {
                        // Downloads Tab Content (Encrypted Offline Reading)
                        DownloadsTabContent(
                            downloadedList = uiState.downloadedChapters,
                            totalStorageFormatted = uiState.formattedTotalStorage,
                            onReadChapter = { mangaId, chNum -> onChapterClick(mangaId, chNum) },
                            onDeleteDownload = onDeleteDownloadedChapter,
                            onExploreHome = { onTabSelected(0) }
                        )
                    }
                    4 -> {
                        // Updates & Changelog Tab Content
                        UpdatesTabContent(
                            uiState = uiState,
                            onTriggerUpdate = onTriggerUpdate,
                            onCheckCloudUpdates = onCheckCloudUpdates,
                            onRefreshData = onRefresh
                        )
                    }
                }
            }
        }

        // Modern Bottom Navigation Footer Bar
        NexusBottomFooterBar(
            selectedTab = uiState.selectedTab,
            favoritesCount = uiState.favorites.size,
            downloadedCount = uiState.downloadedChapters.size,
            hasUpdate = uiState.updateInfo.updateAvailable,
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Favorites Popup Dialog
        if (showFavoritesPopup) {
            FavoritesPopupDialog(
                favoriteMangaList = uiState.favoriteMangaList,
                onMangaClick = onMangaClick,
                onToggleFavorite = onToggleFavorite,
                onDismiss = { showFavoritesPopup = false }
            )
        }

        // In-App Update Dialog Prompt
        if (uiState.showUpdateDialog) {
            AppUpdateDialog(
                updateInfo = uiState.updateInfo,
                onUpdateClick = onTriggerUpdate,
                onDismiss = onDismissUpdateDialog
            )
        }
    }
}

@Composable
fun NexusHomeTopBar(
    selectedTab: Int = 0,
    favoritesCount: Int = 0,
    hasUpdate: Boolean = false,
    onFavoritesClick: () -> Unit = {},
    onUpdateBadgeClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Brand & Name ONLY
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Brand Logo Box
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NexusGold, NexusOrange)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    fontWeight = FontWeight.Black,
                    color = BackgroundDark,
                    fontSize = 20.sp
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "NEXUS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = NexusGoldLight,
                            fontSize = 18.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NexusGoldDark,
                        border = BorderStroke(0.5.dp, NexusGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "v1.6.2",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NexusGold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Text(
                    text = when (selectedTab) {
                        1 -> "الأعمال المفضلة"
                        2 -> "سجل القراءة الذكي"
                        3 -> "التحميلات المشفرة"
                        4 -> "مركز التحديثات والمميزات"
                        else -> "بوابة المانهوا والمانغا السحابية"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (selectedTab == 0) TextSecondary else NexusGoldLight,
                        fontWeight = if (selectedTab == 0) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Left Section: Favorites Shortcut & Update Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Favorites Quick Access Button
            if (favoritesCount > 0) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NexusOrange.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, NexusOrangeLight.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onFavoritesClick() }
                        .testTag("topbar_favorites_shortcut")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = NexusOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$favoritesCount",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NexusOrangeLight,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            if (hasUpdate) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NexusGold,
                    border = BorderStroke(1.dp, NexusOrangeLight),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onUpdateBadgeClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "تحديث",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
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

@Composable
fun HeroCarouselSection(
    heroList: List<MangaItem>,
    favorites: Set<String>,
    onMangaClick: (String) -> Unit,
    onChapterClick: (String, Int) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { heroList.size })

    // Auto-advance hero carousel every 4.5 seconds
    LaunchedEffect(pagerState, heroList.size) {
        while (true) {
            delay(4500)
            if (heroList.isNotEmpty()) {
                val next = (pagerState.currentPage + 1) % heroList.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .testTag("hero_carousel_pager")
        ) { page ->
            val manga = heroList[page]
            val isFav = favorites.contains(manga.id)

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onMangaClick(manga.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Hero Image Backdrop supporting remote URL & fallback
                    NexusMangaImage(
                        imageUrl = manga.bannerUrl ?: manga.coverUrl,
                        fallbackRes = manga.bannerRes ?: manga.coverRes,
                        contentDescription = manga.titleAr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient Overlay for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent,
                                        BackgroundDark.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    // Top badges and Favorite button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (manga.type == MangaType.MANHWA) NexusGold else NexusOrange,
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = manga.type.labelAr,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BackgroundDark
                                )
                            )
                        }

                        IconButton(
                            onClick = { onToggleFavorite(manga.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .testTag("hero_fav_button_${manga.id}")
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "المفضلة",
                                tint = if (isFav) NexusOrange else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Bottom info overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${manga.rating}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NexusGold
                                )
                            )
                            Text(
                                text = "• ${manga.genres.take(2).joinToString(" ، ")}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = manga.titleAr,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ElevatedButton(
                                onClick = { onMangaClick(manga.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = NexusGold,
                                    contentColor = BackgroundDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "عرض التفاصيل",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val lastCh = manga.chapters.lastOrNull()
                            if (lastCh != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceDark.copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, NexusOrange.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clickable { onChapterClick(manga.id, lastCh.number) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = NexusOrangeLight,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "اقرأ الفصل ${lastCh.number}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NexusOrangeLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pager indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(heroList.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(5.dp)
                        .width(if (isSelected) 22.dp else 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) NexusGoldLight else SurfaceElevated)
                )
            }
        }
    }
}

@Composable
fun SearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    val categories = listOf("الكل", "أكشن", "خيال", "فنون قتال", "تناسخ", "سحر", "بوابات", "مانهوا كورية", "مانها صينية")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "ابحث عن مانهوا أو مانها المفضلة لديك...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextTertiary)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = NexusGoldLight
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "مسح",
                            tint = TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
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
                .testTag("search_text_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(categories, key = { it }) { category ->
                val isSelected = selectedCategory == category
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) NexusGold else SurfaceCard,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) NexusOrange else SurfaceElevated
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onCategorySelect(category) }
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BackgroundDark else TextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeaderTitle(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NexusGold)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextTertiary,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(start = 10.dp, top = 2.dp)
            )
        }
    }
}

/**
 * 2-Column Grid Card for Latest Works
 * Requirement:
 * - Cover image
 * - Above cover image: Name of work + Favorite button
 * - Under cover image: Last 3 chapters with a "NEW" badge next to each
 * - Clicking work image navigates to Details Page
 * - Clicking directly on a chapter navigates to Reader Page
 */
@Composable
fun LatestMangaGridCard(
    manga: MangaItem,
    isFavorite: Boolean,
    onMangaClick: () -> Unit,
    onChapterClick: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SurfaceElevated, RoundedCornerShape(14.dp))
            .testTag("manga_grid_card_${manga.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Above Image: Title & Favorite Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = manga.titleAr,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("fav_btn_${manga.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "المفضلة",
                        tint = if (isFavorite) NexusOrange else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Work Cover Image (Clickable -> Details Page)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onMangaClick() }
                    .testTag("cover_image_${manga.id}")
            ) {
                NexusMangaImage(
                    imageUrl = manga.coverUrl,
                    fallbackRes = manga.coverRes,
                    contentDescription = manga.titleAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Type badge on cover corner
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    color = if (manga.type == MangaType.MANHWA) NexusGold.copy(alpha = 0.95f) else NexusOrange.copy(alpha = 0.95f),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = if (manga.type == MangaType.MANHWA) "مانهوا" else "مانها",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BackgroundDark
                        )
                    )
                }

                // Rating overlay on bottom end
                Surface(
                    shape = RoundedCornerShape(topStart = 6.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
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
                            text = "${manga.rating}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Under Image: Last 3 Chapters with "NEW" badge next to each chapter
            val latest3 = manga.latestThreeChapters
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                latest3.forEach { chapter ->
                    LatestChapterItemRow(
                        chapter = chapter,
                        onChapterClick = { onChapterClick(chapter.number) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Chapter row under the manga cover with "NEW" badge
 */
@Composable
fun LatestChapterItemRow(
    chapter: Chapter,
    onChapterClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SurfaceVariantDark,
        border = BorderStroke(0.5.dp, SurfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onChapterClick() }
            .testTag("chapter_item_${chapter.mangaId}_${chapter.number}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "فصل ${chapter.number}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // "NEW" / "جديد" Radiant Badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = BadgeNew,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "NEW",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
            }
        }
    }
}
