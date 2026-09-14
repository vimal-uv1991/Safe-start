package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ValidationResult
import com.example.ui.theme.TnDeepTeal
import com.example.ui.theme.TnPrimary

/**
 * Material 3 Outlined Text Field with real-time statutory regex validation feedback.
 * Features instant status icons, color borders, detailed error/success explanatory text,
 * and 1-tap statutory preset autofill chips.
 */
@Composable
fun ValidatedGovTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    validation: ValidationResult,
    modifier: Modifier = Modifier,
    testTag: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    sampleValue: String? = null,
    sampleLabel: String = "Test Valid Format",
    onUseSample: (() -> Unit)? = null
) {
    val isError = validation.errorMessage != null
    val isValid = validation.isValid
    val hasContent = value.isNotBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label)
                    if (isValid) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("✓", color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            placeholder = { Text(placeholder, color = Color(0xFF94A3B8)) },
            singleLine = singleLine,
            isError = isError,
            keyboardOptions = keyboardOptions,
            trailingIcon = {
                when {
                    isValid -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Format Valid",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    isError -> {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Format Invalid",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Format Info",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when {
                    isValid -> Color(0xFF059669)
                    isError -> Color(0xFFDC2626)
                    else -> TnPrimary
                },
                unfocusedBorderColor = when {
                    isValid -> Color(0xFF10B981)
                    isError -> Color(0xFFEF4444)
                    else -> Color(0xFFCBD5E1)
                },
                errorBorderColor = Color(0xFFDC2626),
                focusedLabelColor = when {
                    isValid -> Color(0xFF065F46)
                    isError -> Color(0xFF991B1B)
                    else -> TnPrimary
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )

        // Real-Time Feedback Display (Success, Error, or Statutory Helper)
        when {
            isError -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = validation.errorMessage ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFFB91C1C),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 14.sp
                    )
                }
            }
            isValid -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = validation.successMessage ?: "✓ Valid format verified",
                        fontSize = 11.sp,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }
            }
            validation.helperText != null -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = validation.helperText ?: "",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // Optional 1-Tap Sample Autofill Chip
        if (sampleValue != null && onUseSample != null) {
            AnimatedVisibility(
                visible = !isValid,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .clickable { onUseSample() }
                        .padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = TnDeepTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "$sampleLabel: ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = sampleValue,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TnDeepTeal
                        )
                    }
                }
            }
        }
    }
}
