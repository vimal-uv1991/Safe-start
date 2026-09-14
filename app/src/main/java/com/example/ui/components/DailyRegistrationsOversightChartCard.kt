package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NewbornRecord
import com.example.ui.theme.GovSaffronGold
import com.example.ui.theme.TnDeepTeal
import com.example.ui.theme.TnPrimary
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

enum class OversightChartTab(val title: String, val shortTitle: String) {
    DAILY_GENDER("Daily Trend & Gender", "Daily Trend"),
    WARD_STATUS("Ward Status Breakdown", "Ward Status"),
    CROSS_MATRIX("Oversight Matrix", "Matrix")
}

data class DailyRegistrationStats(
    val dateKey: String,          // e.g. "13 Sep"
    val displayDate: String,      // e.g. "13 Sep 2026"
    val dayOfWeek: String,        // e.g. "Sun"
    val maleCount: Int,
    val femaleCount: Int,
    val totalCount: Int,
    val wardPostnatal: Int,
    val wardLaborDelivery: Int,
    val wardNicu: Int,
    val wardSpecialCare: Int
)

data class WardAggregate(
    val wardName: String,
    val count: Int,
    val maleCount: Int,
    val femaleCount: Int,
    val clearanceStatus: String,
    val badgeColor: Color,
    val containerColor: Color
)

/**
 * Administrative Oversight Summary Card for Dashboard
 * Inspired by Recharts / D3 vector visualization architecture:
 * - Grouped / Stacked daily bars with interactive inspection tooltip
 * - Demographic gender breakdown (Male vs Female)
 * - Clinical ward status distribution (Postnatal, Labor & Delivery, NICU, Special Care)
 * - Administrative compliance & surveillance indicators
 */
@Composable
fun DailyRegistrationsOversightChartCard(
    records: List<NewbornRecord>,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(OversightChartTab.DAILY_GENDER) }
    var selectedDayKey by remember { mutableStateOf<String?>(null) }

    // Parse and aggregate records by day and ward
    val dailyStatsList = remember(records) {
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val dayKeyFormatter = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        val dayOfWeekFormatter = SimpleDateFormat("EEE", Locale.ENGLISH)

        // Generate last 7 days keys
        val dayBuckets = mutableMapOf<String, MutableList<NewbornRecord>>()
        val dayInfoMap = mutableMapOf<String, Pair<String, String>>() // key -> (displayDate, dayOfWeek)

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = dayKeyFormatter.format(cal.time)
            dayBuckets[key] = mutableListOf()
            dayInfoMap[key] = Pair(dateFormatter.format(cal.time), dayOfWeekFormatter.format(cal.time))
        }

        // Map records into buckets
        records.forEach { record ->
            // Try extracting date from birthTimestamp, e.g. "13 Sep 2026, 04:15 AM"
            val parts = record.birthTimestamp.split(",")
            val datePart = parts.firstOrNull()?.trim() ?: ""
            var matchedKey: String? = null

            for (k in dayBuckets.keys) {
                if (datePart.contains(k, ignoreCase = true)) {
                    matchedKey = k
                    break
                }
            }

            if (matchedKey == null) {
                // Try parsing or fallback to today/latest
                val firstKey = dayBuckets.keys.lastOrNull() ?: "13 Sep"
                matchedKey = firstKey
            }

            dayBuckets[matchedKey]?.add(record)
        }

        dayBuckets.map { (key, recs) ->
            val info = dayInfoMap[key] ?: Pair("$key 2026", "Day")
            val male = recs.count { it.gender.equals("Male", ignoreCase = true) }
            val female = recs.count { it.gender.equals("Female", ignoreCase = true) }
            val postnatal = recs.count { it.wardStatus.contains("Postnatal", ignoreCase = true) }
            val labor = recs.count { it.wardStatus.contains("Labor", ignoreCase = true) }
            val nicu = recs.count { it.wardStatus.contains("NICU", ignoreCase = true) || it.wardStatus.contains("SNCU", ignoreCase = true) }
            val scbu = recs.size - postnatal - labor - nicu

            DailyRegistrationStats(
                dateKey = key,
                displayDate = info.first,
                dayOfWeek = info.second,
                maleCount = male,
                femaleCount = female,
                totalCount = recs.size,
                wardPostnatal = postnatal,
                wardLaborDelivery = labor,
                wardNicu = nicu,
                wardSpecialCare = max(0, scbu)
            )
        }
    }

    // Overall aggregate metrics
    val totalRegistrations = remember(records) { records.size }
    val totalMale = remember(records) { records.count { it.gender.equals("Male", ignoreCase = true) } }
    val totalFemale = remember(records) { records.count { it.gender.equals("Female", ignoreCase = true) } }
    val maleRatio = if (totalRegistrations > 0) ((totalMale.toFloat() / totalRegistrations) * 100).toInt() else 0
    val femaleRatio = if (totalRegistrations > 0) 100 - maleRatio else 0

    // Ward distribution aggregates
    val wardAggregates = remember(records) {
        val postnatal = records.filter { it.wardStatus.contains("Postnatal", ignoreCase = true) }
        val labor = records.filter { it.wardStatus.contains("Labor", ignoreCase = true) }
        val nicu = records.filter { it.wardStatus.contains("NICU", ignoreCase = true) || it.wardStatus.contains("SNCU", ignoreCase = true) }
        val scbu = records.filter {
            !it.wardStatus.contains("Postnatal", ignoreCase = true) &&
                    !it.wardStatus.contains("Labor", ignoreCase = true) &&
                    !it.wardStatus.contains("NICU", ignoreCase = true) &&
                    !it.wardStatus.contains("SNCU", ignoreCase = true)
        }

        listOf(
            WardAggregate(
                wardName = "Postnatal Recovery Ward",
                count = postnatal.size,
                maleCount = postnatal.count { it.gender.equals("Male", ignoreCase = true) },
                femaleCount = postnatal.count { it.gender.equals("Female", ignoreCase = true) },
                clearanceStatus = "Optimal Flow • Discharge Ready",
                badgeColor = Color(0xFF059669),
                containerColor = Color(0xFFECFDF5)
            ),
            WardAggregate(
                wardName = "Labor & Delivery Suite",
                count = labor.size,
                maleCount = labor.count { it.gender.equals("Male", ignoreCase = true) },
                femaleCount = labor.count { it.gender.equals("Female", ignoreCase = true) },
                clearanceStatus = "Active Admissions • Intake Clearance",
                badgeColor = Color(0xFF0284C7),
                containerColor = Color(0xFFF0F9FF)
            ),
            WardAggregate(
                wardName = "Neonatal Intensive Care (NICU)",
                count = nicu.size,
                maleCount = nicu.count { it.gender.equals("Male", ignoreCase = true) },
                femaleCount = nicu.count { it.gender.equals("Female", ignoreCase = true) },
                clearanceStatus = "Statutory Surveillance • High Care",
                badgeColor = Color(0xFFD97706),
                containerColor = Color(0xFFFFFBEB)
            ),
            WardAggregate(
                wardName = "Special Care Nursery (SCBU)",
                count = scbu.size,
                maleCount = scbu.count { it.gender.equals("Male", ignoreCase = true) },
                femaleCount = scbu.count { it.gender.equals("Female", ignoreCase = true) },
                clearanceStatus = "Phototherapy & Transitional Care",
                badgeColor = Color(0xFF7C3AED),
                containerColor = Color(0xFFF5F3FF)
            )
        )
    }

    val selectedStats = remember(selectedDayKey, dailyStatsList) {
        dailyStatsList.find { it.dateKey == selectedDayKey }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_admin_oversight_summary"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
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
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                tint = TnDeepTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ADMINISTRATIVE OVERSIGHT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TnDeepTeal,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFF10B981))
                            ) {
                                Text(
                                    text = "D3 / RECHARTS ENGINE",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Daily Registrations & Ward Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                // Active audit status pill
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(0.8.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = "LIVE AUDIT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // Quick Metrics Row (Male, Female, Ward Total)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Registered
                MetricMiniCard(
                    title = "Total Daily Logs",
                    value = "$totalRegistrations",
                    subtitle = "7-day window",
                    badge = "ACTIVE",
                    badgeBg = Color(0xFFF1F5F9),
                    badgeColor = Color(0xFF334155),
                    accentColor = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )

                // Male Split
                MetricMiniCard(
                    title = "Male Newborns",
                    value = "$totalMale ($maleRatio%)",
                    subtitle = "ஆண் குழந்தைகள்",
                    badge = "👦 MALE",
                    badgeBg = Color(0xFFE0F2FE),
                    badgeColor = Color(0xFF0369A1),
                    accentColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )

                // Female Split
                MetricMiniCard(
                    title = "Female Newborns",
                    value = "$totalFemale ($femaleRatio%)",
                    subtitle = "பெண் குழந்தைகள்",
                    badge = "👧 FEMALE",
                    badgeBg = Color(0xFFFCE7F3),
                    badgeColor = Color(0xFFBE185D),
                    accentColor = Color(0xFFEC4899),
                    modifier = Modifier.weight(1f)
                )
            }

            // Interactive Tab Switcher (Styled like a modern web dashboard)
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OversightChartTab.values().forEach { tab ->
                        val isSelected = activeTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { activeTab = tab }
                                .testTag("tab_${tab.name.lowercase()}"),
                            color = if (isSelected) Color.White else Color.Transparent,
                            shadowElevation = if (isSelected) 1.dp else 0.dp
                        ) {
                            Text(
                                text = tab.shortTitle,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TnDeepTeal else Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Main Visual Display depending on selected tab
            when (activeTab) {
                OversightChartTab.DAILY_GENDER -> {
                    DailyGenderBarChart(
                        dailyStatsList = dailyStatsList,
                        selectedKey = selectedDayKey,
                        onSelectKey = { key ->
                            selectedDayKey = if (selectedDayKey == key) null else key
                        }
                    )

                    // Recharts/D3 Inspection Tooltip Callout
                    AnimatedVisibility(
                        visible = selectedStats != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        selectedStats?.let { stats ->
                            RechartsInspectionTooltip(
                                stats = stats,
                                onDismiss = { selectedDayKey = null }
                            )
                        }
                    }

                    // Legend & Interpretation note
                    RechartsStyleLegend()
                }

                OversightChartTab.WARD_STATUS -> {
                    WardStatusOversightSection(
                        wardAggregates = wardAggregates,
                        totalCount = totalRegistrations
                    )
                }

                OversightChartTab.CROSS_MATRIX -> {
                    OversightMatrixTable(
                        dailyStatsList = dailyStatsList
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Footer note with administrative statutory reassurance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Statutory Cross-Verification: National Health Mission & TN DPH Mandate",
                        fontSize = 9.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Text(
                    text = "Auto-Synced",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

/**
 * Recharts / D3 SVG-Inspired Grouped Bar Chart
 * Renders daily Male and Female bars with dashed gridlines, numeric Y-axis, and touch hitboxes.
 */
@Composable
private fun DailyGenderBarChart(
    dailyStatsList: List<DailyRegistrationStats>,
    selectedKey: String?,
    onSelectKey: (String) -> Unit
) {
    val maxCount = remember(dailyStatsList) {
        val maxTotal = dailyStatsList.maxOfOrNull { max(it.maleCount, it.femaleCount) } ?: 2
        max(maxTotal + 1, 3)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("d3_daily_gender_chart")
    ) {
        Text(
            text = "Tap any day column for demographic and clinical ward breakdown",
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFFFAFAFA), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            // Background Canvas with D3 Dashed Grid Lines & Y-axis scale
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, bottom = 22.dp)
            ) {
                val gridLineCount = 3
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                for (i in 0..gridLineCount) {
                    val y = size.height * (i.toFloat() / gridLineCount)
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = pathEffect
                    )
                }
            }

            // Y-Axis Labels
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$maxCount", fontSize = 9.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                Text("${maxCount / 2}", fontSize = 9.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                Text("0", fontSize = 9.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
            }

            // Grouped Bars Container
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 28.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyStatsList.forEach { dayStats ->
                    val isSelected = selectedKey == dayStats.dateKey
                    val maleHeightFraction = (dayStats.maleCount.toFloat() / maxCount).coerceIn(0.04f, 1f)
                    val femaleHeightFraction = (dayStats.femaleCount.toFloat() / maxCount).coerceIn(0.04f, 1f)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFFE2E8F0).copy(alpha = 0.5f) else Color.Transparent)
                            .clickable { onSelectKey(dayStats.dateKey) }
                            .padding(horizontal = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Total count pill
                        if (dayStats.totalCount > 0) {
                            Surface(
                                color = if (isSelected) TnDeepTeal else Color(0xFF0F172A),
                                shape = RoundedCornerShape(3.dp),
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) {
                                Text(
                                    text = "${dayStats.totalCount}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // Side-by-side grouped bars (Male & Female)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Male Bar (Cyan / Sky)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(maleHeightFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                        )
                                    )
                            )

                            Spacer(modifier = Modifier.width(2.dp))

                            // Female Bar (Pink / Rose)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(femaleHeightFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFFF472B6), Color(0xFFDB2777))
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // X-Axis Labels Row (Days of Week & Date)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 28.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dailyStatsList.forEach { dayStats ->
                    val isSelected = selectedKey == dayStats.dateKey
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectKey(dayStats.dateKey) }
                    ) {
                        Text(
                            text = dayStats.dayOfWeek,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) TnDeepTeal else Color(0xFF64748B)
                        )
                        Text(
                            text = dayStats.dateKey.split(" ").firstOrNull() ?: "",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TnDeepTeal else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Recharts-Style Floating Inspection Tooltip
 */
@Composable
private fun RechartsInspectionTooltip(
    stats: DailyRegistrationStats,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recharts_tooltip_card"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1220)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = GovSaffronGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${stats.dayOfWeek}, ${stats.displayDate}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close Tooltip",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Registered on Date:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text("${stats.totalCount} infants", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Gender breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF38BDF8)))
                    Text("Male (ஆண்):", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }
                Text("${stats.maleCount} (${if (stats.totalCount > 0) (stats.maleCount * 100 / stats.totalCount) else 0}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF472B6)))
                    Text("Female (பெண்):", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                }
                Text("${stats.femaleCount} (${if (stats.totalCount > 0) (stats.femaleCount * 100 / stats.totalCount) else 0}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF472B6))
            }

            // Clinical ward distribution for this date
            HorizontalDivider(color = Color(0xFF1E293B))

            Text(
                text = "Clinical Ward Distribution on this Date:",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = GovSaffronGold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• Postnatal Recovery Ward:", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                Text("${stats.wardPostnatal}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• Labor & Delivery Suite:", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                Text("${stats.wardLaborDelivery}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• NICU / Special Nursery Care:", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                Text("${stats.wardNicu + stats.wardSpecialCare}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
            }
        }
    }
}

/**
 * Recharts Style Legend Indicator
 */
@Composable
private fun RechartsStyleLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF0284C7))
            )
            Text("Male Registrations", fontSize = 10.5.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFDB2777))
            )
            Text("Female Registrations", fontSize = 10.5.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.size(10.dp)
            ) {}
            Text("Total Daily Sum", fontSize = 10.5.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * Ward Status Oversight Section
 */
@Composable
private fun WardStatusOversightSection(
    wardAggregates: List<WardAggregate>,
    totalCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ward_status_oversight_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "CLINICAL WARD DISTRIBUTION & HEALTH STATUS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569),
            letterSpacing = 0.5.sp
        )

        wardAggregates.forEach { ward ->
            val percentage = if (totalCount > 0) ((ward.count.toFloat() / totalCount) * 100).toInt() else 0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = ward.containerColor),
                border = BorderStroke(0.8.dp, ward.badgeColor.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = ward.wardName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = ward.clearanceStatus,
                                fontSize = 10.sp,
                                color = ward.badgeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            color = ward.badgeColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${ward.count} infants ($percentage%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) ward.count.toFloat() / totalCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ward.badgeColor,
                        trackColor = Color.White.copy(alpha = 0.6f)
                    )

                    // Gender breakdown in ward
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Demographic Split:",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "👦 ${ward.maleCount} Male",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                            Text(
                                text = "👧 ${ward.femaleCount} Female",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDB2777)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cross-Tabulation Matrix Table
 */
@Composable
private fun OversightMatrixTable(
    dailyStatsList: List<DailyRegistrationStats>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("oversight_matrix_table"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "DAILY CROSS-TABULATION MATRIX (GENDER × WARD)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569),
            letterSpacing = 0.5.sp
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            color = Color.White
        ) {
            Column {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Date", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(1.2f))
                    Text("Male", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text("Fem", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDB2777), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text("Postnatal", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("NICU/L&D", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("Total", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TnDeepTeal, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Table Rows
                dailyStatsList.forEachIndexed { index, stats ->
                    val bg = if (index % 2 == 0) Color.White else Color(0xFFFAFAFA)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bg)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${stats.dayOfWeek} ${stats.dateKey.split(" ").firstOrNull()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = "${stats.maleCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.weight(0.8f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${stats.femaleCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDB2777),
                            modifier = Modifier.weight(0.8f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${stats.wardPostnatal}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF059669),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${stats.wardLaborDelivery + stats.wardNicu + stats.wardSpecialCare}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD97706),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${stats.totalCount}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TnDeepTeal,
                            modifier = Modifier.weight(0.8f),
                            textAlign = TextAlign.End
                        )
                    }
                    if (index < dailyStatsList.size - 1) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

/**
 * Metric Mini Card for KPI row
 */
@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    subtitle: String,
    badge: String,
    badgeBg: Color,
    badgeColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = title,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                fontSize = 8.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
