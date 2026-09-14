package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.AppWing
import com.example.data.SafeStartAssets
import com.example.data.SafeStartRepository
import com.example.data.UserRole
import com.example.network.ResendEmailService
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * 1. Top bar: thin black strip, saffron/gold text, left = official portal name + tagline,
 * right = helpline/status info in white/green text.
 * Responsive layout ensures no text is hidden on 360dp phone screens.
 */
@Composable
fun GovTopBar(
    modifier: Modifier = Modifier
) {
    Surface(
        color = GovTopBarBlack,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: official portal name + regional tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = "🇮🇳",
                    fontSize = 10.sp
                )
                Text(
                    text = "GOVT. OF TAMIL NADU",
                    color = GovSaffronGold,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
                Text(
                    text = "• NDHM",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            // Right: helpline / status info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = "Helpline",
                        tint = GovSaffronGold,
                        modifier = Modifier.size(9.dp)
                    )
                    Text(
                        text = "104",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "•",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 8.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Text(
                        text = "ONLINE",
                        color = Color(0xFF4ADE80),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 2. Header: solid green background (#0E7C4A deep green), official unreleased baby protection
 * logo in a white circle on the left, bold white title (bilingual — English + regional language subtitle),
 * role switcher and quick settings for Emblem Choice and Resend Email verification.
 */
@Composable
fun GovHeader(
    currentWing: AppWing,
    currentRole: UserRole,
    onWingSelected: (AppWing) -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoChoiceDialog by remember { mutableStateOf(false) }
    var showResendDialog by remember { mutableStateOf(false) }
    val selectedLogoChoice by SafeStartRepository.selectedLogoChoice.collectAsState()

    // Dialogs
    if (showLogoChoiceDialog) {
        OfficialLogoConfirmationDialog(
            currentChoice = selectedLogoChoice,
            onChoiceConfirmed = { choice ->
                SafeStartRepository.setSelectedLogoChoice(choice)
                showLogoChoiceDialog = false
            },
            onDismiss = { showLogoChoiceDialog = false }
        )
    }

    if (showResendDialog) {
        ResendApiKeyDialog(
            onDismiss = { showResendDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GovDeepGreen)
    ) {
        // Official Top Bar Strip
        GovTopBar()

        // Main Header Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Unreleased Baby Protection Emblem + Bilingual Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Clickable Emblem with Choice indicator
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, GovSaffronGold, CircleShape)
                            .clickable { showLogoChoiceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        val logoRes = when (selectedLogoChoice) {
                            1 -> R.drawable.img_logo_choice1
                            2 -> R.drawable.img_logo_choice2
                            3 -> R.drawable.img_logo_choice3
                            4 -> R.drawable.img_safestart_logo
                            else -> R.drawable.img_logo_choice3
                        }
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = logoRes),
                            contentDescription = "Official Tamil Nadu Baby Protection Emblem",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "SAFE START",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp,
                                maxLines = 1
                            )
                            Surface(
                                color = GovSaffronGold.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(3.dp),
                                border = BorderStroke(0.5.dp, GovSaffronGold)
                            ) {
                                Text(
                                    text = "TN-GOV",
                                    color = GovSaffronGold,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "காவல் துவக்கம் • NEWBORN IDENTITY",
                            color = GovGoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Statutory Institutional Registry & Biometric Custody",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 8.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Role Switcher Button
                Surface(
                    color = GovDarkNavy.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, GovSaffronGold),
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .clickable {
                            val nextRole = if (currentRole == UserRole.HOSPITAL_REGISTRAR) {
                                UserRole.MEDICAL_COUNCIL
                            } else {
                                UserRole.HOSPITAL_REGISTRAR
                            }
                            onRoleSelected(nextRole)
                        }
                        .testTag("role_switcher_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (currentRole == UserRole.HOSPITAL_REGISTRAR) {
                                Icons.Default.LocalHospital
                            } else {
                                Icons.Default.VerifiedUser
                            },
                            contentDescription = "Role icon",
                            tint = GovSaffronGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Column {
                            Text(
                                text = if (currentRole == UserRole.HOSPITAL_REGISTRAR) "REGISTRAR" else "COUNCIL",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "SWITCH ROLE",
                                color = GovSaffronGold,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Quick Configuration Chips Row (Logo Choices + Resend Real Email & OTP)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Official Emblem Choice Trigger Chip
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.8.dp, GovSaffronGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { showLogoChoiceDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = GovSaffronGold, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Emblem: Option $selectedLogoChoice (View Pictures)",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Resend Email API Status / Settings Trigger Chip
                val resendReady = ResendEmailService.isConfigured()
                Surface(
                    color = if (resendReady) Color(0xFF047857).copy(alpha = 0.5f) else Color(0xFFB45309).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.8.dp, if (resendReady) Color(0xFF34D399) else Color(0xFFFBBF24)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { showResendDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (resendReady) Icons.Default.MarkEmailRead else Icons.Default.Email,
                            contentDescription = null,
                            tint = if (resendReady) Color(0xFF6EE7B7) else Color(0xFFFDE68A),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (resendReady) "Resend API: Active" else "Resend API: Setup Key",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Operational Wing Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = currentWing.ordinal,
            containerColor = Color.Black.copy(alpha = 0.15f),
            contentColor = Color.White,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                if (currentWing.ordinal < tabPositions.size) {
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[currentWing.ordinal])
                            .height(3.dp)
                            .background(GovSaffronGold)
                    )
                }
            },
            divider = {},
            modifier = Modifier.testTag("wing_nav_tabs")
        ) {
            AppWing.values().forEach { wing ->
                val isSelected = currentWing == wing
                Tab(
                    selected = isSelected,
                    onClick = { onWingSelected(wing) },
                    modifier = Modifier.testTag("tab_${wing.name.lowercase()}"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            val icon = when (wing) {
                                AppWing.DASHBOARD -> Icons.Default.Dashboard
                                AppWing.AUTH -> Icons.Default.Lock
                                AppWing.HOSPITAL_REGISTRY -> Icons.Default.LocalHospital
                                AppWing.COUNCIL_OVERSIGHT -> Icons.Default.Gavel
                                AppWing.DISPUTE_VERIFICATION -> Icons.Default.Fingerprint
                                AppWing.AUDIT_LEDGER -> Icons.AutoMirrored.Filled.ReceiptLong
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = wing.title,
                                tint = if (isSelected) GovSaffronGold else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = wing.title.uppercase(),
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                letterSpacing = 0.4.sp
                            )
                        }
                    }
                )
            }
        }

        // Thin Gold/Yellow Bottom Border Accent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(GovSaffronGold)
        )
    }
}

/**
 * 3. Page hero/login card: dark navy (#0B1220) rounded-top banner with centered icon
 * in a green circle, bold white heading, gray subtext underneath.
 */
@Composable
fun GovPageHero(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Default.Security,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GovDarkNavy,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Centered icon in a green circle (#0E7C4A)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GovDeepGreen)
                    .border(2.dp, GovSaffronGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Bold white heading
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )

            // Gray subtext underneath
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * 4. Tabs: two-option segmented tabs below the hero, active tab has light green background
 * + green bottom border + green icon/text, inactive tab is gray/white with muted gray text.
 */
@Composable
fun GovSegmentedTabs(
    options: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .border(1.dp, GovGrayBorder)
    ) {
        options.forEachIndexed { index, (label, icon) ->
            val isActive = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (isActive) GovLightGreenTabBg else Color(0xFFF8FAFC))
                    .clickable { onSelectIndex(index) }
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isActive) GovDeepGreen else GovGrayText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 11.5.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) GovDeepGreen else GovGrayText,
                            letterSpacing = 0.4.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Green bottom border for active tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(3.dp)
                            .background(if (isActive) GovDeepGreen else Color.Transparent)
                    )
                }
            }
        }
    }
}

/**
 * 5. Info callout box: pale blue or pale green background, colored left icon,
 * bold heading line, smaller gray description text, subtle border, rounded corners.
 */
@Composable
fun GovInfoCalloutBox(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    isGreenVariant: Boolean = false,
    icon: ImageVector = if (isGreenVariant) Icons.Default.CheckCircle else Icons.Default.Info
) {
    val bgColor = if (isGreenVariant) GovPaleGreenBg else GovPaleBlueBg
    val borderColor = if (isGreenVariant) GovPaleGreenBorder else GovPaleBlueBorder
    val iconColor = if (isGreenVariant) GovPaleGreenIcon else GovPaleBlueIcon
    val titleColor = if (isGreenVariant) GovPaleGreenIcon else Color(0xFF0369A1)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 1.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * 6. Form fields: label in bold uppercase small caps with a red asterisk for required,
 * input has a light gray border, rounded corners, left-aligned icon inside the field, generous padding.
 */
@Composable
fun GovFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isRequired: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label in bold uppercase with red asterisk for required
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    append(label.uppercase())
                    if (isRequired) {
                        append(" ")
                        withStyle(SpanStyle(color = GovAlertRed, fontWeight = FontWeight.Bold)) {
                            append("*")
                        }
                    }
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155),
                letterSpacing = 0.5.sp
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = GovGrayText,
                    fontSize = 12.5.sp
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = GovDeepGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GovDarkText,
                unfocusedTextColor = GovDarkText,
                disabledTextColor = Color(0xFF64748B),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = GovDeepGreen,
                unfocusedBorderColor = GovGrayBorder,
                errorBorderColor = GovAlertRed,
                focusedLeadingIconColor = GovDeepGreen,
                unfocusedLeadingIconColor = Color(0xFF475569),
                cursorColor = GovDeepGreen
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = GovAlertRed,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

/**
 * 7. Primary button: full-width, solid dark green (#0E7C4A), white bold uppercase text
 * with a small icon, rounded corners, subtle shadow.
 */
@Composable
fun GovPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.Default.Check,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GovDeepGreen,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF94A3B8),
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PROCESSING...",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = Color.White
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = text.uppercase(),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 8. Secondary/quick-select cards: flexible adaptive grid of light bordered cards
 * with bold colored title + gray ID/subtext below, hover/click highlight.
 * Automatically adapts across phone screen widths to prevent content overflow.
 */
@Composable
fun GovQuickSelectCard(
    title: String,
    subtext: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    badgeText: String? = null
) {
    Surface(
        color = if (isSelected) GovLightGreenTabBg else Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.2.dp,
            if (isSelected) GovDeepGreen else GovGrayBorder
        ),
        shadowElevation = if (isSelected) 2.dp else 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) GovDeepGreen else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else GovDeepGreen,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) GovDeepGreen else GovDarkText,
                        maxLines = 2,
                        lineHeight = 14.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (badgeText != null) {
                        Surface(
                            color = if (isSelected) GovDeepGreen else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtext,
                    fontSize = 9.5.sp,
                    color = GovGrayText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Flexible arrangement container for secondary / quick-select cards.
 * Automatically adapts column count based on available width (1 column on narrow phones < 340dp,
 * weight-based 2-column row on standard phones and tablets) to completely prevent content clipping.
 */
@Composable
fun <T> GovAdaptiveQuickSelectGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    minColumnWidth: androidx.compose.ui.unit.Dp = 150.dp,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    itemContent: @Composable (item: T, isSingleColumn: Boolean) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val availableWidth = maxWidth
        // If available width is below 340dp or too tight for 2 columns, fall back to single column
        val isSingleColumn = availableWidth < 340.dp
        val columns = if (isSingleColumn) 1 else 2

        val rows = items.chunked(columns)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
                ) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            itemContent(item, isSingleColumn)
                        }
                    }
                    val emptySlots = columns - rowItems.size
                    for (i in 0 until emptySlots) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 9. Section cards (multi-field forms): white rounded cards with light gray border
 * and subtle shadow, bold uppercase numbered section titles ("1. FIELD GROUP NAME"),
 * a colored status pill on the right ("✕ Not Captured" in red pill / "✓ Verified" in green pill).
 */
@Composable
fun GovSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    number: String? = null,
    statusPillText: String? = null,
    isStatusVerified: Boolean = false,
    isVerified: Boolean = isStatusVerified,
    statusTextOverride: String? = statusPillText,
    content: @Composable ColumnScope.() -> Unit
) {
    val displayTitle = if (number != null && !title.startsWith(number)) "$number. $title" else title
    val effectiveVerified = isVerified || isStatusVerified
    val statusText = statusTextOverride ?: statusPillText ?: if (effectiveVerified) "✓ Verified" else "✕ Not Captured"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, GovGrayBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Header Row: Numbered Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = displayTitle.uppercase(),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GovDarkNavy,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )

                // Colored status pill on the right
                Surface(
                    color = if (effectiveVerified) GovVerifiedGreenBg else GovAlertRedBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (effectiveVerified) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
                ) {
                    Text(
                        text = statusText,
                        color = if (effectiveVerified) GovVerifiedGreen else GovAlertRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFFF1F5F9),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Body
            content()
        }
    }
}

/**
 * 10. Biometric/upload capture boxes: dashed-border placeholder box, centered icon or
 * scan animation (concentric dashed circles in green while "scanning"),
 * three small action buttons underneath: green "Capture", dark navy "Upload", gray "Clear".
 */
@Composable
fun GovBiometricCaptureBox(
    title: String = "",
    label: String = title,
    previewUrl: String? = null,
    capturedImageUri: String? = previewUrl,
    fileName: String = "",
    isCaptured: Boolean = !previewUrl.isNullOrBlank() || !capturedImageUri.isNullOrBlank() || fileName.isNotBlank(),
    isScanning: Boolean = false,
    onCaptureClick: () -> Unit = {},
    onCapture: () -> Unit = onCaptureClick,
    onUploadClick: () -> Unit = {},
    onUpload: () -> Unit = onUploadClick,
    onClearClick: () -> Unit = {},
    onClear: () -> Unit = onClearClick,
    modifier: Modifier = Modifier
) {
    val displayTitle = if (title.isNotEmpty()) title else label
    val effectiveCaptureAction = if (onCapture != onCaptureClick) onCapture else onCaptureClick
    val effectiveUploadAction = if (onUpload != onUploadClick) onUpload else onUploadClick
    val effectiveClearAction = if (onClear != onClearClick) onClear else onClearClick

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = displayTitle.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Dashed Border Placeholder Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCaptured) GovLightGreenTabBg else Color(0xFFF8FAFC))
                .drawWithDashedBorder(
                    color = if (isCaptured) GovDeepGreen else Color(0xFF94A3B8),
                    strokeWidth = 1.5f,
                    cornerRadius = 8f
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                // Concentric scan animation
                ConcentricScanAnimation()
            } else if (isCaptured) {
                val effectiveUri = if (!capturedImageUri.isNullOrBlank()) capturedImageUri else previewUrl
                if (!effectiveUri.isNullOrBlank()) {
                    // Display the real uploaded JPG/PNG or preview image
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = effectiveUri,
                            contentDescription = "Selected biometric scan",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        // Translucent badge overlay at bottom
                        Surface(
                            color = Color(0xDD0F172A),
                            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4ADE80),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (fileName.isNotBlank()) fileName else "JPG/PNG Attached",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "SHA-256 HASHED",
                                    color = GovSaffronGold,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GovVerifiedGreenBg)
                                .border(1.5.dp, GovDeepGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Captured",
                                tint = GovDeepGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            text = "BIOMETRIC FOOTPRINT RECORDED",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GovDeepGreen,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "SHA-256 Hash Generated & Ready for Custody",
                            fontSize = 9.sp,
                            color = Color(0xFF047857)
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Place foot on sensor or click Upload JPG/PNG",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Supports certified JPG, PNG, or flatbed optical scanner",
                        fontSize = 8.5.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Three responsive action buttons underneath: green "Capture", dark navy "Upload", gray "Clear"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Green "Capture" button
            Button(
                onClick = effectiveCaptureAction,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovDeepGreen),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 36.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Capture", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            // Dark Navy "Upload" button (Supports JPG / PNG)
            Button(
                onClick = effectiveUploadAction,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovDarkNavy),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1.15f)
                    .defaultMinSize(minHeight = 36.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Upload JPG/PNG", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            // Gray "Clear" button
            OutlinedButton(
                onClick = effectiveClearAction,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, GovGrayBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(0.85f)
                    .defaultMinSize(minHeight = 36.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Clear", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    }
}

/**
 * Concentric dashed circles scan animation
 */
@Composable
private fun ConcentricScanAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(90.dp)) {
            val stroke = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )
            drawCircle(
                color = GovDeepGreen.copy(alpha = 0.3f),
                radius = size.minDimension / 2 * pulse,
                style = stroke
            )
            drawCircle(
                color = GovDeepGreen.copy(alpha = 0.7f),
                radius = size.minDimension / 3 * pulse,
                style = stroke
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = GovDeepGreen,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "SCANNING...",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GovDeepGreen,
                letterSpacing = 0.8.sp
            )
        }
    }
}

/**
 * Custom modifier helper for drawing dashed border
 */
fun Modifier.drawWithDashedBorder(color: Color, strokeWidth: Float = 1.5f, cornerRadius: Float = 8f): Modifier =
    this.drawWithCache {
        val stroke = Stroke(
            width = strokeWidth * density,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )
        onDrawWithContent {
            drawContent()
            drawRoundRect(
                color = color,
                style = stroke,
                cornerRadius = CornerRadius(cornerRadius * density, cornerRadius * density)
            )
        }
    }

/**
 * 11. Success/notification toasts:
 * Style 1: slide-in card, solid green background (#0E7C4A), white bold heading with checkmark icon,
 * white regular subtext, dismiss (×) icon top-right.
 */
@Composable
fun GovSuccessToast(
    title: String,
    message: String = "",
    description: String = message,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayMessage = if (description.isNotEmpty()) description else message

    Surface(
        color = GovDeepGreen,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, GovSaffronGold),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = displayMessage,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 11. Style 2: dark navy toast style (#0B1220) for "SMS/message delivered" simulation
 * with monospace-style body text and a green confirmation line at the bottom.
 */
@Composable
fun GovSmsDeliveredToast(
    recipient: String = "REGISTERED PHONE",
    smsBody: String = "",
    title: String = "GOVT SMS DISPATCHED: $recipient",
    monospaceBody: String = smsBody,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayTitle = if (title.isNotEmpty()) title else "GOVT SMS DISPATCHED: $recipient"
    val displayBody = if (monospaceBody.isNotEmpty()) monospaceBody else smsBody

    Surface(
        color = GovDarkNavy,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = GovSaffronGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = displayTitle,
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Monospace Body Text
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = displayBody,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFFF1F5F9),
                    lineHeight = 16.sp
                )
            }

            // Green confirmation line at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GovVerifiedGreenBg)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GovVerifiedGreen,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Delivered via State C-DAC SMS Gateway • Instant Receipt Confirmed",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovVerifiedGreen
                )
            }
        }
    }
}

/**
 * 12. Data table: clean white rows, bold uppercase gray column headers,
 * colored/mono IDs, status badges, verified icon indicators.
 */
@Composable
fun GovDataTableHeader(
    columns: List<String>,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, GovGrayBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEachIndexed { index, col ->
                Text(
                    text = col.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(if (index == 0) 1.2f else 1f)
                )
            }
        }
    }
}

/**
 * Dialog enabling the official confirmation of unreleased Tamil Nadu baby protection emblem designs.
 */
@Composable
fun OfficialLogoConfirmationDialog(
    currentChoice: Int,
    onChoiceConfirmed: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedChoice by remember { mutableStateOf(currentChoice) }
    var zoomedImageRes by remember { mutableStateOf<Int?>(null) }

    // Full-screen zoomed view modal
    if (zoomedImageRes != null) {
        Dialog(onDismissRequest = { zoomedImageRes = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(2.dp, GovSaffronGold),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENLARGED EMBLEM INSPECTION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GovDarkNavy
                        )
                        IconButton(onClick = { zoomedImageRes = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    androidx.compose.foundation.Image(
                        painter = painterResource(id = zoomedImageRes!!),
                        contentDescription = "Zoomed Logo Preview",
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .border(3.dp, GovSaffronGold, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = "Tamil Nadu Government • Civil Registration Directorate\nStatutory Tamper-Proof Newborn Protection Emblem",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF64748B)
                    )

                    Button(
                        onClick = { zoomedImageRes = null },
                        colors = ButtonDefaults.buttonColors(containerColor = GovDeepGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BACK TO SELECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    val options = remember {
        listOf(
            LogoOptionItem(
                id = 1,
                title = "Choice 1: Gopuram Apex & Protective Cradle",
                tamilTitle = "ஸ்ரீவில்லிபுத்தூர் கோபுரம் & பொற்கர தொட்டில்",
                badgeLabel = "TEMPLE GOPURAM & GOLDEN CRADLE",
                description = "Sacred Srivilliputhur Gopuram temple tower apex with cupped golden hands safely cradling a peaceful newborn infant, encircled by biometric fingerprint ridge ribbons and emerald-gold roundel.",
                drawableRes = R.drawable.img_logo_choice1
            ),
            LogoOptionItem(
                id = 2,
                title = "Choice 2: Ashoka Lion & Maternal Shield",
                tamilTitle = "அசோக சிங்க முத்திரை & தாய்-சேய் பாதுகாப்பு கேடயம்",
                badgeLabel = "ASHOKA CAPITAL & DEFENSE SHIELD",
                description = "Sovereign state crest featuring the Ashoka Lion Capital atop a circular golden shield with maternal silhouette holding infant, wheat ears, and biometric security pattern.",
                drawableRes = R.drawable.img_logo_choice2
            ),
            LogoOptionItem(
                id = 3,
                title = "Choice 3: Sacred Lotus Throne & Protected Infant",
                tamilTitle = "புனித தாமரை மலர் & பச்சிளம் குழந்தை சிம்மாசனம்",
                badgeLabel = "SACRED LOTUS & GENTLE INFANT",
                description = "Tamil Nadu state seal style with a sacred blooming lotus flower holding a swaddled infant, crowned by Tamil Nadu Gopuram architectural silhouette and biometric plantar footprint motifs.",
                drawableRes = R.drawable.img_logo_choice3
            ),
            LogoOptionItem(
                id = 4,
                title = "Choice 4: Sovereign Laurel & Biometric Roundel",
                tamilTitle = "அரசு வெற்றி வாகை & பாதரேகை முத்திரை",
                badgeLabel = "LAUREL WREATH & SOVEREIGN CREST",
                description = "Deep emerald laurel wreath ring encircling a golden infant silhouette, biometric plantar ridges, and bilingual state roundel inscription.",
                drawableRes = R.drawable.img_safestart_logo
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, GovDeepGreen),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = GovDeepGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "OFFICIAL EMBLEM SELECTION",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GovDarkNavy,
                                letterSpacing = 0.4.sp
                            )
                            Text(
                                text = "Tamil Nadu Government Newborn Protection Reference",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Spotlight Preview of Active Selection
                val currentSelectedOption = options.find { it.id == selectedChoice } ?: options.first()
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, GovSaffronGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = GovDeepGreen,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE PREVIEW • OPTION $selectedChoice",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Tap image to enlarge 🔍",
                                fontSize = 8.5.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Large Spotlight Image
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = currentSelectedOption.drawableRes),
                            contentDescription = currentSelectedOption.title,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, GovSaffronGold, CircleShape)
                                .clickable { zoomedImageRes = currentSelectedOption.drawableRes },
                            contentScale = ContentScale.Crop
                        )

                        Text(
                            text = currentSelectedOption.title,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GovDarkNavy,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = currentSelectedOption.tamilTitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GovDeepGreen,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Text(
                    text = "Tap any picture below to preview and choose the active emblem for institutional dossiers, child health records, and QR custody tokens:",
                    fontSize = 10.sp,
                    color = Color(0xFF475569),
                    lineHeight = 14.sp
                )

                // Render Option Cards
                options.forEach { option ->
                    val isSelected = selectedChoice == option.id
                    Surface(
                        color = if (isSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) GovDeepGreen else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedChoice = option.id }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Clickable Thumbnail for zoom
                            Box(
                                contentAlignment = Alignment.BottomEnd,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, if (isSelected) GovSaffronGold else Color(0xFFCBD5E1), CircleShape)
                                    .clickable { zoomedImageRes = option.drawableRes }
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = option.drawableRes),
                                    contentDescription = option.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(GovDarkNavy.copy(alpha = 0.8f), CircleShape)
                                        .border(0.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Zoom",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "OPTION ${option.id}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) GovDeepGreen else GovDarkNavy
                                    )
                                    if (isSelected) {
                                        Surface(
                                            color = GovDeepGreen,
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(
                                                text = "SELECTED",
                                                color = Color.White,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = option.title.substringAfter(": "),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GovDarkNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = option.tamilTitle,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GovDeepGreen
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = option.description,
                                    fontSize = 8.5.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 12.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Confirmation Button
                Button(
                    onClick = { onChoiceConfirmed(selectedChoice) },
                    colors = ButtonDefaults.buttonColors(containerColor = GovDeepGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONFIRM OPTION $selectedChoice AS ACTIVE EMBLEM",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Data model for Official Tamil Nadu Baby Protection Logo options.
 */
private data class LogoOptionItem(
    val id: Int,
    val title: String,
    val tamilTitle: String,
    val badgeLabel: String,
    val description: String,
    val drawableRes: Int
)

/**
 * Dialog enabling configuration and live testing of the Resend API transactional email & OTP gateway.
 */
@Composable
fun ResendApiKeyDialog(
    onDismiss: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf<String>(ResendEmailService.apiKey) }
    var testEmailInput by remember { mutableStateOf("vimal.uv1991@gmail.com") }
    var isSending by remember { mutableStateOf(false) }
    var sendStatusMessage by remember { mutableStateOf<String?>(null) }
    var sendStatusSuccess by remember { mutableStateOf<Boolean?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, GovDarkNavy),
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = GovDeepGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "RESEND EMAIL & OTP GATEWAY",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GovDarkNavy
                            )
                            Text(
                                text = "Statutory Transactional Dispatch (api.resend.com)",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Key Status indicator
                val isConfigured = ResendEmailService.isConfigured()
                Surface(
                    color = if (isConfigured) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (isConfigured) Color(0xFFA7F3D0) else Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isConfigured) GovDeepGreen else Color(0xFFD97706),
                            modifier = Modifier.size(15.dp)
                        )
                        val activeKey = ResendEmailService.apiKey
                        val activeKeyPreview = if (activeKey.length >= 10) "${activeKey.take(6)}...${activeKey.takeLast(4)}" else activeKey
                        Text(
                            text = if (isConfigured) {
                                "Active Resend Key Configured: $activeKeyPreview"
                            } else {
                                "No Resend key found in BuildConfig. Enter below to enable live email delivery."
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isConfigured) GovDeepGreen else Color(0xFF92400E)
                        )
                    }
                }

                // API Key Input
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Resend API Key (re_...)", fontSize = 10.5.sp) },
                    placeholder = { Text("re_123456789...", fontSize = 10.5.sp) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth()
                )

                // Save Key button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            ResendEmailService.setApiKey(apiKeyInput.trim())
                            sendStatusMessage = "Resend API Key saved for active session."
                            sendStatusSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GovDarkNavy),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Real Test Dispatch
                Text(
                    text = "TEST REAL EMAIL & OTP DISPATCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovDarkNavy
                )

                OutlinedTextField(
                    value = testEmailInput,
                    onValueChange = { testEmailInput = it },
                    label = { Text("Recipient Email Address", fontSize = 10.5.sp) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        isSending = true
                        sendStatusMessage = null
                        sendStatusSuccess = null
                        coroutineScope.launch {
                            val otp = (100000..999999).random().toString()
                            val result = ResendEmailService.sendOtpEmail(
                                toEmail = testEmailInput.trim(),
                                otp = otp,
                                officerName = "Dr. Officer"
                            )
                            isSending = false
                            if (result.isSuccess) {
                                val txId = result.getOrNull()
                                sendStatusSuccess = true
                                sendStatusMessage = "✓ Real OTP Email ($otp) dispatched successfully!\nResend Message ID: $txId\nCheck inbox at ${testEmailInput.trim()}."
                            } else {
                                sendStatusSuccess = false
                                sendStatusMessage = "✕ Resend Dispatch Notice: ${result.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    enabled = !isSending && testEmailInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GovDeepGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatching via Resend API...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Real 6-Digit OTP Email", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Status banner if sent
                sendStatusMessage?.let { msg ->
                    Surface(
                        color = if (sendStatusSuccess == true) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (sendStatusSuccess == true) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (sendStatusSuccess == true) GovDeepGreen else Color(0xFFDC2626),
                            modifier = Modifier.padding(8.dp),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
