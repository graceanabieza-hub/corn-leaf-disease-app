package com.example.cornleafdisease.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornleafdisease.R
import com.example.cornleafdisease.ui.theme.*

@Preview(showBackground = true)
@Composable
fun DiseaseInfoScreenPreview() {
    DiseaseInfoScreen(onBack = {})
}

// ---- Data ---------------------------------------------------------------

data class DiseaseInfo(
    val name: String,
    val scientificName: String,
    val shortDescription: String,
    val severity: String,
    val severityColor: Color,
    val icon: ImageVector,
    val symptoms: List<String>,
    val causes: String,
    val management: List<String>,
    val sourceUrl: String,
    @DrawableRes val imageRes: Int? = null
)

private fun diseaseCatalog(): List<DiseaseInfo> = listOf(
    DiseaseInfo(
        name = "Healthy",
        scientificName = "Zea mays",
        shortDescription = "No visible signs of disease — leaf tissue is uniformly green and intact.",
        severity = "Healthy",
        severityColor = SeverityLow,
        icon = Icons.Default.CheckCircle,
        symptoms = listOf(
            "Uniform green color across the leaf",
            "No lesions, spots, or discoloration",
            "Normal leaf shape and texture"
        ),
        causes = "N/A — indicating optimal plant health.",
        management = listOf(
            "Regular field monitoring: Inspect leaves from vegetative growth through tasseling",
            "Balanced fertilization: Apply based on soil conditions; avoid excessive nitrogen",
            "Planting schedule: Avoid prolonged periods of leaf wetness"
        ),
        sourceUrl = "https://extension.umn.edu",
        imageRes = R.drawable.healthy
    ),
    DiseaseInfo(
        name = "Common Rust",
        scientificName = "Puccinia sorghi",
        shortDescription = "Small, reddish-brown circular to elongated pustules on both leaf surfaces.",
        severity = "Moderate",
        severityColor = SeverityModerate,
        icon = Icons.Default.Grain,
        symptoms = listOf(
            "Cinnamon-brown, powdery pustules on upper and lower leaf surfaces",
            "Pustules may turn dark brown/black as the leaf matures",
            "Severe cases cause leaves to yellow and die prematurely"
        ),
        causes = "Caused by the fungus Puccinia sorghi. Favored by cool temperatures (60–77°F) and high humidity.",
        management = listOf(
            "Cultural Control: Plant resistant hybrids; rotate corn with non-host crops (legumes) for 1+ cropping cycle",
            "Field Sanitation: Manage infected corn stalks/leaves post-harvest; avoid excessive nitrogen application",
            "Biological Control: Apply Trichoderma to suppress plant pathogens through competition and antibiosis",
            "Chemical Control: Apply fungicides (chlorothalonil, azoxystrobin & pyraclostrobin) when secondary inoculum is still low",
            "Regular Monitoring: Inspect leaves regularly from vegetative growth through tasseling"
        ),
        sourceUrl = "https://cropprotectionnetwork.org/encyclopedia/common-rust-of-corn",
        imageRes = R.drawable.rust
    ),
    DiseaseInfo(
        name = "Southern Leaf Blight",
        scientificName = "Helminthosporium maydis (Bipolaris maydis)",
        shortDescription = "Small, oval or elliptical tan-to-brown lesions on leaves, sheaths, and husks.",
        severity = "Moderate",
        severityColor = SeverityModerate,
        icon = Icons.Default.BlurOn,
        symptoms = listOf(
            "Small, oval or elliptical tan-to-brown lesions on leaves",
            "Lesions typically smaller and more uniform than northern leaf blight",
            "Can affect stalks, husks, and ear shanks in severe infections"
        ),
        causes = "Caused by the fungus Helminthosporium maydis (Bipolaris maydis). Thrives in warm, wet conditions (70–90°F).",
        management = listOf(
            "Cultural Control: Plant resistant hybrids; rotate corn with non-host crops (legumes) for 1+ cropping cycle",
            "Field Sanitation: Manage infected corn stalks/leaves post-harvest; avoid excessive nitrogen application",
            "Biological Control: Apply Trichoderma to suppress plant pathogens through competition and antibiosis",
            "Chemical Control: Apply fungicides (Azoxystrobin + Difenoconazole, Propiconazole) to reduce yield loss",
            "Regular Monitoring: Inspect leaves regularly from vegetative growth through tasseling"
        ),
        sourceUrl = "https://cropprotectionnetwork.org/encyclopedia/southern-corn-leaf-blight",
        imageRes = R.drawable.southern
    ),
    DiseaseInfo(
        name = "Northern Leaf Blight",
        scientificName = "Helminthosporium turcicum (Exserohilum turcicum)",
        shortDescription = "Long, cigar-shaped gray-green to tan lesions that quickly blight leaves.",
        severity = "High",
        severityColor = SeverityHigh,
        icon = Icons.Default.WarningAmber,
        symptoms = listOf(
            "Large, elongated cigar-shaped lesions (1–6 inches long)",
            "Lesions gray-green at first, turning tan/brown with age",
            "Significant leaf area loss and yield reduction"
        ),
        causes = "Caused by the fungus Helminthosporium turcicum (Exserohilum turcicum). Favored by moderate temperatures and dew.",
        management = listOf(
            "Cultural Control: Plant resistant hybrids; rotate corn with non-host crops (legumes) for 1+ cropping cycle",
            "Field Sanitation: Manage infected corn stalks/leaves post-harvest; avoid excessive nitrogen application",
            "Biological Control: Apply Trichoderma to suppress plant pathogens through competition and antibiosis",
            "Chemical Control: Apply fungicides (Azoxystrobin + Difenoconazole, Propiconazole) to reduce yield loss",
            "Regular Monitoring: Inspect leaves regularly from vegetative growth through tasseling"
        ),
        sourceUrl = "https://extension.umn.edu/corn-pest-management/northern-corn-leaf-blight",
        imageRes = R.drawable.northern
    )
)

// ---- Helpers ------------------------------------------------------------

private enum class DiseaseFilter(val label: String) {
    All("All"),
    Healthy("Healthy"),
    Moderate("Moderate"),
    High("High risk");

    fun matches(disease: DiseaseInfo): Boolean = when (this) {
        All -> true
        Healthy -> disease.severity == "Healthy"
        Moderate -> disease.severity == "Moderate"
        High -> disease.severity == "High"
    }
}

/** 0 = healthy, 1..3 = low..high. Drives the risk meter. */
private val DiseaseInfo.riskLevel: Int
    get() = when (severity) {
        "Healthy" -> 0
        "Low" -> 1
        "Moderate" -> 2
        else -> 3
    }

private val DiseaseInfo.sourceHost: String
    get() = sourceUrl.substringAfter("//").substringBefore("/")

private val PanelColor = Color(0xFFF3F6F3)
private val MeterOff = Color(0xFFE0E5E0)

// ---- Screen -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseInfoScreen(onBack: () -> Unit) {
    val diseases = remember { diseaseCatalog() }
    var selectedFilter by remember { mutableStateOf(DiseaseFilter.All) }
    var expandedName by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredDiseases = remember(searchQuery, selectedFilter) {
        diseases.filter { disease ->
            val query = searchQuery.trim()
            selectedFilter.matches(disease) && (
                    query.isEmpty() ||
                            disease.name.contains(query, ignoreCase = true) ||
                            disease.scientificName.contains(query, ignoreCase = true)
                    )
        }
    }

    Scaffold(
        containerColor = Color(0xFFF6F8F6),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Disease Information",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                        Text(
                            "${diseases.size} corn leaf conditions",
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
            // ---- Search ---------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(GreenPrimary)
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 18.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search by name or scientific name", color = TextSecondary, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = GreenPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = GreenPrimary,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            }

            // ---- Filters --------------------------------------------------
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 14.dp)
            ) {
                items(DiseaseFilter.values().toList()) { filter ->
                    val count = diseases.count { filter.matches(it) }
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text("${filter.label} · $count", fontSize = 13.sp) },
                        shape = RoundedCornerShape(20.dp),
                        leadingIcon = if (selectedFilter == filter) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            // ---- List -----------------------------------------------------
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp)
            ) {
                if (filteredDiseases.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            onReset = {
                                searchQuery = ""
                                selectedFilter = DiseaseFilter.All
                            }
                        )
                    }
                } else {
                    items(filteredDiseases, key = { it.name }) { disease ->
                        DiseaseCard(
                            disease = disease,
                            expanded = expandedName == disease.name,
                            onToggle = {
                                expandedName = if (expandedName == disease.name) null else disease.name
                            }
                        )
                    }
                }

                item(key = "footer") { DisclaimerCard() }
            }
        }
    }
}

// ---- Card ---------------------------------------------------------------

@Composable
private fun DiseaseCard(
    disease: DiseaseInfo,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevronRotation"
    )
    val uriHandler = LocalUriHandler.current
    // Start on the first tab each time the card is opened
    var tab by remember(expanded) { mutableStateOf(0) }
    val isHealthy = disease.severity == "Healthy"
    val tabLabels = listOf(if (isHealthy) "Signs" else "Symptoms", "Cause", "Management")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            if (expanded) disease.severityColor.copy(alpha = 0.5f) else Color(0xFFEAEEEA)
        )
    ) {
        Column {
            // ---- Photo header (always visible) -----------------------------
            Column(
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = if (expanded) "Collapse details" else "Expand details",
                    onClick = onToggle
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    if (disease.imageRes != null) {
                        Image(
                            painter = painterResource(id = disease.imageRes),
                            contentDescription = "Example of ${disease.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        0.0f to Color.Black.copy(alpha = 0.25f),
                                        0.5f to Color.Transparent
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(disease.severityColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                disease.icon,
                                contentDescription = null,
                                tint = disease.severityColor,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    // Severity badge on the photo
                    Surface(
                        color = Color.White.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(disease.severityColor)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                disease.severity,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                disease.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextDark
                            )
                            Text(
                                disease.scientificName,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontStyle = FontStyle.Italic,
                                maxLines = if (expanded) Int.MAX_VALUE else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .rotate(rotation)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        disease.shortDescription,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = TextSecondary,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (disease.riskLevel > 0) {
                        Spacer(Modifier.height(12.dp))
                        RiskMeter(level = disease.riskLevel, color = disease.severityColor)
                    }
                }
            }

            // ---- Details (expanded) -----------------------------------------
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFEFEFEF))

                    DetailTabs(labels = tabLabels, selected = tab, onSelect = { tab = it })

                    Surface(
                        color = PanelColor,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when (tab) {
                                0 -> disease.symptoms.forEach { SymptomLine(it, disease.severityColor) }
                                1 -> Text(
                                    disease.causes,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = TextDark
                                )
                                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    disease.management.forEach { ManagementLine(it) }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { uriHandler.openUri(disease.sourceUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Read full report · ${disease.sourceHost}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ---- Building blocks ----------------------------------------------------

/** Three-segment bar so risk can be read at a glance without relying on color alone. */
@Composable
private fun RiskMeter(level: Int, color: Color) {
    val label = when (level) {
        1 -> "Low risk"
        2 -> "Moderate risk"
        else -> "High risk"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) { index ->
                Box(
                    Modifier
                        .width(22.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (index < level) color else MeterOff)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
    }
}

/** Segmented tabs: keeps the expanded card short instead of one long scroll. */
@Composable
private fun DetailTabs(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEAEFEA))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) GreenPrimary else Color.Transparent)
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SymptomLine(text: String, color: Color) {
    Row {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, lineHeight = 19.sp, color = TextDark)
    }
}

/** Splits "Label: detail" entries so the action is bold and the explanation sits below it. */
@Composable
private fun ManagementLine(text: String) {
    val separator = text.indexOf(": ")
    val title = if (separator > 0) text.substring(0, separator) else null
    val detail = if (separator > 0) text.substring(separator + 2) else text

    Row {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GreenPrimary,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            if (title != null) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
            Text(detail, fontSize = 13.sp, lineHeight = 19.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun EmptyState(onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("No matching conditions", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextDark)
        Spacer(Modifier.height(4.dp))
        Text(
            "Try a different name or clear the filters.",
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onReset,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
            border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.5f))
        ) {
            Text("Clear search and filters")
        }
    }
}

@Composable
private fun DisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GreenLight)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Summarized from the linked agricultural extension sources. Catching disease early limits yield loss. " +
                        "Before applying any fungicide, confirm products and rates with your local agronomist.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = TextDark
            )
        }
    }
}