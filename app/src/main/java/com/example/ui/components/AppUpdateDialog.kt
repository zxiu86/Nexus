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

                // Quick Feature Badges
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuickBadge(text = "🎨 خلفية كافر أسطورية")
                    Spacer(modifier = Modifier.width(4.dp))
                    QuickBadge(text = "🔄 تحديث تلقائي 30ث")
                    Spacer(modifier = Modifier.width(4.dp))
                    QuickBadge(text = "🎬 إعلانات بيانية سلسة")
                    Spacer(modifier = Modifier.width(4.dp))
                    QuickBadge(text = "🛡️ حدود إعلانية منضبطة")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Release notes title
                Text(
                    text = if (updateInfo.updateAvailable) "ما الجديد في التحديث:" else "مميزات الإصدار (v1.8.3):",
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
                            "• 🎨 خلفية كافر الأعمال الأسطورية: دمج خلفية هالة اللهب الكونية لمربعات الأعمال في الصفحة الرئيسية.\n" +
                                    "• 🔄 تحديث تلقائي للإعلانات (Auto-Refresh): تجديد هادئ كل 30 ثانية في جميع الصفحات بدون وميض.\n" +
                                    "• 🎬 إعلانات بيانية تلقائية: ظهور فوري منظم عند فتح أي فصل وعند دخول صفحة التفاصيل.\n" +
                                    "• 🛡️ معالجة شاملة لحدود الإعلانات: ضبط أبعاد ومحاذاة الإعلانات ومنع خروجها نهائياً عن حدود الشاشة.\n" +
                                    "• 🏷️ مؤشر إعلاني متقن: عبارة 'إعلان' مركزة ومريحة أسفل الإعلان مباشرة.\n" +
                                    "• ⚡ كاش ذكي فائق السرعة وتصفح انسيابي بدون لاج.\n" +
                                    "• 📶 قراءة أوفلاين وتنزيل مشفر مع حماية المحتوى."
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

                // Installation Advice Note
                if (updateInfo.updateAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NexusGoldDark.copy(alpha = 0.35f),
                        border = BorderStroke(0.6.dp, NexusGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "نصيحة: إذا ظهرت رسالة 'تعارض الحزم'، قم بحذف الإصدار القديم مرة واحدة وتثبيت التحديث لمطابقة شهادة التوقيع الرسمية.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NexusGoldLight,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (updateInfo.updateAvailable) {
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
