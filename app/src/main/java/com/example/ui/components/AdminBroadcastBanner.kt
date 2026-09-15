package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminAnnouncement
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdminBroadcastBanner(
    announcement: AdminAnnouncement?,
    modifier: Modifier = Modifier,
    onDismissLocally: () -> Unit = {}
) {
    if (announcement == null || !announcement.active || announcement.title.isBlank()) return

    var isLocallyDismissed by remember(announcement.id + announcement.timestamp) { mutableStateOf(false) }
    if (isLocallyDismissed) return

    val (bannerColor, iconVector, badgeTitle) = when (announcement.priority) {
        "urgent" -> Triple(
            Color(0xFFEF4444),
            Icons.Default.Warning,
            "تنبيه عاجل من الإدارة"
        )
        "warning" -> Triple(
            Color(0xFFF59E0B),
            Icons.Default.Warning,
            "تنبيه مهم"
        )
        "update" -> Triple(
            Color(0xFF3B82F6),
            Icons.Default.NewReleases,
            "تحديث جديد من الإدارة"
        )
        else -> Triple(
            NexusGold,
            Icons.Default.Campaign,
            "إعلان رسمي"
        )
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bannerColor.copy(alpha = 0.12f),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        bannerColor.copy(alpha = 0.6f),
                        NexusOrange.copy(alpha = 0.3f)
                    )
                )
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = bannerColor,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = bannerColor.copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = badgeTitle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = bannerColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = announcement.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (announcement.message.isNotBlank()) {
                        Text(
                            text = announcement.message,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        isLocallyDismissed = true
                        onDismissLocally()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق التنبيه",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
