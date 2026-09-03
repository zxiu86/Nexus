package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.NexusGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextTertiary

@Composable
fun NexusMangaImage(
    imageUrl: String?,
    fallbackRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Mystical dual-flame aura background for the work cover box
        Image(
            painter = painterResource(id = R.drawable.img_boxcover_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (!imageUrl.isNullOrBlank()) {
            val context = LocalContext.current
            val imageRequest = remember(imageUrl) {
                ImageRequest.Builder(context)
                    .data(imageUrl)
                    .memoryCacheKey(imageUrl)
                    .diskCacheKey(imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .crossfade(150)
                    .build()
            }

            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NexusGold,
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    if (fallbackRes != null && fallbackRes != 0) {
                        Image(
                            painter = painterResource(id = fallbackRes),
                            contentDescription = contentDescription,
                            contentScale = contentScale,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = NexusGold.copy(alpha = 0.8f),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            )
        } else if (fallbackRes != null && fallbackRes != 0) {
            Image(
                painter = painterResource(id = fallbackRes),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = NexusGold.copy(alpha = 0.8f),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
