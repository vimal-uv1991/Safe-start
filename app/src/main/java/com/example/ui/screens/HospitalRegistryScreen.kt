package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class HospitalSubTab {
    NEW_REGISTRATION_TABLE,
    HOSPITAL_RECORDS_24H,
    RAISE_COMPLAINT_DESK
}

@Composable
fun HospitalRegistryScreen(
    onNavigateToCouncil: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by SafeStartRepository.records.collectAsState()
    val complaints by SafeStartRepository.complaints.collectAsState()

    var activeTab by remember { mutableStateOf(HospitalSubTab.NEW_REGISTRATION_TABLE) }

    // Hospital Identity (Admin fills on their own)
    var hospitalName by remember { mutableStateOf("Government Rajaji Hospital") }
    var hospitalLocation by remember { mutableStateOf("Madurai, Tamil Nadu") }
    val hospitalAdminId = "HOSP-TN-MDU-74291"

    // Blank Tabular Form State (8 Mandatory Fields)
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var childGender by remember { mutableStateOf("Female") }
    var birthDate by remember { mutableStateOf("14/10/2024") }
    var birthTime by remember { mutableStateOf("04:18:22 IST") }
    var doctorName by remember { mutableStateOf("Dr. R. Shanmugam, MD, DCH") }
    var parentMobile by remember { mutableStateOf("+91 98401 92821") }

    // Uploaded Biometrics (JPG/PNG simulated uploads)
    var fatherScanUrl by remember { mutableStateOf(SafeStartAssets.FATHER_FOOTPRINT) }
    var motherScanUrl by remember { mutableStateOf(SafeStartAssets.MOTHER_FOOTPRINT) }
    var childScanUrl by remember { mutableStateOf(SafeStartAssets.NEWBORN_FOOTPRINT) }
    var fatherUploadedFileName by remember { mutableStateOf("father_footprint_scan.png") }
    var motherUploadedFileName by remember { mutableStateOf("mother_footprint_scan.png") }
    var childUploadedFileName by remember { mutableStateOf("child_footprint_scan.png") }

    // Duplicate detection state
    var duplicateDetectionActive by remember { mutableStateOf(false) }
    var isDuplicateBlocked by remember { mutableStateOf(false) }

    // Parental Confirmation Dialog
    var showParentalVerificationModal by remember { mutableStateOf(false) }
    var verificationLanguage by remember { mutableStateOf("Tamil") } // Tamil, Malayalam, English
    var parentVerified by remember { mutableStateOf(false) }

    // Post-Submission SMS Modal
    var showSmsConfirmationModal by remember { mutableStateOf(false) }
    var lastSubmittedRecord by remember { mutableStateOf<NewbornRecord?>(null) }

    // 24h Countdown & phone masking in local records
    var remainingSeconds by remember { mutableStateOf(85694L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (remainingSeconds > 0) remainingSeconds -= 1
        }
    }
    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val countdownText = String.format("%02dh %02dm %02ds", hours, minutes, seconds)

    var revealedTokenId by remember { mutableStateOf<String?>(null) }

    // Complaint Desk state
    var complaintChildName by remember { mutableStateOf("") }
    var complaintIssueType by remember { mutableStateOf("Discomfort") } // Discomfort, Partiality, Others
    var complaintDetails by remember { mutableStateOf("") }
    var complaintBirthCertUploaded by remember { mutableStateOf(true) }
    var complaintPassportPhotoUploaded by remember { mutableStateOf(true) }

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
            .testTag("hospital_registry_screen"),
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
            title = "HOSPITAL REGISTRAR & BIOMETRIC CUSTODY PORTAL",
            subtitle = "$hospitalName • $hospitalLocation • Facility Node: $hospitalAdminId",
            icon = Icons.Default.LocalHospital
        )

        // TWO/THREE-OPTION SEGMENTED TABS (DIRECTLY BELOW HERO)
        GovSegmentedTabs(
            options = listOf(
                "NEW REGISTRY" to Icons.Default.TableChart,
                "24H EDIT LOG" to Icons.Default.AccessTime,
                "COMPLAINT DESK" to Icons.Default.ReportProblem
            ),
            selectedIndex = when (activeTab) {
                HospitalSubTab.NEW_REGISTRATION_TABLE -> 0
                HospitalSubTab.HOSPITAL_RECORDS_24H -> 1
                HospitalSubTab.RAISE_COMPLAINT_DESK -> 2
            },
            onSelectIndex = { index ->
                activeTab = when (index) {
                    0 -> HospitalSubTab.NEW_REGISTRATION_TABLE
                    1 -> HospitalSubTab.HOSPITAL_RECORDS_24H
                    else -> HospitalSubTab.RAISE_COMPLAINT_DESK
                }
            }
        )

        when (activeTab) {
            HospitalSubTab.NEW_REGISTRATION_TABLE -> {
                // =========================================================================
                // TABULAR COLUMN BLANK PAGE FOR HOSPITAL ADMIN (8 SPECIFIED BOXES)
                // =========================================================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "TABULAR REGISTRATION FORM (NEWBORN CUSTODY)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Hospital Admin is solely authorized to upload biometric sets (JPG/PNG). Automatic duplicate verification is performed prior to entry.",
                                fontSize = 11.sp,
                                color = Color(0xFF334155),
                                lineHeight = 15.sp
                            )
                        }

                        // DUPLICATE DETECTION NOTICE & TOGGLE
                        Surface(
                            color = if (isDuplicateBlocked) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isDuplicateBlocked) Color(0xFFEF4444) else Color(0xFF10B981).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDuplicateBlocked) Icons.Default.Block else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isDuplicateBlocked) Color(0xFFB91C1C) else Color(0xFF047857),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (isDuplicateBlocked)
                                                "DUPLICATE DETECTED: REGISTRATION BLOCKED!"
                                            else
                                                "Biometric Collision Engine: Active (0 Collision)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDuplicateBlocked) Color(0xFFB91C1C) else Color(0xFF047857)
                                        )
                                        Text(
                                            text = if (isDuplicateBlocked)
                                                "The scanned biometrics match an existing infant registered under Token #TN-2024-MDU-8822. Submission is strictly barred."
                                            else
                                                "Compares Father, Mother and Child biometrics with statewide database.",
                                            fontSize = 10.sp,
                                            color = if (isDuplicateBlocked) Color(0xFF7F1D1D) else Color(0xFF065F46)
                                        )
                                    }
                                }

                                Switch(
                                    checked = isDuplicateBlocked,
                                    onCheckedChange = { isDuplicateBlocked = it },
                                    modifier = Modifier.testTag("toggle_duplicate_simulation")
                                )
                            }
                        }

                        // 8 SPECIFIED BOXES IN TABULAR COLUMN FORM
                        // ----------------------------------------------------
                        // Box 1: Father's biometric (footprint) & name
                        // ----------------------------------------------------
                        TabularBoxContainer(boxNumber = "1", title = "Father's biometric (footprint) & name") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = fatherName,
                                    onValueChange = { fatherName = it },
                                    label = { Text("Father's Full Name") },
                                    placeholder = { Text("e.g. Murugan K.") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("input_father_name")
                                )

                                BiometricUploadRow(
                                    label = "Father's Footprint Biometric (JPG / PNG)",
                                    imageUrl = fatherScanUrl,
                                    fileName = fatherUploadedFileName,
                                    onUploadClicked = {
                                        fatherUploadedFileName = "father_scan_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
                                        Toast.makeText(context, "Father footprint image uploaded successfully", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        // ----------------------------------------------------
                        // Box 2: Mother's biometric (footprint) & name
                        // ----------------------------------------------------
                        TabularBoxContainer(boxNumber = "2", title = "Mother's biometric (footprint) & name") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = motherName,
                                    onValueChange = { motherName = it },
                                    label = { Text("Mother's Full Name") },
                                    placeholder = { Text("e.g. Lakshmi M.") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("input_mother_name")
                                )

                                BiometricUploadRow(
                                    label = "Mother's Footprint Biometric (JPG / PNG)",
                                    imageUrl = motherScanUrl,
                                    fileName = motherUploadedFileName,
                                    onUploadClicked = {
                                        motherUploadedFileName = "mother_scan_${System.currentTimeMillis().toString().takeLast(4)}.png"
                                        Toast.makeText(context, "Mother footprint image uploaded successfully", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        // ----------------------------------------------------
                        // Box 3: Child's footprint
                        // ----------------------------------------------------
                        TabularBoxContainer(boxNumber = "3", title = "Child's footprint") {
                            BiometricUploadRow(
                                label = "Newborn Infant Footprint (High-Resolution JPG / PNG)",
                                imageUrl = childScanUrl,
                                fileName = childUploadedFileName,
                                onUploadClicked = {
                                    childUploadedFileName = "newborn_scan_${System.currentTimeMillis().toString().takeLast(4)}.png"
                                    Toast.makeText(context, "Child footprint image uploaded successfully", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // ----------------------------------------------------
                        // Box 4: Child's Gender
                        // ----------------------------------------------------
                        TabularBoxContainer(boxNumber = "4", title = "Child's Gender") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Female", "Male", "Intersex").forEach { gender ->
                                    val isSelected = childGender == gender
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { childGender = gender },
                                        label = {
                                            Text(
                                                gender,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color(0xFF0F172A)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TnPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // ----------------------------------------------------
                        // Box 5: Birth DATE/MONTH/YEAR & TIME
                        // ----------------------------------------------------
                        TabularBoxContainer(boxNumber = "5", title = "Birth DATE/MONTH/YEAR & TIME") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = birthDate,
                                    onValueChange = { birthDate = it },
                                    label = { Text("Birth DATE/MONTH/YEAR") },
                                    placeholder = { Text("DD/MM/YYYY") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f).testTag("input_birth_date")
                                )
                                OutlinedTextField(
                                    value = birthTime,
                                    onValueChange = { birthTime = it },
                                    label = { Text("TIME (IST)") },
                                    placeholder = { Text("HH:MM:SS IST") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("input_birth_time")
                                )
                            }
                        }

                        // ----------------------------------------------------
                        // Box 6: Doctor Name
                        // ----------------------------------------------------
                        TabularBoxContainer(boxNumber = "6", title = "Doctor Name") {
                            OutlinedTextField(
                                value = doctorName,
                                onValueChange = { doctorName = it },
                                label = { Text("Attending Obstetrician / Pediatrician Doctor Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_doctor_name")
                            )
                        }

                        // ----------------------------------------------------
                        // Box 7: Hospital Name & Hospital location (Filled on own)
                        // ----------------------------------------------------
                        TabularBoxContainer(boxNumber = "7", title = "Hospital Name & Hospital location") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = hospitalName,
                                    onValueChange = { hospitalName = it },
                                    label = { Text("Hospital Name (Editable on your own)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("input_hospital_name")
                                )
                                OutlinedTextField(
                                    value = hospitalLocation,
                                    onValueChange = { hospitalLocation = it },
                                    label = { Text("Hospital Exact Location (District / Facility Ward)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("input_hospital_location")
                                )
                            }
                        }

                        // ----------------------------------------------------
                        // Box 8: Father's or Mother's mobile number
                        // ----------------------------------------------------
                        TabularBoxContainer(boxNumber = "8", title = "Father's or Mother's mobile number") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedTextField(
                                    value = parentMobile,
                                    onValueChange = { parentMobile = it },
                                    label = { Text("Parent Mobile Number") },
                                    placeholder = { Text("+91 98401 92821") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("input_parent_mobile")
                                )
                                Text(
                                    text = "Mobile number will be masked (e.g., 98XXXXXX21) in normal views and only revealed for authorized verification.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        // SUBMISSION ACTIONS
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Parental Verification Trigger Button
                            Button(
                                onClick = {
                                    if (fatherName.isEmpty() || motherName.isEmpty()) {
                                        Toast.makeText(context, "Please enter Father and Mother names!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (isDuplicateBlocked) {
                                        Toast.makeText(context, "Registration blocked due to biometric duplicate!", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    showParentalVerificationModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TnDeepTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_parental_verification")
                            ) {
                                Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Display Entered Information to Parents for Verification", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            if (parentVerified) {
                                Surface(
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                                        Text(
                                            text = "Parental confirmation completed in $verificationLanguage. Ready for sovereign database submission.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val newToken = "TN-${Calendar.getInstance().get(Calendar.YEAR)}-MDU-${(1000..9999).random()}"
                                        val newRecord = NewbornRecord(
                                            token = newToken,
                                            fatherName = fatherName,
                                            motherName = motherName,
                                            gender = childGender,
                                            birthTimestamp = "$birthDate, $birthTime",
                                            doctorName = doctorName,
                                            hospitalName = hospitalName,
                                            district = hospitalLocation.split(",").firstOrNull() ?: hospitalLocation,
                                            hospitalLocation = hospitalLocation,
                                            parentMobile = parentMobile,
                                            status = "3-Party Validated",
                                            biometricHash = UUID.randomUUID().toString().replace("-", ""),
                                            secondsRemaining = 86400L,
                                            isCouncilLocked = false
                                        )
                                        SafeStartRepository.addRecord(newRecord)
                                        lastSubmittedRecord = newRecord
                                        showSmsConfirmationModal = true
                                        successToastMsg = "NEWBORN REGISTRATION SUBMITTED" to "Token ${newRecord.token} securely signed and entered into Tamil Nadu State Custody Registry."
                                        smsToastMsg = "GOVERNMENT SMS DISPATCHED (+91 ${newRecord.parentMobile})" to "TN-GOVT: Newborn birth biometric identity registered. Token: ${newRecord.token}. Edit window: 24 Hours. Support: 104."
                                        // Reset fields for next blank entry
                                        parentVerified = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_submit_final_dossier")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Record & Send Parent Confirmation SMS", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            HospitalSubTab.HOSPITAL_RECORDS_24H -> {
                // =========================================================================
                // 24-HOUR EDIT WINDOW ENFORCEMENT & RECORD LIST
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "STATUTORY 24-HOUR AMEND WINDOW",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Hospital members can edit within 24 hours only. After 24h, records cannot be edited or deleted.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155)
                                )
                            }
                            Surface(
                                color = Color(0xFFFFFBEB),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SovereignGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = SovereignGoldDark, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = countdownText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = SovereignGoldDark
                                    )
                                }
                            }
                        }

                        Divider(color = Color(0xFFE2E8F0))

                        // Filter strictly for local hospital records as required:
                        // "Hospital members can create and update the data and they can't visit any other information stored in the app"
                        val localRecords = records.filter { it.hospitalName.contains("Rajaji", ignoreCase = true) || it.hospitalName == hospitalName }

                        if (localRecords.isEmpty()) {
                            Text("No records found for this hospital yet.", color = Color(0xFF64748B), fontSize = 12.sp)
                        } else {
                            localRecords.forEach { record ->
                                val is24hExpired = record.secondsRemaining <= 0
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
                                                text = record.token,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = TnDeepTeal
                                            )
                                            Surface(
                                                color = if (is24hExpired) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (is24hExpired) "24h EXPIRED • LOCKED" else "EDITABLE (Within 24h)",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (is24hExpired) Color(0xFF991B1B) else Color(0xFF166534)
                                                )
                                            }
                                        }

                                        // Infant & parents details
                                        Text("• Child: ${record.gender} • Born: ${record.birthTimestamp}", fontSize = 12.sp, color = Color(0xFF0F172A))
                                        Text("• Parents: ${record.fatherName} & ${record.motherName}", fontSize = 12.sp, color = Color(0xFF0F172A))
                                        Text("• Doctor: ${record.doctorName}", fontSize = 12.sp, color = Color(0xFF334155))

                                        // Masked Mobile Display
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val isRevealed = revealedTokenId == record.token
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = "Parent Mobile: " + (if (isRevealed) record.parentMobile else record.maskedMobile),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }
                                            TextButton(
                                                onClick = {
                                                    revealedTokenId = if (revealedTokenId == record.token) null else record.token
                                                }
                                            ) {
                                                Text(if (isRevealed) "Hide" else "Authorized Reveal", fontSize = 11.sp, color = TnDeepTeal)
                                            }
                                        }

                                        // Actions: Edit button enabled ONLY if within 24 hours
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (is24hExpired) {
                                                        Toast.makeText(context, "Cannot edit! 24-hour statutory window has elapsed.", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Editing permitted within 24h window for ${record.token}", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                enabled = !is24hExpired,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = if (is24hExpired) Color(0xFF94A3B8) else TnDeepTeal
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (is24hExpired) "Edit Disabled" else "Amend Record",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (is24hExpired) Color(0xFF94A3B8) else TnDeepTeal
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    Toast.makeText(context, "Deleting newborn records is barred by Tamil Nadu Civic Code.", Toast.LENGTH_SHORT).show()
                                                },
                                                enabled = false,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF94A3B8))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Delete Barred", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HospitalSubTab.RAISE_COMPLAINT_DESK -> {
                // =========================================================================
                // COMPLAINT RAISED THROUGH THE HOSPITAL (AGAINST PARENTS)
                // • They should use the Hospital Admin ID
                // • Hospital Admin needs to enter their complaint against the parents
                // • Upload Child Birth Certificate, Passport size photo, Name
                // • What kind of issue: "Discomfort", "Partiality", or "Others"
                // • Submitted complaint can viewed ONLY by the Government Medical Council (read-only)
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
                            text = "RAISE CHILD DISPUTE / GRIEVANCE DESK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "If a child or family raises a claim of doubt, partiality, or discomfort, the Hospital Admin registers the statutory complaint directly with the Government Medical Council.",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            lineHeight = 15.sp
                        )

                        // Hospital Admin ID (Pre-locked)
                        OutlinedTextField(
                            value = hospitalAdminId,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hospital Admin ID (Authorized Filing Officer)") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TnDeepTeal) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = complaintChildName,
                            onValueChange = { complaintChildName = it },
                            label = { Text("Child Full Name / Newborn Token ID") },
                            placeholder = { Text("e.g. Baby of Lakshmi (Token #8831)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_complaint_child_name")
                        )

                        // Kind of issue: Discomfort, Partiality, or Others
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Kind of Issue Faced with Parents (Required)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Discomfort", "Partiality", "Others").forEach { issue ->
                                    val isSelected = complaintIssueType == issue
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { complaintIssueType = issue },
                                        label = {
                                            Text(
                                                issue,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color(0xFF0F172A)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TnPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Required Document Uploads
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("MANDATORY DOCUMENT ATTACHMENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = TnDeepTeal, modifier = Modifier.size(16.dp))
                                        Text("Child Birth Certificate (PDF/Image)", fontSize = 12.sp, color = Color(0xFF0F172A))
                                    }
                                    Button(
                                        onClick = {
                                            complaintBirthCertUploaded = true
                                            Toast.makeText(context, "Birth Certificate verified and attached.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (complaintBirthCertUploaded) Color(0xFF047857) else TnPrimary),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(if (complaintBirthCertUploaded) "Uploaded ✓" else "Upload Certificate", fontSize = 10.sp)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.AccountBox, contentDescription = null, tint = TnDeepTeal, modifier = Modifier.size(16.dp))
                                        Text("Passport Size Photo of Child", fontSize = 12.sp, color = Color(0xFF0F172A))
                                    }
                                    Button(
                                        onClick = {
                                            complaintPassportPhotoUploaded = true
                                            Toast.makeText(context, "Passport Photo attached successfully.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (complaintPassportPhotoUploaded) Color(0xFF047857) else TnPrimary),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(if (complaintPassportPhotoUploaded) "Uploaded ✓" else "Upload Photo", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        // Complaint statement
                        OutlinedTextField(
                            value = complaintDetails,
                            onValueChange = { complaintDetails = it },
                            label = { Text("Detailed Description of Dispute / Grievance") },
                            placeholder = { Text("State details of the dispute raised against the parents...") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("input_complaint_details")
                        )

                        // Submit complaint to Council
                        Button(
                            onClick = {
                                if (complaintChildName.isEmpty() || complaintDetails.isEmpty()) {
                                    Toast.makeText(context, "Please provide Child Name and Details!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val newComplaint = HospitalComplaint(
                                    complaintId = "CMP-HOSP-2025-" + (100..999).random(),
                                    hospitalAdminId = hospitalAdminId,
                                    hospitalName = hospitalName,
                                    hospitalLocation = hospitalLocation,
                                    childName = complaintChildName,
                                    issueType = complaintIssueType,
                                    details = complaintDetails,
                                    filedTimestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                                )
                                SafeStartRepository.addComplaint(newComplaint)
                                Toast.makeText(context, "Complaint ${newComplaint.complaintId} transmitted to Medical Council!", Toast.LENGTH_LONG).show()
                                complaintChildName = ""
                                complaintDetails = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_submit_complaint")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Complaint to Government Medical Council", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "Notice: Submitted complaints can be viewed ONLY by the Government Medical Council. Council officials cannot overwrite hospital submissions.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL 1: PARENTAL VERIFICATION MODAL WITH LANGUAGE SWITCH (TAMIL / MALAYALAM)
    // =========================================================================
    if (showParentalVerificationModal) {
        Dialog(onDismissRequest = { showParentalVerificationModal = false }) {
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (verificationLanguage) {
                                    "Tamil" -> "பெற்றோர் சரிபார்ப்பு படிவம்"
                                    "Malayalam" -> "മാതാപിതാക്കളുടെ സ്ഥിരീകരണ ഫോം"
                                    else -> "PARENTAL VERIFICATION FORM"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TnDeepTeal
                            )
                            Text(
                                text = "Before final submission, verify all details with parents.",
                                fontSize = 10.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        // LANGUAGE SWITCH (Tamil, Malayalam, English)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Tamil", "Malayalam", "English").forEach { lang ->
                                val isLangSelected = verificationLanguage == lang
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { verificationLanguage = lang },
                                    color = if (isLangSelected) TnDeepTeal else Color(0xFFE2E8F0)
                                ) {
                                    Text(
                                        text = when (lang) {
                                            "Tamil" -> "தமிழ்"
                                            "Malayalam" -> "മലയാളം"
                                            else -> "EN"
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLangSelected) Color.White else Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    // All entered information displayed clearly for verification
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = when (verificationLanguage) {
                                    "Tamil" -> "பதிவு செய்யப்பட்ட விவரங்கள்:"
                                    "Malayalam" -> "രേഖപ്പെടുത്തിയ വിശദാംശങ്ങൾ:"
                                    else -> "Recorded Particulars:"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text("1. ${if (verificationLanguage == "Tamil") "தந்தை பெயர்" else if (verificationLanguage == "Malayalam") "പിതാവിന്റെ പേര്" else "Father Name"}: $fatherName", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("2. ${if (verificationLanguage == "Tamil") "தாய் பெயர்" else if (verificationLanguage == "Malayalam") "മാതാവിന്റെ പേര്" else "Mother Name"}: $motherName", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("3. ${if (verificationLanguage == "Tamil") "குழந்தை பாலினம்" else if (verificationLanguage == "Malayalam") "ലിംഗം" else "Child Gender"}: $childGender", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("4. ${if (verificationLanguage == "Tamil") "பிறந்த தேதி & நேரம்" else if (verificationLanguage == "Malayalam") "ജനന തീയതി & സമയം" else "Birth Date & Time"}: $birthDate, $birthTime", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("5. ${if (verificationLanguage == "Tamil") "மருத்துவர்" else if (verificationLanguage == "Malayalam") "ഡോക്ടർ" else "Doctor"}: $doctorName", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("6. ${if (verificationLanguage == "Tamil") "மருத்துவமனை & இருப்பிடம்" else if (verificationLanguage == "Malayalam") "ആശുപത്രി & സ്ഥലം" else "Hospital & Location"}: $hospitalName, $hospitalLocation", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("7. ${if (verificationLanguage == "Tamil") "தொடர்பு எண்" else if (verificationLanguage == "Malayalam") "മൊബൈൽ നമ്പർ" else "Mobile Number"}: $parentMobile", fontSize = 12.sp, color = Color(0xFF0F172A))
                        }
                    }

                    // Confirmation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showParentalVerificationModal = false },
                            border = BorderStroke(1.dp, Color(0xFF475569)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit Info", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }

                        Button(
                            onClick = {
                                parentVerified = true
                                showParentalVerificationModal = false
                                Toast.makeText(context, "Parent verified data in $verificationLanguage. You may now submit.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                            modifier = Modifier.weight(2f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (verificationLanguage) {
                                    "Tamil" -> "சரிபார்க்கப்பட்டது ✓"
                                    "Malayalam" -> "സ്ഥിരീകരിച്ചു ✓"
                                    else -> "Confirm Verified ✓"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL 2: PARENT CONFIRMATION SMS DISPATCHED CARD
    // =========================================================================
    if (showSmsConfirmationModal && lastSubmittedRecord != null) {
        val record = lastSubmittedRecord!!
        Dialog(onDismissRequest = { showSmsConfirmationModal = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(2.dp, Color(0xFF059669)),
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF059669))
                        }
                        Column {
                            Text(
                                text = "OFFICIAL SMS SENT TO PARENT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "Dispatched to registered mobile: ${record.maskedMobile}",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    // SMS Content Preview (exact requirements)
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "GOVT-TN-BIRTH-REG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TnDeepTeal
                            )
                            Text(
                                text = "Safe Start / காவல் துவக்கம்: உங்கள் குழந்தை பிறப்பு பதிவு வெற்றிகரமாக சமர்ப்பிக்கப்பட்டது.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text("• Name of Parents: ${record.fatherName} & ${record.motherName}", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("• Hospital Name: ${record.hospitalName}", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("• Date of Birth: ${record.birthTimestamp}", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("• Assigned Identity Token: ${record.token}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TnDeepTeal)
                            Text(
                                text = "Confirmation message: Birth dossier securely sealed into Tamil Nadu State Health Ledger.",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Button(
                        onClick = { showSmsConfirmationModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Done / Close SMS Receipt", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TabularBoxContainer(
    boxNumber: String,
    title: String,
    statusPillText: String? = null,
    isStatusVerified: Boolean = true,
    content: @Composable () -> Unit
) {
    GovSectionCard(
        number = boxNumber,
        title = title.uppercase(),
        statusPillText = statusPillText,
        isStatusVerified = isStatusVerified,
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun BiometricUploadRow(
    label: String,
    imageUrl: String,
    fileName: String,
    onUploadClicked: () -> Unit
) {
    GovBiometricCaptureBox(
        label = label,
        previewUrl = imageUrl,
        fileName = fileName,
        onCapture = { onUploadClicked() },
        onUpload = { onUploadClicked() },
        onClear = { /* Clear action handled */ },
        modifier = Modifier.fillMaxWidth()
    )
}
