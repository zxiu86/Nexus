package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ReportCategory
import com.example.data.model.ReportSubCategory

private val BackgroundDark = Color(0xFF0F1115)
private val SurfaceElevated = Color(0xFF181B20)
private val SurfaceCard = Color(0xFF22262E)
private val NexusGold = Color(0xFFF59E0B)
private val NexusOrange = Color(0xFFEA580C)
private val TextPrimary = Color(0xFFF1F5F9)
private val TextSecondary = Color(0xFF94A3B8)
private val SuccessGreen = Color(0xFF10B981)
private val ErrorRed = Color(0xFFEF4444)

@Composable
fun SubmitReportDialog(
    isOpen: Boolean,
    initialTargetTitle: String = "",
    initialChapterNumber: String = "",
    onDismiss: () -> Unit,
    onSubmit: (category: ReportCategory, subCategory: ReportSubCategory, title: String, chapter: String, details: String) -> Unit
) {
    if (!isOpen) return

    var selectedCategory by remember { mutableStateOf(ReportCategory.REQUEST) }
    var selectedSubCategory by remember { mutableStateOf(ReportSubCategory.ADD_FEATURE) }
    var targetTitle by remember(initialTargetTitle) { mutableStateOf(initialTargetTitle) }
    var chapterNumber by remember(initialChapterNumber) { mutableStateOf(initialChapterNumber) }
    var detailsText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isSubmittedSuccess by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (!isSubmittedSuccess) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundDark),
            border = BorderStroke(1.2.dp, NexusGold.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .background(SurfaceElevated, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "مركز البلاغات والطلبات",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 17.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NexusGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddComment,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isSubmittedSuccess) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreen.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = "تم حفظ البلاغ في الذاكرة المؤقتة بنجاح!",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "تم تخزين البلاغ محلياً وسيرسل تلقائياً بعد 30 دقيقة إلى السيرفر الرئيسي (github/zxiu86/Data) وقاعدة البيانات. ستتلقى إشعاراً فور مراجعة المشرف لبلاغك.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                isSubmittedSuccess = false
                                onDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NexusGold)
                        ) {
                            Text(
                                text = "حسناً، فهمت",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Category Selector Tabs (طلب vs مشاكل)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceElevated, RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val isRequest = selectedCategory == ReportCategory.REQUEST
                        val requestBg = if (isRequest) Modifier.background(Brush.horizontalGradient(listOf(NexusGold, NexusOrange))) else Modifier

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .then(requestBg)
                                .clickable {
                                    selectedCategory = ReportCategory.REQUEST
                                    selectedSubCategory = ReportSubCategory.ADD_FEATURE
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isRequest) Color.Black else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "طلب ميزة أو عمل",
                                    fontWeight = if (isRequest) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isRequest) Color.Black else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        val isIssue = selectedCategory == ReportCategory.ISSUE
                        val issueBg = if (isIssue) Modifier.background(Color(0xFFEF4444)) else Modifier

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .then(issueBg)
                                .clickable {
                                    selectedCategory = ReportCategory.ISSUE
                                    selectedSubCategory = ReportSubCategory.BROKEN_FEATURE
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    tint = if (isIssue) Color.White else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "إبلاغ عن مشاكل",
                                    fontWeight = if (isIssue) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isIssue) Color.White else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sub-category Options
                    Text(
                        text = if (selectedCategory == ReportCategory.REQUEST) "اختر نوع الطلب:" else "اختر نوع المشكلة:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        if (selectedCategory == ReportCategory.REQUEST) {
                            FilterChip(
                                selected = selectedSubCategory == ReportSubCategory.ADD_WORK,
                                onClick = { selectedSubCategory = ReportSubCategory.ADD_WORK },
                                label = { Text("• إضافة عمل") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NexusGold.copy(alpha = 0.25f),
                                    selectedLabelColor = NexusGold
                                )
                            )
                            FilterChip(
                                selected = selectedSubCategory == ReportSubCategory.ADD_FEATURE,
                                onClick = { selectedSubCategory = ReportSubCategory.ADD_FEATURE },
                                label = { Text("• إضافة ميزة") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NexusGold.copy(alpha = 0.25f),
                                    selectedLabelColor = NexusGold
                                )
                            )
                        } else {
                            FilterChip(
                                selected = selectedSubCategory == ReportSubCategory.BROKEN_CHAPTER,
                                onClick = { selectedSubCategory = ReportSubCategory.BROKEN_CHAPTER },
                                label = { Text("• فصول لا تعمل") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ErrorRed.copy(alpha = 0.25f),
                                    selectedLabelColor = ErrorRed
                                )
                            )
                            FilterChip(
                                selected = selectedSubCategory == ReportSubCategory.BROKEN_FEATURE,
                                onClick = { selectedSubCategory = ReportSubCategory.BROKEN_FEATURE },
                                label = { Text("• ميزات لا تعمل") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ErrorRed.copy(alpha = 0.25f),
                                    selectedLabelColor = ErrorRed
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Optional Title / Chapter Fields if relevant
                    if (selectedSubCategory == ReportSubCategory.ADD_WORK || selectedSubCategory == ReportSubCategory.BROKEN_CHAPTER) {
                        OutlinedTextField(
                            value = targetTitle,
                            onValueChange = { targetTitle = it },
                            label = { Text("اسم العمل / المانهوا") },
                            placeholder = { Text("مثال: Solo Leveling") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NexusGold,
                                unfocusedBorderColor = SurfaceCard,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (selectedSubCategory == ReportSubCategory.BROKEN_CHAPTER) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = chapterNumber,
                                onValueChange = { chapterNumber = it },
                                label = { Text("رقم الفصل المتأثر") },
                                placeholder = { Text("مثال: 145 أو الفصول من 10 إلى 15") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NexusGold,
                                    unfocusedBorderColor = SurfaceCard,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Details multi-line text input
                    Text(
                        text = if (selectedCategory == ReportCategory.REQUEST) "تفاصيل عن الطلب:" else "تفاصيل عن المشكلة:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = detailsText,
                        onValueChange = {
                            detailsText = it
                            validationError = null
                        },
                        placeholder = {
                            Text(
                                if (selectedCategory == ReportCategory.REQUEST)
                                    "اكتب هنا وصفاً دقيقاً للميزة أو العمل الذي ترغب في إضافته..."
                                else
                                    "صف المشكلة التي واجهتها بالتفصيل لكي يتمكن المشرف من حلها..."
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp),
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexusGold,
                            unfocusedBorderColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = validationError ?: "",
                            color = ErrorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice Banner about 30-minute temp cache
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceElevated,
                        border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "يخزن البلاغ في كاش مؤقت عند المستخدم وبعد 30 دقيقة يتم رفعه تلقائياً إلى السيرفر (github/zxiu86/Data)، وعند مراجعة المشرف ستتلقى إشعاراً جانبياً بنتيجة الموافقة أو الرفض.",
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = NexusGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (detailsText.isBlank()) {
                                validationError = "يرجى كتابة تفاصيل البلاغ أو الطلب"
                                return@Button
                            }
                            onSubmit(
                                selectedCategory,
                                selectedSubCategory,
                                targetTitle,
                                chapterNumber,
                                detailsText
                            )
                            isSubmittedSuccess = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategory == ReportCategory.REQUEST) NexusGold else Color(0xFFEF4444)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = if (selectedCategory == ReportCategory.REQUEST) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "حفظ وإرسال البلاغ",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCategory == ReportCategory.REQUEST) Color.Black else Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
