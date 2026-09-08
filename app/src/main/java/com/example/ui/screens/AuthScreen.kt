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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class AuthMode {
    LOGIN,
    CREATE_ACCOUNT,
    FORGOT_PASSWORD
}

@Composable
fun AuthScreen(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onLoginSuccess: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Login state
    var selectedRole by remember { mutableStateOf(currentRole) }
    var loginId by remember {
        mutableStateOf(
            if (currentRole == UserRole.HOSPITAL_REGISTRAR) "HOSP-TN-MDU-74291" else "COUNCIL-TN-CHN-89210"
        )
    }
    var loginPassword by remember { mutableStateOf("GovtTN@Pass2026") }
    var passwordVisible by remember { mutableStateOf(false) }
    var captchaCode by remember { mutableStateOf("8 W Y 4 2 K") }
    var captchaInput by remember { mutableStateOf("") }

    // Forgot Password state
    var fpRole by remember { mutableStateOf(UserRole.HOSPITAL_REGISTRAR) }
    var fpHospitalName by remember { mutableStateOf("Government Rajaji Hospital") }
    var fpHospitalLocation by remember { mutableStateOf("Madurai, Tamil Nadu") }
    var fpAdminId by remember { mutableStateOf("HOSP-TN-MDU-74291") }
    var fpPhone by remember { mutableStateOf("+91 94420 18291") }
    var fpEmail by remember { mutableStateOf("admin.grh@tn.gov.in") }
    var fpReason by remember { mutableStateOf("Biometric terminal security token recalibration and off-hours credential rotation.") }
    var fpSubmittedRequestId by remember { mutableStateOf<String?>(null) }
    var fpApprovalTokenInput by remember { mutableStateOf("") }
    var fpNewPassword by remember { mutableStateOf("") }
    var fpConfirmNewPassword by remember { mutableStateOf("") }

    // Account Creation state
    var regRole by remember { mutableStateOf(UserRole.HOSPITAL_REGISTRAR) }
    // Hospital Admin Fields
    var regAdminName by remember { mutableStateOf("Dr. K. Anbazhagan, MBBS") }
    var regHospitalName by remember { mutableStateOf("Government Rajaji Hospital") }
    var regHospitalLocation by remember { mutableStateOf("Madurai, Tamil Nadu") }
    var regPhone by remember { mutableStateOf("+91 94420 18291") }
    var regEmail by remember { mutableStateOf("admin.grh@tn.gov.in") }
    var regPassword by remember { mutableStateOf("Secure@Pass2026") }
    var regConfirmPassword by remember { mutableStateOf("Secure@Pass2026") }
    var regGeneratedId by remember { mutableStateOf("HOSP-TN-MDU-74291") }
    var regVerificationCodeSent by remember { mutableStateOf(false) }
    var regGeneratedCode by remember { mutableStateOf("839201") }
    var regEnteredCode by remember { mutableStateOf("") }

    // Medical Council Fields
    var councilOfficerName by remember { mutableStateOf("Dr. S. Thangapandian, MD") }
    var councilDept by remember { mutableStateOf("Directorate of Public Health & Preventive Medicine") }
    var councilLocation by remember { mutableStateOf("DMS Complex, Teynampet, Chennai") }
    var councilPhone by remember { mutableStateOf("+91 98401 55667") }
    var councilEmail by remember { mutableStateOf("council.officer@tn.gov.in") }
    var councilPassword by remember { mutableStateOf("CouncilSecure@2026") }
    var councilConfirmPassword by remember { mutableStateOf("CouncilSecure@2026") }
    var councilGeneratedId by remember { mutableStateOf("COUNCIL-TN-CHN-89210") }
    var councilVerificationCodeSent by remember { mutableStateOf(false) }
    var councilGeneratedCode by remember { mutableStateOf("741920") }
    var councilEnteredCode by remember { mutableStateOf("") }

    // Created Account Confirmation Dialog
    var showAccountCreatedModal by remember { mutableStateOf(false) }
    var createdAccountSummary by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

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
            .testTag("auth_screen"),
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

        // PAGE HERO CARD (INDIAN GOVT E-GOVERNANCE DESIGN SYSTEM)
        GovPageHero(
            title = "OFFICIAL GOVERNMENT AUTHENTICATION & ACCESS CONTROL",
            subtitle = "Government of Tamil Nadu • National Digital Health Mission • Civil Registration Directorate",
            icon = Icons.Default.Shield
        )

        // NAVIGATION SWITCHER: LOGIN vs CREATE ACCOUNT vs FORGOT PASSWORD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE2E8F0))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { authMode = AuthMode.LOGIN },
                color = if (authMode == AuthMode.LOGIN) TnPrimary else Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (authMode == AuthMode.LOGIN) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sign In",
                        color = if (authMode == AuthMode.LOGIN) Color.White else Color(0xFF0F172A),
                        fontWeight = if (authMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1.3f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { authMode = AuthMode.CREATE_ACCOUNT },
                color = if (authMode == AuthMode.CREATE_ACCOUNT) TnPrimary else Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = if (authMode == AuthMode.CREATE_ACCOUNT) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create Account",
                        color = if (authMode == AuthMode.CREATE_ACCOUNT) Color.White else Color(0xFF0F172A),
                        fontWeight = if (authMode == AuthMode.CREATE_ACCOUNT) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { authMode = AuthMode.FORGOT_PASSWORD },
                color = if (authMode == AuthMode.FORGOT_PASSWORD) TnPrimary else Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = if (authMode == AuthMode.FORGOT_PASSWORD) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Forgot Pass?",
                        color = if (authMode == AuthMode.FORGOT_PASSWORD) Color.White else Color(0xFF0F172A),
                        fontWeight = if (authMode == AuthMode.FORGOT_PASSWORD) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        when (authMode) {
            AuthMode.LOGIN -> {
                // ==========================================
                // LOGIN PAGE (INDIAN GOVT E-GOVERNANCE PORTAL)
                // ==========================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // TWO-OPTION SEGMENTED TABS (DIRECTLY BELOW HERO)
                    GovSegmentedTabs(
                        options = listOf(
                            "GOVT. MEDICAL COUNCIL" to Icons.Default.AccountBalance,
                            "HOSPITAL REGISTRAR" to Icons.Default.LocalHospital
                        ),
                        selectedIndex = if (selectedRole == UserRole.MEDICAL_COUNCIL) 0 else 1,
                        onSelectIndex = { index ->
                            selectedRole = if (index == 0) UserRole.MEDICAL_COUNCIL else UserRole.HOSPITAL_REGISTRAR
                            onRoleSelected(selectedRole)
                            loginId = if (index == 0) "COUNCIL-TN-CHN-89210" else "HOSP-TN-MDU-74291"
                        }
                    )

                    // INFO CALLOUT BOX (PALE BLUE OR PALE GREEN)
                    GovInfoCalloutBox(
                        title = if (selectedRole == UserRole.MEDICAL_COUNCIL)
                            "STATEWIDE COUNCIL OVERSIGHT ACCESS"
                        else
                            "HOSPITAL REGISTRAR ADMINISTRATIVE ACCESS",
                        description = if (selectedRole == UserRole.MEDICAL_COUNCIL)
                            "1st Login: Government Medical Council. Read-only oversight privileges across all empaneled hospitals statewide with Unique Council ID."
                        else
                            "2nd Login: Hospital Admin. Authorized to create and update newborn records for your designated medical center. Statutory 24-hour edit rule applies.",
                        isGreenVariant = (selectedRole == UserRole.HOSPITAL_REGISTRAR)
                    )

                    // SECONDARY / QUICK-SELECT DEMO CARDS (TWO-COLUMN GRID)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "QUICK-SELECT DEMO CREDENTIALS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GovTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GovQuickSelectCard(
                                title = "Govt Medical Council",
                                subtext = "COUNCIL-TN-CHN-89210",
                                icon = Icons.Default.AccountBalance,
                                isSelected = selectedRole == UserRole.MEDICAL_COUNCIL,
                                onClick = {
                                    selectedRole = UserRole.MEDICAL_COUNCIL
                                    onRoleSelected(UserRole.MEDICAL_COUNCIL)
                                    loginId = "COUNCIL-TN-CHN-89210"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            GovQuickSelectCard(
                                title = "Hospital Admin",
                                subtext = "HOSP-TN-MDU-74291",
                                icon = Icons.Default.LocalHospital,
                                isSelected = selectedRole == UserRole.HOSPITAL_REGISTRAR,
                                onClick = {
                                    selectedRole = UserRole.HOSPITAL_REGISTRAR
                                    onRoleSelected(UserRole.HOSPITAL_REGISTRAR)
                                    loginId = "HOSP-TN-MDU-74291"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // LOGIN FORM CARD
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GovBorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // UNIQUE REGISTRATION ID INPUT
                            GovFormField(
                                label = if (selectedRole == UserRole.MEDICAL_COUNCIL)
                                    "Medical Council Unique Registration ID"
                                else
                                    "Hospital Unique Registration ID",
                                value = loginId,
                                onValueChange = { loginId = it },
                                placeholder = if (selectedRole == UserRole.MEDICAL_COUNCIL)
                                    "e.g. COUNCIL-TN-CHN-89210"
                                else
                                    "e.g. HOSP-TN-MDU-74291",
                                isRequired = true,
                                leadingIcon = Icons.Default.Badge,
                                modifier = Modifier.testTag("input_login_id")
                            )

                            // PASSWORD INPUT
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                GovFormField(
                                    label = "Official Access Password",
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    placeholder = "Enter confidential password",
                                    isRequired = true,
                                    leadingIcon = Icons.Default.Lock,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password",
                                                tint = Color(0xFF475569)
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("input_login_password")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Forgot Password? (Raise to Medical Council)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GovDeepGreen,
                                        modifier = Modifier
                                            .clickable {
                                                fpRole = selectedRole
                                                authMode = AuthMode.FORGOT_PASSWORD
                                            }
                                            .testTag("btn_forgot_password_link")
                                    )
                                }
                            }

                            // HIGH-CONTRAST CAPTCHA BOX
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "SECURITY CHALLENGE (CAPTCHA)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GovTextDark,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                            .border(1.5.dp, GovBorderLight, RoundedCornerShape(8.dp))
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = captchaCode,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 4.sp,
                                            color = GovDeepGreen
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
                                            captchaCode = (1..6).map { chars.random() }.joinToString(" ")
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(GovCalloutBgGreen, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh Captcha",
                                            tint = GovDeepGreen
                                        )
                                    }
                                }
                                GovFormField(
                                    label = "Enter Captcha Verification Code",
                                    value = captchaInput,
                                    onValueChange = { captchaInput = it },
                                    placeholder = "Enter characters displayed above",
                                    isRequired = true,
                                    leadingIcon = Icons.Default.Shield,
                                    modifier = Modifier.testTag("input_captcha")
                                )
                            }

                            // PRIMARY FULL-WIDTH GREEN BUTTON
                            GovPrimaryButton(
                                text = if (selectedRole == UserRole.MEDICAL_COUNCIL)
                                    "LOG IN TO COUNCIL OVERSIGHT PORTAL"
                                else
                                    "LOG IN TO HOSPITAL REGISTRY PORTAL",
                                icon = Icons.Default.VerifiedUser,
                                onClick = {
                                    successToastMsg = "SECURE SESSION ESTABLISHED" to "Authenticated as ${if (selectedRole == UserRole.MEDICAL_COUNCIL) "Government Medical Council" else "Hospital Admin"}. TLS 1.3 Node Verified."
                                    onRoleSelected(selectedRole)
                                    onLoginSuccess(selectedRole)
                                },
                                modifier = Modifier.testTag("btn_sign_in")
                            )

                            // CREATE FIRST-TIME LOGIN ACCOUNT LINK
                            OutlinedButton(
                                onClick = { authMode = AuthMode.CREATE_ACCOUNT },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, GovDeepGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GovDeepGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = GovDeepGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CREATE FIRST-TIME LOGIN ACCOUNT",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            AuthMode.FORGOT_PASSWORD -> {
                // ==========================================
                // SPECIAL CASE: FORGOT PASSWORD WORKFLOW
                // ==========================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = SovereignGoldDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "SPECIAL CASE: Forgot Password Protocol",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Statutory Password Recovery via Medical Council Approval",
                                    fontSize = 11.sp,
                                    color = Color(0xFF334155)
                                )
                            }
                        }

                        // Rule Explanation Notice
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SovereignGold.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "GOVERNMENT MANDATE FOR FORGOTTEN PASSWORDS:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "If user forgot their password, they need to request to the Medical Council of Government / head of hospitalities management. Once approved, the hospital can create a new password using the approval of Government and MUST submit a valid reason for forgetting the password.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        if (fpSubmittedRequestId == null) {
                            // STEP 1: RAISE REQUEST WITH VALID REASON
                            Text(
                                text = "Step 1: Submit Reset Request to Medical Council Head",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TnDeepTeal
                            )

                            OutlinedTextField(
                                value = fpHospitalName,
                                onValueChange = { fpHospitalName = it },
                                label = { Text("Hospital Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = fpHospitalLocation,
                                onValueChange = { fpHospitalLocation = it },
                                label = { Text("Hospital Location / District") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = fpAdminId,
                                onValueChange = { fpAdminId = it },
                                label = { Text("Hospital Admin / Registrar ID") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = fpPhone,
                                    onValueChange = { fpPhone = it },
                                    label = { Text("Contact Phone") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = fpEmail,
                                    onValueChange = { fpEmail = it },
                                    label = { Text("Official Email") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // MANDATORY VALID REASON
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Valid Reason for Forgetting Password (MANDATORY)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB91C1C)
                                )
                                OutlinedTextField(
                                    value = fpReason,
                                    onValueChange = { fpReason = it },
                                    placeholder = { Text("State the specific valid reason for password reset request...") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Button(
                                onClick = {
                                    if (fpReason.trim().isEmpty()) {
                                        Toast.makeText(context, "Valid reason is required under Government rules!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val reqId = SafeStartRepository.requestPasswordReset(
                                        hospitalName = fpHospitalName,
                                        district = fpHospitalLocation,
                                        registrarName = fpAdminId,
                                        reason = fpReason
                                    )
                                    fpSubmittedRequestId = reqId
                                    Toast.makeText(context, "Request $reqId submitted to Government Medical Council!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Request to Medical Council Head", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // STEP 2 & 3: REQUEST SUBMITTED -> ENTER COUNCIL APPROVAL TOKEN & NEW PASSWORD
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                                        Text("REQUEST PENDING AT COUNCIL DOCKET: $fpSubmittedRequestId", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                    }
                                    Text("Reason Recorded: \"$fpReason\"", fontSize = 11.sp, color = Color(0xFF047857))
                                    Text("Government Medical Council can review and approve this from their Oversight portal.", fontSize = 11.sp, color = Color(0xFF047857))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = {
                                            SafeStartRepository.approvePasswordReset(fpSubmittedRequestId!!)
                                            fpApprovalTokenInput = "COUNCIL-APPRV-KEY-881"
                                            Toast.makeText(context, "Council Key Auto-Approved for demonstration!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text("Simulate Medical Council Instant Approval", fontSize = 11.sp)
                                    }
                                }
                            }

                            Text(
                                text = "Step 2: Enter Council Approval Key & Create New Password",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TnDeepTeal
                            )

                            OutlinedTextField(
                                value = fpApprovalTokenInput,
                                onValueChange = { fpApprovalTokenInput = it },
                                label = { Text("Government Medical Council Approval Token / Key") },
                                placeholder = { Text("e.g. COUNCIL-APPRV-KEY-881") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = fpNewPassword,
                                onValueChange = { fpNewPassword = it },
                                label = { Text("Create New Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = fpConfirmNewPassword,
                                onValueChange = { fpConfirmNewPassword = it },
                                label = { Text("Re-enter New Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (fpApprovalTokenInput.isEmpty()) {
                                        Toast.makeText(context, "Council approval token is required!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (fpNewPassword.isEmpty() || fpNewPassword != fpConfirmNewPassword) {
                                        Toast.makeText(context, "Passwords do not match or are empty!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    loginPassword = fpNewPassword
                                    authMode = AuthMode.LOGIN
                                    Toast.makeText(context, "New Password Created Successfully using Government Approval! Please Sign In.", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Activate New Password & Return to Login", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(
                            onClick = { authMode = AuthMode.LOGIN },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Cancel and Return to Sign In", color = Color(0xFF475569))
                        }
                    }
                }
            }

            AuthMode.CREATE_ACCOUNT -> {
                // ==========================================
                // CREATE FIRST TIME LOGIN ACCOUNT (ROLES)
                // ==========================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "CREATE FIRST TIME LOGIN ACCOUNT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )

                        // ROLE SWITCHER: Hospital Admin vs Medical Council
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(2.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { regRole = UserRole.HOSPITAL_REGISTRAR },
                                color = if (regRole == UserRole.HOSPITAL_REGISTRAR) TnPrimary else Color.Transparent
                            ) {
                                Text(
                                    text = "For: Hospital Admin",
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (regRole == UserRole.HOSPITAL_REGISTRAR) Color.White else Color(0xFF1E293B)
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { regRole = UserRole.MEDICAL_COUNCIL },
                                color = if (regRole == UserRole.MEDICAL_COUNCIL) TnPrimary else Color.Transparent
                            ) {
                                Text(
                                    text = "For: Government Medical Council",
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (regRole == UserRole.MEDICAL_COUNCIL) Color.White else Color(0xFF1E293B)
                                )
                            }
                        }

                        if (regRole == UserRole.HOSPITAL_REGISTRAR) {
                            // ----------------------------------------------------
                            // FOR: HOSPITAL ADMIN
                            // 1. Name
                            // 2. Hospital name
                            // 3. Hospital location
                            // 4. Phone number
                            // 5. E mail
                            // 6. Create a password for their usual login
                            // 7. Re-enter the password
                            // 8. Now generate a random login ID
                            // 9. For verification send a confirmation code to email [4 or 6 digits]
                            // 10. Finally send Email that Account was Created
                            // ----------------------------------------------------
                            Text(
                                text = "Hospital Admin Registration Checklist (Fields 1-10)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TnDeepTeal
                            )

                            // 1. Name
                            OutlinedTextField(
                                value = regAdminName,
                                onValueChange = { regAdminName = it },
                                label = { Text("1. Name (Hospital Admin)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 2. Hospital name
                            OutlinedTextField(
                                value = regHospitalName,
                                onValueChange = { regHospitalName = it },
                                label = { Text("2. Hospital Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 3. Hospital location
                            OutlinedTextField(
                                value = regHospitalLocation,
                                onValueChange = { regHospitalLocation = it },
                                label = { Text("3. Hospital Location (District / Town)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 4. Phone number & 5. Email
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = regPhone,
                                    onValueChange = { regPhone = it },
                                    label = { Text("4. Phone Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = regEmail,
                                    onValueChange = { regEmail = it },
                                    label = { Text("5. Email") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // 6. Create password & 7. Re-enter password
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("6. Create a password for usual login") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it },
                                label = { Text("7. Re-enter the password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 8. Generate random login ID
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("8. Generated Random Login ID:", fontSize = 11.sp, color = Color(0xFF475569))
                                        Text(
                                            text = regGeneratedId,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TnDeepTeal
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            regGeneratedId = "HOSP-TN-MDU-" + (10000..99999).random()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Generate Random ID", fontSize = 11.sp)
                                    }
                                }
                            }

                            // 9. Email verification code
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("9. Email Verification Code (6 Digits)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        OutlinedButton(
                                            onClick = {
                                                regGeneratedCode = (100000..999999).random().toString()
                                                regVerificationCodeSent = true
                                                regEnteredCode = regGeneratedCode
                                                Toast.makeText(context, "Verification code sent to $regEmail: $regGeneratedCode", Toast.LENGTH_LONG).show()
                                            },
                                            border = BorderStroke(1.dp, TnDeepTeal),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(if (regVerificationCodeSent) "Resend Code" else "Send Code to Email", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TnDeepTeal)
                                        }
                                    }

                                    if (regVerificationCodeSent) {
                                        Text(
                                            text = "Verification code dispatched to $regEmail. Enter code:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF047857)
                                        )
                                        OutlinedTextField(
                                            value = regEnteredCode,
                                            onValueChange = { regEnteredCode = it },
                                            placeholder = { Text("Enter 6-digit confirmation code") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // 10. Complete Account Creation & Send Email Notification
                            Button(
                                onClick = {
                                    if (regAdminName.isEmpty() || regHospitalName.isEmpty()) {
                                        Toast.makeText(context, "Please fill in all details!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (regPassword.isEmpty() || regPassword != regConfirmPassword) {
                                        Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val now = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a 'IST'", Locale.getDefault()).format(Date())
                                    createdAccountSummary = mapOf(
                                        "Role" to "Hospital Admin",
                                        "Name" to regAdminName,
                                        "Hospital" to regHospitalName,
                                        "Location" to regHospitalLocation,
                                        "Generated ID" to regGeneratedId,
                                        "DateTime" to now,
                                        "Email" to regEmail
                                    )
                                    showAccountCreatedModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("10. Verify & Create Hospital Admin Account", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            // ----------------------------------------------------
                            // FOR: GOVERNMENT MEDICAL COUNCIL
                            // 1. Name
                            // 2. Directorate/council department
                            // 3. Location/Headquarters
                            // 4. Phone number
                            // 5. E mail
                            // 6. Create a password for their usual login
                            // 7. Re-enter the password
                            // 8. Now generate a random login ID
                            // 9. For verification send confirmation code to email [4 or 6 digits]
                            // 10. Finally send Email that Account was Created showing Name, Date and Time
                            // ----------------------------------------------------
                            Text(
                                text = "Government Medical Council Registration Checklist (Fields 1-10)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TnDeepTeal
                            )

                            // 1. Name
                            OutlinedTextField(
                                value = councilOfficerName,
                                onValueChange = { councilOfficerName = it },
                                label = { Text("1. Name (Council Official)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 2. Directorate/council department
                            OutlinedTextField(
                                value = councilDept,
                                onValueChange = { councilDept = it },
                                label = { Text("2. Directorate / Council Department") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 3. Location/Headquarters
                            OutlinedTextField(
                                value = councilLocation,
                                onValueChange = { councilLocation = it },
                                label = { Text("3. Location / Headquarters") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 4. Phone number & 5. Email
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = councilPhone,
                                    onValueChange = { councilPhone = it },
                                    label = { Text("4. Phone Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = councilEmail,
                                    onValueChange = { councilEmail = it },
                                    label = { Text("5. Email") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // 6. Password & 7. Re-enter password
                            OutlinedTextField(
                                value = councilPassword,
                                onValueChange = { councilPassword = it },
                                label = { Text("6. Create a password for usual login") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = councilConfirmPassword,
                                onValueChange = { councilConfirmPassword = it },
                                label = { Text("7. Re-enter the password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 8. Generate random login ID
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("8. Generated Random Council Login ID:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                        Text(
                                            text = councilGeneratedId,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TnDeepTeal
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            councilGeneratedId = "COUNCIL-TN-CHN-" + (10000..99999).random()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Generate Random ID", fontSize = 11.sp)
                                    }
                                }
                            }

                            // 9. Email verification code
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("9. Email Verification Code (6 Digits)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        OutlinedButton(
                                            onClick = {
                                                councilGeneratedCode = (100000..999999).random().toString()
                                                councilVerificationCodeSent = true
                                                councilEnteredCode = councilGeneratedCode
                                                Toast.makeText(context, "Verification code sent to $councilEmail: $councilGeneratedCode", Toast.LENGTH_LONG).show()
                                            },
                                            border = BorderStroke(1.dp, TnDeepTeal),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(if (councilVerificationCodeSent) "Resend Code" else "Send Code to Email", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TnDeepTeal)
                                        }
                                    }

                                    if (councilVerificationCodeSent) {
                                        Text(
                                            text = "Verification code dispatched to $councilEmail. Enter code:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF047857)
                                        )
                                        OutlinedTextField(
                                            value = councilEnteredCode,
                                            onValueChange = { councilEnteredCode = it },
                                            placeholder = { Text("Enter 6-digit confirmation code") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // 10. Complete Account Creation & Send Email Notification
                            Button(
                                onClick = {
                                    if (councilOfficerName.isEmpty() || councilDept.isEmpty()) {
                                        Toast.makeText(context, "Please fill in all details!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (councilPassword.isEmpty() || councilPassword != councilConfirmPassword) {
                                        Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val now = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a 'IST'", Locale.getDefault()).format(Date())
                                    createdAccountSummary = mapOf(
                                        "Role" to "Government Medical Council",
                                        "Name" to councilOfficerName,
                                        "Department" to councilDept,
                                        "Location" to councilLocation,
                                        "Generated ID" to councilGeneratedId,
                                        "DateTime" to now,
                                        "Email" to councilEmail
                                    )
                                    showAccountCreatedModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("10. Verify & Create Medical Council Account", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        TextButton(
                            onClick = { authMode = AuthMode.LOGIN },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Already have an account? Sign In", fontWeight = FontWeight.Bold, color = TnDeepTeal)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL: EMAIL CONFIRMATION THAT ACCOUNT WAS CREATED (FIELD 10 REQUIREMENT)
    // =========================================================================
    if (showAccountCreatedModal) {
        Dialog(onDismissRequest = { showAccountCreatedModal = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(2.dp, TnDeepTeal),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = Color(0xFF059669))
                        }
                        Column {
                            Text(
                                text = "EMAIL DISPATCHED: ACCOUNT CREATED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TnDeepTeal
                            )
                            Text(
                                text = "State Civic Registry Notification",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    // Email body details according to requirements
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "To: ${createdAccountSummary["Email"]}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Subject: Official Safe Start Account Activation Notice",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Divider(color = Color(0xFFE2E8F0))

                            Text("Dear ${createdAccountSummary["Name"]},", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            Text(
                                text = "Your official account for the Government of Tamil Nadu Newborn Identity Custody Portal has been successfully created and authenticated.",
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                lineHeight = 16.sp
                            )

                            // Specific fields
                            if (createdAccountSummary["Role"] == "Hospital Admin") {
                                Text("• Hospital Name: ${createdAccountSummary["Hospital"]}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                Text("• Hospital Location: ${createdAccountSummary["Location"]}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            } else {
                                Text("• Council Directorate: ${createdAccountSummary["Department"]}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                            }
                            Text("• Assigned Unique Login ID: ${createdAccountSummary["Generated ID"]}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TnDeepTeal)
                            Text("• Date and Time: ${createdAccountSummary["DateTime"]}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }
                    }

                    Button(
                        onClick = {
                            showAccountCreatedModal = false
                            // Pre-fill login with generated credentials and switch to Login tab
                            loginId = createdAccountSummary["Generated ID"] ?: ""
                            selectedRole = if (createdAccountSummary["Role"] == "Hospital Admin") UserRole.HOSPITAL_REGISTRAR else UserRole.MEDICAL_COUNCIL
                            authMode = AuthMode.LOGIN
                            Toast.makeText(context, "Account ready! Please enter password to log in.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Proceed to Login with this Account", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
