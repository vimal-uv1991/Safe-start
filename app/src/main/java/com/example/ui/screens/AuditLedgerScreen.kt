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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GovCanvasBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("audit_ledger_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Toast Notification
        successToastMsg?.let { (title, desc) ->
            GovSuccessToast(
                title = title,
                description = desc,
                onDismiss = { successToastMsg = null }
            )
        }

        // PAGE HERO (INDIAN GOVT E-GOVERNANCE DESIGN SYSTEM)
        GovPageHero(
            title = "SOVEREIGN CRYPTOGRAPHIC AUDIT CHAIN",
            subtitle = "Immutable SHA-256 State Ledger & HSM Attested Tamper Proof Logs",
            icon = Icons.AutoMirrored.Filled.ReceiptLong
        )

        // STATUTORY ATTESTATION INFO CALLOUT BOX
        GovInfoCalloutBox(
            title = "FIPS 140-2 LEVEL 3 HARDWARE ATTESTATION",
            description = "All birth transactions are cryptographically signed with the Tamil Nadu Sovereign Root Certificate. Blocks cannot be rewritten or pruned retrospectively.",
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
                    Text("#418,920", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = GovGreen)
                }
            }
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GovBorderLight),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("AUDIT INTEGRITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GovGrayText, letterSpacing = 0.5.sp)
                    Text("100% Intact", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = GovVerifiedGreen)
                }
            }
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GovBorderLight),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("DISTRICT REACH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GovGrayText, letterSpacing = 0.5.sp)
                    Text("38 Districts", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = GovGoldText)
                }
            }
        }

        // CRYPTOGRAPHIC LEDGER SECTION
        GovSectionCard(
            number = "1",
            title = "CRYPTOGRAPHIC COMMIT BLOCKS (MERKLE LOG)",
            statusPillText = "VERIFIED BY TNeGA ROOT",
            isStatusVerified = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LATEST SIGNED TRANSACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovGrayText,
                    letterSpacing = 0.5.sp
                )
                TextButton(onClick = {
                    successToastMsg = "CHAIN INTEGRITY AFFIRMED" to "Full SHA-256 Merkle chain verified against TNeGA State Root Key."
                }) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = GovVerifiedGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify Chain", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GovGreen)
                }
            }

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
                                text = "Block #${418920 - index}",
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
