package com.example.cornleafdisease.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornleafdisease.data.model.ScanResult
import com.example.cornleafdisease.ui.theme.*
import kotlinx.coroutines.launch

private val ResultPageColor = Color(0xFFF6F8F5)
private val PanelTint = Color(0xFFF3F6F3)
private val WarnTint = Color(0xFFFFF6DC)
private val WarnAccent = Color(0xFFB77900)

@Preview(showBackground = true, heightDp = 900)
@Composable
fun ClassificationResultScreenPreview() {
    val sampleResult = ScanResult(
        id = "1",
        date = "Oct 16, 2026 • 10:00 AM",
        diseaseName = "Northern Leaf Blight",
        scientificName = "Helminthosporium turcicum",
        confidence = 0.965f,
        severity = "High",
        severityColorHex = 0xFFE53935,
        recommendations = listOf(
            "Cultural: Plant resistant hybrids; rotate with non-host crops.",
            "Chemical: Apply fungicides (Azoxystrobin + Difenoconazole) early."
        ),
        anfisFeatures = listOf("Cigar-shaped lesions", "Gray-green to tan spots")
    )
    CornLeafTheme {
        ClassificationResultScreen(
            result = sampleResult,
            onBack = {},
            onNewScan = {},
            onSaveClick = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun ClassificationResultHealthyPreview() {
    val sampleResult = ScanResult(
        id = "2",
        date = "Oct 16, 2026 • 10:05 AM",
        diseaseName = "Healthy",
        scientificName = "Not applicable",
        confidence = 0.58f,
        severity = "Healthy",
        severityColorHex = 0xFF2E7D32,
        recommendations = listOf(
            "Regular field monitoring: Inspect leaves from vegetative growth through tasseling.",
            "Balanced fertilization: Apply based on soil conditions."
        ),
        anfisFeatures = emptyList()
    )
    CornLeafTheme {
        ClassificationResultScreen(
            result = sampleResult,
            onBack = {},
            onNewScan = {},
            onSaveClick = {}
        )
    }
}

// ---- Helpers ------------------------------------------------------------

private fun isHealthy(result: ScanResult?): Boolean =
    result?.diseaseName?.let {
        it.equals("Healthy Leaves", ignoreCase = true) || it.equals("Healthy", ignoreCase = true)
    } ?: false

/** Top and bottom colors of the hero. The top bar uses the top color so the two blend. */
private fun heroColors(result: ScanResult?, healthy: Boolean): Pair<Color, Color> {
    if (result == null || healthy) {
        return GreenPrimary to lerp(GreenPrimary, Color.Black, 0.3f)
    }
    val base = Color(result.severityColorHex)
    return lerp(base, Color.Black, 0.2f) to lerp(base, Color.Black, 0.45f)
}

private enum class ConfidenceBand(val label: String, val advice: String?) {
    High("High confidence", null),
    Moderate("Moderate confidence", "Consider scanning again to double-check this result."),
    Low("Low confidence", "Retake the photo in bright daylight with a single leaf filling the frame.")
}

private fun bandFor(confidence: Float): ConfidenceBand = when {
    confidence >= 0.85f -> ConfidenceBand.High
    confidence >= 0.60f -> ConfidenceBand.Moderate
    else -> ConfidenceBand.Low
}

// ---- Screen -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationResultScreen(
    result: ScanResult?,
    isFromHistory: Boolean = false,
    onBack: () -> Unit,
    onNewScan: () -> Unit,
    onSaveClick: (ScanResult) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isSaved by remember(result?.id) { mutableStateOf(false) }

    val healthy = isHealthy(result)
    val (heroTop, heroBottom) = heroColors(result, healthy)

    Scaffold(
        containerColor = ResultPageColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Classification Result",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                        if (result != null) {
                            Text(
                                text = result.date,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = heroTop)
            )
        },
        bottomBar = {
            if (result != null && !isFromHistory) {
                BottomActionBar(
                    isSaved = isSaved,
                    onSave = {
                        onSaveClick(result)
                        isSaved = true
                        scope.launch { snackbarHostState.showSnackbar("Save requested") }
                    },
                    onNewScan = onNewScan
                )
            }
        }
    ) { padding ->

        if (result == null) {
            EmptyResultState(
                onNewScan = onNewScan,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            val confidence = result.confidence.coerceIn(0f, 1f)
            val band = bandFor(confidence)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ---- Hero: diagnosis + confidence ring ------------------------
                ResultHero(
                    result = result,
                    healthy = healthy,
                    confidence = confidence,
                    top = heroTop,
                    bottom = heroBottom
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ReadingCard(confidence = confidence, band = band)

                    RecommendationsCard(recommendations = result.recommendations)

                    ModelInfoCard()

                    Text(
                        text = "Scan ID: ${result.id}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isFromHistory) {
                        Spacer(Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}

// ---- Hero ---------------------------------------------------------------

@Composable
private fun ResultHero(
    result: ScanResult,
    healthy: Boolean,
    confidence: Float,
    top: Color,
    bottom: Color
) {
    // The ring fills once when the result opens
    var target by remember(result.id) { mutableStateOf(0f) }
    LaunchedEffect(result.id) { target = confidence }
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 900),
        label = "confidenceRing"
    )
    val confidenceText = String.format("%.1f%%", confidence * 100f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (healthy) "Result" else "Detected condition",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = result.diseaseName,
                        fontSize = 27.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    if (result.scientificName.isNotBlank() && result.scientificName != "Not applicable") {
                        Text(
                            text = result.scientificName,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (healthy) Icons.Default.CheckCircle else Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (healthy) "No disease detected"
                                else "General risk: ${result.severity}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Confidence ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .semantics { contentDescription = "Model confidence $confidenceText" }
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White.copy(alpha = 0.25f),
                        strokeWidth = 8.dp
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White,
                        strokeWidth = 8.dp,
                        trackColor = Color.Transparent
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = confidenceText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "confidence",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (healthy) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (healthy) "The model classified this leaf as healthy."
                    else "The model identified a possible leaf disease.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}

// ---- Cards --------------------------------------------------------------

/** Explains what the confidence score and the missing severity mean, in plain words. */
@Composable
private fun ReadingCard(confidence: Float, band: ConfidenceBand) {
    val isLow = band == ConfidenceBand.Low
    val accent = if (isLow) WarnAccent else GreenPrimary
    val percent = String.format("%.1f%%", confidence * 100f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isLow) WarnTint else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Reading this result",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            InfoRow(
                icon = if (isLow) Icons.Default.WarningAmber else Icons.Default.Insights,
                accent = accent,
                title = "${band.label} ($percent)",
                body = "This score is the model's estimate of the CNN-ANFIS." +
                        (band.advice?.let { " $it" } ?: "")
            )

            HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))

            InfoRow(
                icon = Icons.Default.Info,
                accent = TextSecondary,
                title = "Severity not assessed",
                body = "The current CNN–ANFIS model classifies corn leaf diseases but does not estimate disease severity."
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, accent: Color, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(
                body,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TaskAlt,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "What to do next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            Spacer(Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                recommendations.forEach { RecommendationLine(it) }
            }

            Spacer(Modifier.height(14.dp))

            Surface(color = PanelTint, shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "For disease confirmation and treatment, consult a qualified " +
                                "agricultural expert or your local agriculture office.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/** Splits "Label: detail" so the action type is bold and the detail sits under it. */
@Composable
private fun RecommendationLine(text: String) {
    val separator = text.indexOf(": ")
    val title = if (separator > 0) text.substring(0, separator) else null
    val detail = if (separator > 0) text.substring(separator + 2) else text

    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GreenPrimary,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (title != null) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
            Text(detail, fontSize = 13.sp, lineHeight = 19.sp, color = TextSecondary)
        }
    }
}

/** Collapsed by default so the technical details don't crowd the result. */
@Composable
private fun ModelInfoCard() {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "modelChevron")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClickLabel = if (expanded) "Hide model details" else "Show model details"
                    ) { expanded = !expanded }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenPrimary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Memory,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "How the model decided",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Text(
                        "MobileNetV2 and Generalized Bell ANFIS",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFEFEFEF))

                    ModelStep(
                        title = "1. CNN feature extraction",
                        body = "MobileNetV2 extracts eight learned numerical features from the corn leaf image."
                    )
                    ModelStep(
                        title = "2. ANFIS classification",
                        body = "Generalized Bell ANFIS processes the CNN features using trained fuzzy " +
                                "membership functions and rules to generate classification scores."
                    )

                    Text(
                        text = "Note: The model's learned numerical features are not automatically " +
                                "equivalent to visible disease symptoms.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelStep(title: String, body: String) {
    Column {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GreenPrimary)
        Text(
            body,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ---- Bottom bar & empty state ------------------------------------------------

/** Always visible so Save and New Scan never hide behind the scroll. */
@Composable
private fun BottomActionBar(
    isSaved: Boolean,
    onSave: () -> Unit,
    onNewScan: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = !isSaved,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.55f),
                    disabledContentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isSaved) "Saved" else "Save Result",
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onNewScan,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "New Scan", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EmptyResultState(onNewScan: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "No classification result",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Scan a corn leaf first and the result will appear here.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onNewScan,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text("Start new scan", fontWeight = FontWeight.SemiBold)
        }
    }
}