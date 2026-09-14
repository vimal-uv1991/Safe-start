package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GovSaffronGold
import com.example.ui.theme.TnDeepTeal

data class FormFieldStatus(
    val id: String,
    val label: String,
    val isValid: Boolean,
    val isMandatory: Boolean = true
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GovValidationHealthBanner(
    fields: List<FormFieldStatus>,
    onAutofillValidSample: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = fields.size
    val validCount = fields.count { it.isValid }
    val progress = if (totalCount > 0) validCount.toFloat() / totalCount else 0f
    val isAllValid = validCount == totalCount

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "validation_progress")
    val bannerBg by animateColorAsState(
        targetValue = if (isAllValid) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
        label = "banner_bg"
    )
    val bannerBorder by animateColorAsState(
        targetValue = if (isAllValid) Color(0xFF10B981) else Color(0xFFCBD5E1),
        label = "banner_border"
    )

    Surface(
        color = bannerBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, bannerBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("gov_validation_health_banner")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isAllValid) Icons.Default.CheckCircle else Icons.Default.Gavel,
                        contentDescription = null,
                        tint = if (isAllValid) Color(0xFF059669) else Color(0xFF1E293B),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "TN STATUTORY REGEX VALIDATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAllValid) Color(0xFF065F46) else Color(0xFF0F172A)
                        )
                        Text(
                            text = if (isAllValid)
                                "All statutory Tamil Nadu government ID formats verified ✓"
                            else
                                "Real-time verification: $validCount of $totalCount government formats compliant",
                            fontSize = 10.5.sp,
                            color = if (isAllValid) Color(0xFF047857) else Color(0xFF475569)
                        )
                    }
                }

                // Sample Autofill Button
                OutlinedButton(
                    onClick = onAutofillValidSample,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, TnDeepTeal),
                    modifier = Modifier.testTag("btn_autofill_valid_tn_sample")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = TnDeepTeal,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Fill Valid TN IDs",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TnDeepTeal
                    )
                }
            }

            // Real-Time Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = if (isAllValid) Color(0xFF059669) else Color(0xFF2563EB),
                    trackColor = Color(0xFFE2E8F0),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% Statutory Compliance",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllValid) Color(0xFF047857) else Color(0xFF64748B)
                    )
                    Text(
                        text = if (isAllValid) "Ready for Registration" else "${totalCount - validCount} field(s) pending",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isAllValid) Color(0xFF047857) else Color(0xFFDC2626)
                    )
                }
            }

            // Real-time Chip Badges for each ID format
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                fields.forEach { field ->
                    Surface(
                        color = if (field.isValid) Color(0xFFD1FAE5) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(
                            1.dp,
                            if (field.isValid) Color(0xFF10B981) else Color(0xFFCBD5E1)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = if (field.isValid) "✓" else "•",
                                color = if (field.isValid) Color(0xFF059669) else Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = field.label,
                                fontSize = 10.sp,
                                color = if (field.isValid) Color(0xFF065F46) else Color(0xFF334155),
                                fontWeight = if (field.isValid) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
