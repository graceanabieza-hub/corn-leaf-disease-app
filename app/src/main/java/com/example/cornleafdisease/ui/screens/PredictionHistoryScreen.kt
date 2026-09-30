package com.example.cornleafdisease.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornleafdisease.data.model.ScanResult
import com.example.cornleafdisease.ui.theme.*

import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun PredictionHistoryScreenPreview() {
    PredictionHistoryScreen(history = emptyList(), onBack = {}, onDelete = {}, onItemClick = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionHistoryScreen(
    history: List<ScanResult>,
    onBack: () -> Unit,
    onDelete: (String) -> Unit,
    onItemClick: (ScanResult) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Healthy", "Diseased")

    val filteredHistory = when (selectedFilter) {
        "Healthy" -> history.filter { it.diseaseName == "Healthy" }
        "Diseased" -> history.filter { it.diseaseName != "Healthy" }
        else -> history
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prediction History", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFBFA))
                .padding(padding)
        ) {
            // ---- Summary stats strip -------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryStatCard(
                    modifier = Modifier.weight(1f),
                    value = history.size.toString(),
                    label = "Total Scans",
                    icon = Icons.Default.Insights,
                    color = GreenPrimary
                )
                SummaryStatCard(
                    modifier = Modifier.weight(1f),
                    value = history.count { it.diseaseName == "Healthy" }.toString(),
                    label = "Healthy",
                    icon = Icons.Default.CheckCircle,
                    color = SeverityLow
                )
                SummaryStatCard(
                    modifier = Modifier.weight(1f),
                    value = history.count { it.diseaseName != "Healthy" }.toString(),
                    label = "Diseased",
                    icon = Icons.Default.WarningAmber,
                    color = SeverityHigh
                )
            }

            // ---- Filter chips -----------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredHistory.isEmpty()) {
                EmptyHistoryState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredHistory, key = { it.id }) { item ->
                        HistoryCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onDelete = { onDelete(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Text(label, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun HistoryCard(
    item: ScanResult,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isHealthy = item.diseaseName == "Healthy"
    val statusIcon = if (isHealthy) Icons.Default.CheckCircle else Icons.Default.WarningAmber
    val severityColor = Color(item.severityColorHex)
    val confidencePercentage = String.format("%.1f%%", item.confidence * 100)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // colored accent strip matching severity
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(severityColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // status icon instead of a plain gray thumbnail
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(severityColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(statusIcon, null, tint = severityColor, modifier = Modifier.size(26.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(item.date, fontSize = 11.sp, color = TextSecondary)
                    Text(item.diseaseName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)

                    Spacer(Modifier.height(6.dp))

                    // confidence as a mini progress bar instead of plain text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { item.confidence },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = severityColor,
                            trackColor = severityColor.copy(alpha = 0.15f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(confidencePercentage, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
                    }

                    Spacer(Modifier.height(6.dp))

                    Surface(
                        color = severityColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            item.severity,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = severityColor
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete history", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.History, null, tint = GreenPrimary, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("No scans found", fontWeight = FontWeight.SemiBold, color = TextDark)
        Text(
            "Try a different filter or run a new scan",
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}
