package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DisputeVerificationScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val disputeCases by SafeStartRepository.disputeCases.collectAsState()
    var successToastMsg by remember { mutableStateOf<Pair<String, String>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GovCanvasBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dispute_verification_screen"),
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
            title = "STATE JUDICIAL DISPUTE TRIBUNAL",
            subtitle = "Biometric Parentage & Identity Collision Adjudication • TN Civil Records Act Sec. 14",
            icon = Icons.Default.Balance
        )

        // STATUTORY INFO CALLOUT BOX
        GovInfoCalloutBox(
            title = "INCONTROVERTIBLE FORENSIC ADMISSIBILITY",
            description = "High Court Circular No. 11/2024: Digital Plantar Ridge Minutiae and 3-Party Hospital Custody logs constitute conclusive forensic evidence under Indian Evidence Act.",
            isGreenVariant = true
        )

        // ACTIVE DOCKET SECTION
        GovSectionCard(
            number = "1",
            title = "ACTIVE ADJUDICATION DOCKET (CROSS-CHECK PETITIONS)",
            statusPillText = "${disputeCases.count { !it.isResolved }} HEARINGS ACTIVE",
            isStatusVerified = false
        ) {
            disputeCases.forEach { caseItem ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (caseItem.isResolved) GovVerifiedGreen else GovBorderLight),
                    modifier = Modifier.fillMaxWidth()
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
                            Text(
                                text = "Docket #${caseItem.caseId}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GovGreen
                            )
                            Surface(
                                color = if (caseItem.isResolved) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (caseItem.isResolved) "RESOLVED ✓" else "HEARING ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (caseItem.isResolved) Color(0xFF166534) else Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.5.dp, GovBorderLight, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = caseItem.childPhotoUrl,
                                    contentDescription = "Child Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "Child: ${caseItem.childName} (${caseItem.childAge})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GovNavyDark
                                )
                                Text(
                                    text = "Dispute: ${caseItem.grievanceCategory}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Facility: ${caseItem.reportingFacility}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Plantar Ridge Minutiae Correlation", fontSize = 10.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                                    Text("${caseItem.matchPercentage}% Incontrovertible Match", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = GovVerifiedGreen)
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GovVerifiedGreen)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GovPrimaryButton(
                                text = if (caseItem.isResolved) "Affirmed & Closed" else "Affirm Parentage",
                                icon = Icons.Default.Gavel,
                                enabled = !caseItem.isResolved,
                                onClick = {
                                    SafeStartRepository.resolveDispute(caseItem.caseId)
                                    successToastMsg = "TRIBUNAL ORDER SIGNED" to "Biological Parentage Affirmed for Docket #${caseItem.caseId}."
                                },
                                modifier = Modifier.weight(1.3f)
                            )

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Exporting forensic docket to High Court Registrar...", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF475569)),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = GovNavyDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Transmit File", color = GovNavyDark, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
