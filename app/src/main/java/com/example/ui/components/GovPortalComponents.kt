package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppWing
import com.example.data.SafeStartAssets
import com.example.data.UserRole
import com.example.ui.theme.*

/**
 * 1. Top bar: thin black strip, saffron/gold text, left = official portal name + tagline,
 * right = helpline/status info in white/green text.
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
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: official portal name + tagline in saffron/gold
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🇮🇳",
                    fontSize = 11.sp
                )
                Text(
                    text = "GOVT. OF TAMIL NADU",
                    color = GovSaffronGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = "•",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
                Text(
                    text = "NATIONAL DIGITAL HEALTH MISSION",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                )
            }

            // Right: helpline / status info in white/green text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = "Helpline",
                        tint = GovSaffronGold,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "Helpline: 104",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "|",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Text(
                        text = "Operational",
                        color = Color(0xFF4ADE80),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 2. Header: solid green background (#0E7C4A deep green), white govt emblem/logo in a white circle
 * on the left, bold white title (bilingual — English + regional language subtitle),
 * thin gold/yellow bottom border accent.
 */
@Composable
fun GovHeader(
    currentWing: AppWing,
    currentRole: UserRole,
    onWingSelected: (AppWing) -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: White Govt Emblem in a White Circle + Bilingual Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, GovSaffronGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = SafeStartAssets.EMBLEM_URL,
                            contentDescription = "Government Emblem of Tamil Nadu",
                            modifier = Modifier.size(38.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column {
                        Text(
                            text = "SAFE START",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "காவல் துவக்கம் • NEWBORN IDENTITY PORTAL",
                            color = GovGoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tamper-Evident Institutional Registry & Statutory Custody",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Role Switcher Button
                Surface(
                    color = GovDarkNavy.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GovSaffronGold),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (currentRole == UserRole.HOSPITAL_REGISTRAR) {
                                Icons.Default.LocalHospital
                            } else {
                                Icons.Default.VerifiedUser
                            },
                            contentDescription = "Role icon",
                            tint = GovSaffronGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Column {
                            Text(
                                text = if (currentRole == UserRole.HOSPITAL_REGISTRAR) "HOSPITAL REGISTRAR" else "STATE COUNCIL",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                            Text(
                                text = "TAP TO SWITCH ROLE",
                                color = GovSaffronGold,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
                                AppWing.AUTH -> Icons.Default.Lock
                                AppWing.HOSPITAL_REGISTRY -> Icons.Default.LocalHospital
                                AppWing.COUNCIL_OVERSIGHT -> Icons.Default.Gavel
                                AppWing.DISPUTE_VERIFICATION -> Icons.Default.Fingerprint
                                AppWing.AUDIT_LEDGER -> Icons.Default.ReceiptLong
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
    readOnly: Boolean = false
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
 * 8. Secondary/quick-select cards: two-column grid of light bordered cards
 * with bold colored title + gray ID/subtext below, hover/click highlight.
 */
@Composable
fun GovQuickSelectCard(
    title: String,
    subtext: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
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
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) GovDeepGreen else GovDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
 * 9. Section cards (multi-field forms): white rounded cards with light gray border
 * and subtle shadow, bold uppercase numbered section titles ("1. FIELD GROUP NAME"),
 * a colored status pill on the right ("✕ Not Captured" in red pill / "✓ Verified" in green pill).
 */
@Composable
fun GovSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    isVerified: Boolean = false,
    statusTextOverride: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
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
                    text = title.uppercase(),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GovDarkNavy,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )

                // Colored status pill on the right
                val statusText = statusTextOverride ?: if (isVerified) "✓ Verified" else "✕ Not Captured"
                Surface(
                    color = if (isVerified) GovVerifiedGreenBg else GovAlertRedBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isVerified) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
                ) {
                    Text(
                        text = statusText,
                        color = if (isVerified) GovVerifiedGreen else GovAlertRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Divider(
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
    title: String,
    isCaptured: Boolean,
    isScanning: Boolean,
    onCaptureClick: () -> Unit,
    onUploadClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    capturedImageUri: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GovVerifiedGreenBg)
                            .border(1.5.dp, GovDeepGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Captured",
                            tint = GovDeepGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "BIOMETRIC FOOTPRINT RECORDED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GovDeepGreen,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "SHA-256 Hash Generated & Ready for Custody",
                        fontSize = 9.5.sp,
                        color = Color(0xFF047857)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Place baby's foot on sensor or click Capture",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Supports USB optical scanner, flatbed, or certified file",
                        fontSize = 9.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Three small action buttons underneath: green "Capture", dark navy "Upload", gray "Clear"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Green "Capture" button
            Button(
                onClick = onCaptureClick,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovDeepGreen),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Capture", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Dark Navy "Upload" button
            Button(
                onClick = onUploadClick,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovDarkNavy),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Gray "Clear" button
            OutlinedButton(
                onClick = onClearClick,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, GovGrayBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.weight(0.9f).height(36.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
fun Modifier.drawWithDashedBorder(color: Color, strokeWidth: Float, cornerRadius: Float): Modifier =
    this.drawWithCache {
        val stroke = Stroke(
            width = strokeWidth.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )
        onDrawWithContent {
            drawContent()
            drawRoundRect(
                color = color,
                style = stroke,
                cornerRadius = CornerRadius(cornerRadius.dp.toPx(), cornerRadius.dp.toPx())
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
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = message,
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
    recipient: String,
    smsBody: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                        text = "GOVT SMS DISPATCHED: $recipient",
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
                    text = smsBody,
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
