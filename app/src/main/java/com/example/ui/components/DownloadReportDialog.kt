package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.NewbornRecord
import com.example.ui.theme.GovSaffronGold
import com.example.ui.theme.TnDeepTeal
import com.example.ui.theme.TnPrimary
import com.example.util.ExportResult
import com.example.util.ReportExporter
import com.example.util.ReportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Administrative Record Report Export Dialog for downloading and sharing
 * statutory newborn records as PDF or CSV.
 */
@Composable
fun DownloadReportDialog(
    filteredRecords: List<NewbornRecord>,
    allRecords: List<NewbornRecord>,
    currentFilterName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedFormat by remember { mutableStateOf(ReportFormat.PDF) }
    var exportOnlyFiltered by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<ExportResult?>(null) }

    val activeRecords = if (exportOnlyFiltered) filteredRecords else allRecords
    val activeFilterTitle = if (exportOnlyFiltered) currentFilterName else "All Registry Records"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("dialog_download_report"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TnDeepTeal.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = TnDeepTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Download Registry Report",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Statutory newborn record-keeping export",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_report_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Scope Selector: Filtered vs All
                Text(
                    text = "1. SELECT RECORD SCOPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (exportOnlyFiltered) TnDeepTeal.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            1.2.dp,
                            if (exportOnlyFiltered) TnDeepTeal else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                exportOnlyFiltered = true
                                exportResult = null
                            }
                            .testTag("scope_filtered")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (exportOnlyFiltered) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (exportOnlyFiltered) TnDeepTeal else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "Current Filter",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (exportOnlyFiltered) TnDeepTeal else Color(0xFF334155)
                                )
                                Text(
                                    text = "$currentFilterName (${filteredRecords.size})",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!exportOnlyFiltered) TnDeepTeal.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            1.2.dp,
                            if (!exportOnlyFiltered) TnDeepTeal else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                exportOnlyFiltered = false
                                exportResult = null
                            }
                            .testTag("scope_all")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (!exportOnlyFiltered) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (!exportOnlyFiltered) TnDeepTeal else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "All Records",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!exportOnlyFiltered) TnDeepTeal else Color(0xFF334155)
                                )
                                Text(
                                    text = "Full Roster (${allRecords.size})",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // Format Selector: PDF vs CSV
                Text(
                    text = "2. SELECT EXPORT FORMAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    letterSpacing = 0.5.sp
                )

                // Option 1: PDF
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedFormat == ReportFormat.PDF) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                    border = BorderStroke(
                        1.5.dp,
                        if (selectedFormat == ReportFormat.PDF) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedFormat = ReportFormat.PDF
                            exportResult = null
                        }
                        .testTag("format_pdf_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Official PDF Document",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFF59E0B))
                                ) {
                                    Text(
                                        text = "STATUTORY SEAL",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Formal Tamil Nadu Health Dept letterhead, summary metrics, tabular register & SHA-256 integrity seal for council audit.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 14.sp
                            )
                        }

                        if (selectedFormat == ReportFormat.PDF) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Option 2: CSV
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedFormat == ReportFormat.CSV) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                    border = BorderStroke(
                        1.5.dp,
                        if (selectedFormat == ReportFormat.CSV) Color(0xFF10B981) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedFormat = ReportFormat.CSV
                            exportResult = null
                        }
                        .testTag("format_csv_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "CSV Spreadsheet (Excel / HMIS)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Surface(
                                    color = Color(0xFFD1FAE5),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFF059669))
                                ) {
                                    Text(
                                        text = "DATA TABLE",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Clean RFC-4180 comma-separated values compatible with Excel, Google Sheets, and Tamil Nadu State Health database import.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 14.sp
                            )
                        }

                        if (selectedFormat == ReportFormat.CSV) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Output Result Card (If generated)
                AnimatedVisibility(visible = exportResult != null) {
                    exportResult?.let { result ->
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth().testTag("card_export_result")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Report Generated Successfully",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    Text(
                                        text = result.fileSizeFormatted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF475569)
                                    )
                                }

                                Text(
                                    text = result.fileName,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF334155)
                                )

                                Text(
                                    text = "${result.recordCount} newborn records and verification statuses exported.",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )

                                HorizontalDivider(color = Color(0xFFCBD5E1))

                                // Quick Actions: Share, Save to Downloads, Open
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            ReportExporter.shareReport(context, result)
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp).testTag("btn_share_report"),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TnPrimary),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            ReportExporter.saveToDownloads(context, result)
                                        },
                                        modifier = Modifier.weight(1.1f).height(40.dp).testTag("btn_save_to_downloads"),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Downloads", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            ReportExporter.openReport(context, result)
                                        },
                                        modifier = Modifier.weight(0.9f).height(40.dp).testTag("btn_open_report"),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Primary Generate Button
                Button(
                    onClick = {
                        isGenerating = true
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                when (selectedFormat) {
                                    ReportFormat.PDF -> ReportExporter.exportToPdf(
                                        context = context,
                                        records = activeRecords,
                                        filterTitle = activeFilterTitle
                                    )
                                    ReportFormat.CSV -> ReportExporter.exportToCsv(
                                        context = context,
                                        records = activeRecords,
                                        filterTitle = activeFilterTitle
                                    )
                                }
                            }
                            isGenerating = false
                            exportResult = result
                            Toast.makeText(
                                context,
                                "✅ ${result.format.displayName} generated (${result.recordCount} records)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirm_download_report"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFormat == ReportFormat.PDF) TnPrimary else Color(0xFF059669)
                    ),
                    enabled = !isGenerating && activeRecords.isNotEmpty()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compiling ${selectedFormat.displayName}...")
                    } else {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download Report (${activeRecords.size} Records • ${selectedFormat.extension.uppercase()})",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Security Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Encrypted in app custody • Authorized registrar access only",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
