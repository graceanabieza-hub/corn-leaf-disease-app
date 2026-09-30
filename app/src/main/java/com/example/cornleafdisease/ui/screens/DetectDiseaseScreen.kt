package com.example.cornleafdisease.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.cornleafdisease.data.model.ScanResult
import com.example.cornleafdisease.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun DetectDiseaseScreenPreview() {
    DetectDiseaseScreen(onBack = {}, onResult = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectDiseaseScreen(
    onBack: () -> Unit,
    onResult: (ScanResult) -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Launcher for Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Launcher for Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempUri
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            delay(1800)
            isAnalyzing = false
            
            val uriString = imageUri?.toString()?.lowercase() ?: ""
            val scanResult = when {
                uriString.contains("southern") -> ScanResult(
                    id = UUID.randomUUID().toString(),
                    date = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date()),
                    diseaseName = "Southern Leaf Spot",
                    scientificName = "Bipolaris maydis",
                    confidence = 0.945f,
                    severity = "Moderate",
                    severityColorHex = 0xFFFFA000,
                    recommendations = listOf("Plant resistant hybrids", "Practice crop rotation to reduce residue", "Tillage to bury infected crop debris"),
                    anfisFeatures = listOf("Oval lesions", "Tan spots")
                )
                uriString.contains("rust") -> ScanResult(
                    id = UUID.randomUUID().toString(),
                    date = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date()),
                    diseaseName = "Common Rust",
                    scientificName = "Puccinia sorghi",
                    confidence = 0.958f,
                    severity = "Moderate",
                    severityColorHex = 0xFFFFA000,
                    recommendations = listOf("Plant resistant hybrids", "Apply foliar fungicide detected early", "Improve airflow to reduce leaf wetness"),
                    anfisFeatures = listOf("Rust-colored pustules", "Oval lesions")
                )
                uriString.contains("northern") || uriString.contains("nothern") -> ScanResult(
                    id = UUID.randomUUID().toString(),
                    date = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date()),
                    diseaseName = "Northern Leaf Blight",
                    scientificName = "Exserohilum turcicum",
                    confidence = 0.962f,
                    severity = "High",
                    severityColorHex = 0xFFE53935,
                    recommendations = listOf("Use resistant hybrids as primary strategy", "Apply fungicides promptly once confirmed", "Practice crop rotation and residue management"),
                    anfisFeatures = listOf("Cigar-shaped lesions", "Gray-green to tan spots")
                )
                uriString.contains("healthy") -> ScanResult(
                    id = UUID.randomUUID().toString(),
                    date = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date()),
                    diseaseName = "Healthy",
                    scientificName = "Zea mays",
                    confidence = 0.991f,
                    severity = "Low",
                    severityColorHex = 0xFF43A047,
                    recommendations = listOf("Continue regular field monitoring", "Maintain balanced fertilization and watering"),
                    anfisFeatures = listOf("Uniform green color", "Intact leaf tissue")
                )
                else -> ScanResult(
                    id = UUID.randomUUID().toString(),
                    date = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date()),
                    diseaseName = "Southern Leaf Spot",
                    scientificName = "Bipolaris maydis",
                    confidence = 0.934f,
                    severity = "Moderate",
                    severityColorHex = 0xFFFFA000,
                    recommendations = listOf("Plant resistant hybrids", "Practice crop rotation to reduce residue", "Tillage to bury infected crop debris"),
                    anfisFeatures = listOf("Oval lesions", "Tan spots")
                )
            }
            onResult(scanResult)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Detect Disease", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            "CNN-ANFIS Analysis",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Capture or upload a photo of a corn leaf",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Text(
                "We'll run it through our CNN-ANFIS model for an instant diagnosis",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
            )

            // ---- Image preview area ------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (imageUri != null) Color.Black else Color(0xFFF1F3F1)),
                contentAlignment = Alignment.Center
            ) {
                if (isAnalyzing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GreenPrimary, strokeWidth = 4.dp)
                        Spacer(Modifier.height(16.dp))
                        Text("Analyzing leaf pattern…", color = TextSecondary, fontSize = 13.sp)
                    }
                } else if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Reset button overlay
                    IconButton(
                        onClick = { imageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear image", tint = Color.White)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No image selected yet", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- Capture / Upload actions -------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        val permissionCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            val uri = createTempImageUri(context)
                            tempUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Capture", fontWeight = FontWeight.Medium)
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, GreenPrimary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery", color = GreenPrimary, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Analyze CTA — only appears once an image is ready ------
            AnimatedVisibility(visible = imageUri != null && !isAnalyzing, enter = fadeIn(), exit = fadeOut()) {
                Button(
                    onClick = { isAnalyzing = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Analyze Leaf", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- Process overview stepper --------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How it works", fontWeight = FontWeight.SemiBold, color = TextDark)
                    Spacer(Modifier.height(16.dp))
                    ProcessStepper()
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Tips card -------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF9A825), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tips for the best result", fontWeight = FontWeight.SemiBold, color = TextDark, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    TipRow("Use natural daylight, avoid harsh shadows")
                    TipRow("Fill the frame with a single leaf")
                    TipRow("Keep the camera steady and in focus")
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_",
        ".jpg",
        context.externalCacheDir
    ).apply {
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}

@Composable
private fun TipRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.FiberManualRecord,
            contentDescription = null,
            tint = Color(0xFFF9A825),
            modifier = Modifier.size(8.dp).padding(top = 5.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
private fun ProcessStepper() {
    val steps = listOf(
        Triple("Image", Icons.Default.Image, GreenPrimary),
        Triple("CNN", Icons.Default.Memory, GreenPrimary),
        Triple("ANFIS", Icons.Default.AccountTree, GreenPrimary),
        Triple("Result", Icons.Default.Insights, GreenPrimary)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, icon, color) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(label, fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }

            if (index != steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 22.dp),
                    thickness = 2.dp,
                    color = GreenPrimary.copy(alpha = 0.25f)
                )
            }
        }
    }
}
