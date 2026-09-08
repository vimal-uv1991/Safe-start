package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.AppWing
import com.example.data.UserRole
import com.example.ui.components.PortalHeader
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SafeStartApp()
            }
        }
    }
}

@Composable
fun SafeStartApp() {
    var currentWing by remember { mutableStateOf(AppWing.AUTH) }
    var currentRole by remember { mutableStateOf(UserRole.HOSPITAL_REGISTRAR) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("safe_start_scaffold"),
        topBar = {
            PortalHeader(
                currentWing = currentWing,
                currentRole = currentRole,
                onWingSelected = { wing -> currentWing = wing },
                onRoleSelected = { role ->
                    currentRole = role
                    // When role changes, if on auth or if switching context, auto-route to corresponding wing
                    if (currentWing != AppWing.AUTH) {
                        currentWing = if (role == UserRole.HOSPITAL_REGISTRAR) {
                            AppWing.HOSPITAL_REGISTRY
                        } else {
                            AppWing.COUNCIL_OVERSIGHT
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentWing) {
                AppWing.AUTH -> {
                    AuthScreen(
                        currentRole = currentRole,
                        onRoleSelected = { role -> currentRole = role },
                        onLoginSuccess = { role ->
                            currentRole = role
                            currentWing = if (role == UserRole.HOSPITAL_REGISTRAR) {
                                AppWing.HOSPITAL_REGISTRY
                            } else {
                                AppWing.COUNCIL_OVERSIGHT
                            }
                        }
                    )
                }

                AppWing.HOSPITAL_REGISTRY -> {
                    HospitalRegistryScreen(
                        onNavigateToCouncil = {
                            currentRole = UserRole.MEDICAL_COUNCIL
                            currentWing = AppWing.COUNCIL_OVERSIGHT
                        }
                    )
                }

                AppWing.COUNCIL_OVERSIGHT -> {
                    CouncilOversightScreen()
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
