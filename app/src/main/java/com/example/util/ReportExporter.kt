package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.NewbornRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportFormat(val extension: String, val mimeType: String, val displayName: String) {
    PDF("pdf", "application/pdf", "Official PDF Document"),
    CSV("csv", "text/csv", "CSV Spreadsheet (Excel / HMIS)")
}

data class ExportResult(
    val file: File,
    val uri: Uri,
    val format: ReportFormat,
    val recordCount: Int,
    val fileName: String,
    val fileSizeFormatted: String
)

object ReportExporter {

    private fun getReportsDir(context: Context): File {
        val dir = File(context.filesDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Exports the given newborn records into a clean, RFC 4180-compliant CSV file.
     */
    fun exportToCsv(context: Context, records: List<NewbornRecord>, filterTitle: String = "All"): ExportResult {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SafeStart_Newborn_Report_${timestamp}.csv"
        val file = File(getReportsDir(context), fileName)

        val stringBuilder = StringBuilder()

        // CSV Header with metadata comments
        stringBuilder.append("# GOVERNMENT OF TAMIL NADU - SAFE START NEWBORN IDENTITY CUSTODY REGISTER\n")
        stringBuilder.append("# Report Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        stringBuilder.append("# Scope: $filterTitle | Total Records: ${records.size}\n")
        stringBuilder.append("\n")

        // Column Titles
        val headers = listOf(
            "Token",
            "Child Gender",
            "Mother Name",
            "Father Name",
            "Attending Doctor",
            "Hospital Name",
            "District",
            "Hospital Location",
            "Date and Time of Birth",
            "Verification Status",
            "Medical Council Locked",
            "Parent Mobile",
            "Parent Email",
            "Biometric Hash (SHA-256)"
        )
        stringBuilder.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

        // Rows
        for (record in records) {
            val row = listOf(
                record.token,
                record.gender,
                record.motherName,
                record.fatherName,
                record.doctorName,
                record.hospitalName,
                record.district,
                record.hospitalLocation,
                record.birthTimestamp,
                record.status,
                if (record.isCouncilLocked) "YES (Locked)" else "NO (Pending)",
                record.parentMobile,
                record.parentEmail,
                record.biometricHash
            )
            stringBuilder.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
        }

        FileOutputStream(file).use { out ->
            out.write(stringBuilder.toString().toByteArray(Charsets.UTF_8))
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return ExportResult(
            file = file,
            uri = uri,
            format = ReportFormat.CSV,
            recordCount = records.size,
            fileName = fileName,
            fileSizeFormatted = formatFileSize(file.length())
        )
    }

    /**
     * Exports the given newborn records into an official multi-page PDF document
     * with Tamil Nadu Health Department formatting, summary analytics, and statutory seals.
     */
    fun exportToPdf(context: Context, records: List<NewbornRecord>, filterTitle: String = "All Records"): ExportResult {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SafeStart_Statutory_Report_${timestamp}.pdf"
        val file = File(getReportsDir(context), fileName)

        val document = PdfDocument()

        // A4 page dimensions in points (72 points = 1 inch): 595 x 842 pt
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = Paint().apply {
            color = Color.rgb(13, 92, 117) // TnDeepTeal
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerMetaPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val cellBoldPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val cellNormalPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val cellMonoPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 6.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }

        val validatedBadgePaint = Paint().apply {
            color = Color.rgb(5, 150, 105)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val pendingBadgePaint = Paint().apply {
            color = Color.rgb(217, 119, 6)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.6f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val bgPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val recordsPerPage = 7
        val totalPages = if (records.isEmpty()) 1 else ((records.size - 1) / recordsPerPage) + 1

        val verifiedCount = records.count { it.status.contains("Validated", ignoreCase = true) || it.status.contains("Confirmed", ignoreCase = true) }
        val pendingCount = records.size - verifiedCount
        val councilLockedCount = records.count { it.isCouncilLocked }

        var currentRecordIndex = 0

        for (pageIndex in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Page Background
            bgPaint.color = Color.WHITE
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

            // Top Header Government Banner
            bgPaint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(20f, 20f, (pageWidth - 20).toFloat(), 92f, bgPaint)

            bgPaint.color = Color.rgb(13, 92, 117)
            canvas.drawRect(20f, 20f, 25f, 92f, bgPaint) // Teal left stripe

            canvas.drawText("GOVERNMENT OF TAMIL NADU • DEPARTMENT OF HEALTH", 35f, 38f, titlePaint)
            canvas.drawText("SAFE START — NEWBORN IDENTITY CUSTODY & VERIFICATION REGISTER", 35f, 53f, subTitlePaint)
            val formattedDate = SimpleDateFormat("dd MMMM yyyy, hh:mm a 'IST'", Locale.getDefault()).format(Date())
            canvas.drawText("Report Scope: $filterTitle | Issued: $formattedDate", 35f, 68f, headerMetaPaint)
            canvas.drawText("Official Document: Tamil Nadu Child Protection & Identity Verification Act", 35f, 81f, headerMetaPaint)

            // KPI Summary Counters Bar
            val summaryTop = 100f
            val summaryHeight = 44f
            bgPaint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(RectF(20f, summaryTop, (pageWidth - 20).toFloat(), summaryTop + summaryHeight), 6f, 6f, bgPaint)

            linePaint.color = Color.rgb(226, 232, 240)
            canvas.drawRoundRect(RectF(20f, summaryTop, (pageWidth - 20).toFloat(), summaryTop + summaryHeight), 6f, 6f, linePaint)

            // Summary metrics inside bar
            val colW = (pageWidth - 40) / 4f
            val metricY1 = summaryTop + 18f
            val metricY2 = summaryTop + 34f

            // Col 1: Total
            canvas.drawText("TOTAL IN REGISTER", 30f, metricY1, headerMetaPaint)
            canvas.drawText("${records.size} Infants", 30f, metricY2, cellBoldPaint)

            // Col 2: Verified
            canvas.drawText("3-PARTY VALIDATED", 30f + colW, metricY1, headerMetaPaint)
            canvas.drawText("$verifiedCount Verified", 30f + colW, metricY2, validatedBadgePaint)

            // Col 3: Pending
            canvas.drawText("PENDING OTP / AUDIT", 30f + colW * 2, metricY1, headerMetaPaint)
            canvas.drawText("$pendingCount Pending", 30f + colW * 2, metricY2, pendingBadgePaint)

            // Col 4: Council Locked
            canvas.drawText("STATUTORY SEALED", 30f + colW * 3, metricY1, headerMetaPaint)
            canvas.drawText("$councilLockedCount Locked", 30f + colW * 3, metricY2, cellBoldPaint)

            // Table Setup
            val tableTop = 154f
            val tableHeaderHeight = 24f
            val tableLeft = 20f
            val tableRight = (pageWidth - 20).toFloat()

            // Header Background
            bgPaint.color = Color.rgb(13, 92, 117)
            canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + tableHeaderHeight, bgPaint)

            // Column Header Labels
            canvas.drawText("TOKEN / IDENTITY", 28f, tableTop + 16f, tableHeaderPaint)
            canvas.drawText("NEWBORN & PARENTS", 140f, tableTop + 16f, tableHeaderPaint)
            canvas.drawText("HOSPITAL & FACILITY", 275f, tableTop + 16f, tableHeaderPaint)
            canvas.drawText("DOCTOR / TIMING", 410f, tableTop + 16f, tableHeaderPaint)
            canvas.drawText("STATUS / SEAL", 495f, tableTop + 16f, tableHeaderPaint)

            // Table Rows
            var rowY = tableTop + tableHeaderHeight
            val rowHeight = 84f

            val recordsForThisPage = records.drop(currentRecordIndex).take(recordsPerPage)

            for ((idx, rec) in recordsForThisPage.withIndex()) {
                // Alternating row background
                bgPaint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(tableLeft, rowY, tableRight, rowY + rowHeight, bgPaint)

                // Row bottom divider
                linePaint.color = Color.rgb(226, 232, 240)
                canvas.drawLine(tableLeft, rowY + rowHeight, tableRight, rowY + rowHeight, linePaint)

                // Column 1: Token & Gender
                canvas.drawText(rec.token, 28f, rowY + 16f, cellBoldPaint)
                canvas.drawText("Gender: ${rec.gender}", 28f, rowY + 28f, cellNormalPaint)
                canvas.drawText(if (rec.isCouncilLocked) "🔒 Council Locked" else "🔓 Open Intake", 28f, rowY + 40f, headerMetaPaint)

                // Column 2: Newborn & Parents
                canvas.drawText("M: ${rec.motherName.take(22)}", 140f, rowY + 16f, cellBoldPaint)
                canvas.drawText("F: ${rec.fatherName.take(22)}", 140f, rowY + 28f, cellNormalPaint)
                canvas.drawText("Ph: ${rec.parentMobile}", 140f, rowY + 40f, cellNormalPaint)
                canvas.drawText("Em: ${rec.parentEmail.take(20)}", 140f, rowY + 52f, cellMonoPaint)

                // Column 3: Hospital & District
                val truncatedHosp = if (rec.hospitalName.length > 25) rec.hospitalName.take(25) + "..." else rec.hospitalName
                canvas.drawText(truncatedHosp, 275f, rowY + 16f, cellBoldPaint)
                canvas.drawText("Dist: ${rec.district}", 275f, rowY + 28f, cellNormalPaint)
                canvas.drawText(rec.hospitalLocation.take(24), 275f, rowY + 40f, headerMetaPaint)

                // Column 4: Doctor & Birth Timestamp
                val truncatedDoc = if (rec.doctorName.length > 18) rec.doctorName.take(18) + "..." else rec.doctorName
                canvas.drawText(truncatedDoc, 410f, rowY + 16f, cellBoldPaint)
                canvas.drawText(rec.birthTimestamp.take(20), 410f, rowY + 28f, cellNormalPaint)

                // Column 5: Status & Seal
                val isVal = rec.status.contains("Validated", ignoreCase = true) || rec.status.contains("Confirmed", ignoreCase = true)
                if (isVal) {
                    canvas.drawText("✓ 3-Party Valid", 495f, rowY + 16f, validatedBadgePaint)
                    canvas.drawText("OTP Confirmed", 495f, rowY + 28f, validatedBadgePaint)
                } else {
                    canvas.drawText("⏳ Pending OTP", 495f, rowY + 16f, pendingBadgePaint)
                    canvas.drawText("Audit Required", 495f, rowY + 28f, pendingBadgePaint)
                }

                // Biometric Hash preview on row bottom
                val hashShort = rec.biometricHash.take(28)
                canvas.drawText("SHA-256: $hashShort...", 28f, rowY + 70f, cellMonoPaint)

                rowY += rowHeight
            }

            // Outer border of table
            linePaint.color = Color.rgb(203, 213, 225)
            canvas.drawRect(tableLeft, tableTop, tableRight, rowY, linePaint)

            // Page Footer
            val footerY = (pageHeight - 30).toFloat()
            canvas.drawLine(20f, footerY - 10f, (pageWidth - 20).toFloat(), footerY - 10f, linePaint)
            canvas.drawText(
                "CONFIDENTIAL RECORD • HEALTH & FAMILY WELFARE DEPT, TAMIL NADU • AUDIT REF: TN-REG-${timestamp.take(8)}",
                20f,
                footerY,
                headerMetaPaint
            )
            val pageStr = "Page $pageIndex of $totalPages"
            val pageStrWidth = headerMetaPaint.measureText(pageStr)
            canvas.drawText(pageStr, pageWidth - 20f - pageStrWidth, footerY, headerMetaPaint)

            document.finishPage(page)
            currentRecordIndex += recordsPerPage
        }

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return ExportResult(
            file = file,
            uri = uri,
            format = ReportFormat.PDF,
            recordCount = records.size,
            fileName = fileName,
            fileSizeFormatted = formatFileSize(file.length())
        )
    }

    /**
     * Shares the generated report via system chooser (Email, Drive, Files, Chat, etc.).
     */
    fun shareReport(context: Context, result: ExportResult) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = result.format.mimeType
            putExtra(Intent.EXTRA_STREAM, result.uri)
            putExtra(Intent.EXTRA_SUBJECT, "Tamil Nadu Safe Start: Newborn Register Report (${result.format.name})")
            putExtra(
                Intent.EXTRA_TEXT,
                "Enclosed is the official administrative record report containing ${result.recordCount} newborn identities and their statutory verification statuses from the Tamil Nadu Safe Start registry."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Newborn Report (${result.format.displayName})"))
    }

    /**
     * Opens the report in a compatible PDF/CSV viewer app on the device.
     */
    fun openReport(context: Context, result: ExportResult) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(result.uri, result.format.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to open ${result.format.extension.uppercase()} file", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies the report file to the public Downloads directory so the registrar can access it easily.
     */
    fun saveToDownloads(context: Context, result: ExportResult): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, result.fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, result.format.mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SafeStartReports")
                }
                val resolver = context.contentResolver
                val downloadUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (downloadUri != null) {
                    resolver.openOutputStream(downloadUri)?.use { out ->
                        result.file.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    Toast.makeText(context, "Saved to Downloads/SafeStartReports/${result.fileName}", Toast.LENGTH_LONG).show()
                    true
                } else {
                    false
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadsDir, result.fileName)
                result.file.copyTo(targetFile, overwrite = true)
                Toast.makeText(context, "Saved to ${targetFile.absolutePath}", Toast.LENGTH_LONG).show()
                true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Report saved in app storage: ${result.file.name}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
            else -> "$bytes bytes"
        }
    }
}
