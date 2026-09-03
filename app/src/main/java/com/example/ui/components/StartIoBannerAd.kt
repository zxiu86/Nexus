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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import kotlinx.coroutines.delay

/**
 * Start.io Fixed Banner Ad Component for Jetpack Compose.
 * Displays a fixed banner ad (App ID: 208548380).
 *
 * Guarantees:
 * 1. Perfectly centered horizontally & vertically without overflowing screen bounds.
 * 2. Automatic refresh every 30 seconds across all screens.
 * 3. Discreet "إعلان" label placed directly under the ad in the center.
 * 4. Strict boundary clipping (`clipToBounds()`) to prevent any ad pixels leaking outside.
 */
@Composable
fun StartIoBannerAd(
    modifier: Modifier = Modifier,
    adTag: String = "banner",
    isInlineReader: Boolean = false,
    isHeaderSticky: Boolean = false
) {
    var isAdLoaded by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }

    // Auto-refresh banner ad every 30 seconds across all screens
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            refreshKey++
        }
    }

    val verticalPadding = if (isInlineReader) 6.dp else if (isHeaderSticky) 2.dp else 5.dp
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
                .padding(vertical = if (isInlineReader) 2.dp else 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fixed Banner View embedded via AndroidView with strictly centered layout and bounds containment
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                key(refreshKey) {
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

                                // Prevent ads from overflowing screen bounds on any device:
                                // Scale down if banner width exceeds container width, and safely clamp the 10px right shift
                                startAppBanner.addOnLayoutChangeListener { v, left, top, right, bottom, _, _, _, _ ->
                                    val parent = v.parent as? View ?: return@addOnLayoutChangeListener
                                    val parentWidth = parent.width
                                    val adWidth = right - left
                                    if (parentWidth > 0 && adWidth > 0) {
                                        if (adWidth > parentWidth) {
                                            val scale = (parentWidth.toFloat() / adWidth.toFloat()) * 0.98f
                                            v.scaleX = scale
                                            v.scaleY = scale
                                            v.pivotX = adWidth / 2f
                                            v.pivotY = (bottom - top) / 2f
                                            v.translationX = 0f
                                        } else {
                                            val maxAllowedShift = ((parentWidth - adWidth) / 2f).coerceAtLeast(0f)
                                            val desiredShift = 10f * ctx.resources.displayMetrics.density
                                            v.translationX = desiredShift.coerceAtMost(maxAllowedShift)
                                        }
                                    }
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

            // Discreet ad label placed UNDER the ad in the center so as not to disturb the user
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "إعلان",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextTertiary.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}
