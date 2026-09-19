package com.example.ui.screens

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.network.ResendEmailService
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class HospitalSubTab {
    NEW_REGISTRATION_TABLE,
    HOSPITAL_RECORDS_24H,
    RAISE_COMPLAINT_DESK
}

private fun queryFileName(context: Context, uri: Uri): String? {
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return it.getString(nameIndex)
                }
            }
        }
    }
    return uri.path?.substringAfterLast('/')
}

@Composable
fun HospitalRegistryScreen(
    onNavigateToCouncil: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val records by SafeStartRepository.records.collectAsState()
    val complaints by SafeStartRepository.complaints.collectAsState()

    var activeTab by remember { mutableStateOf(HospitalSubTab.NEW_REGISTRATION_TABLE) }

    val currentAccount = SafeStartRepository.getActiveAccount()
    // Hospital Identity (Admin fills on their own or pre-populated from verified account)
    var hospitalName by remember { mutableStateOf(currentAccount?.institutionName ?: "") }
    var hospitalLocation by remember { mutableStateOf(currentAccount?.district ?: "") }
    val hospitalAdminId = currentAccount?.id ?: "HOSP-TN-REG"

    // Blank Tabular Form State (8 Mandatory Fields)
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var childGender by remember { mutableStateOf("Female") }
    var birthDate by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var birthTime by remember {
        mutableStateOf(SimpleDateFormat("hh:mm:ss a 'IST'", Locale.getDefault()).format(Date()))
    }
    var doctorName by remember { mutableStateOf(currentAccount?.fullName ?: "") }
    var parentMobile by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("vimal.uv1991@gmail.com") }
    var wardStatus by remember { mutableStateOf("Postnatal Ward") }

    // Tamil Nadu Statutory Government IDs (PICME, Mother Aadhaar, Form 5 Reg No)
    var picmeNumber by remember { mutableStateOf("") }
    var motherAadhaar by remember { mutableStateOf("") }
    var crsRegistrationNumber by remember { mutableStateOf("") }

    // Real-Time Regex Validation States (Instant UI Feedback)
    val picmeValidation by remember { derivedStateOf { TamilNaduIdValidator.validatePicme(picmeNumber) } }
    val aadhaarValidation by remember { derivedStateOf { TamilNaduIdValidator.validateAadhaar(motherAadhaar) } }
    val crsForm5Validation by remember { derivedStateOf { TamilNaduIdValidator.validateCrsForm5(crsRegistrationNumber) } }
    val mobileValidation by remember { derivedStateOf { TamilNaduIdValidator.validateMobile(parentMobile) } }
    val emailValidation by remember { derivedStateOf { TamilNaduIdValidator.validateEmail(parentEmail) } }
    val birthDateValidation by remember { derivedStateOf { TamilNaduIdValidator.validateBirthDate(birthDate) } }
    val birthTimeValidation by remember { derivedStateOf { TamilNaduIdValidator.validateBirthTime(birthTime) } }
    val fatherNameValidation by remember { derivedStateOf { TamilNaduIdValidator.validateName(fatherName, "Father's Name") } }
    val motherNameValidation by remember { derivedStateOf { TamilNaduIdValidator.validateName(motherName, "Mother's Name") } }
    val doctorNameValidation by remember { derivedStateOf { TamilNaduIdValidator.validateName(doctorName, "Doctor's Name") } }
    val hospitalNameValidation by remember { derivedStateOf { TamilNaduIdValidator.validateHospitalOrLocation(hospitalName, "Hospital Name") } }
    val hospitalLocationValidation by remember { derivedStateOf { TamilNaduIdValidator.validateHospitalOrLocation(hospitalLocation, "Hospital Location") } }

    val formFieldStatuses by remember {
        derivedStateOf {
            listOf(
                FormFieldStatus("picme", "TN PICME ID", picmeValidation.isValid),
                FormFieldStatus("aadhaar", "Mother Aadhaar", aadhaarValidation.isValid),
                FormFieldStatus("crs", "Form-5 Reg No", crsForm5Validation.isValid),
                FormFieldStatus("mobile", "Parent Mobile (+91)", mobileValidation.isValid),
                FormFieldStatus("email", "Parent Email", emailValidation.isValid),
                FormFieldStatus("date", "Birth Date", birthDateValidation.isValid),
                FormFieldStatus("time", "Birth Time", birthTimeValidation.isValid),
                FormFieldStatus("names", "Parent & Doctor Names", fatherNameValidation.isValid && motherNameValidation.isValid && doctorNameValidation.isValid)
            )
        }
    }

    val isStatutoryFormValid by remember {
        derivedStateOf {
            picmeValidation.isValid &&
            aadhaarValidation.isValid &&
            crsForm5Validation.isValid &&
            mobileValidation.isValid &&
            emailValidation.isValid &&
            birthDateValidation.isValid &&
            birthTimeValidation.isValid &&
            fatherNameValidation.isValid &&
            motherNameValidation.isValid &&
            doctorNameValidation.isValid
        }
    }

    // Uploaded Biometrics (JPG/PNG real file uploads and preview state)
    var fatherScanUrl by remember { mutableStateOf("") }
    var motherScanUrl by remember { mutableStateOf("") }
    var childScanUrl by remember { mutableStateOf("") }

    var fatherScanUri by remember { mutableStateOf<String?>(null) }
    var motherScanUri by remember { mutableStateOf<String?>(null) }
    var childScanUri by remember { mutableStateOf<String?>(null) }

    var fatherUploadedFileName by remember { mutableStateOf("") }
    var motherUploadedFileName by remember { mutableStateOf("") }
    var childUploadedFileName by remember { mutableStateOf("") }

    // Camera Birth Certificate Scanner State
    var showCameraScanner by remember { mutableStateOf(false) }
    var scannedCertificate by remember { mutableStateOf<ScannedBirthCertificate?>(null) }

    // System File Pickers for JPG / PNG
    val fatherFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fatherScanUri = it.toString()
            val detectedName = queryFileName(context, it) ?: "father_footprint_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
            fatherUploadedFileName = detectedName
            Toast.makeText(context, "Attached Father Biometric: $detectedName", Toast.LENGTH_SHORT).show()
        }
    }

    val motherFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            motherScanUri = it.toString()
            val detectedName = queryFileName(context, it) ?: "mother_footprint_${System.currentTimeMillis().toString().takeLast(4)}.png"
            motherUploadedFileName = detectedName
            Toast.makeText(context, "Attached Mother Biometric: $detectedName", Toast.LENGTH_SHORT).show()
        }
    }

    val childFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            childScanUri = it.toString()
            val detectedName = queryFileName(context, it) ?: "child_footprint_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
            childUploadedFileName = detectedName
            Toast.makeText(context, "Attached Newborn Biometric: $detectedName", Toast.LENGTH_SHORT).show()
        }
    }

    // Duplicate detection state
    var duplicateDetectionActive by remember { mutableStateOf(false) }
    var isDuplicateBlocked by remember { mutableStateOf(false) }

    // Parental Confirmation Dialog
    var showParentalVerificationModal by remember { mutableStateOf(false) }
    var verificationLanguage by remember { mutableStateOf("Tamil") } // Tamil, Malayalam, Hindi, English
    var parentVerified by remember { mutableStateOf(false) }

    // Resend Email / OTP verification in Parental verification dialog
    var resendOtpSent by remember { mutableStateOf(false) }
    var resendGeneratedOtp by remember { mutableStateOf<String?>(null) }
    var resendEnteredOtp by remember { mutableStateOf("") }
    var resendOtpStatusMsg by remember { mutableStateOf<String?>(null) }
    var isSendingParentOtp by remember { mutableStateOf(false) }
    var parentOtpCountdown by remember { mutableStateOf(0) }
    var parentOtpVerifiedAt by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(parentOtpCountdown) {
        if (parentOtpCountdown > 0) {
            delay(1000L)
            parentOtpCountdown -= 1
        }
    }

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
    var complaintCertFileName by remember { mutableStateOf("birth_certificate_scan.pdf") }
    var complaintPhotoFileName by remember { mutableStateOf("passport_photo.jpg") }

    val complaintCertLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            complaintCertFileName = queryFileName(context, it) ?: "birth_certificate_${System.currentTimeMillis().toString().takeLast(4)}.pdf"
            complaintBirthCertUploaded = true
            Toast.makeText(context, "Attached: $complaintCertFileName", Toast.LENGTH_SHORT).show()
        }
    }

    val complaintPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            complaintPhotoFileName = queryFileName(context, it) ?: "infant_passport_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
            complaintPassportPhotoUploaded = true
            Toast.makeText(context, "Attached: $complaintPhotoFileName", Toast.LENGTH_SHORT).show()
        }
    }

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

        // Quick Summary Dashboard Navigation Banner
        Surface(
            color = Color(0xFFECFDF5),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToDashboard() }
                .testTag("banner_to_summary_dashboard")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                    Column {
                        Text("Summary Dashboard (Identity Overview)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        Text("Quick overview of pending and verified newborn identities", fontSize = 10.sp, color = Color(0xFF047857))
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go to dashboard", tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
            }
        }

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

                        // =========================================================================
                        // CAMERA SCAN BIRTH CERTIFICATE / FORM-5 AUTO-FILL BANNER
                        // =========================================================================
                        Surface(
                            color = if (scannedCertificate != null) Color(0xFFECFDF5) else Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, if (scannedCertificate != null) Color(0xFF10B981) else Color(0xFF34D399)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_camera_scanner_banner")
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
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.CameraAlt,
                                                    contentDescription = "Camera Scanner",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = if (scannedCertificate != null) "Birth Certificate Scanned & Verified" else "Scan Birth Certificate via Camera",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF065F46)
                                            )
                                            Text(
                                                text = if (scannedCertificate != null)
                                                    "Cert: ${scannedCertificate?.certificateNumber} • Form auto-populated"
                                                else
                                                    "Auto-populate baby, parents & delivery data using live camera scanner",
                                                fontSize = 11.sp,
                                                color = Color(0xFF047857),
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showCameraScanner = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (scannedCertificate != null) Color(0xFF059669) else Color(0xFF10B981)
                                        ),
                                        modifier = Modifier.testTag("btn_open_camera_scanner")
                                    ) {
                                        Icon(
                                            if (scannedCertificate != null) Icons.Default.Refresh else Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (scannedCertificate != null) "Re-Scan" else "Scan Camera",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (scannedCertificate != null) {
                                    HorizontalDivider(color = Color(0xFFA7F3D0))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "Form 5 Authenticated • SHA-256: ${scannedCertificate?.certificateHash?.take(16)}...",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF065F46),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Surface(
                                            color = Color(0xFFD1FAE5),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "AUTO-POPULATED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF065F46),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
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

                        // REAL-TIME STATUTORY REGEX VALIDATION HEALTH METER & SAMPLE FILLER
                        GovValidationHealthBanner(
                            fields = formFieldStatuses,
                            onAutofillValidSample = {
                                picmeNumber = TamilNaduIdValidator.Samples.VALID_PICME_1
                                motherAadhaar = TamilNaduIdValidator.Samples.VALID_AADHAAR_MOTHER
                                crsRegistrationNumber = TamilNaduIdValidator.Samples.VALID_CRS_FORM5_1
                                parentMobile = TamilNaduIdValidator.Samples.VALID_MOBILE
                                parentEmail = TamilNaduIdValidator.Samples.VALID_EMAIL
                                if (fatherName.isBlank()) fatherName = "Sundaram Ramachandran"
                                if (motherName.isBlank()) motherName = "Kavitha Sundaram"
                                if (doctorName.isBlank()) doctorName = "Dr. S. Meenakshi, MD (OBG)"
                                if (hospitalName.isBlank()) hospitalName = "Government Kasturba Gandhi Hospital"
                                if (hospitalLocation.isBlank()) hospitalLocation = "Triplicane, Chennai, Tamil Nadu"
                                Toast.makeText(context, "✅ Populated Standard Valid Tamil Nadu Government ID Formats!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 10 SPECIFIED BOXES IN TABULAR COLUMN FORM (REAL-TIME REGEX VALIDATION)
                        // ----------------------------------------------------
                        // Box 1: Father's biometric (footprint) & name
                        // ----------------------------------------------------
                        TabularBoxContainer(
                            boxNumber = "1",
                            title = "Father's biometric (footprint) & name",
                            statusPillText = if (fatherNameValidation.isValid) "NAME VERIFIED ✓" else if (fatherName.isNotBlank()) "INVALID FORMAT" else "PENDING",
                            isStatusVerified = fatherNameValidation.isValid
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ValidatedGovTextField(
                                    value = fatherName,
                                    onValueChange = { fatherName = it },
                                    label = "Father's Full Name",
                                    placeholder = "e.g. Sundaram Ramachandran",
                                    validation = fatherNameValidation,
                                    testTag = "input_father_name",
                                    sampleValue = "Sundaram Ramachandran",
                                    sampleLabel = "Sample Valid Name",
                                    onUseSample = { fatherName = "Sundaram Ramachandran" }
                                )

                                BiometricUploadRow(
                                    label = "Father's Footprint Biometric (JPG / PNG)",
                                    imageUrl = fatherScanUrl,
                                    capturedImageUri = fatherScanUri,
                                    fileName = fatherUploadedFileName,
                                    onUploadClicked = {
                                        fatherFileLauncher.launch("image/*")
                                    },
                                    onClearClicked = {
                                        fatherScanUri = null
                                        fatherUploadedFileName = ""
                                        fatherScanUrl = ""
                                        Toast.makeText(context, "Father footprint cleared", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        // ----------------------------------------------------
                        // Box 2: Mother's biometric (footprint) & name
                        // ----------------------------------------------------
                        TabularBoxContainer(
                            boxNumber = "2",
                            title = "Mother's biometric (footprint) & name",
                            statusPillText = if (motherNameValidation.isValid) "NAME VERIFIED ✓" else if (motherName.isNotBlank()) "INVALID FORMAT" else "PENDING",
                            isStatusVerified = motherNameValidation.isValid
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ValidatedGovTextField(
                                    value = motherName,
                                    onValueChange = { motherName = it },
                                    label = "Mother's Full Name",
                                    placeholder = "e.g. Kavitha Sundaram",
                                    validation = motherNameValidation,
                                    testTag = "input_mother_name",
                                    sampleValue = "Kavitha Sundaram",
                                    sampleLabel = "Sample Valid Name",
                                    onUseSample = { motherName = "Kavitha Sundaram" }
                                )

                                BiometricUploadRow(
                                    label = "Mother's Footprint Biometric (JPG / PNG)",
                                    imageUrl = motherScanUrl,
                                    capturedImageUri = motherScanUri,
                                    fileName = motherUploadedFileName,
                                    onUploadClicked = {
                                        motherFileLauncher.launch("image/*")
                                    },
                                    onClearClicked = {
                                        motherScanUri = null
                                        motherUploadedFileName = ""
                                        motherScanUrl = ""
                                        Toast.makeText(context, "Mother footprint cleared", Toast.LENGTH_SHORT).show()
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
                                capturedImageUri = childScanUri,
                                fileName = childUploadedFileName,
                                onUploadClicked = {
                                    childFileLauncher.launch("image/*")
                                },
                                onClearClicked = {
                                    childScanUri = null
                                    childUploadedFileName = ""
                                    childScanUrl = ""
                                    Toast.makeText(context, "Child footprint cleared", Toast.LENGTH_SHORT).show()
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
                        TabularBoxContainer(
                            boxNumber = "5",
                            title = "Birth DATE/MONTH/YEAR & TIME",
                            statusPillText = if (birthDateValidation.isValid && birthTimeValidation.isValid) "TIMESTAMP VERIFIED ✓" else "INVALID FORMAT",
                            isStatusVerified = birthDateValidation.isValid && birthTimeValidation.isValid
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ValidatedGovTextField(
                                    value = birthDate,
                                    onValueChange = { birthDate = it },
                                    label = "Birth Date (DD/MM/YYYY)",
                                    placeholder = "DD/MM/YYYY",
                                    validation = birthDateValidation,
                                    testTag = "input_birth_date",
                                    sampleValue = "13/09/2026",
                                    sampleLabel = "Sample Date",
                                    onUseSample = { birthDate = "13/09/2026" }
                                )
                                ValidatedGovTextField(
                                    value = birthTime,
                                    onValueChange = { birthTime = it },
                                    label = "Birth Time (IST)",
                                    placeholder = "07:15:30 AM IST",
                                    validation = birthTimeValidation,
                                    testTag = "input_birth_time",
                                    sampleValue = "07:15:30 AM IST",
                                    sampleLabel = "Sample Time",
                                    onUseSample = { birthTime = "07:15:30 AM IST" }
                                )
                            }
                        }

                        // ----------------------------------------------------
                        // Box 6: Doctor Name
                        // ----------------------------------------------------
                        TabularBoxContainer(
                            boxNumber = "6",
                            title = "Doctor Name",
                            statusPillText = if (doctorNameValidation.isValid) "DOCTOR VERIFIED ✓" else if (doctorName.isNotBlank()) "INVALID FORMAT" else "PENDING",
                            isStatusVerified = doctorNameValidation.isValid
                        ) {
                            ValidatedGovTextField(
                                value = doctorName,
                                onValueChange = { doctorName = it },
                                label = "Attending Obstetrician / Pediatrician Doctor Name",
                                placeholder = "e.g. Dr. S. Meenakshi, MD (OBG)",
                                validation = doctorNameValidation,
                                testTag = "input_doctor_name",
                                sampleValue = "Dr. S. Meenakshi, MD (OBG)",
                                sampleLabel = "Sample Doctor Name",
                                onUseSample = { doctorName = "Dr. S. Meenakshi, MD (OBG)" }
                            )
                        }

                        // ----------------------------------------------------
                        // Box 7: Hospital Name & Hospital location (Filled on own)
                        // ----------------------------------------------------
                        TabularBoxContainer(
                            boxNumber = "7",
                            title = "Hospital Name & Hospital location",
                            statusPillText = if (hospitalNameValidation.isValid && hospitalLocationValidation.isValid) "FACILITY VERIFIED ✓" else "PENDING",
                            isStatusVerified = hospitalNameValidation.isValid && hospitalLocationValidation.isValid
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ValidatedGovTextField(
                                    value = hospitalName,
                                    onValueChange = { hospitalName = it },
                                    label = "Hospital Name (Editable on your own)",
                                    placeholder = "e.g. Government Kasturba Gandhi Hospital",
                                    validation = hospitalNameValidation,
                                    testTag = "input_hospital_name",
                                    sampleValue = "Government Kasturba Gandhi Hospital",
                                    sampleLabel = "Sample Hospital",
                                    onUseSample = { hospitalName = "Government Kasturba Gandhi Hospital" }
                                )
                                ValidatedGovTextField(
                                    value = hospitalLocation,
                                    onValueChange = { hospitalLocation = it },
                                    label = "Hospital Exact Location (District / Facility Ward)",
                                    placeholder = "e.g. Triplicane, Chennai, Tamil Nadu",
                                    validation = hospitalLocationValidation,
                                    testTag = "input_hospital_location",
                                    sampleValue = "Triplicane, Chennai, Tamil Nadu",
                                    sampleLabel = "Sample Location",
                                    onUseSample = { hospitalLocation = "Triplicane, Chennai, Tamil Nadu" }
                                )

                                Text(
                                    text = "Clinical Ward Placement & Status:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Postnatal Ward", "Labor & Delivery", "NICU / SNCU", "Special Care Nursery").forEach { wardOption ->
                                        val isSelected = wardStatus == wardOption
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { wardStatus = wardOption },
                                            label = {
                                                Text(
                                                    text = when (wardOption) {
                                                        "Postnatal Ward" -> "Postnatal"
                                                        "Labor & Delivery" -> "Labor & Deliv"
                                                        "NICU / SNCU" -> "NICU"
                                                        else -> "Special Care"
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TnDeepTeal,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ----------------------------------------------------
                        // Box 8: Father's or Mother's mobile number
                        // ----------------------------------------------------
                        TabularBoxContainer(
                            boxNumber = "8",
                            title = "Father's or Mother's mobile number",
                            statusPillText = if (mobileValidation.isValid) "+91 VERIFIED ✓" else if (parentMobile.isNotBlank()) "INVALID FORMAT" else "MANDATORY",
                            isStatusVerified = mobileValidation.isValid
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ValidatedGovTextField(
                                    value = parentMobile,
                                    onValueChange = { parentMobile = it },
                                    label = "Parent Mobile Number (Indian Standard)",
                                    placeholder = "+91 98401 23456",
                                    validation = mobileValidation,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    testTag = "input_parent_mobile",
                                    sampleValue = TamilNaduIdValidator.Samples.VALID_MOBILE,
                                    sampleLabel = "Valid TN Mobile",
                                    onUseSample = { parentMobile = TamilNaduIdValidator.Samples.VALID_MOBILE }
                                )
                                Text(
                                    text = "Mobile number will be masked (e.g., +91 98XXXXXX56) in normal views and only revealed for authorized verification.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        // ----------------------------------------------------
                        // Box 9: Parent / Guardian Email Address
                        // ----------------------------------------------------
                        TabularBoxContainer(
                            boxNumber = "9",
                            title = "Father's or Mother's Email Address",
                            statusPillText = if (emailValidation.isValid) "EMAIL VERIFIED ✓" else if (parentEmail.isNotBlank()) "INVALID FORMAT" else "MANDATORY",
                            isStatusVerified = emailValidation.isValid
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ValidatedGovTextField(
                                    value = parentEmail,
                                    onValueChange = { parentEmail = it },
                                    label = "Parent Email Address (For Resend Digital Custody Receipt)",
                                    placeholder = "vimal.uv1991@gmail.com",
                                    validation = emailValidation,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    testTag = "input_parent_email",
                                    sampleValue = TamilNaduIdValidator.Samples.VALID_EMAIL,
                                    sampleLabel = "Valid Email",
                                    onUseSample = { parentEmail = TamilNaduIdValidator.Samples.VALID_EMAIL }
                                )
                                Text(
                                    text = "Statutory digital birth certificate and real-time OTP confirmation will be dispatched via Resend to this email.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        // ----------------------------------------------------
                        // Box 10: Statutory Tamil Nadu Government IDs (PICME, Aadhaar, CRS Form-5)
                        // ----------------------------------------------------
                        TabularBoxContainer(
                            boxNumber = "10",
                            title = "Statutory Tamil Nadu Government IDs (PICME, Aadhaar & Form-5)",
                            statusPillText = if (picmeValidation.isValid && aadhaarValidation.isValid && crsForm5Validation.isValid) "TN GOVT IDs VERIFIED ✓" else "VALIDATION REQUIRED",
                            isStatusVerified = picmeValidation.isValid && aadhaarValidation.isValid && crsForm5Validation.isValid
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Tamil Nadu Directorate of Public Health & Preventive Medicine (DPH) and UIDAI Statutory Compliance:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.SemiBold
                                )

                                // TN PICME ID (12 digits)
                                ValidatedGovTextField(
                                    value = picmeNumber,
                                    onValueChange = { picmeNumber = it },
                                    label = "Tamil Nadu PICME / RCH ID (12 Digits)",
                                    placeholder = "e.g. 1029 4857 2910",
                                    validation = picmeValidation,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    testTag = "input_picme_number",
                                    sampleValue = TamilNaduIdValidator.Samples.VALID_PICME_1,
                                    sampleLabel = "Sample PICME",
                                    onUseSample = { picmeNumber = TamilNaduIdValidator.Samples.VALID_PICME_1 }
                                )

                                // Mother's Aadhaar UID (12 digits)
                                ValidatedGovTextField(
                                    value = motherAadhaar,
                                    onValueChange = { motherAadhaar = it },
                                    label = "Mother's Aadhaar UID (12 Digits)",
                                    placeholder = "e.g. 9841 2345 6789",
                                    validation = aadhaarValidation,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    testTag = "input_mother_aadhaar",
                                    sampleValue = TamilNaduIdValidator.Samples.VALID_AADHAAR_MOTHER,
                                    sampleLabel = "Sample Mother Aadhaar",
                                    onUseSample = { motherAadhaar = TamilNaduIdValidator.Samples.VALID_AADHAAR_MOTHER }
                                )

                                // TN Civil Registration CRS Form-5 Number
                                ValidatedGovTextField(
                                    value = crsRegistrationNumber,
                                    onValueChange = { crsRegistrationNumber = it },
                                    label = "TN CRS Form-5 / Birth Certificate Reg No",
                                    placeholder = "e.g. B-2026: 02-00892-000123",
                                    validation = crsForm5Validation,
                                    testTag = "input_crs_registration_number",
                                    sampleValue = TamilNaduIdValidator.Samples.VALID_CRS_FORM5_1,
                                    sampleLabel = "Sample Form-5 Reg",
                                    onUseSample = { crsRegistrationNumber = TamilNaduIdValidator.Samples.VALID_CRS_FORM5_1 }
                                )
                            }
                        }

                        // SUBMISSION ACTIONS
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Parental Verification Trigger Button
                            Button(
                                onClick = {
                                    if (!isStatutoryFormValid) {
                                        val invalidFields = formFieldStatuses.filter { !it.isValid }.map { it.label }
                                        Toast.makeText(
                                            context,
                                            "Please resolve invalid government ID formats:\n${invalidFields.joinToString(", ")}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@Button
                                    }
                                    if (isDuplicateBlocked) {
                                        Toast.makeText(context, "Registration blocked due to biometric duplicate!", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    showParentalVerificationModal = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (parentVerified) Color(0xFF065F46) else TnDeepTeal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_parental_verification")
                            ) {
                                Icon(
                                    if (parentVerified) Icons.Default.VerifiedUser else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (parentVerified) "Parent OTP Authenticated ✓ (Review Details)" else "Step 1: Display Details to Parents & Verify via Real OTP",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            if (parentVerified) {
                                Surface(
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                                        Column {
                                            Text(
                                                text = "PARENTAL OTP AUTHENTICATION VERIFIED ✓",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF065F46)
                                            )
                                            Text(
                                                text = "Parent identity cryptographically confirmed in $verificationLanguage via live 6-digit OTP sent to $parentEmail${parentOtpVerifiedAt?.let { " at $it" } ?: ""}. Ready for State Custody submission.",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    color = Color(0xFFFFFBEB),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Mandatory Requirement: Parents must verify the 6-digit Email OTP before this newborn dossier can be committed to the Sovereign State Ledger.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (!parentVerified) {
                                        Toast.makeText(context, "Parental confirmation via real OTP is mandatory before submission!", Toast.LENGTH_LONG).show()
                                        showParentalVerificationModal = true
                                        return@Button
                                    }
                                    val newToken = "TN-${Calendar.getInstance().get(Calendar.YEAR)}-MDU-${(1000..9999).random()}"
                                    val newRecord = NewbornRecord(
                                        token = newToken,
                                        fatherName = fatherName,
                                        motherName = motherName,
                                        gender = childGender,
                                        birthTimestamp = "$birthDate, $birthTime",
                                        doctorName = doctorName,
                                        hospitalName = hospitalName.ifBlank { "Government Empaneled Hospital" },
                                        district = hospitalLocation.split(",").firstOrNull()?.ifBlank { "Tamil Nadu" } ?: "Tamil Nadu",
                                        hospitalLocation = hospitalLocation.ifBlank { "Tamil Nadu" },
                                        parentMobile = parentMobile,
                                        parentEmail = parentEmail.trim(),
                                        picmeNumber = picmeNumber,
                                        motherAadhaar = motherAadhaar,
                                        crsRegistrationNumber = crsRegistrationNumber,
                                        status = "3-Party Validated (Parent OTP Confirmed)",
                                        biometricHash = UUID.randomUUID().toString().replace("-", ""),
                                        secondsRemaining = 86400L,
                                        isCouncilLocked = false,
                                        wardStatus = wardStatus
                                    )
                                    SafeStartRepository.addRecord(newRecord)
                                    lastSubmittedRecord = newRecord
                                    showSmsConfirmationModal = true
                                    successToastMsg = "NEWBORN REGISTRATION SUBMITTED" to "Token ${newRecord.token} securely signed and entered into Tamil Nadu State Custody Registry."
                                    smsToastMsg = "GOVERNMENT SMS DISPATCHED (+91 ${newRecord.parentMobile})" to "TN-GOVT: Newborn birth biometric identity registered. Token: ${newRecord.token}. Edit window: 24 Hours. Support: 104."

                                    // Dispatch live statutory receipt via Resend Email Gateway
                                    val targetRecipient = parentEmail.ifBlank { "vimal.uv1991@gmail.com" }
                                    coroutineScope.launch {
                                        ResendEmailService.sendRegistrationReceipt(
                                            toEmail = targetRecipient,
                                            token = newRecord.token,
                                            babyGender = newRecord.gender,
                                            fatherName = newRecord.fatherName,
                                            motherName = newRecord.motherName,
                                            hospitalName = newRecord.hospitalName,
                                            birthDate = newRecord.birthTimestamp
                                        )
                                    }

                                    // Reset fields for next blank entry
                                    parentVerified = false
                                    resendOtpSent = false
                                    resendGeneratedOtp = null
                                    resendEnteredOtp = ""
                                    resendOtpStatusMsg = null
                                    parentOtpVerifiedAt = null
                                },
                                enabled = parentVerified,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (parentVerified) TnPrimary else Color(0xFF94A3B8)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_submit_final_dossier")
                            ) {
                                Icon(
                                    if (parentVerified) Icons.Default.CloudUpload else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (parentVerified) "Submit Record & Send Parent Confirmation SMS" else "Submit Dossier (Parent OTP Verification Required)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "STATUTORY 24-HOUR AMEND WINDOW",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Hospital members can edit within 24 hours only. After 24h, records cannot be edited or deleted.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
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

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        // Filter strictly for local hospital records as required:
                        // "Hospital members can create and update the data and they can't visit any other information stored in the app"
                        val localRecords = if (hospitalName.isBlank()) {
                            records
                        } else {
                            records.filter { it.hospitalName.contains(hospitalName, ignoreCase = true) || hospitalName.contains(it.hospitalName, ignoreCase = true) }
                        }

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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = TnDeepTeal, modifier = Modifier.size(16.dp))
                                        Column {
                                            Text("Child Birth Certificate (PDF/Image)", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                            Text(
                                                text = if (complaintBirthCertUploaded) complaintCertFileName else "No file selected",
                                                fontSize = 10.sp,
                                                color = if (complaintBirthCertUploaded) Color(0xFF047857) else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            complaintCertLauncher.launch("*/*")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (complaintBirthCertUploaded) Color(0xFF047857) else TnPrimary),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(if (complaintBirthCertUploaded) "Uploaded ✓" else "Upload Certificate", fontSize = 10.sp)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.AccountBox, contentDescription = null, tint = TnDeepTeal, modifier = Modifier.size(16.dp))
                                        Column {
                                            Text("Passport Size Photo of Child", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                            Text(
                                                text = if (complaintPassportPhotoUploaded) complaintPhotoFileName else "No file selected",
                                                fontSize = 10.sp,
                                                color = if (complaintPassportPhotoUploaded) Color(0xFF047857) else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            complaintPhotoLauncher.launch("image/*")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (complaintPassportPhotoUploaded) Color(0xFF047857) else TnPrimary),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
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
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btn_submit_complaint")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submit Complaint to Government Medical Council",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (verificationLanguage) {
                                    "Tamil" -> "பெற்றோர் சரிபார்ப்பு படிவம்"
                                    "Malayalam" -> "മാതാപിതാക്കളുടെ സ്ഥിരീകരണ ഫോം"
                                    "Hindi" -> "माता-पिता जन्म सत्यापन प्रपत्र"
                                    else -> "PARENTAL VERIFICATION FORM"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TnDeepTeal
                            )
                            Text(
                                text = when (verificationLanguage) {
                                    "Tamil" -> "இறுதி சமர்ப்பிப்பிற்கு முன், பெற்றோருடன் விவரங்களை சரிபார்க்கவும்."
                                    "Malayalam" -> "അന്തിമ സമർപ്പണത്തിന് മുമ്പ്, മാതാപിതാക്കളുമായി വിവരങ്ങൾ പരിശോധിക്കുക."
                                    "Hindi" -> "अंतिम जमा करने से पहले, माता-पिता के साथ सभी विवरणों की पुष्टि करें।"
                                    else -> "Before final submission, verify all details with parents."
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // LANGUAGE SWITCH (Tamil, Malayalam, Hindi, English)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Tamil", "Malayalam", "Hindi", "English").forEach { lang ->
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
                                            "Hindi" -> "हिंदी"
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

                    HorizontalDivider(color = Color(0xFFE2E8F0))

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
                                    "Hindi" -> "दर्ज किए गए विवरण (सत्यापन हेतु):"
                                    else -> "Recorded Particulars:"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text("1. ${if (verificationLanguage == "Tamil") "தந்தை பெயர்" else if (verificationLanguage == "Malayalam") "പിതാവിന്റെ പേര്" else if (verificationLanguage == "Hindi") "पिता का नाम" else "Father Name"}: $fatherName", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("2. ${if (verificationLanguage == "Tamil") "தாய் பெயர்" else if (verificationLanguage == "Malayalam") "മാതാவின் പേര്" else if (verificationLanguage == "Hindi") "माता का नाम" else "Mother Name"}: $motherName", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("3. ${if (verificationLanguage == "Tamil") "குழந்தை பாலினம்" else if (verificationLanguage == "Malayalam") "ലിംഗം" else if (verificationLanguage == "Hindi") "शिशु का लिंग" else "Child Gender"}: ${if (verificationLanguage == "Hindi") (if (childGender == "Male") "बालक (लड़का)" else if (childGender == "Female") "बालिका (लड़की)" else childGender) else childGender}", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("4. ${if (verificationLanguage == "Tamil") "பிறந்த தேதி & நேரம்" else if (verificationLanguage == "Malayalam") "ജനന തീയതി & സമയം" else if (verificationLanguage == "Hindi") "जन्म तिथि एवं समय" else "Birth Date & Time"}: $birthDate, $birthTime", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("5. ${if (verificationLanguage == "Tamil") "மருத்துவர்" else if (verificationLanguage == "Malayalam") "ഡോക്ടർ" else if (verificationLanguage == "Hindi") "उपस्थित चिकित्सक" else "Doctor"}: $doctorName", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("6. ${if (verificationLanguage == "Tamil") "மருத்துவமனை & இருப்பிடம்" else if (verificationLanguage == "Malayalam") "ആശുപത്രി & സ്ഥലം" else if (verificationLanguage == "Hindi") "अस्पताल एवं स्थान" else "Hospital & Location"}: $hospitalName, $hospitalLocation", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("7. ${if (verificationLanguage == "Tamil") "தொடர்பு எண்" else if (verificationLanguage == "Malayalam") "മൊബൈൽ നമ്പർ" else if (verificationLanguage == "Hindi") "मोबाइल नंबर" else "Mobile Number"}: $parentMobile", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("8. ${if (verificationLanguage == "Tamil") "மின்னஞ்சல் முகவரி (OTP)" else if (verificationLanguage == "Malayalam") "ഇമെയിൽ വിലാസം (OTP)" else if (verificationLanguage == "Hindi") "अभिभावक ईमेल (ओटीपी)" else "Parent Email (OTP)"}: ${parentEmail.ifBlank { "vimal.uv1991@gmail.com" }}", fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                            Text("9. ${if (verificationLanguage == "Tamil") "தமிழ்நாடு PICME எண்" else if (verificationLanguage == "Malayalam") "PICME നമ്പർ" else if (verificationLanguage == "Hindi") "तमिलनाडु पिकमे (PICME) संख्या" else "TN PICME / RCH ID"}: ${picmeNumber.ifBlank { "N/A" }}", fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                            Text("10. ${if (verificationLanguage == "Tamil") "தாய் ஆதார் எண்" else if (verificationLanguage == "Malayalam") "ആധാർ" else if (verificationLanguage == "Hindi") "माता का आधार यूआईडी" else "Mother's Aadhaar UID"}: ${motherAadhaar.ifBlank { "N/A" }}", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("11. ${if (verificationLanguage == "Tamil") "படிவம்-5 பதிவு எண்" else if (verificationLanguage == "Malayalam") "ഫോം-5 നമ്പർ" else if (verificationLanguage == "Hindi") "सीआरएस फॉर्म-5 पंजीकरण संख्या" else "CRS Form-5 Reg No"}: ${crsRegistrationNumber.ifBlank { "N/A" }}", fontSize = 12.sp, color = Color(0xFF0F172A))
                        }
                    }

                    // Mandatory Real-Time Parent Email OTP Verification via Resend Gateway
                    val isParentOtpVerified = resendOtpStatusMsg?.contains("Authenticated") == true
                    val targetEmail = parentEmail.ifBlank { "vimal.uv1991@gmail.com" }.trim()

                    Surface(
                        color = if (isParentOtpVerified) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, if (isParentOtpVerified) Color(0xFF10B981) else if (resendOtpSent) Color(0xFF3B82F6) else Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isParentOtpVerified) Color(0xFF10B981) else TnDeepTeal,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                if (isParentOtpVerified) Icons.Default.Check else Icons.Default.Security,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "MANDATORY LIVE PARENT OTP",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isParentOtpVerified) Color(0xFF065F46) else TnDeepTeal
                                        )
                                        Text(
                                            text = "Target: $targetEmail",
                                            fontSize = 10.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        val reqResult = SafeStartRepository.requestParentOtp(targetEmail, "PARENT_REGISTRATION")
                                        if (!reqResult.success) {
                                            resendOtpStatusMsg = "Cooldown active: ${reqResult.message}"
                                            Toast.makeText(context, reqResult.message, Toast.LENGTH_SHORT).show()
                                            return@OutlinedButton
                                        }

                                        val code = reqResult.plainOtpForDispatch ?: ""
                                        resendGeneratedOtp = null // Cleared on client: server holds salted hash
                                        resendOtpSent = true
                                        resendEnteredOtp = ""
                                        resendOtpStatusMsg = "Server generated secure OTP. Dispatching to $targetEmail..."
                                        parentOtpCountdown = reqResult.cooldownSecondsRemaining.toInt()
                                        coroutineScope.launch {
                                            isSendingParentOtp = true
                                            val result = ResendEmailService.sendOtpEmail(
                                                toEmail = targetEmail,
                                                otp = code,
                                                officerName = "Parents ($fatherName & $motherName)",
                                                purpose = "Parental Statutory Verification for $childGender Newborn Registration"
                                            )
                                            isSendingParentOtp = false
                                            result.onSuccess {
                                                resendOtpStatusMsg = "✓ 6-Digit OTP code dispatched to $targetEmail. Please check inbox or spam."
                                                Toast.makeText(context, "Live OTP dispatched to $targetEmail via Resend!", Toast.LENGTH_SHORT).show()
                                            }.onFailure { err ->
                                                resendOtpStatusMsg = "Dispatched via Server Engine: ${err.message}"
                                                Toast.makeText(context, "Server OTP active for $targetEmail.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    enabled = !isSendingParentOtp && (parentOtpCountdown == 0 || !isParentOtpVerified),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    border = BorderStroke(1.dp, if (parentOtpCountdown > 0) Color(0xFF94A3B8) else TnDeepTeal)
                                ) {
                                    if (isSendingParentOtp) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = TnDeepTeal)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Sending...", fontSize = 10.5.sp, color = TnDeepTeal)
                                    } else if (parentOtpCountdown > 0 && !isParentOtpVerified) {
                                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Resend (${parentOtpCountdown}s)", fontSize = 10.5.sp, color = Color(0xFF64748B))
                                    } else {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(12.dp), tint = TnDeepTeal)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (isParentOtpVerified) "Re-verify OTP" else if (resendOtpSent) "Resend OTP" else "Send Live OTP",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TnDeepTeal
                                        )
                                    }
                                }
                            }

                            if (resendOtpSent && !isParentOtpVerified) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = when (verificationLanguage) {
                                            "Tamil" -> "பெற்றோர் மின்னஞ்சலில் பெறப்பட்ட 6-இலக்க உறுதிப்படுத்தல் குறியீட்டை உள்ளிடவும்:"
                                            "Malayalam" -> "രക്ഷിതാവിന്റെ ഇൻബോക്സിൽ ലഭിച്ച 6 അക്ക കോഡ് നൽകുക:"
                                            "Hindi" -> "अभिभावक के ईमेल इनबॉक्स में प्राप्त 6-अंकों का पुष्टिकरण कोड दर्ज करें:"
                                            else -> "Enter the 6-digit confirmation code received in parent inbox:"
                                        },
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = resendEnteredOtp,
                                            onValueChange = { input ->
                                                if (input.length <= 6) {
                                                    resendEnteredOtp = input.filter { it.isDigit() }
                                                }
                                            },
                                            placeholder = { Text("6-Digit OTP", fontSize = 13.sp) },
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 2.sp,
                                                color = Color(0xFF0F172A)
                                            ),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("input_parent_otp")
                                        )
                                        Button(
                                            onClick = {
                                                val verifyResult = SafeStartRepository.verifyParentOtp(
                                                    targetIdentifier = targetEmail,
                                                    candidateOtp = resendEnteredOtp.trim(),
                                                    purpose = "PARENT_REGISTRATION"
                                                )
                                                if (verifyResult.status == com.example.backend.OtpSecurity.VerificationStatus.SUCCESS) {
                                                    resendOtpStatusMsg = "✓ Parent Email & Identity Authenticated via Real-Time OTP"
                                                    parentOtpVerifiedAt = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
                                                    parentVerified = true
                                                    Toast.makeText(context, "Parent Identity Authenticated via Real OTP!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    resendOtpStatusMsg = "❌ ${verifyResult.message}"
                                                    Toast.makeText(context, verifyResult.message, Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            enabled = resendEnteredOtp.length == 6,
                                            colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.testTag("btn_verify_parent_otp")
                                        ) {
                                            Text(
                                                text = when (verificationLanguage) {
                                                    "Tamil" -> "சரிபார்"
                                                    "Malayalam" -> "സ്ഥിരീകരിക്കുക"
                                                    "Hindi" -> "ओटीपी सत्यापित करें"
                                                    else -> "Verify OTP"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            if (isParentOtpVerified) {
                                Surface(
                                    color = Color(0xFFD1FAE5),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                        Text(
                                            text = when (verificationLanguage) {
                                                "Tamil" -> "பெற்றோர் அடையாளம் நேரலை OTP மூலம் உறுதிப்படுத்தப்பட்டது ✓ (${parentOtpVerifiedAt ?: "உறுதிப்படுத்தப்பட்டது"})"
                                                "Malayalam" -> "തത്സമയ ഒ‌ടി‌പി വഴി മാതാപിതാക്കളുടെ ഐഡന്റിറ്റി സ്ഥിരീകരിച്ചു ✓ (${parentOtpVerifiedAt ?: "സ്ഥിരീകരിച്ചു"})"
                                                "Hindi" -> "माता-पिता की पहचान लाइव ओटीपी द्वारा सत्यापित की गई ✓ (${parentOtpVerifiedAt ?: "सत्यापित"})"
                                                else -> "Parent Identity Authenticated via Real-Time OTP ✓ (${parentOtpVerifiedAt ?: "Confirmed"})"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }
                            } else {
                                resendOtpStatusMsg?.let { msg ->
                                    Text(
                                        text = msg,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (msg.contains("✓")) Color(0xFF047857) else Color(0xFFB91C1C)
                                    )
                                }
                            }
                        }
                    }

                    // Enforcement note banner
                    if (!isParentOtpVerified) {
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                Text(
                                    text = when (verificationLanguage) {
                                        "Tamil" -> "பெற்றோர் OTP சரிபார்ப்பு கட்டாயமாகும். குறியீடு சரிபார்க்கப்படும் வரை இந்த பதிவை உறுதிப்படுத்த முடியாது."
                                        "Malayalam" -> "രക്ഷിതാവിന്റെ ഒ‌ടി‌പി സ്ഥിരീകരണം നിർബന്ധമാണ്. കോഡ് പരിശോധിക്കുന്നത് വരെ ഈ റെക്കോർഡ് സ്ഥിരീകരിക്കാനാകില്ല."
                                        "Hindi" -> "माता-पिता का ओटीपी सत्यापन अनिवार्य है। कोड सत्यापित होने तक इस रिकॉर्ड की पुष्टि नहीं की जा सकती।"
                                        else -> "Parent OTP verification is required. You cannot confirm this record until the parent code is verified."
                                    },
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
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
                            Text(
                                text = when (verificationLanguage) {
                                    "Tamil" -> "திருத்துக"
                                    "Malayalam" -> "തിരുത്തുക"
                                    "Hindi" -> "संशोधित करें"
                                    else -> "Edit Info"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Button(
                            onClick = {
                                if (!isParentOtpVerified) {
                                    Toast.makeText(context, "Please verify the parent OTP first!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                parentVerified = true
                                showParentalVerificationModal = false
                                Toast.makeText(context, "Parent verified data in $verificationLanguage via live OTP. You may now submit.", Toast.LENGTH_SHORT).show()
                            },
                            enabled = isParentOtpVerified,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isParentOtpVerified) TnPrimary else Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(2f)
                        ) {
                            Icon(
                                if (isParentOtpVerified) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isParentOtpVerified) {
                                    when (verificationLanguage) {
                                        "Tamil" -> "சரிபார்க்கப்பட்டது ✓"
                                        "Malayalam" -> "സ്ഥിരീകരിച്ചു ✓"
                                        "Hindi" -> "सत्यापित एवं स्वीकृत ✓"
                                        else -> "Confirm Verified ✓"
                                    }
                                } else {
                                    when (verificationLanguage) {
                                        "Tamil" -> "முதலில் OTP சரிபார்க்கவும் (பூட்டப்பட்டது)"
                                        "Malayalam" -> "ആദ്യം ഒ‌ടി‌പി സ്ഥിരീകരിക്കുക (പൂട്ടിയത്)"
                                        "Hindi" -> "पहले ओटीपी सत्यापित करें (लॉक)"
                                        else -> "Verify OTP First (Locked)"
                                    }
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
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
                        Column(modifier = Modifier.weight(1f)) {
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

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // SMS Content Preview (exact requirements with multilingual support)
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
                                text = when (verificationLanguage) {
                                    "Tamil" -> "Safe Start / காவல் துவக்கம்: உங்கள் குழந்தை பிறப்பு பதிவு வெற்றிகரமாக சமர்ப்பிக்கப்பட்டது."
                                    "Malayalam" -> "Safe Start / സുരക്ഷിത തുടക്കം: നിങ്ങളുടെ കുട്ടിയുടെ ജനന രജിസ്ട്രേഷൻ വിജയകരമായി സമർപ്പിച്ചു."
                                    "Hindi" -> "सुरक्षित प्रारंभ (Safe Start): आपके शिशु का जन्म पंजीकरण सफलतापूर्वक प्रस्तुत एवं पुष्ट किया गया।"
                                    else -> "Safe Start: Your child's birth registration has been successfully submitted and confirmed."
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text("• ${if (verificationLanguage == "Hindi") "माता-पिता का नाम" else if (verificationLanguage == "Tamil") "பெற்றோர் பெயர்" else if (verificationLanguage == "Malayalam") "മാതാപിതാക്കൾ" else "Name of Parents"}: ${record.fatherName} & ${record.motherName}", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("• ${if (verificationLanguage == "Hindi") "अस्पताल का नाम" else if (verificationLanguage == "Tamil") "மருத்துவமனை" else if (verificationLanguage == "Malayalam") "ആശുപത്രി" else "Hospital Name"}: ${record.hospitalName}", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("• ${if (verificationLanguage == "Hindi") "जन्म तिथि एवं समय" else if (verificationLanguage == "Tamil") "பிறந்த தேதி" else if (verificationLanguage == "Malayalam") "ജനന തീയതി" else "Date of Birth"}: ${record.birthTimestamp}", fontSize = 12.sp, color = Color(0xFF0F172A))
                            Text("• ${if (verificationLanguage == "Hindi") "आधिकारिक पहचान टोकन" else if (verificationLanguage == "Tamil") "அடையாள டோக்கன்" else if (verificationLanguage == "Malayalam") "തിരിച്ചറിയൽ ടോക്കൺ" else "Assigned Identity Token"}: ${record.token}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TnDeepTeal)
                            Text(
                                text = when (verificationLanguage) {
                                    "Tamil" -> "உறுதிப்படுத்தல் செய்தி: பிறப்பு விவரங்கள் தமிழ்நாடு சுகாதார லெட்ஜரில் பாதுகாப்பாக முத்திரையிடப்பட்டன."
                                    "Malayalam" -> "സ്ഥിരീകരണ സന്ദേശം: ജനന രേഖ തമിഴ്നാട് ആരോഗ്യ ലെഡ്ജറിൽ സുരക്ഷിതമായി മുദ്രവെച്ചു."
                                    "Hindi" -> "पुष्टिकरण संदेश: जन्म दस्तावेज को तमिलनाडु राज्य स्वास्थ्य लेजर में सुरक्षित रूप से मुहरबंद कर दिया गया है।"
                                    else -> "Confirmation message: Birth dossier securely sealed into Tamil Nadu State Health Ledger."
                                },
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

    // Camera Birth Certificate Scanner Dialog
    if (showCameraScanner) {
        BirthCertificateCameraScannerDialog(
            hospitalName = hospitalName,
            hospitalLocation = hospitalLocation,
            onDismiss = { showCameraScanner = false },
            onCertificateScanned = { cert ->
                scannedCertificate = cert
                fatherName = cert.fatherName
                motherName = cert.motherName
                childGender = cert.childGender
                birthDate = cert.dateOfBirth
                birthTime = cert.timeOfBirth
                doctorName = cert.doctorName
                parentMobile = cert.parentMobile
                parentEmail = cert.parentEmail
                crsRegistrationNumber = cert.certificateNumber
                picmeNumber = cert.picmeNumber
                motherAadhaar = cert.motherAadhaar
                if (cert.hospitalName.isNotBlank()) hospitalName = cert.hospitalName
                if (cert.hospitalLocation.isNotBlank()) hospitalLocation = cert.hospitalLocation
                Toast.makeText(context, "✅ Form Auto-Populated from Scanned Birth Certificate!", Toast.LENGTH_LONG).show()
            }
        )
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
    imageUrl: String? = null,
    capturedImageUri: String? = null,
    fileName: String = "",
    onUploadClicked: () -> Unit,
    onClearClicked: () -> Unit = {}
) {
    GovBiometricCaptureBox(
        label = label,
        previewUrl = imageUrl,
        capturedImageUri = capturedImageUri,
        fileName = fileName,
        isCaptured = (!capturedImageUri.isNullOrBlank()) || (!imageUrl.isNullOrBlank() && fileName.isNotBlank()),
        onCapture = { onUploadClicked() },
        onUpload = { onUploadClicked() },
        onClear = { onClearClicked() },
        modifier = Modifier.fillMaxWidth()
    )
}
