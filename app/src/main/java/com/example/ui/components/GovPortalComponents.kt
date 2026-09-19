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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
 * Mandatory Academic Prototype Disclaimers for SafeStart.
 */
@Composable
fun AcademicPrototypeNotice(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFFEF3C7),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ACADEMIC PROTOTYPE — NOT AN OFFICIAL GOVERNMENT APPLICATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF92400E),
                letterSpacing = 0.4.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 1. Top bar: thin black strip, saffron/gold text, left = official portal name + tagline,
 * right = helpline/status info in white/green text.
 * Responsive layout ensures no text is hidden on 360dp phone screens.
 */
@Composable
fun GovTopBar(
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeDrawingTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val displayCutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val topInset = maxOf(statusBarTop, safeDrawingTop, displayCutoutTop, 44.dp)

    Surface(
        color = GovTopBarBlack,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Downward offset guaranteeing full clearance below device status bar icons (clock, battery, wifi, camera cutouts)
            Spacer(modifier = Modifier.height(topInset))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "• NDHM",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    var showResendDialog by remember { mutableStateOf(false) }

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

        // Academic Prototype Disclaimer Banner
        AcademicPrototypeNotice()

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
                // Left: Bilingual Title & Authority
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "SAFE START",
                            color = Color.White,
                            fontSize = 17.sp,
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
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "காவல் துவக்கம் • NEWBORN IDENTITY REGISTRY",
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

            // Institutional Communication Gateway Chip (Secure & Encrypted)
            Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { showResendDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = GovGoldAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Encrypted Institutional Gateway • Verified Email & OTP Dispatch",
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
 * Dialog enabling configuration and live testing of the Resend API transactional email & OTP gateway.
 */
@Composable
fun ResendApiKeyDialog(
    onDismiss: () -> Unit
) {
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
                                text = "OFFICIAL EMAIL & OTP DISPATCH",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GovDarkNavy
                            )
                            Text(
                                text = "Statutory Transactional Delivery Gateway",
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

                // Security status badge (Key is hidden from everyone and securely protected server-side)
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = GovDeepGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Institutional Gateway Active • Credentials Protected",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GovDeepGreen
                            )
                            Text(
                                text = "All API keys and credentials are permanently hidden and secured server-side. Transactional OTP & notification dispatch is operational.",
                                fontSize = 9.sp,
                                color = Color(0xFF065F46),
                                lineHeight = 13.sp
                            )
                        }
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
