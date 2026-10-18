package com.example.cornleafdisease.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornleafdisease.data.model.ScanResult
import com.example.cornleafdisease.ui.theme.*

@Preview(showBackground = true)
@Composable
fun ClassificationHistoryScreenPreview() {
    ClassificationHistoryScreen(history = emptyList(), onBack = {}, onDelete = {}, onItemClick = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationHistoryScreen(
    history: List<ScanResult>,
    onBack: () -> Unit,
    onDelete: (String) -> Unit,
    onItemClick: (ScanResult) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var pendingDelete by remember { mutableStateOf<ScanResult?>(null) }

    val healthyCount = history.count { it.diseaseName == "Healthy" }
    val diseasedCount = history.size - healthyCount

    // label, count, icon
    val filters = listOf(
        Triple("All", history.size, Icons.Default.Apps),
        Triple("Healthy", healthyCount, Icons.Default.CheckCircle),
        Triple("Diseased", diseasedCount, Icons.Default.WarningAmber)
    )

    val filteredHistory = when (selectedFilter) {
        "Healthy" -> history.filter { it.diseaseName == "Healthy" }
        "Diseased" -> history.filter { it.diseaseName != "Healthy" }
        else -> history
    }

    Scaffold(
        containerColor = Color(0xFFF6F8F6),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Scan History",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                        Text(
                            "${history.size} ${if (history.size == 1) "scan" else "scans"} recorded",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ---- Filter chips (fixed, with counts) --------------------------
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { (label, count, icon) ->
                        val selected = selectedFilter == label
                        FilterChip(
                            selected = selected,
                            onClick = { selectedFilter = label },
                            label = { Text("$label · $count", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
            }

            if (filteredHistory.isEmpty()) {
                EmptyHistoryState(
                    hasAnyHistory = history.isNotEmpty(),
                    onClearFilter = { selectedFilter = "All" }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Summary scrolls away with the list to give cards more room
                    item {
                        SummaryCard(
                            total = history.size,
                            healthy = healthyCount,
                            diseased = diseasedCount
                        )
                    }

                    item {
                        Text(
                            text = if (selectedFilter == "All") "Recent scans"
                            else "$selectedFilter scans",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    items(filteredHistory, key = { it.id }) { item ->
                        HistoryCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onDelete = { pendingDelete = item }
                        )
                    }
                }
            }
        }
    }

    // ---- Delete confirmation ---------------------------------------------
    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { Text("Delete this scan?") },
            text = {
                Text(
                    "${target.diseaseName} • ${target.date}\nThis can't be undone.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(target.id)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = SeverityHigh)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

// =============================================================================
// Summary
// =============================================================================

@Composable
private fun SummaryCard(total: Int, healthy: Int, diseased: Int) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryStat(Modifier.weight(1f), total.toString(), "Total", Icons.Default.Insights, GreenPrimary)
                VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0xFFE5E9E5))
                SummaryStat(Modifier.weight(1f), healthy.toString(), "Healthy", Icons.Default.CheckCircle, SeverityLow)
                VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0xFFE5E9E5))
                SummaryStat(Modifier.weight(1f), diseased.toString(), "Diseased", Icons.Default.WarningAmber, SeverityHigh)
            }

            if (total > 0) {
                Spacer(Modifier.height(14.dp))
                // Healthy vs diseased ratio bar
                val healthyFraction = healthy.toFloat() / total
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SeverityHigh.copy(alpha = 0.25f))
                ) {
                    if (healthyFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(healthyFraction)
                                .fillMaxHeight()
                                .background(SeverityLow)
                        )
                    }
                    if (healthyFraction < 1f) {
                        Spacer(Modifier.weight(1f - healthyFraction))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "${(healthyFraction * 100).toInt()}% of your scans were healthy",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

// =============================================================================
// History card
// =============================================================================

@Composable
private fun HistoryCard(
    item: ScanResult,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isHealthy = item.diseaseName == "Healthy"
    val severityColor = Color(item.severityColorHex)
    val confidence = item.confidence.coerceIn(0f, 1f)
    val confidencePercentage = String.format("%.0f%%", confidence * 100)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "${item.diseaseName}, ${item.severity}, confidence $confidencePercentage, ${item.date}"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // severity accent strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(severityColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Confidence ring with status icon-free, number-first design
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(58.dp)) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = severityColor.copy(alpha = 0.15f),
                        strokeWidth = 5.dp
                    )
                    CircularProgressIndicator(
                        progress = { confidence },
                        modifier = Modifier.fillMaxSize(),
                        color = severityColor,
                        strokeWidth = 5.dp,
                        trackColor = Color.Transparent
                    )
                    Text(
                        confidencePercentage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.diseaseName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            item.date,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // severity pill with icon
                    Surface(
                        color = severityColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isHealthy) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = severityColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                item.severity,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = severityColor
                            )
                        }
                    }
                }

                // subtle delete action, confirmation handled by the screen
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete scan",
                        tint = Color(0xFF9AA39A)
                    )
                }
            }
        }
    }
}

// =============================================================================
// Empty state
// =============================================================================

@Composable
private fun EmptyHistoryState(
    hasAnyHistory: Boolean,
    onClearFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (hasAnyHistory) Icons.Default.FilterListOff else Icons.Default.History,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            if (hasAnyHistory) "No matching scans" else "No scans yet",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = TextDark
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (hasAnyHistory) "Nothing fits this filter. Try another one or view all scans."
            else "Scan a corn leaf and your results will show up here.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        if (hasAnyHistory) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onClearFilter,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Show all scans", color = GreenPrimary)
            }
        }
    }
}