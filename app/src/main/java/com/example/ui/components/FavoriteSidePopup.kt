package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FavoriteToastData
import kotlinx.coroutines.delay

/**
 * Animated Side Popup Toast shown when a Manga is added to or removed from Favorites.
 * Slides in from the edge with cover art thumbnail, title, and theme accent colors.
 */
@Composable
fun FavoriteSidePopup(
    toastData: FavoriteToastData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentPrimary = MaterialTheme.colorScheme.primary
    val accentSecondary = MaterialTheme.colorScheme.secondary

    // Auto-dismiss after 2.8 seconds
    LaunchedEffect(toastData?.timestamp) {
        if (toastData != null) {
            delay(2800)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 12.dp, end = 12.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        AnimatedVisibility(
            visible = toastData != null,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 380f)
            ) + fadeIn(),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)
            ) + fadeOut()
        ) {
            if (toastData != null) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                    ),
                    border = BorderStroke(
                        1.2.dp,
                        Brush.horizontalGradient(
                            listOf(
                                accentPrimary,
                                accentSecondary.copy(alpha = 0.6f),
                                accentPrimary.copy(alpha = 0.3f)
                            )
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    modifier = Modifier
                        .width(310.dp)
                        .shadow(12.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onDismiss() }
                        .testTag("favorite_side_popup_toast")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Thumbnail Manga Image
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, accentPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .size(width = 44.dp, height = 58.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            NexusMangaImage(
                                imageUrl = toastData.coverUrl,
                                fallbackRes = toastData.coverRes,
                                contentDescription = toastData.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Info Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Status Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (toastData.isAdded) {
                                        Color(0xFFE53935).copy(alpha = 0.2f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (toastData.isAdded) Color(0xFFE53935).copy(alpha = 0.6f) else accentPrimary.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (toastData.isAdded) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (toastData.isAdded) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (toastData.isAdded) "أُضيفت إلى المفضلة" else "أُزيلت من المفضلة",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (toastData.isAdded) accentPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // Manga Title
                            Text(
                                text = toastData.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Dismiss button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
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
