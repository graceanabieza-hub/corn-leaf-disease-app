package com.example.cornleafdisease.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornleafdisease.ui.theme.*

import androidx.compose.ui.tooling.preview.Preview

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
    val sourceUrl: String
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
            "Continue regular field monitoring",
            "Maintain balanced fertilization and watering",
            "Scout weekly during peak growing season"
        ),
        sourceUrl = "https://extension.umn.edu"
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
            "Plant resistant hybrids",
            "Apply foliar fungicide detected early",
            "Improve airflow to reduce leaf wetness"
        ),
        sourceUrl = "https://cropprotectionnetwork.org/encyclopedia/common-rust-of-corn"
    ),
    DiseaseInfo(
        name = "Southern Leaf Spot",
        scientificName = "Bipolaris maydis",
        shortDescription = "Small, oval or elliptical tan-to-brown lesions on leaves, sheaths, and husks.",
        severity = "Moderate",
        severityColor = SeverityModerate,
        icon = Icons.Default.BlurOn,
        symptoms = listOf(
            "Small, oval or elliptical tan-to-brown lesions on leaves",
            "Lesions typically smaller and more uniform than northern leaf blight",
            "Can affect stalks, husks, and ear shanks in severe infections"
        ),
        causes = "Caused by the fungus Bipolaris maydis. Thrives in warm, wet conditions (temperatures of 70–90°F).",
        management = listOf(
            "Plant resistant hybrids",
            "Practice crop rotation to reduce residue",
            "Tillage to bury infected crop debris"
        ),
        sourceUrl = "https://cropprotectionnetwork.org/encyclopedia/southern-corn-leaf-blight"
    ),
    DiseaseInfo(
        name = "Northern Leaf Blight",
        scientificName = "Exserohilum turcicum",
        shortDescription = "Long, cigar-shaped gray-green to tan lesions that quickly blight leaves.",
        severity = "High",
        severityColor = SeverityHigh,
        icon = Icons.Default.WarningAmber,
        symptoms = listOf(
            "Large, elongated cigar-shaped lesions (1–6 inches long)",
            "Lesions gray-green at first, turning tan/brown with age",
            "Significant leaf area loss and yield reduction"
        ),
        causes = "Caused by the fungus Exserohilum turcicum. Favored by moderate temperatures and heavy dew.",
        management = listOf(
            "Use resistant hybrids as primary strategy",
            "Apply fungicides promptly once confirmed",
            "Practice crop rotation and residue management"
        ),
        sourceUrl = "https://extension.umn.edu/corn-pest-management/northern-corn-leaf-blight"
    )
)

// ---- Screen ---------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseInfoScreen(onBack: () -> Unit) {
    val diseases = remember { diseaseCatalog() }
    var selectedFilter by remember { mutableStateOf("All") }
    var expandedName by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filters = listOf("All", "Healthy", "Diseased")
    
    val filteredDiseases = diseases.filter { disease ->
        val matchesFilter = when (selectedFilter) {
            "Healthy" -> disease.severity == "Healthy"
            "Diseased" -> disease.severity != "Healthy"
            else -> true
        }
        val matchesSearch = disease.name.contains(searchQuery, ignoreCase = true) || 
                           disease.scientificName.contains(searchQuery, ignoreCase = true)
        
        matchesFilter && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Information", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
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
            // ---- Search & Header -----------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenPrimary)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name or species...", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // ---- Filter chips ------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp, bottom = 6.dp),
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp)
            ) {
                items(filteredDiseases) { disease ->
                    DiseaseCard(
                        disease = disease,
                        expanded = expandedName == disease.name,
                        onToggle = {
                            expandedName = if (expandedName == disease.name) null else disease.name
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenLight)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Info, null, tint = GreenPrimary)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Early detection helps in managing disease effectively. Information sourced from reliable agricultural extension services.",
                                fontSize = 12.sp,
                                color = TextDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiseaseCard(
    disease: DiseaseInfo,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f, label = "chevronRotation")
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(disease.severityColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(disease.icon, null, tint = disease.severityColor, modifier = Modifier.size(26.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(disease.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Text(
                        disease.scientificName,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = disease.severityColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            disease.severity,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = disease.severityColor
                        )
                    }
                }

                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = Color.Gray,
                    modifier = Modifier.rotate(rotation)
                )
            }

            if (expanded) {
                Spacer(Modifier.height(14.dp))
                Text(disease.shortDescription, fontSize = 13.sp, color = TextDark)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFEFEFEF))
                Spacer(Modifier.height(12.dp))

                DiseaseSection(title = "Symptoms", icon = Icons.Default.Visibility) {
                    disease.symptoms.forEach { BulletLine(it, disease.severityColor) }
                }

                Spacer(Modifier.height(12.dp))

                DiseaseSection(title = "Cause", icon = Icons.Default.Science) {
                    Text(disease.causes, fontSize = 13.sp, color = TextSecondary)
                }

                Spacer(Modifier.height(12.dp))

                DiseaseSection(title = "Management", icon = Icons.Default.Handyman) {
                    disease.management.forEach { BulletLine(it, GreenPrimary) }
                }

                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { uriHandler.openUri(disease.sourceUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary.copy(alpha = 0.1f), contentColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Read Full Reliable Report", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DiseaseSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
        }
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun BulletLine(text: String, color: Color) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(
            Icons.Default.FiberManualRecord,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(6.dp).padding(top = 6.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = TextSecondary)
    }
}
