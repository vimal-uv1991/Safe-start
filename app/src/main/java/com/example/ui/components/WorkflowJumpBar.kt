package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppWing
import com.example.ui.theme.GovGoldAccent
import com.example.ui.theme.GovSaffronGold
import com.example.ui.theme.TnDeepTeal
import com.example.ui.theme.TnPrimary

/**
 * Monster-Level Interactive Workflow Navigation HUD.
 * Renders an animated breadcrumb and quick-jump bar showing current workflow position,
 * rapid tactile "Jump Back", and "Next Interface Page" buttons.
 */
@Composable
fun WorkflowJumpBar(
    currentWing: AppWing,
    canGoBack: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToWing: (AppWing) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // Pulsing energy laser for the active workflow connection
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val workflowSteps = listOf(
        WorkflowStep(AppWing.AUTH, "Login", Icons.Default.Lock, "0"),
        WorkflowStep(AppWing.DASHBOARD, "Dashboard", Icons.Default.Dashboard, "1"),
        WorkflowStep(AppWing.HOSPITAL_REGISTRY, "Registration", Icons.Default.LocalHospital, "2"),
        WorkflowStep(AppWing.COUNCIL_OVERSIGHT, "Council", Icons.Default.Gavel, "3"),
        WorkflowStep(AppWing.AUDIT_LEDGER, "Ledger", Icons.AutoMirrored.Filled.ReceiptLong, "4"),
        WorkflowStep(AppWing.DISPUTE_VERIFICATION, "Tribunal", Icons.Default.Shield, "5")
    )

    val currentIndex = workflowSteps.indexOfFirst { it.wing == currentWing }

    val nextWing = when (currentWing) {
        AppWing.AUTH -> AppWing.DASHBOARD
        AppWing.DASHBOARD -> AppWing.HOSPITAL_REGISTRY
        AppWing.HOSPITAL_REGISTRY -> AppWing.COUNCIL_OVERSIGHT
        AppWing.COUNCIL_OVERSIGHT -> AppWing.AUDIT_LEDGER
        AppWing.AUDIT_LEDGER -> AppWing.DISPUTE_VERIFICATION
        AppWing.DISPUTE_VERIFICATION -> AppWing.DASHBOARD
    }

    val nextWingLabel = when (nextWing) {
        AppWing.DASHBOARD -> "Dashboard"
        AppWing.HOSPITAL_REGISTRY -> "Registration"
        AppWing.COUNCIL_OVERSIGHT -> "Council Audit"
        AppWing.AUDIT_LEDGER -> "SHA Ledger"
        AppWing.DISPUTE_VERIFICATION -> "Tribunal"
        AppWing.AUTH -> "Login"
    }

    Surface(
        color = Color(0xFF0A192F),
        border = BorderStroke(1.dp, Color(0xFF1E3A8A)),
        shape = RoundedCornerShape(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("workflow_jump_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Laser energy line along top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                GovSaffronGold.copy(alpha = pulseAlpha),
                                Color(0xFF10B981).copy(alpha = pulseAlpha),
                                Color(0xFF38BDF8).copy(alpha = pulseAlpha),
                                GovSaffronGold.copy(alpha = pulseAlpha)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button (If not at root or can go back)
                if (canGoBack || currentWing != AppWing.DASHBOARD) {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateBack()
                            }
                            .testTag("btn_jump_back")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Jump Back",
                                tint = GovSaffronGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "BACK",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = "LIVE CUSTODY",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Center: Scrollable interactive step pills with jump capabilities
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    workflowSteps.forEachIndexed { index, step ->
                        val isCurrent = step.wing == currentWing
                        val isPassed = currentIndex != -1 && index < currentIndex

                        Surface(
                            color = when {
                                isCurrent -> TnPrimary.copy(alpha = 0.85f)
                                isPassed -> Color(0xFF065F46)
                                else -> Color(0xFF1E293B)
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isCurrent -> GovSaffronGold
                                    isPassed -> Color(0xFF10B981)
                                    else -> Color(0xFF334155)
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    if (!isCurrent) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onNavigateToWing(step.wing)
                                    }
                                }
                                .testTag("step_${step.wing.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    step.icon,
                                    contentDescription = null,
                                    tint = if (isCurrent) GovSaffronGold else if (isPassed) Color(0xFF34D399) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = step.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) Color.White else if (isPassed) Color(0xFFE2E8F0) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // Next Page Jump Button
                Surface(
                    color = TnDeepTeal,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GovGoldAccent),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToWing(nextWing)
                        }
                        .testTag("btn_jump_next")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = nextWingLabel.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Jump Next",
                            tint = GovSaffronGold,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class WorkflowStep(
    val wing: AppWing,
    val label: String,
    val icon: ImageVector,
    val stepNumber: String
)
