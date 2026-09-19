package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.AuditLedgerEngine
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AuditLedgerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by SafeStartRepository.records.collectAsState()
    var successToastMsg by remember { mutableStateOf<Pair<String, String>?>(null) }
    var auditEntries by remember { mutableStateOf<List<AuditLedgerEngine.AuditEntry>>(emptyList()) }
    var chainVerification by remember { mutableStateOf<AuditLedgerEngine.ChainVerificationResult?>(null) }

    // Fetch real audit entries on load
    LaunchedEffect(Unit) {
        val entries = SafeStartRepository.getAuditHistory()
        auditEntries = entries
        chainVerification = SafeStartRepository.verifyAuditChain()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GovCanvasBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("audit_ledger_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MANDATORY ACADEMIC PROTOTYPE NOTICE
        AcademicPrototypeNotice()

        // Active Toast Notification
        successToastMsg?.let { (title, desc) ->
            GovSuccessToast(
                title = title,
                description = desc,
                onDismiss = { successToastMsg = null }
            )
        }

        // PAGE HERO
        GovPageHero(
            title = "TAMPER-EVIDENT CRYPTOGRAPHIC AUDIT LOG",
            subtitle = "Append-Only SHA-256 Chained Ledger • Forensic Verification Engine",
            icon = Icons.AutoMirrored.Filled.ReceiptLong
        )

        // STATUTORY ATTESTATION INFO CALLOUT BOX
        GovInfoCalloutBox(
            title = "ACADEMIC PROTOTYPE • TAMPER-EVIDENT AUDIT TRAIL",
            description = "Every civil transaction is committed to an append-only SQLite log with SHA-256 hash chaining: H[n] = SHA-256(H[n-1] || EventPayload). Any retroactive modification breaks the chain and is flagged immediately.",
            isGreenVariant = true
        )

        // LEDGER BLOCK METRICS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GovBorderLight),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("BLOCK HEIGHT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GovGrayText, letterSpacing = 0.5.sp)
                    val count = if (auditEntries.isNotEmpty()) auditEntries.size else records.size
                    Text("#$count", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = GovGreen)
                }
            }
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GovBorderLight),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("CHAIN STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GovGrayText, letterSpacing = 0.5.sp)
                    val statusText = if (chainVerification?.isValid == false) "CORRUPTED" else "Intact"
                    val statusColor = if (chainVerification?.isValid == false) Color(0xFFDC2626) else GovVerifiedGreen
                    Text(statusText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = statusColor)
                }
            }
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GovBorderLight),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("HASH STANDARD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GovGrayText, letterSpacing = 0.5.sp)
                    Text("SHA-256", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = GovGoldText)
                }
            }
        }

        // CRYPTOGRAPHIC LEDGER SECTION
        GovSectionCard(
            number = "1",
            title = "CRYPTOGRAPHIC COMMIT BLOCKS (HASH-CHAINED LOG)",
            statusPillText = "SHA-256 CHAINED",
            isStatusVerified = chainVerification?.isValid ?: true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMMITTED TRANSACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovGrayText,
                    letterSpacing = 0.5.sp
                )
                TextButton(onClick = {
                    val result = SafeStartRepository.verifyAuditChain()
                    chainVerification = result
                    auditEntries = SafeStartRepository.getAuditHistory()
                    if (result.isValid) {
                        successToastMsg = "CHAIN INTEGRITY VERIFIED" to result.message
                    } else {
                        successToastMsg = "TAMPER DETECTED" to result.message
                    }
                }) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = GovVerifiedGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify Chain", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GovGreen)
                }
            }

            if (auditEntries.isNotEmpty()) {
                auditEntries.take(15).forEach { entry ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GovBorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Block #${entry.sequenceNumber} • ${entry.eventType}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GovGreen
                                )
                                Text(
                                    text = entry.timestampStr,
                                    fontSize = 10.sp,
                                    color = GovGrayText
                                )
                            }
                            Text(
                                text = "Actor: ${entry.actorId} (${entry.actorRole}) • Record: ${entry.affectedRecordId ?: "N/A"}",
                                fontSize = 11.sp,
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Details: ${entry.details}",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Text(
                                text = "Prev Hash: ${entry.previousHash.take(24)}...",
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "Curr Hash: ${entry.currentHash.take(24)}...",
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = GovNavyDark
                            )
                        }
                    }
                }
            } else {
                records.forEachIndexed { index, record ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GovBorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Token: ${record.token}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = GovGreen
                                )
                                Text(
                                    text = "Block #${100 - index}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GovGrayText
                                )
                            }
                            Text(
                                text = "SHA-256: ${record.biometricHash}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = GovNavyDark
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Node: ${record.hospitalName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "Committed: ${record.birthTimestamp}",
                                    fontSize = 10.sp,
                                    color = GovGrayText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
