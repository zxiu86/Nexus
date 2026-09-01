package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUpdateState
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusGoldDark
import com.example.ui.theme.NexusGoldLight
import com.example.ui.theme.NexusOrange
import com.example.ui.theme.NexusOrangeLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusHamburgerMenuSheet(
    favoritesCount: Int,
    updateInfo: AppUpdateState,
    onOpenFavorites: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    onOpenUpdates: () -> Unit,
    onCheckCloudUpdates: () -> Unit,
    onRefreshData: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        contentColor = TextPrimary,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 44.dp, height = 4.dp),
                shape = CircleShape,
                color = SurfaceElevated
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .testTag("hamburger_menu_sheet")
        ) {
            // Header: Brand & App Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                        fontSize = 24.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "NEXUS MANGA",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = NexusGoldLight,
                                fontSize = 18.sp
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NexusGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, NexusGoldLight.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "v${updateInfo.currentVersion}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NexusGoldLight,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = "مستودع السحابة: zxiu86/Nexus (متصل)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = SurfaceElevated, thickness = 1.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Item 1: قائمة المفضلة (Favorites)
            HamburgerMenuItem(
                icon = Icons.Default.Favorite,
                iconColor = NexusOrange,
                iconBgColor = NexusOrange.copy(alpha = 0.15f),
                title = "قائمة المفضلة",
                subtitle = "عرض قائمة الأعمال المحفوظة والمميزة",
                badgeText = if (favoritesCount > 0) "$favoritesCount أعمال" else null,
                badgeColor = NexusGold,
                onClick = {
                    onDismiss()
                    onOpenFavorites()
                },
                testTag = "menu_item_favorites"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Item 2: سجل القراءة الذكي (History)
            HamburgerMenuItem(
                icon = Icons.Default.CloudDone,
                iconColor = NexusGoldLight,
                iconBgColor = NexusGoldLight.copy(alpha = 0.15f),
                title = "سجل القراءة ومتابعة الفصول",
                subtitle = "الوصول لآخر الفصول والصفحات المقروءة",
                onClick = {
                    onDismiss()
                    onOpenHistory()
                },
                testTag = "menu_item_history"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Item 3: التحميلات والقراءة بدون إنترنت (Downloads)
            HamburgerMenuItem(
                icon = Icons.Default.CloudSync,
                iconColor = NexusGold,
                iconBgColor = NexusGold.copy(alpha = 0.15f),
                title = "التحميلات والقراءة بدون إنترنت 🔒",
                subtitle = "إدارة الفصول المحملة مشفرة داخل التطبيق",
                onClick = {
                    onDismiss()
                    onOpenDownloads()
                },
                testTag = "menu_item_downloads"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Item 4: آخر التحديثات (Latest Updates & Features)
            HamburgerMenuItem(
                icon = Icons.Default.AutoAwesome,
                iconColor = NexusGold,
                iconBgColor = NexusGold.copy(alpha = 0.15f),
                title = "مركز التحديثات ومميزات v1.6.1",
                subtitle = "شرح كل الميزات الجديدة وتنزيل الـ APK",
                badgeText = if (updateInfo.updateAvailable) "تحديث متوفر! 🚀" else "v1.6.1 جديد",
                badgeColor = if (updateInfo.updateAvailable) NexusOrange else NexusGoldLight,
                onClick = {
                    onDismiss()
                    onOpenUpdates()
                },
                testTag = "menu_item_updates"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Item 3: فحص التحديثات الديناميكي من مستودع zxiu86/Nexus
            HamburgerMenuItem(
                icon = Icons.Default.CloudSync,
                iconColor = NexusGoldLight,
                iconBgColor = NexusGoldLight.copy(alpha = 0.15f),
                title = "فحص التحديثات السحابية",
                subtitle = "تحقق فوري من مستودع zxiu86/Nexus وتنزيل الـ APK",
                badgeText = "GitHub",
                badgeColor = SurfaceVariantDark,
                onClick = {
                    onDismiss()
                    onCheckCloudUpdates()
                },
                testTag = "menu_item_check_cloud"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Item 4: تحديث البيانات (Refresh / Sync)
            HamburgerMenuItem(
                icon = Icons.Default.Refresh,
                iconColor = TextSecondary,
                iconBgColor = SurfaceVariantDark,
                title = "مزامنة البيانات وتحديث الفصول",
                subtitle = "إعادة جلب أحدث المانهوا والبيانات السحابية",
                onClick = {
                    onDismiss()
                    onRefreshData()
                },
                testTag = "menu_item_refresh"
            )
        }
    }
}

@Composable
private fun HamburgerMenuItem(
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = NexusGold,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, SurfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    )

                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BackgroundDark,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
