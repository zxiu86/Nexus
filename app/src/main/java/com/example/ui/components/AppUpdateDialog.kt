package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.example.util.InAppUpdateManager
import androidx.compose.ui.window.Dialog
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

/**
 * Redesigned, Modern Material 3 App Update Dialog.
 * Highlights release features for v1.8.3 with high visual polish,
 * feature pills, and complete absence of any external repository links.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateState,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(NexusGold, NexusOrange.copy(alpha = 0.8f), NexusGoldDark)
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("app_update_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Glowing Badge with Animated Theme
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(NexusGold.copy(alpha = 0.35f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = SurfaceVariantDark,
                        border = BorderStroke(1.2.dp, NexusGoldLight)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (updateInfo.updateAvailable) Icons.Default.RocketLaunch else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dialog Title
                Text(
                    text = if (updateInfo.updateAvailable) "تحديث جديد متوفر! 🚀" else "نكسوس مانجا • التحديثات ✨",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version Badge Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, SurfaceElevated),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    if (updateInfo.updateAvailable) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "الإصدار المثبت: v${updateInfo.currentVersion}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                            )
                            Text(text = "◄", color = NexusOrange, fontSize = 11.sp)
                            Text(
                                text = "الإصدار الجديد: v${updateInfo.latestVersion}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NexusGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "أحدث إصدار مثبت: v${updateInfo.currentVersion}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NexusGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val context = LocalContext.current

                // Quick Feature Badges
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuickBadge(text = "🚀 تثبيت سلس ومباشر")
                    Spacer(modifier = Modifier.width(4.dp))
                    QuickBadge(text = "⚡ سرعة فائقة بدون لاج")
                    Spacer(modifier = Modifier.width(4.dp))
                    QuickBadge(text = "📖 تصفح وقراءة انسيابية")
                    Spacer(modifier = Modifier.width(4.dp))
                    QuickBadge(text = "📶 قراءة بدون إنترنت")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Release notes title
                Text(
                    text = if (updateInfo.updateAvailable) "ما الجديد في التحديث:" else "مميزات الإصدار (v1.8.7):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NexusGoldLight,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Release notes container
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BackgroundDark,
                    border = BorderStroke(1.dp, SurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val notes = if (updateInfo.releaseNotes.isNotBlank()) {
                            updateInfo.releaseNotes
                        } else {
                            "• 🚀 حل مشكلة فك الحزمة: إتاحة تنزيل التحديث وتثبيته مباشرة وبسلاسة من داخل التطبيق بنقرة واحدة.\n" +
                                    "• ⚡ سرعة فائقة في فتح الفصول: تحسين شامل لسرعة تحميل وتصفح الصفحات بأعلى دقة.\n" +
                                    "• 🎨 واجهة مستخدم نقية: إزالة العناصر الزائدة والتركيز على قراءة أعمالك المفضلة.\n" +
                                    "• 📶 قراءة أوفلاين: تحميل الفصول مسبقاً وتصفحها بأي وقت بدون الحاجة للاتصال بالإنترنت.\n" +
                                    "• 📖 حفظ ومتابعة تلقائية: تتبع دقيق للفصول المقروءة واستئناف القراءة فوراً من حيث توقفت."
                        }
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                if (updateInfo.updateAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SurfaceElevated),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextTertiary)
                        ) {
                            Text("لاحقاً", fontSize = 13.sp)
                        }

                        Button(
                            onClick = onUpdateClick,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(46.dp)
                                .testTag("confirm_app_update_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexusGold,
                                contentColor = BackgroundDark
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text("تحديث الآن", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Direct Browser Download Fallback
                    TextButton(
                        onClick = {
                            InAppUpdateManager.openDownloadInBrowser(context, updateInfo.downloadUrl)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "أو التحميل المباشر عبر المتصفح ↗",
                            color = NexusGoldLight,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("close_update_dialog_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NexusGold,
                            contentColor = BackgroundDark
                        )
                    ) {
                        Text("حسناً، فهمت", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SurfaceVariantDark,
        border = BorderStroke(0.5.dp, NexusGold.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = NexusGoldLight,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
