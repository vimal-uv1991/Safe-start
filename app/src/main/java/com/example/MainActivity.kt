package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AppWing
import com.example.data.UserRole
import com.example.ui.components.PortalHeader
import com.example.ui.components.WorkflowJumpBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            MyApplicationTheme {
                SafeStartApp()
            }
        }
    }
}

@Composable
fun SafeStartApp() {
    var currentWing by remember { mutableStateOf(AppWing.DASHBOARD) }
    var currentRole by remember { mutableStateOf(UserRole.HOSPITAL_REGISTRAR) }
    val haptic = LocalHapticFeedback.current

    // Navigation History Stack for seamless back-navigation
    val navHistory = remember { mutableStateListOf(AppWing.DASHBOARD) }
    var isNavigatingBack by remember { mutableStateOf(false) }

    fun navigateToWing(targetWing: AppWing, isBack: Boolean = false) {
        if (targetWing == currentWing) return

        isNavigatingBack = isBack || targetWing.ordinal < currentWing.ordinal

        if (isBack) {
            // Popping back
            if (navHistory.size > 1) {
                navHistory.removeAt(navHistory.lastIndex)
            }
        } else {
            // Pushing forward
            navHistory.add(targetWing)
        }

        currentWing = targetWing
    }

    fun navigateBack() {
        if (navHistory.size > 1) {
            val previousWing = navHistory[navHistory.lastIndex - 1]
            navigateToWing(previousWing, isBack = true)
        } else if (currentWing != AppWing.DASHBOARD) {
            navigateToWing(AppWing.DASHBOARD, isBack = true)
        }
    }

    // System Back Gesture / Hardware Back Button Handler
    BackHandler(enabled = navHistory.size > 1 || currentWing != AppWing.DASHBOARD) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        navigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("safe_start_scaffold"),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                PortalHeader(
                    currentWing = currentWing,
                    currentRole = currentRole,
                    onWingSelected = { wing ->
                        navigateToWing(wing)
                    },
                    onRoleSelected = { role ->
                        currentRole = role
                        // When role changes, if on operational registry/council wing, auto-route
                        if (currentWing == AppWing.HOSPITAL_REGISTRY || currentWing == AppWing.COUNCIL_OVERSIGHT) {
                            val target = if (role == UserRole.HOSPITAL_REGISTRAR) {
                                AppWing.HOSPITAL_REGISTRY
                            } else {
                                AppWing.COUNCIL_OVERSIGHT
                            }
                            navigateToWing(target)
                        }
                    }
                )

                // High-Security Workflow Jump & Progress HUD
                WorkflowJumpBar(
                    currentWing = currentWing,
                    canGoBack = navHistory.size > 1 || currentWing != AppWing.DASHBOARD,
                    onNavigateBack = {
                        navigateBack()
                    },
                    onNavigateToWing = { wing ->
                        navigateToWing(wing)
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Material 3 AnimatedContent Transition Spec
            AnimatedContent(
                targetState = currentWing,
                transitionSpec = {
                    val isAuthTransition = initialState == AppWing.AUTH || targetState == AppWing.AUTH

                    if (isAuthTransition) {
                        // =========================================================================
                        // MATERIAL 3 FADE-THROUGH TRANSITION
                        // Standard M3 pattern for top-level mode changes & authentication workflows:
                        // Outgoing screen fades out and contracts slightly (0.94x);
                        // Incoming screen fades in after slight delay and expands to 1.0x.
                        // =========================================================================
                        (fadeIn(
                            animationSpec = tween(
                                durationMillis = 240,
                                delayMillis = 80,
                                easing = LinearOutSlowInEasing
                            )
                        ) + scaleIn(
                            initialScale = 0.94f,
                            animationSpec = tween(
                                durationMillis = 240,
                                delayMillis = 80,
                                easing = LinearOutSlowInEasing
                            )
                        )).togetherWith(
                            fadeOut(
                                animationSpec = tween(
                                    durationMillis = 150,
                                    easing = FastOutLinearInEasing
                                )
                            ) + scaleOut(
                                targetScale = 0.94f,
                                animationSpec = tween(
                                    durationMillis = 150,
                                    easing = FastOutLinearInEasing
                                )
                            )
                        )
                    } else if (isNavigatingBack || targetState.ordinal < initialState.ordinal) {
                        // =========================================================================
                        // MATERIAL 3 SHARED AXIS (HORIZONTAL) - BACKWARD / RETURN TRANSITION
                        // e.g., Registration -> Dashboard, or Hardware Back button
                        // =========================================================================
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { width -> -(width * 0.35f).toInt() } +
                                scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = spring(
                                        dampingRatio = 0.82f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) +
                                fadeIn(animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing))
                        ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = 0.82f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { width -> width } +
                                    scaleOut(
                                        targetScale = 0.95f,
                                        animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                                    ) +
                                    fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing))
                        )
                    } else {
                        // =========================================================================
                        // MATERIAL 3 SHARED AXIS (HORIZONTAL) - FORWARD TRANSITION
                        // e.g., Dashboard -> Registration, or stepping forward in workflow
                        // =========================================================================
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) { width -> width } +
                                scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = spring(
                                        dampingRatio = 0.82f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) +
                                fadeIn(animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing))
                        ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = 0.82f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { width -> -(width * 0.35f).toInt() } +
                                    scaleOut(
                                        targetScale = 0.95f,
                                        animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                                    ) +
                                    fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing))
                        )
                    }
                },
                label = "m3_workflow_transition"
            ) { targetWing ->
                when (targetWing) {
                    AppWing.DASHBOARD -> {
                        SummaryDashboardScreen(
                            onNavigateToRegistry = { navigateToWing(AppWing.HOSPITAL_REGISTRY) },
                            onNavigateToCouncil = { navigateToWing(AppWing.COUNCIL_OVERSIGHT) },
                            onNavigateToLedger = { navigateToWing(AppWing.AUDIT_LEDGER) },
                            onNavigateToAuth = { navigateToWing(AppWing.AUTH) }
                        )
                    }

                    AppWing.AUTH -> {
                        AuthScreen(
                            currentRole = currentRole,
                            onRoleSelected = { role -> currentRole = role },
                            onLoginSuccess = { role ->
                                currentRole = role
                                val target = if (role == UserRole.HOSPITAL_REGISTRAR) {
                                    AppWing.HOSPITAL_REGISTRY
                                } else {
                                    AppWing.COUNCIL_OVERSIGHT
                                }
                                navigateToWing(target)
                            },
                            onNavigateToDashboard = {
                                navigateToWing(AppWing.DASHBOARD)
                            },
                            onNavigateToRegistry = {
                                navigateToWing(AppWing.HOSPITAL_REGISTRY)
                            }
                        )
                    }

                    AppWing.HOSPITAL_REGISTRY -> {
                        HospitalRegistryScreen(
                            onNavigateToCouncil = {
                                currentRole = UserRole.MEDICAL_COUNCIL
                                navigateToWing(AppWing.COUNCIL_OVERSIGHT)
                            },
                            onNavigateToDashboard = {
                                navigateToWing(AppWing.DASHBOARD, isBack = true)
                            },
                            onNavigateToAuth = {
                                navigateToWing(AppWing.AUTH)
                            }
                        )
                    }

                    AppWing.COUNCIL_OVERSIGHT -> {
                        CouncilOversightScreen(
                            onNavigateToDashboard = {
                                navigateToWing(AppWing.DASHBOARD, isBack = true)
                            }
                        )
                    }

                    AppWing.DISPUTE_VERIFICATION -> {
                        DisputeVerificationScreen()
                    }

                    AppWing.AUDIT_LEDGER -> {
                        AuditLedgerScreen()
                    }
                }
            }
        }
    }
}
