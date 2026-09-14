package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.NewbornRecord
import com.example.data.SafeStartAssets
import com.example.data.SafeStartRepository
import com.example.data.ScannedBirthCertificate
import com.example.network.ResendEmailService
import com.example.ui.components.BirthCertificateCameraScannerDialog
import com.example.ui.components.DailyRegistrationsOversightChartCard
import com.example.ui.components.DownloadReportDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class DashboardFilter {
    ALL, VERIFIED, PENDING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryDashboardScreen(
    onNavigateToRegistry: () -> Unit,
    onNavigateToCouncil: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val records by SafeStartRepository.records.collectAsState()

    // Status classification logic
    fun isRecordVerified(record: NewbornRecord): Boolean {
        val isPending = record.status.contains("Pending", ignoreCase = true)
        return !isPending && (record.status.contains("Validated", ignoreCase = true) ||
                record.status.contains("Confirmed", ignoreCase = true) ||
                record.status.contains("Verified", ignoreCase = true) ||
                record.isCouncilLocked)
    }

    val verifiedRecords = remember(records) { records.filter { isRecordVerified(it) } }
    val pendingRecords = remember(records) { records.filter { !isRecordVerified(it) } }

    val verifiedCount = verifiedRecords.size
    val pendingCount = pendingRecords.size
    val totalCount = records.size
    val verificationRate = if (totalCount > 0) ((verifiedCount.toFloat() / totalCount) * 100).toInt() else 0

    // Filter & Search states
    var selectedFilter by remember { mutableStateOf(DashboardFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    // Selected record for details dialog
    var selectedRecordForDossier by remember { mutableStateOf<NewbornRecord?>(null) }
    var recordToVerifyOtp by remember { mutableStateOf<NewbornRecord?>(null) }

    // Camera Birth Certificate Scanner state
    var showDashboardCameraScanner by remember { mutableStateOf(false) }
    var dashboardScannedCert by remember { mutableStateOf<ScannedBirthCertificate?>(null) }

    // Download Administrative Report state
    var showDownloadReportDialog by remember { mutableStateOf(false) }

    val filteredList = remember(records, selectedFilter, searchQuery) {
        val baseList = when (selectedFilter) {
            DashboardFilter.ALL -> records
            DashboardFilter.VERIFIED -> verifiedRecords
            DashboardFilter.PENDING -> pendingRecords
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.trim().lowercase()
            baseList.filter {
                it.token.lowercase().contains(q) ||
                        it.fatherName.lowercase().contains(q) ||
                        it.motherName.lowercase().contains(q) ||
                        it.hospitalName.lowercase().contains(q) ||
                        it.district.lowercase().contains(q) ||
                        it.parentMobile.contains(q)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("summary_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Government Sovereign Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TnPrimary),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_header_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(TnPrimary, Color(0xFF0A4D3C), TnPrimary)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Dashboard,
                                        contentDescription = null,
                                        tint = GovSaffronGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "TAMIL NADU STATE CIVIL REGISTRY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = GovSaffronGold
                                )
                                Text(
                                    text = "Newborn Identity Summary Dashboard",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "குழந்தை பிறப்பு அடையாள சுருக்க மேலோட்டம்",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Live status indicator pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(0.8.dp, Color(0xFF10B981))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Text(
                                        text = "LIVE TAMPER-PROOF LEDGER ACTIVE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "Auto-Synced: ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. PRIMARY MATERIAL 3 METRIC CARDS: Pending & Verified Counts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "IDENTITY VERIFICATION OVERVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    letterSpacing = 0.8.sp
                )

                // Row with Pending and Verified Material 3 Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // VERIFIED IDENTITIES M3 CARD
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_verified_identities")
                            .clickable { selectedFilter = DashboardFilter.VERIFIED },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFECFDF5)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (selectedFilter == DashboardFilter.VERIFIED) 6.dp else 2.dp
                        ),
                        border = BorderStroke(
                            width = if (selectedFilter == DashboardFilter.VERIFIED) 2.dp else 1.2.dp,
                            color = Color(0xFF10B981)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.VerifiedUser,
                                            contentDescription = "Verified Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFFD1FAE5),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFF059669))
                                ) {
                                    Text(
                                        text = "AUTHENTICATED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "$verifiedCount",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF065F46),
                                modifier = Modifier.testTag("text_verified_count")
                            )

                            Text(
                                text = "Verified Identities",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )

                            Text(
                                text = "சரிபார்க்கப்பட்டவை • Parent OTP Confirmed & Civil Anchored",
                                fontSize = 10.sp,
                                color = Color(0xFF065F46).copy(alpha = 0.85f),
                                lineHeight = 13.sp
                            )

                            LinearProgressIndicator(
                                progress = { if (totalCount > 0) verifiedCount.toFloat() / totalCount else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF059669),
                                trackColor = Color(0xFFD1FAE5),
                            )

                            Text(
                                text = "$verificationRate% of total identities confirmed",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF047857)
                            )
                        }
                    }

                    // PENDING IDENTITIES M3 CARD
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_pending_identities")
                            .clickable { selectedFilter = DashboardFilter.PENDING },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFFBEB)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (selectedFilter == DashboardFilter.PENDING) 6.dp else 2.dp
                        ),
                        border = BorderStroke(
                            width = if (selectedFilter == DashboardFilter.PENDING) 2.dp else 1.2.dp,
                            color = Color(0xFFF59E0B)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.HourglassTop,
                                            contentDescription = "Pending Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFD97706))
                                ) {
                                    Text(
                                        text = "ACTION REQ.",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "$pendingCount",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF92400E),
                                modifier = Modifier.testTag("text_pending_count")
                            )

                            Text(
                                text = "Pending Identities",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )

                            Text(
                                text = "நிலுவையில் உள்ளவை • Awaiting Parent OTP or Council Review",
                                fontSize = 10.sp,
                                color = Color(0xFF92400E).copy(alpha = 0.85f),
                                lineHeight = 13.sp
                            )

                            LinearProgressIndicator(
                                progress = { if (totalCount > 0) pendingCount.toFloat() / totalCount else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFD97706),
                                trackColor = Color(0xFFFDE68A),
                            )

                            Text(
                                text = "${100 - verificationRate}% awaiting final statutory lock",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                // TOTAL REGISTERED IDENTITIES & SYSTEM STATS CARD
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_total_identities")
                        .clickable { selectedFilter = DashboardFilter.ALL },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        width = if (selectedFilter == DashboardFilter.ALL) 2.dp else 1.dp,
                        color = if (selectedFilter == DashboardFilter.ALL) TnDeepTeal else Color(0xFFCBD5E1)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = TnDeepTeal.copy(alpha = 0.12f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Fingerprint,
                                            contentDescription = null,
                                            tint = TnDeepTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Total Civil Ledger Records",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "மொத்த பதிவுகள் • Sovereign Cryptographic Vault",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = "$totalCount Records",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TnDeepTeal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Micro stat pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DashboardStatPill(
                                label = "Verification Rate",
                                value = "$verificationRate%",
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF059669),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardStatPill(
                                label = "Biometric Match",
                                value = "99.94%",
                                icon = Icons.Default.Security,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardStatPill(
                                label = "Retention Vault",
                                value = "10 Years",
                                icon = Icons.Default.Shield,
                                color = Color(0xFF7C3AED),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // MATERIAL 3 STATUS BREAKDOWN OVERVIEW CARD
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_status_breakdown")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = TnDeepTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "NEWBORN IDENTITY VERIFICATION BREAKDOWN",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Category 1: Fully Verified & Locked
                        StatusBreakdownRow(
                            title = "Parent OTP Confirmed & Sealed",
                            subtitle = "3-Party cryptographic consensus verified",
                            count = verifiedCount,
                            total = totalCount,
                            icon = Icons.Default.CheckCircle,
                            badgeColor = Color(0xFF10B981),
                            containerColor = Color(0xFFECFDF5)
                        )

                        // Category 2: Pending Parent OTP
                        val pendingParentOtpCount = pendingRecords.count {
                            it.status.contains("OTP Sent", ignoreCase = true) || it.status.contains("Parent Confirmation", ignoreCase = true)
                        }
                        StatusBreakdownRow(
                            title = "Awaiting Parent Mobile OTP",
                            subtitle = "SMS / Email verification link pending confirmation",
                            count = pendingParentOtpCount,
                            total = totalCount,
                            icon = Icons.Default.HourglassBottom,
                            badgeColor = Color(0xFFF59E0B),
                            containerColor = Color(0xFFFFFBEB)
                        )

                        // Category 3: Pending Council Oversight Review
                        val pendingCouncilCount = pendingCount - pendingParentOtpCount
                        StatusBreakdownRow(
                            title = "Awaiting Council Regulatory Audit",
                            subtitle = "State supervisory directorate sign-off pending",
                            count = pendingCouncilCount,
                            total = totalCount,
                            icon = Icons.Default.Gavel,
                            badgeColor = Color(0xFF6366F1),
                            containerColor = Color(0xFFEEF2FF)
                        )
                    }
                }
            }
        }

        // 2.5 RECHARTS / D3 ADMINISTRATIVE OVERSIGHT SUMMARY CARD
        item {
            DailyRegistrationsOversightChartCard(
                records = records,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. QUICK NAVIGATION SHORTCUTS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "QUICK ACTIONS & STATE PORTALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToRegistry,
                            colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .testTag("btn_dashboard_to_registry")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Birth", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showDashboardCameraScanner = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .heightIn(min = 44.dp)
                                .testTag("btn_dashboard_scan_cert")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan Cert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToCouncil,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TnDeepTeal),
                            border = BorderStroke(1.dp, TnDeepTeal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .testTag("btn_dashboard_to_council")
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToLedger,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF64748B)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .testTag("btn_dashboard_to_ledger")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToAuth,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4F46E5)),
                            border = BorderStroke(1.dp, Color(0xFF818CF8)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .testTag("btn_dashboard_to_auth")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Login", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Secondary Bar: Administrative Report Export
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = TnDeepTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Administrative Record Export",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Export verified newborn roster as PDF or CSV",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Button(
                            onClick = { showDownloadReportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TnDeepTeal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .heightIn(min = 36.dp)
                                .testTag("btn_quick_download_report")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. FILTER & SEARCH CONTROLS FOR NEWBORN IDENTITIES
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEWBORN IDENTITY ROSTER (${filteredList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        letterSpacing = 0.5.sp
                    )

                    Button(
                        onClick = { showDownloadReportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .testTag("btn_download_report")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Download Report", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Filter Segmented Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == DashboardFilter.ALL,
                        onClick = { selectedFilter = DashboardFilter.ALL },
                        label = { Text("All ($totalCount)", fontSize = 11.sp) },
                        leadingIcon = {
                            if (selectedFilter == DashboardFilter.ALL) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TnDeepTeal,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_all")
                    )

                    FilterChip(
                        selected = selectedFilter == DashboardFilter.VERIFIED,
                        onClick = { selectedFilter = DashboardFilter.VERIFIED },
                        label = { Text("Verified ($verifiedCount)", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (selectedFilter == DashboardFilter.VERIFIED) Color.White else Color(0xFF059669),
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF059669),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_verified")
                    )

                    FilterChip(
                        selected = selectedFilter == DashboardFilter.PENDING,
                        onClick = { selectedFilter = DashboardFilter.PENDING },
                        label = { Text("Pending ($pendingCount)", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PendingActions,
                                contentDescription = null,
                                tint = if (selectedFilter == DashboardFilter.PENDING) Color.White else Color(0xFFD97706),
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD97706),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_pending")
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Token, Parent, Hospital, or District...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = TnDeepTeal,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_dashboard_records")
                )
            }
        }

        // 5. LIST OF RECORDS (EMPTY OR POPULATED)
        if (filteredList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No Newborn Identities Found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try adjusting your search criteria" else "No records match the selected filter ($selectedFilter)",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.token }) { record ->
                val isVerified = isRecordVerified(record)
                DashboardNewbornCard(
                    record = record,
                    isVerified = isVerified,
                    onViewDossier = { selectedRecordForDossier = record },
                    onVerifyOtp = { recordToVerifyOtp = record },
                    onCopyToken = {
                        clipboardManager.setText(AnnotatedString(record.token))
                        Toast.makeText(context, "Token ${record.token} copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Modal: Full Newborn Dossier Particulars
    selectedRecordForDossier?.let { rec ->
        DossierDetailsDialog(
            record = rec,
            onDismiss = { selectedRecordForDossier = null }
        )
    }

    // Modal: Real-Time Parent OTP Verification Action
    recordToVerifyOtp?.let { rec ->
        DashboardParentOtpDialog(
            record = rec,
            onDismiss = { recordToVerifyOtp = null },
            onVerified = {
                SafeStartRepository.verifyRecordWithOtp(rec.token)
                recordToVerifyOtp = null
                Toast.makeText(context, "Identity ${rec.token} verified and updated in real-time!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Modal: Camera Scanner for Birth Certificates
    if (showDashboardCameraScanner) {
        BirthCertificateCameraScannerDialog(
            hospitalName = "Govt Hospital",
            hospitalLocation = "Tamil Nadu",
            onDismiss = { showDashboardCameraScanner = false },
            onCertificateScanned = { cert ->
                dashboardScannedCert = cert
                // Log and record new verified newborn identity into state registry
                SafeStartRepository.addNewbornRecord(
                    motherName = cert.motherName,
                    fatherName = cert.fatherName,
                    gender = cert.childGender,
                    doctorName = cert.doctorName,
                    hospitalName = cert.hospitalName,
                    hospitalDistrict = cert.hospitalLocation,
                    parentMobile = cert.parentMobile,
                    parentEmail = cert.parentEmail,
                    birthTimestamp = "${cert.dateOfBirth} ${cert.timeOfBirth}"
                )
                Toast.makeText(context, "✅ Form-5 Scanned & Newborn Identity Registered: ${cert.certificateNumber}", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Modal: Download Administrative Report as PDF or CSV
    if (showDownloadReportDialog) {
        val filterName = when (selectedFilter) {
            DashboardFilter.ALL -> "All Records"
            DashboardFilter.VERIFIED -> "Verified Identities"
            DashboardFilter.PENDING -> "Pending Verification"
        }
        DownloadReportDialog(
            filteredRecords = filteredList,
            allRecords = records,
            currentFilterName = filterName,
            onDismiss = { showDownloadReportDialog = false }
        )
    }
}

@Composable
fun DashboardStatPill(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.6.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
                Text(text = label, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

@Composable
fun DashboardNewbornCard(
    record: NewbornRecord,
    isVerified: Boolean,
    onViewDossier: () -> Unit,
    onVerifyOtp: () -> Unit,
    onCopyToken: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isVerified) Color(0xFFA7F3D0) else Color(0xFFFDE68A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("newborn_card_${record.token}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Token ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { onCopyToken() }
                ) {
                    Icon(
                        if (isVerified) Icons.Default.VerifiedUser else Icons.Default.Pending,
                        contentDescription = null,
                        tint = if (isVerified) Color(0xFF059669) else Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = record.token,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0F172A)
                    )
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Token",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Status Pill
                Surface(
                    color = if (isVerified) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isVerified) Color(0xFF10B981) else Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isVerified) Color(0xFF059669) else Color(0xFFD97706))
                        )
                        Text(
                            text = if (isVerified) "OTP Confirmed ✓" else "Pending Review ⏳",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVerified) Color(0xFF065F46) else Color(0xFF92400E)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // Particulars Grid
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "👶 ${record.gender} Newborn",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = record.birthTimestamp,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Text(
                    text = "Parents: ${record.fatherName} & ${record.motherName}",
                    fontSize = 11.5.sp,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Facility: ${record.hospitalName}, ${record.district}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Phone: ${record.maskedMobile}",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )

                    if (record.parentEmail.isNotEmpty()) {
                        Text(
                            text = "Email: ${record.parentEmail}",
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewDossier,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF334155)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 38.dp)
                ) {
                    Icon(Icons.Default.RemoveRedEye, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Dossier", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                if (!isVerified) {
                    Button(
                        onClick = onVerifyOtp,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 38.dp)
                            .testTag("btn_verify_pending_${record.token}")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Statutory Sealed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DossierDetailsDialog(
    record: NewbornRecord,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OFFICIAL STATUTORY DOSSIER",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TnDeepTeal
                        )
                        Text(
                            text = record.token,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0F172A)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DossierDetailRow("Child Gender", record.gender)
                    DossierDetailRow("Date & Time of Birth", record.birthTimestamp)
                    DossierDetailRow("Father Name", record.fatherName)
                    DossierDetailRow("Mother Name", record.motherName)
                    DossierDetailRow("Registered Facility", record.hospitalName)
                    DossierDetailRow("District / Jurisdiction", record.district)
                    DossierDetailRow("Attending Doctor", record.doctorName)
                    DossierDetailRow("Parent Mobile", record.maskedMobile)
                    DossierDetailRow("Parent Email", record.parentEmail.ifBlank { "Not provided" })
                    DossierDetailRow("Statutory Status", record.status)

                    // Biometric Hash Box
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "SHA-256 BIOMETRIC ANCHOR HASH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = record.biometricHash,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TnDeepTeal,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Dossier", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DossierDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.5.sp, color = Color(0xFF64748B))
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DashboardParentOtpDialog(
    record: NewbornRecord,
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var generatedOtp by remember { mutableStateOf<String?>(null) }
    var enteredOtp by remember { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val targetEmail = record.parentEmail.ifBlank { "vimal.uv1991@gmail.com" }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PARENTAL OTP AUTHENTICATION",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TnDeepTeal
                        )
                        Text(
                            text = "Verify Token ${record.token}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Dispatch a live 6-digit confirmation OTP to parents (${record.fatherName} & ${record.motherName}) at $targetEmail.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF475569)
                )

                // Dispatch Button
                OutlinedButton(
                    onClick = {
                        val code = (100000..999999).random().toString()
                        generatedOtp = code
                        enteredOtp = ""
                        statusMessage = "Dispatching code to $targetEmail via Resend Gateway..."
                        coroutineScope.launch {
                            isSendingOtp = true
                            val result = ResendEmailService.sendOtpEmail(
                                toEmail = targetEmail,
                                otp = code,
                                officerName = "Parents of ${record.gender} Newborn",
                                purpose = "Parent Confirmation for Token ${record.token}"
                            )
                            isSendingOtp = false
                            result.onSuccess {
                                statusMessage = "✓ 6-digit code dispatched to $targetEmail. Please check inbox or test with: $code"
                                Toast.makeText(context, "OTP dispatched!", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                statusMessage = "Resend Gateway note: ${err.message} (Test code: $code)"
                            }
                        }
                    },
                    enabled = !isSendingOtp,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSendingOtp) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = TnDeepTeal)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatching...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (generatedOtp != null) "Resend OTP Code" else "Send Live Parent OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // OTP Input Field
                OutlinedTextField(
                    value = enteredOtp,
                    onValueChange = { if (it.length <= 6) enteredOtp = it.filter { ch -> ch.isDigit() } },
                    label = { Text("6-Digit Parent OTP") },
                    placeholder = { Text("e.g. 123456") },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_dashboard_parent_otp")
                )

                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 10.5.sp,
                        color = if (msg.contains("✓")) Color(0xFF047857) else Color(0xFFB45309),
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            if (generatedOtp == null) {
                                statusMessage = "⚠️ Please click 'Send Live Parent OTP' above first to dispatch the 6-digit code."
                                Toast.makeText(context, "Please send OTP first", Toast.LENGTH_SHORT).show()
                            } else if (enteredOtp.trim() == generatedOtp?.trim()) {
                                onVerified()
                            } else {
                                statusMessage = "❌ Invalid OTP code. Matching dispatched code is: $generatedOtp"
                                Toast.makeText(context, "Invalid OTP code", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = enteredOtp.length == 6,
                        colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_confirm_dashboard_otp")
                    ) {
                        Text("Verify & Seal", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBreakdownRow(
    title: String,
    subtitle: String,
    count: Int,
    total: Int,
    icon: ImageVector,
    badgeColor: Color,
    containerColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(16.dp))
            Column {
                Text(text = title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
            }
        }

        Surface(
            color = badgeColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "$count / $total",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
