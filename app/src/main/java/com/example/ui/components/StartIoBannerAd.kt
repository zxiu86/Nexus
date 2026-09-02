package com.example.ui.components

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusOrange
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener

/**
 * Start.io Fixed Banner Ad Component for Jetpack Compose.
 * Displays a fixed banner ad (App ID: 208548380).
 *
 * Guarantees:
 * 1. Perfectly centered horizontally & vertically.
 * 2. Strict boundary clipping (`clipToBounds()`) to prevent any ad pixels leaking outside.
 * 3. Compact 15dp-friendly vertical profile for Reader continuous scrolling.
 * 4. Header-friendly styling for sticky Home top banner.
 */
@Composable
fun StartIoBannerAd(
    modifier: Modifier = Modifier,
    adTag: String = "banner",
    isInlineReader: Boolean = false,
    isHeaderSticky: Boolean = false
) {
    var isAdLoaded by remember { mutableStateOf(false) }

    val verticalPadding = if (isInlineReader) 8.dp else if (isHeaderSticky) 2.dp else 6.dp
    val horizontalPadding = if (isInlineReader) 4.dp else 8.dp
    val cornerRadius = if (isInlineReader) 8.dp else 10.dp

    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = if (isInlineReader) BackgroundDark else SurfaceDark,
        border = BorderStroke(
            if (isInlineReader) 0.5.dp else 1.dp,
            if (isInlineReader) SurfaceElevated.copy(alpha = 0.5f) else SurfaceElevated
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .clip(RoundedCornerShape(cornerRadius))
            .clipToBounds()
            .testTag("startio_banner_ad_$adTag")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isInlineReader) 3.dp else 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sleek ad disclaimer pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SurfaceVariantDark,
                    border = BorderStroke(0.5.dp, NexusGold.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "إعلان • Start.io",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextTertiary,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }

            // Fixed Banner View embedded via AndroidView with strictly centered layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clipToBounds(),
                    factory = { ctx ->
                        val frameLayout = FrameLayout(ctx).apply {
                            clipChildren = true
                            clipToPadding = true
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }

                        try {
                            val startAppBanner = Banner(ctx, object : BannerListener {
                                override fun onReceiveAd(bannerView: View?) {
                                    isAdLoaded = true
                                }

                                override fun onFailedToReceiveAd(bannerView: View?) {
                                    isAdLoaded = false
                                }

                                override fun onClick(bannerView: View?) {}
                                override fun onImpression(bannerView: View?) {}
                            })

                            val layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.CENTER
                            }

                            frameLayout.addView(startAppBanner, layoutParams)
                        } catch (e: Exception) {
                            // Non-blocking graceful fallback
                        }

                        frameLayout
                    }
                )
            }
        }
    }
}
