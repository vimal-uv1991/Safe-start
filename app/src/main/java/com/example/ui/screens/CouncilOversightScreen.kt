package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class CouncilSubTab {
    STATEWIDE_RECORDS_TABLE,
    PASSWORD_RESET_APPROVALS,
    COMPLAINTS_DOCKET_READONLY,
    VERIFICATION_REPORT_10YR
}

@Composable
fun CouncilOversightScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by SafeStartRepository.records.collectAsState()
    val securityAlert by SafeStartRepository.securityAlert.collectAsState()
    val passwordResets by SafeStartRepository.passwordResets.collectAsState()
    val complaints by SafeStartRepository.complaints.collectAsState()
    val disputeCases by SafeStartRepository.disputeCases.collectAsState()

    var activeTab by remember { mutableStateOf(CouncilSubTab.STATEWIDE_RECORDS_TABLE) }

    // Search bar: Search for particular hospital name and where it is located
    var hospitalSearchQuery by remember { mutableStateOf("") }
    var locationSearchQuery by remember { mutableStateOf("") }

    // Filter records by searched Hospital Name and Location
    val filteredRecords = records.filter { record ->
        (hospitalSearchQuery.isEmpty() || record.hospitalName.contains(hospitalSearchQuery, ignoreCase = true)) &&
                (locationSearchQuery.isEmpty() ||
                        record.district.contains(locationSearchQuery, ignoreCase = true) ||
                        record.hospitalLocation.contains(locationSearchQuery, ignoreCase = true))
    }

    // Authorized Phone Reveal (timed 30s)
    var revealedTokenId by remember { mutableStateOf<String?>(null) }
    var revealCountdown by remember { mutableStateOf(0) }

    LaunchedEffect(revealedTokenId) {
        if (revealedTokenId != null) {
            revealCountdown = 30
            while (revealCountdown > 0) {
                delay(1000L)
                revealCountdown -= 1
            }
            revealedTokenId = null
        }
    }

    // Verification Report Dialog
    var selectedComplaintForReport by remember { mutableStateOf<HospitalComplaint?>(null) }
    var selectedDisputeForReport by remember { mutableStateOf<ParentageDisputeCase?>(null) }

    // Toast Notifications
    var successToastMsg by remember { mutableStateOf<Pair<String, String>?>(null) }
    var smsToastMsg by remember { mutableStateOf<Pair<String, String>?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GovCanvasBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("council_oversight_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Toast Notifications
        successToastMsg?.let { (title, desc) ->
            GovSuccessToast(
                title = title,
                description = desc,
                onDismiss = { successToastMsg = null }
            )
        }
        smsToastMsg?.let { (title, mono) ->
            GovSmsDeliveredToast(
                title = title,
                monospaceBody = mono,
                onDismiss = { smsToastMsg = null }
            )
        }

        // PAGE HERO (INDIAN GOVT E-GOVERNANCE DESIGN SYSTEM)
        GovPageHero(
            title = "GOVERNMENT MEDICAL COUNCIL OVERSIGHT PORTAL",
            subtitle = "Statewide Civil Registry Oversight & Audit Directorate • Sovereign Read-Only Node",
            icon = Icons.Default.AccountBalance
        )

        // STATUTORY ENFORCEMENT INFO CALLOUT BOX
        GovInfoCalloutBox(
            title = "STATUTORY COUNCIL OVERSIGHT MANDATE",
            description = "1st Login Authority: Government Medical Council members visit and audit statewide records with Unique ID. Council members cannot edit or delete registered data by law.",
            isGreenVariant = false
        )

        // UNUSUAL LOGIN ALERT (Mandatory Requirement)
        // "If there is an unusual login the Government Medical Council receives an alert."
        if (!securityAlert.isOverridden) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth().testTag("unusual_login_alert_banner")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "ALERT: ${securityAlert.title.uppercase()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB91C1C)
                                )
                                Text(
                                    text = "${securityAlert.location} • ${securityAlert.timestamp}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFDC2626),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "CRITICAL",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = "${securityAlert.anomalyDescription} (Node IP: ${securityAlert.ipAddress}). Council oversight protocol triggered.",
                        fontSize = 11.sp,
                        color = Color(0xFF991B1B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                SafeStartRepository.triggerCouncilOverrideLock()
                                Toast.makeText(context, "Council Cryptographic Override Lock Enforced!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Enforce Lock on Node", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                SafeStartRepository.flagForStatutoryInquiry()
                                Toast.makeText(context, "Hospital Node Flagged for Statutory Audit Inquiry.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Flag for Inquiry", fontSize = 11.sp, color = Color(0xFFB91C1C))
                        }
                    }
                }
            }
        }

        // SUB-NAVIGATION SEGMENTED TABS (DIRECTLY BELOW HERO & CALLOUT)
        GovSegmentedTabs(
            options = listOf(
                "RECORDS" to Icons.Default.TableChart,
                "PW RESETS (${passwordResets.filter { it.status == "PENDING" }.size})" to Icons.Default.LockReset,
                "COMPLAINTS" to Icons.Default.Assignment,
                "10-YR AUDIT" to Icons.Default.Fingerprint
            ),
            selectedIndex = when (activeTab) {
                CouncilSubTab.STATEWIDE_RECORDS_TABLE -> 0
                CouncilSubTab.PASSWORD_RESET_APPROVALS -> 1
                CouncilSubTab.COMPLAINTS_DOCKET_READONLY -> 2
                CouncilSubTab.VERIFICATION_REPORT_10YR -> 3
            },
            onSelectIndex = { index ->
                activeTab = when (index) {
                    0 -> CouncilSubTab.STATEWIDE_RECORDS_TABLE
                    1 -> CouncilSubTab.PASSWORD_RESET_APPROVALS
                    2 -> CouncilSubTab.COMPLAINTS_DOCKET_READONLY
                    else -> CouncilSubTab.VERIFICATION_REPORT_10YR
                }
            }
        )

        when (activeTab) {
            CouncilSubTab.STATEWIDE_RECORDS_TABLE -> {
                // =========================================================================
                // SEARCH BAR & TABULAR COLUMN VIEW OF HOSPITAL RECORDS
                // "Search bar: we can search for a particular hospital name and where it is located.
                // Hospital is identified then we can see the records of the particular hospital like a tabular column it contains:
                // Father's biometric (footprint) & name
                // Mother's biometric (footprint) & name
                // Child's footprint
                // Child's Gender
                // Birth DATE/MONTH/YEAR & TIME.
                // Doctor Name
                // Hospital Name & Hospital location
                // Father's or Mother's mobile number"
                // =========================================================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "SEARCH HOSPITAL BY NAME & LOCATION",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )

                        // SEARCH BARS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = hospitalSearchQuery,
                                onValueChange = { hospitalSearchQuery = it },
                                colors = safeStartTextFieldColors(),
                                label = { Text("Search Hospital Name") },
                                placeholder = { Text("e.g. Rajaji, Rajiv Gandhi, Coimbatore") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TnDeepTeal) },
                                singleLine = true,
                                modifier = Modifier.weight(1.3f).testTag("search_hospital_name")
                            )

                            OutlinedTextField(
                                value = locationSearchQuery,
                                onValueChange = { locationSearchQuery = it },
                                colors = safeStartTextFieldColors(),
                                label = { Text("Search Location / District") },
                                placeholder = { Text("e.g. Madurai, Chennai, Salem") },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = TnDeepTeal) },
                                singleLine = true,
                                modifier = Modifier.weight(1.1f).testTag("search_hospital_location")
                            )
                        }

                        // Quick Filter Chips
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All Hospitals", "Rajaji (Madurai)", "Rajiv Gandhi (Chennai)", "Coimbatore MCH", "Salem GH", "Tiruchirappalli GH").forEach { preset ->
                                val isSelected = when (preset) {
                                    "All Hospitals" -> hospitalSearchQuery.isEmpty() && locationSearchQuery.isEmpty()
                                    "Rajaji (Madurai)" -> hospitalSearchQuery.contains("Rajaji")
                                    "Rajiv Gandhi (Chennai)" -> hospitalSearchQuery.contains("Rajiv")
                                    "Coimbatore MCH" -> hospitalSearchQuery.contains("Coimbatore")
                                    "Salem GH" -> hospitalSearchQuery.contains("Salem")
                                    "Tiruchirappalli GH" -> hospitalSearchQuery.contains("Tiruchirappalli")
                                    else -> false
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        when (preset) {
                                            "All Hospitals" -> {
                                                hospitalSearchQuery = ""
                                                locationSearchQuery = ""
                                            }
                                            "Rajaji (Madurai)" -> {
                                                hospitalSearchQuery = "Rajaji"
                                                locationSearchQuery = "Madurai"
                                            }
                                            "Rajiv Gandhi (Chennai)" -> {
                                                hospitalSearchQuery = "Rajiv Gandhi"
                                                locationSearchQuery = "Chennai"
                                            }
                                            "Coimbatore MCH" -> {
                                                hospitalSearchQuery = "Coimbatore"
                                                locationSearchQuery = "Coimbatore"
                                            }
                                            "Salem GH" -> {
                                                hospitalSearchQuery = "Salem"
                                                locationSearchQuery = "Salem"
                                            }
                                            "Tiruchirappalli GH" -> {
                                                hospitalSearchQuery = "Tiruchirappalli"
                                                locationSearchQuery = "Tiruchirappalli"
                                            }
                                        }
                                    },
                                    label = { Text(preset, fontSize = 11.sp) }
                                )
                            }
                        }

                        Divider(color = Color(0xFFE2E8F0))

                        Text(
                            text = "HOSPITAL IDENTIFIED: ${filteredRecords.size} INFANT DOSSIERS FOUND",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TnDeepTeal
                        )

                        if (filteredRecords.isEmpty()) {
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No hospital records match the searched criteria.", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                            }
                        } else {
                            // TABULAR COLUMN FORMAT FOR EACH IDENTIFIED RECORD
                            filteredRecords.forEach { record ->
                                CouncilRecordTabularCard(
                                    record = record,
                                    isPhoneRevealed = revealedTokenId == record.token,
                                    revealCountdown = revealCountdown,
                                    onRevealClicked = {
                                        revealedTokenId = if (revealedTokenId == record.token) null else record.token
                                    }
                                )
                            }
                        }
                    }
                }
            }

            CouncilSubTab.PASSWORD_RESET_APPROVALS -> {
                // =========================================================================
                // FORGOT PASSWORD APPROVALS DOCKET
                // Medical council reviews requests raised by hospitals with valid reasons!
                // =========================================================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "HOSPITAL PASSWORD RESET APPROVAL QUEUE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Special Case Policy: Hospitals must submit a valid statutory reason. Medical Council head must approve before the hospital can set a new password.",
                            fontSize = 11.sp,
                            color = Color(0xFF334155)
                        )

                        passwordResets.forEach { req ->
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (req.status == "APPROVED") Color(0xFF10B981) else Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = req.id,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TnDeepTeal
                                        )
                                        Surface(
                                            color = when (req.status) {
                                                "APPROVED" -> Color(0xFFDCFCE7)
                                                "REJECTED" -> Color(0xFFFEE2E2)
                                                else -> Color(0xFFFEF3C7)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = req.status,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (req.status) {
                                                    "APPROVED" -> Color(0xFF166534)
                                                    "REJECTED" -> Color(0xFF991B1B)
                                                    else -> Color(0xFF92400E)
                                                }
                                            )
                                        }
                                    }

                                    Text("• Hospital: ${req.hospitalName} (${req.district})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                    Text("• Officer / Registrar: ${req.registrarName}", fontSize = 12.sp, color = Color(0xFF0F172A))
                                    Surface(
                                        color = Color(0xFFFFFBEB),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.8.dp, SovereignGold.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("SUBMITTED VALID REASON FOR FORGETTING PASSWORD:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                            Text("\"${req.reason}\"", fontSize = 11.sp, color = Color(0xFF78350F), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                        }
                                    }

                                    if (req.status == "PENDING") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    SafeStartRepository.approvePasswordReset(req.id)
                                                    successToastMsg = "COUNCIL AUTHORIZATION GRANTED" to "Reset token generated for ${req.hospitalName} (${req.id})."
                                                    smsToastMsg = "HOSPITAL NOTIFICATION DISPATCHED" to "TN-COUNCIL: Password reset approved for Admin ID ${req.id}. Code: RST-TN-9921."
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1.5f).height(38.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Approve with Council Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    SafeStartRepository.rejectPasswordReset(req.id)
                                                    Toast.makeText(context, "Reset request rejected.", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Text("Reject", fontSize = 11.sp, color = Color(0xFFB91C1C))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            CouncilSubTab.COMPLAINTS_DOCKET_READONLY -> {
                // =========================================================================
                // COMPLAINTS SUBMITTED BY HOSPITALS (READ-ONLY FOR COUNCIL)
                // "Submitted complaint can viewed only by the Government Medical council and they can't overwrite on it."
                // =========================================================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "HOSPITAL COMPLAINTS DOCKET (STRICT READ-ONLY)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Complaints filed by Hospital Admins against parents (Discomfort, Partiality, or Others). Medical Council officials can review and order biometric verification reports, but CANNOT overwrite complaint content.",
                            fontSize = 11.sp,
                            color = Color(0xFF334155)
                        )

                        complaints.forEach { complaint ->
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = complaint.complaintId,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TnDeepTeal
                                        )
                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "ISSUE: " + complaint.issueType.uppercase(),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E)
                                            )
                                        }
                                    }

                                    Text("• Hospital: ${complaint.hospitalName} (${complaint.hospitalLocation})", fontSize = 12.sp, color = Color(0xFF0F172A))
                                    Text("• Filing Hospital Admin ID: ${complaint.hospitalAdminId}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF475569))
                                    Text("• Child Name / Token: ${complaint.childName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                    Text("• Details: ${complaint.details}", fontSize = 12.sp, color = Color(0xFF334155))

                                    // Uploaded Document Badges
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = Color(0xFFEFF6FF),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.8.dp, Color(0xFF93C5FD))
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.FilePresent, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(14.dp))
                                                Text("Birth Cert: ${complaint.birthCertificateDocument}", fontSize = 10.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.SemiBold)
                                            }
                                        }

                                        Surface(
                                            color = Color(0xFFECFDF5),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.8.dp, Color(0xFF6EE7B7))
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                                Text("Passport Photo Attached", fontSize = 10.sp, color = Color(0xFF065F46), fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    // Verification Report Trigger
                                    Button(
                                        onClick = {
                                            selectedComplaintForReport = complaint
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Conduct Biometric Cross-Check & Issue Official Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            CouncilSubTab.VERIFICATION_REPORT_10YR -> {
                // =========================================================================
                // 10-YEAR RETENTION STATUTORY AUDIT & PARENTAGE TRIBUNAL
                // "The records are stored for 10 years from their updated date.
                // it will be deleted automatically when it reaches the same date after 10 years."
                // =========================================================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "10-YEAR DATA RETENTION & AUTOMATIC DELETION POLICY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Under the Tamil Nadu Civic Custody Act, newborn biometric dossiers are legally retained for 10 years from their updated date, and automatically deleted when they reach the same date after 10 years.",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            lineHeight = 15.sp
                        )

                        records.forEach { record ->
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(record.token, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TnDeepTeal)
                                        Text("Registered: ${record.birthTimestamp}", fontSize = 11.sp, color = Color(0xFF475569))
                                        Text("Hospital: ${record.hospitalName}", fontSize = 11.sp, color = Color(0xFF0F172A))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Surface(
                                            color = Color(0xFFE0F2FE),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "RETENTION: 10 YEARS",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Auto-Purge Date: ${record.autoPurgeDate}", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // OFFICIAL VERIFICATION REPORT MODAL (CROSS CHECKING REPORT)
    // =========================================================================
    if (selectedComplaintForReport != null) {
        val cmp = selectedComplaintForReport!!
        Dialog(onDismissRequest = { selectedComplaintForReport = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(2.dp, TnDeepTeal),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = SovereignGold, modifier = Modifier.size(28.dp))
                        Column {
                            Text(
                                text = "OFFICIAL VERIFICATION REPORT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TnDeepTeal
                            )
                            Text(
                                text = "Tamil Nadu Government Medical Council Biometric Tribunal",
                                fontSize = 10.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("• Complaint ID: ${cmp.complaintId}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("• Subject Child: ${cmp.childName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            Text("• Issue Category: ${cmp.issueType}", fontSize = 11.sp, color = Color(0xFF0F172A))
                            Text("• Facility: ${cmp.hospitalName}", fontSize = 11.sp, color = Color(0xFF0F172A))
                            Divider(color = Color(0xFFE2E8F0))
                            Text("BIOMETRIC FORENSIC CROSS-CHECK RESULTS:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                            Text("1. Newborn Footprint Hash: MATCHED (99.98% Confidence)", fontSize = 11.sp, color = Color(0xFF047857))
                            Text("2. Maternal Footprint Hash: MATCHED (100% Identity Integrity)", fontSize = 11.sp, color = Color(0xFF047857))
                            Text("3. Paternal Footprint Hash: MATCHED (100% Biological Alignment)", fontSize = 11.sp, color = Color(0xFF047857))
                            Text("Conclusion: Stored records verify infant authenticity without doubt. Grievance resolved under Council Authority.", fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                        }
                    }

                    Button(
                        onClick = {
                            selectedComplaintForReport = null
                            Toast.makeText(context, "Official Verification Report Issued and Dispatched to Hospital Node!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Sign & Seal Verification Report", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CouncilRecordTabularCard(
    record: NewbornRecord,
    isPhoneRevealed: Boolean,
    revealCountdown: Int,
    onRevealClicked: () -> Unit
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Token & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TnDeepTeal, modifier = Modifier.size(16.dp))
                    Text(
                        text = record.token,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TnDeepTeal
                    )
                }
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "COUNCIL AUDITED",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                }
            }

            // EXACT TABULAR COLUMN LAYOUT (8 SPECIFIED BOXES)
            // 1. Father's biometric & name
            CouncilFieldRow(
                title = "1. Father's biometric (footprint) & name",
                value = record.fatherName,
                imageUrl = record.fatherScanUrl
            )

            // 2. Mother's biometric & name
            CouncilFieldRow(
                title = "2. Mother's biometric (footprint) & name",
                value = record.motherName,
                imageUrl = record.motherScanUrl
            )

            // 3. Child's footprint
            CouncilFieldRow(
                title = "3. Child's footprint",
                value = "Newborn Biometric Hash: ${record.biometricHash.take(16)}...",
                imageUrl = record.childScanUrl
            )

            // 4. Child's Gender & 5. Birth Date/Month/Year & Time
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("4. Child's Gender", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        Text(record.gender, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("5. Birth DATE/MONTH/YEAR & TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        Text(record.birthTimestamp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                }
            }

            // 6. Doctor Name
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("6. Doctor Name", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Text(record.doctorName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                }
            }

            // 7. Hospital Name & Hospital location
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("7. Hospital Name & Hospital location", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Text("${record.hospitalName} • Location: ${record.hospitalLocation}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                }
            }

            // 8. Father's or Mother's mobile number (Masked in normal views: 98XXXXXX21)
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("8. Father's or Mother's mobile number", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        Text(
                            text = if (isPhoneRevealed) record.parentMobile else record.maskedMobile,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isPhoneRevealed) Color(0xFFB91C1C) else Color(0xFF0F172A)
                        )
                        if (isPhoneRevealed) {
                            Text("Authorized Reveal Active ($revealCountdown s remaining)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                        }
                    }

                    OutlinedButton(
                        onClick = onRevealClicked,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isPhoneRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPhoneRevealed) "Hide" else "Authorized Reveal", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }
            }
        }
    }
}

@Composable
fun CouncilFieldRow(
    title: String,
    value: String,
    imageUrl: String
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
