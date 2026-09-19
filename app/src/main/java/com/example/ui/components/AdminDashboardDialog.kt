package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AdminAnnouncement
import com.example.data.model.NexusUser
import com.example.data.model.UserReport
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusOrange
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.util.AppVersionConfig
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun AdminDashboardDialog(
    isOpen: Boolean,
    currentUser: NexusUser?,
    activeAnnouncement: AdminAnnouncement?,
    totalMangaCount: Int,
    isCloudSyncing: Boolean,
    incomingReports: List<UserReport> = emptyList(),
    onDismiss: () -> Unit,
    onPostAnnouncement: (title: String, message: String, priority: String) -> Unit,
    onDismissAnnouncement: () -> Unit,
    onTriggerManualSync: () -> Unit,
    onApproveReport: (reportId: String, note: String) -> Unit = { _, _ -> },
    onRejectReport: (reportId: String, note: String) -> Unit = { _, _ -> },
    onTestGitHubConnection: (token: String, owner: String, repo: String) -> Unit = { _, _, _ -> },
    onSaveGitHubCredentials: (token: String, owner: String, repo: String, branch: String) -> Unit = { _, _, _, _ -> },
    onForceSyncAllToGitHub: () -> Unit = {},
    gitHubTestResult: com.example.data.network.GitHubConnectionTestResult? = null,
    isTestingGitHub: Boolean = false,
    syncStatusMessage: String? = null
) {
    if (!isOpen || currentUser == null || !currentUser.isAdmin) return

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    var selectedReportsFilter by remember { mutableStateOf("ALL") }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("info") }
    var isPosting by remember { mutableStateOf(false) }

    // GitHub Settings State
    var ghToken by remember { mutableStateOf(com.example.data.network.GitHubNetworkModule.getCustomToken().ifEmpty { com.example.data.network.GitHubNetworkModule.getActiveToken() }) }
    var ghOwner by remember { mutableStateOf(com.example.data.network.GitHubNetworkModule.getConfiguredOwner()) }
    var ghRepo by remember { mutableStateOf(com.example.data.network.GitHubNetworkModule.getConfiguredRepo()) }
    var ghBranch by remember { mutableStateOf(com.example.data.network.GitHubNetworkModule.getConfiguredBranch()) }
    var isTokenVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val timeFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("admin_dashboard_card"),
            colors = CardDefaults.cardColors(
                containerColor = BackgroundDark.copy(alpha = 0.98f)
            ),
            border = BorderStroke(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(
                        NexusGold,
                        Color(0xFFE5A93C),
                        NexusOrange.copy(alpha = 0.4f)
                    )
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Admin Badge & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
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
                        Surface(
                            shape = CircleShape,
                            color = NexusGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, NexusGold),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = NexusGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "لوحة تحكم المشرف (Admin)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "الإصدار ${AppVersionConfig.VERSION_NAME} | محمي بالحساب المعتمد",
                                fontSize = 10.sp,
                                color = NexusGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verified Personal Account Indicator (Protection Banner)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تم التحقق الأمني من هوية المشرف",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )
                            Text(
                                text = "الحساب: ${currentUser.email}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Admin Tabs: 0: Overview, 1: Incoming Reports, 2: Broadcast Alerts, 3: GitHub Sync, 4: Security & Rules
                val pendingCount = incomingReports.count { it.isPending() }
                val tabs = listOf(
                    "الإحصائيات",
                    if (pendingCount > 0) "البلاغات ($pendingCount)" else "البلاغات",
                    "بث تنبيه",
                    "ربط GitHub",
                    "الأمان"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tabs.forEachIndexed { index, label ->
                        val tabBackgroundModifier = if (selectedAdminTab == index) {
                            Modifier.background(Brush.horizontalGradient(listOf(NexusGold, NexusOrange)))
                        } else {
                            Modifier
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .then(tabBackgroundModifier)
                                .clickable { selectedAdminTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAdminTab == index) Color.Black else TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedAdminTab) {
                    // TAB 0: Stats & Overview
                    0 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AdminStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "الأعمال المتاحة",
                                    value = "$totalMangaCount عمل",
                                    icon = Icons.Default.Star,
                                    iconTint = NexusGold
                                )
                                AdminStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "حالة Firebase",
                                    value = "سحابي متصل",
                                    icon = Icons.Default.CheckCircle,
                                    iconTint = Color(0xFF10B981)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AdminStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "مزامنة الأدمن",
                                    value = if (isCloudSyncing) "جارِ التزامن..." else "متزامن بنجاح",
                                    icon = Icons.Default.Refresh,
                                    iconTint = NexusOrange
                                )
                                AdminStatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "رتبة الحساب",
                                    value = "أدمن موثق",
                                    icon = Icons.Default.Shield,
                                    iconTint = Color(0xFF8B5CF6)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Action button: Manual Full Cloud Sync
                            OutlinedButton(
                                onClick = onTriggerManualSync,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = NexusGold
                                ),
                                border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.5f)),
                                enabled = !isCloudSyncing
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isCloudSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = NexusGold,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("فرض مزامنة سحابية شاملة الآن", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // TAB 1: Incoming Reports (البلاغات والطلبات)
                    1 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "البلاغات والطلبات الواردة من المستخدمين",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "عند الضغط على موافقة أو رفض، يتم إشعار المستخدم فوراً عبر إشعار جانبي وبوب منبثق في جهازه.",
                                fontSize = 11.5.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            // Filter Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
                            ) {
                                FilterChip(
                                    selected = selectedReportsFilter == "ALL",
                                    onClick = { selectedReportsFilter = "ALL" },
                                    label = { Text("الكل (${incomingReports.size})") }
                                )
                                FilterChip(
                                    selected = selectedReportsFilter == "PENDING",
                                    onClick = { selectedReportsFilter = "PENDING" },
                                    label = { Text("معلقة (${incomingReports.count { it.isPending() }})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NexusGold.copy(alpha = 0.25f),
                                        selectedLabelColor = NexusGold
                                    )
                                )
                                FilterChip(
                                    selected = selectedReportsFilter == "APPROVED",
                                    onClick = { selectedReportsFilter = "APPROVED" },
                                    label = { Text("مقبولة") }
                                )
                                FilterChip(
                                    selected = selectedReportsFilter == "REJECTED",
                                    onClick = { selectedReportsFilter = "REJECTED" },
                                    label = { Text("مرفوضة") }
                                )
                            }

                            val filtered = when (selectedReportsFilter) {
                                "PENDING" -> incomingReports.filter { it.isPending() }
                                "APPROVED" -> incomingReports.filter { it.isApproved() }
                                "REJECTED" -> incomingReports.filter { it.isRejected() }
                                else -> incomingReports
                            }

                            if (filtered.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SurfaceElevated,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = NexusGold.copy(alpha = 0.5f),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "لا توجد أي بلاغات في هذا القسم",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                filtered.forEach { report ->
                                    AdminReportCard(
                                        report = report,
                                        timeFormat = timeFormat,
                                        onApprove = { onApproveReport(report.id, "تمت الموافقة من قِبل المشرف") },
                                        onReject = { onRejectReport(report.id, "للأسف تم رفض البلاغ") }
                                    )
                                }
                            }
                        }
                    }

                    // TAB 2: Broadcast Alerts
                    2 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "بث إعلان / تنبيه عاجل لجميع المستخدمين",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "سيظهر هذا التنبيه أعلى الشاشة الرئيسية لجميع مستخدمي التطبيق عبر السحابة.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            OutlinedTextField(
                                value = announcementTitle,
                                onValueChange = { announcementTitle = it },
                                label = { Text("عنوان التنبيه (مثال: تحديث فصول جديدة)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NexusGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = NexusGold,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = NexusGold
                                )
                            )

                            OutlinedTextField(
                                value = announcementMessage,
                                onValueChange = { announcementMessage = it },
                                label = { Text("نص الرسالة أو الإعلان للمستخدمين") },
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NexusGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = NexusGold,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = NexusGold
                                )
                            )

                            // Priority Selector
                            Text(
                                text = "نوع التنبيه والأولوية:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val priorities = listOf(
                                    "info" to "معلومات",
                                    "update" to "تحديث",
                                    "warning" to "تنبيه",
                                    "urgent" to "عاجل"
                                )
                                priorities.forEach { (key, name) ->
                                    val isSel = selectedPriority == key
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSel) when (key) {
                                                    "urgent" -> Color(0xFFEF4444)
                                                    "warning" -> Color(0xFFF59E0B)
                                                    "update" -> Color(0xFF3B82F6)
                                                    else -> NexusGold
                                                } else SurfaceElevated
                                            )
                                            .clickable { selectedPriority = key }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) Color.Black else TextSecondary
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (announcementTitle.isNotBlank() && announcementMessage.isNotBlank()) {
                                        isPosting = true
                                        onPostAnnouncement(announcementTitle, announcementMessage, selectedPriority)
                                        announcementTitle = ""
                                        announcementMessage = ""
                                        isPosting = false
                                        Toast.makeText(context, "تم نشر الإعلان بنجاح عبر السحابة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NexusGold),
                                enabled = !isPosting && announcementTitle.isNotBlank() && announcementMessage.isNotBlank()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("نشر الإعلان السحابي", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            // Current Active Announcement Card (if any)
                            if (activeAnnouncement != null && activeAnnouncement.active) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SurfaceElevated,
                                    border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.NotificationsActive,
                                                    contentDescription = null,
                                                    tint = NexusGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "الإعلان النشط حالياً",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NexusGold
                                                )
                                            }
                                            IconButton(
                                                onClick = onDismissAnnouncement,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف الإعلان",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = activeAnnouncement.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = activeAnnouncement.message,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 3: GitHub Cloud Sync & Token Settings
                    3 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = NexusGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ربط مستودع GitHub والمزامنة السحابية",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Text(
                                text = "المسار الهدف: https://github.com/zxiu86/Data/tree/main\nيتم حفظ بيانات المستخدمين تلقائياً في user/user.json والبلاغات في data/reports.json.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            // Current Configuration Summary Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceElevated,
                                border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "المستودع المستهدف: $ghOwner / $ghRepo ($ghBranch)",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NexusGold
                                    )
                                    val tokenStatus = if (ghToken.isNotBlank()) "🟢 الرمز موجود في التطبيق" else "🔴 الرمز غير مضبوط (يتطلب رمزاً بحسابك)"
                                    Text(
                                        text = "حالة الرمز: $tokenStatus",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // GitHub Personal Access Token Field
                            OutlinedTextField(
                                value = ghToken,
                                onValueChange = { ghToken = it.trim() },
                                label = { Text("رمز الوصول (GitHub Personal Access Token)") },
                                singleLine = true,
                                visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                        Icon(
                                            imageVector = if (isTokenVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NexusGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = NexusGold,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = NexusGold
                                )
                            )

                            // Paste from clipboard button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        val clip = clipboardManager.getText()?.text.orEmpty().trim()
                                        if (clip.isNotBlank()) {
                                            ghToken = clip
                                            Toast.makeText(context, "تم لصق الرمز من الحافظة", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = NexusGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("لصق الرمز من الحافظة", fontSize = 11.sp, color = NexusGold)
                                }
                            }

                            // Owner and Repo inputs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = ghOwner,
                                    onValueChange = { ghOwner = it.trim() },
                                    label = { Text("المالك (Owner)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = ghRepo,
                                    onValueChange = { ghRepo = it.trim() },
                                    label = { Text("المستودع (Repo)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // Branch input
                            OutlinedTextField(
                                value = ghBranch,
                                onValueChange = { ghBranch = it.trim() },
                                label = { Text("الفرع (Branch)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Action Buttons: Test Connection & Save
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onTestGitHubConnection(ghToken, ghOwner, ghRepo)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                    border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.5f)),
                                    enabled = !isTestingGitHub && ghToken.isNotBlank()
                                ) {
                                    if (isTestingGitHub) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = NexusGold,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = NexusGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("فحص الاتصال", fontSize = 11.5.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onSaveGitHubCredentials(ghToken, ghOwner, ghRepo, ghBranch)
                                        Toast.makeText(context, "تم حفظ وتطبيق إعدادات GitHub بنجاح", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NexusGold)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("حفظ الإعدادات", fontSize = 11.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Connection Test Result Display
                            if (gitHubTestResult != null) {
                                val isSuccess = gitHubTestResult.success
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = gitHubTestResult.message,
                                            fontSize = 11.5.sp,
                                            color = TextPrimary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            // Force Sync All Button
                            Button(
                                onClick = onForceSyncAllToGitHub,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NexusOrange
                                ),
                                enabled = !isCloudSyncing
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isCloudSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isCloudSyncing) "جارِ إرسال وتحديث البيانات إلى GitHub..." else "مزامنة سحابية شاملة الآن (user/user.json)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Sync Status Message Card
                            if (!syncStatusMessage.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceElevated,
                                    border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = syncStatusMessage,
                                        fontSize = 11.sp,
                                        color = NexusGold,
                                        modifier = Modifier.padding(10.dp),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // Step-by-step Guide Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceElevated.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "خطوات الحصول على الرمز (Token) بصلاحية الكتابة:",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NexusGold
                                    )
                                    Text(
                                        text = "1. افتح GitHub ثم Settings > Developer Settings.\n2. اختر Personal access tokens > Tokens (classic).\n3. اضغط Generate new token وضع علامة على صلاحية (repo).\n4. انسخ الرمز والصقه هنا واضغط 'حفظ الإعدادات'.",
                                        fontSize = 10.5.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // TAB 4: Security & Cloud Rules
                    4 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = NexusGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "قواعد حماية Firestore المحمية",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "تضمن هذه القواعد أن كل مستخدم يصل فقط إلى مكتبته الخاصة، بينما الأدمن المعتمد ($PRIMARY_ADMIN_EMAIL) فقط يمكنه نشر التنبيهات وإدارة المحتوى السحابي.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            // Code block for Firestore rules
                            val firestoreRules = """
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // كل مستخدم يقرأ ويكتب بياناته فقط
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    // التنبيهات: يقرأها الجميع، ويكتبها الأدمن فقط
    match /nexus_announcements/{document=**} {
      allow read: if true;
      allow write: if request.auth != null && 
        request.auth.token.email == '${currentUser.email}';
    }
  }
}
                            """.trimIndent()

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = firestoreRules,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = NexusGold,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(firestoreRules))
                                    Toast.makeText(context, "تم نسخ قواعد الأمان إلى الحافظة", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceElevated
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = NexusGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("نسخ القواعد لـ Firebase Console", color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, fontSize = 11.sp, color = TextSecondary)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun AdminReportCard(
    report: UserReport,
    timeFormat: SimpleDateFormat,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val isApproved = report.isApproved()
    val isRejected = report.isRejected()
    val isPending = report.isPending()

    val cardBorder = when {
        isApproved -> Color(0xFF10B981).copy(alpha = 0.4f)
        isRejected -> Color(0xFFEF4444).copy(alpha = 0.4f)
        else -> NexusGold.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isApproved -> Color(0xFF10B981).copy(alpha = 0.15f)
                        isRejected -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        else -> NexusGold.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when {
                            isApproved -> "تم الموافقة"
                            isRejected -> "تم الرفض"
                            else -> "قيد الانتظار"
                        },
                        color = when {
                            isApproved -> Color(0xFF10B981)
                            isRejected -> Color(0xFFEF4444)
                            else -> NexusGold
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${report.category}: ${report.subCategory}",
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // User Info
            Text(
                text = "المرسل: ${report.userDisplayName.ifBlank { "مستخدم" }} (${report.userEmail.ifBlank { "بدون بريد" }})",
                color = TextSecondary,
                fontSize = 11.5.sp
            )

            // Target Work or Chapter if available
            if (report.targetTitle.isNotBlank()) {
                Text(
                    text = "العمل المستهدف: ${report.targetTitle}${if (report.chapterNumber.isNotBlank()) " | الفصل: ${report.chapterNumber}" else ""}",
                    color = NexusGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Details
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = report.details,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormat.format(Date(report.createdAt)),
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontSize = 10.5.sp
                )
            }

            // Action Buttons if pending
            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("تم رفض", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("تم الموافقة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (report.adminResponseNote.isNotBlank()) {
                Text(
                    text = "ملاحظة الإدارة: ${report.adminResponseNote}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private const val PRIMARY_ADMIN_EMAIL = "alsaid66900@gmail.com"
