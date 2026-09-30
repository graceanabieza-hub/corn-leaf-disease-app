package com.example.cornleafdisease.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornleafdisease.data.model.ScanResult
import com.example.cornleafdisease.ui.theme.*
import kotlinx.coroutines.launch

import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun PredictionResultScreenPreview() {
    val sampleResult = ScanResult(
        id = "1",
        date = "Sept 15, 2026",
        diseaseName = "Corn Rust",
        scientificName = "Puccinia sorghi",
        confidence = 0.958f,
        severity = "Moderate",
        severityColorHex = 0xFFFFA000,
        recommendations = listOf("Apply recommended fungicide", "Remove and destroy infected leaves"),
        anfisFeatures = listOf("Rust-colored pustules", "Oval lesions")
    )
    PredictionResultScreen(result = sampleResult, onBack = {}, onNewScan = {}, onSaveClick = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionResultScreen(
    result: ScanResult?,
    isFromHistory: Boolean = false,
    onBack: () -> Unit,
    onNewScan: () -> Unit,
    onSaveClick: (ScanResult) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isSaved by remember(result?.id) { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Prediction Result", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        }
    ) { padding ->
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No detection result available.\nPlease execute a leaf analysis from the home screen first.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = TextSecondary,
                    fontSize = 15.sp
                )
            }
        } else {
            val severityColor = Color(result.severityColorHex)
            val confidencePercentage = String.format("%.1f%%", result.confidence * 100)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Predicted Disease card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Predicted Disease", fontSize = 13.sp, color = TextSecondary)
                            Text(result.diseaseName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("(${result.scientificName})", fontSize = 13.sp, color = TextSecondary)
                        }
                        // Circular image placeholder
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Confidence
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Confidence Score", fontSize = 13.sp, color = TextSecondary)
                        Text(confidencePercentage, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        LinearProgressIndicator(
                            progress = { result.confidence },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = GreenPrimary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Severity
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Disease Severity", fontSize = 13.sp, color = TextSecondary)
                            Text(result.severity, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = severityColor)
                        }
                        // Gauge placeholder
                        Text("◔", fontSize = 40.sp, color = severityColor)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Recommendations
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Recommendation", fontWeight = FontWeight.SemiBold)
                        result.recommendations.forEach {
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("✓  ", color = GreenPrimary)
                                Text(it, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Explainability
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Explainability (ANFIS Rules)", fontWeight = FontWeight.SemiBold)
                        Text(
                            "The prediction was based on the following important features:",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        // Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            result.anfisFeatures.forEach { feature ->
                                AssistChip(onClick = {}, label = { Text(feature) })
                            }
                        }
                    }
                }

                if (!isFromHistory) {
                    Spacer(Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { 
                                onSaveClick(result)
                                isSaved = true
                                scope.launch {
                                    snackbarHostState.showSnackbar("Result saved to history")
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSaved) Color.Gray else GreenPrimary),
                            enabled = !isSaved
                        ) { 
                            if (isSaved) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(if (isSaved) "Saved" else "Save Result") 
                        }

                        OutlinedButton(
                            onClick = onNewScan,
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) { Text("New Scan") }
                    }
                }
            }
        }
    }
}
