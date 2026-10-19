
package com.example.cornleafdisease.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.cornleafdisease.data.local.ScanImageStorage
import com.example.cornleafdisease.CornLeafClassifier
import com.example.cornleafdisease.data.model.ScanResult
import com.example.cornleafdisease.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val DetectSurface = Color(0xFFF6F8F5)
private val DetectGreenSoft = Color(0xFFE4F0E5)
private val DetectOutline = Color(0xFFE1E7E0)
private val DetectAmberSoft = Color(0xFFFFF6DC)
private val DetectAmber = Color(0xFFB77900)

// Temporary confidence cutoff for testing; validate on labeled images before final use.
private const val MIN_CLASSIFICATION_CONFIDENCE = 0.40f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectDiseaseScreen(
    onBack: () -> Unit,
    onResult: (ScanResult) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var isFromCamera by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisError by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            isFromCamera = false
            analysisError = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempUri
            isFromCamera = true
            analysisError = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createTempImageUri(context)
                tempUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Log.e("DetectDiseaseScreen", "Camera error", e)
                analysisError = "Unable to open camera."
            }
        } else {
            analysisError = "Camera permission was denied."
        }
    }

    fun openCamera() {
        val permission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            try {
                val uri = createTempImageUri(context)
                tempUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Log.e("DetectDiseaseScreen", "Camera error", e)
                analysisError = "Unable to open camera."
            }
        } else {
            permissionLauncher.launch(
                android.Manifest.permission.CAMERA
            )
        }
    }

    fun analyzeLeaf() {
        val selectedUri = imageUri ?: return
        if (isAnalyzing) return

        scope.launch {
            isAnalyzing = true
            analysisError = null

            try {
                val prediction = withContext(Dispatchers.Default) {
                    val bitmap = decodeOrientedBitmap(context, selectedUri)

                    CornLeafClassifier(context).use { classifier ->
                        classifier.classify(bitmap)
                    }
                }

                Log.d(
                    "CORN_CLASSIFIER",
                    "Predicted disease: ${prediction.disease}"
                )
                Log.d(
                    "CORN_CLASSIFIER",
                    "Confidence: ${prediction.confidence}"
                )

                val savedImagePath = ScanImageStorage.saveImage(context, selectedUri)
                val scanResult = createScanResult(prediction, savedImagePath)

                isAnalyzing = false
                onResult(scanResult)

            } catch (e: Exception) {
                Log.e(
                    "CORN_CLASSIFIER",
                    "Classification failed",
                    e
                )

                analysisError =
                    e.message ?: "Unable to analyze the leaf."

                isAnalyzing = false
            }
        }
    }

    Scaffold(
        containerColor = GreenPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Detect Disease",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "CNN-ANFIS Analysis",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary
                )
            )
        }
    ) { padding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = DetectSurface,
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 20.dp,
                        vertical = 24.dp
                    )
            ) {
                Text(
                    "Scan a corn leaf",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Text(
                    "Take a photo or pick one from your gallery. " +
                            "The CNN-ANFIS model will classify the leaf.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(
                        top = 4.dp,
                        bottom = 20.dp
                    )
                )

                val showDashed =
                    imageUri == null && !isAnalyzing

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (imageUri != null) {
                                Color.Black
                            } else {
                                Color.White
                            }
                        )
                        .let { base ->
                            if (showDashed) {
                                base.drawBehind {
                                    drawRoundRect(
                                        color = GreenPrimary.copy(
                                            alpha = 0.45f
                                        ),
                                        cornerRadius = CornerRadius(
                                            24.dp.toPx()
                                        ),
                                        style = Stroke(
                                            width = 2.dp.toPx(),
                                            pathEffect =
                                                PathEffect.dashPathEffect(
                                                    floatArrayOf(
                                                        18.dp.toPx(),
                                                        12.dp.toPx()
                                                    )
                                                )
                                        )
                                    )
                                }
                            } else {
                                base
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected corn leaf",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (isAnalyzing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Black.copy(
                                        alpha = if (imageUri != null) {
                                            0.6f
                                        } else {
                                            0f
                                        }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = if (imageUri != null) {
                                        Color.White
                                    } else {
                                        GreenPrimary
                                    },
                                    strokeWidth = 4.dp
                                )

                                Spacer(Modifier.height(16.dp))

                                Text(
                                    "Analyzing leaf pattern…",
                                    color = if (imageUri != null) {
                                        Color.White
                                    } else {
                                        TextSecondary
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (imageUri != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(
                                alpha = 0.55f
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(Modifier.width(6.dp))

                                Text(
                                    "Photo ready",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                imageUri = null
                                analysisError = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(36.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.55f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear image",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            modifier = Modifier.padding(
                                horizontal = 24.dp
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(DetectGreenSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = GreenPrimary
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            Text(
                                "No photo yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )

                            Text(
                                "Add a clear photo of one corn leaf",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { openCamera() },
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Capture",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = DetectGreenSoft
                            )
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Gallery",
                            color = GreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                AnimatedVisibility(
                    visible = imageUri != null && !isAnalyzing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Button(
                        onClick = { analyzeLeaf() },
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .height(58.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B5E20)
                        ),
                        shape = RoundedCornerShape(29.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            "Analyze leaf",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                if (analysisError != null) {
                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = analysisError ?: "",
                            modifier = Modifier.padding(14.dp),
                            color = Color(0xFFB71C1C),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        DetectOutline
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            "How it works",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )

                        Text(
                            "Your photo passes through CNN feature " +
                                    "extraction and ANFIS classification.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(Modifier.height(18.dp))

                        ProcessStepper()
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DetectAmberSoft
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = DetectAmber,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                "Tips for the best result",
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        TipRow(
                            "Use natural daylight, avoid harsh shadows"
                        )
                        TipRow(
                            "Fill the frame with a single leaf"
                        )
                        TipRow(
                            "Keep the camera steady and in focus"
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// Shared orientation-aware decoder for camera and gallery URI images.
// EXIF metadata is read from a fresh stream because decoding consumes its stream.
private fun decodeOrientedBitmap(context: Context, uri: Uri): Bitmap {
    val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input)
    } ?: error("Unable to decode selected image.")
    val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
        ExifInterface(input).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
    }
    if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED) return bitmap
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
        if (it !== bitmap) bitmap.recycle()
    }
}

// ============================================================
// CONVERT ACTUAL MODEL PREDICTION TO SCAN RESULT
// ============================================================

private fun createScanResult(
    prediction: CornLeafClassifier.ClassificationResult,
    imageUrl: String? = null
): ScanResult {

    val confidence = prediction.confidence.toFloat().coerceIn(0f, 1f)
    // Apply the same threshold to camera and gallery results.
    // Never convert a low-confidence prediction into "Healthy Leaves".
    val disease = when {
        prediction.disease == "Non-Corn Leaf" &&
                confidence >= MIN_CLASSIFICATION_CONFIDENCE -> "Corn Leaf Not Detected"
        confidence < MIN_CLASSIFICATION_CONFIDENCE -> "Uncertain Classification"
        else -> prediction.disease
    }
    Log.d(
        "CORN_CLASSIFIER",
        "Raw prediction=${prediction.disease}, confidence=$confidence, " +
                "threshold=$MIN_CLASSIFICATION_CONFIDENCE, displayed=$disease"
    )

    val scientificName = when (disease) {
        "Common Rust" -> "Puccinia sorghi"
        "Northern Leaf Blight" -> "Helminthosporium turcicum"
        "Southern Leaf Blight" -> "Helminthosporium maydis"
        "Healthy Leaves" -> "Zea mays"
        else -> "Unknown"
    }

    val recommendations = when (disease) {
        "Common Rust" -> listOf(
            "Cultural Control: Plant resistant hybrids; rotate with non-host crops (legumes) for 1+ cycle.\nBisaya: Pagtanom og mga binhi nga dili dali sakiton sa taya; puli-pulihi og laing tanom (sama sa mani o munggos) ang uma.",
            "Field Sanitation & Fertilizer: Manage infected corn stalks/leaves post-harvest; apply fertilizer based on soil conditions and avoid excessive nitrogen.\nBisaya: Hipusa ug limpyohi ang mga dahon o punoan sa mais human ganiha; i-apply ang abono base sa kahimtang sa yuta ug likayi ang sobrang nitroheno.",
            "Biological Control: Apply Trichoderma to suppress plant pathogens through competition, antibiosis, and other mechanisms.\nBisaya: Paggamit og Trichoderma aron mapugngan ang pagkatap sa mga kagaw sa tanom.",
            "Chemical Control: Apply fungicides (chlorothalonil, azoxystrobin & pyraclostrobin) when secondary inoculum is still low (few pustules per leaf).\nBisaya: Paggamit og pambomba (fungicide) kung gamay pa kaayo ang samad o taya sa dahon.",
            "Field Monitoring: Inspect leaves regularly from vegetative growth through tasseling.\nBisaya: Susiha kanunay ang mga dahon gikan sa pagtubo hangtod sa pagpagawas og bulak (tasseling)."
        )

        "Northern Leaf Blight" -> listOf(
            "Cultural Control: Plant corn hybrids with documented resistance; rotate with non-host crops (legumes) for at least one cropping cycle.\nBisaya: Pagtanom og mais nga naay lig-on nga panalipod batok sa sakit; puli-pulihi og laing tanom ang uma sulod sa usa ka season.",
            "Field Sanitation & Fertilizer: Properly manage infected corn debris post-harvest; avoid excessive nitrogen application.\nBisaya: Limpyohi ang nahabiling tanom sa uma human sa anihan; likayi ang sobra nga abono nga nitroheno.",
            "Biological Control: Apply Trichoderma to suppress plant pathogens through competition and antibiosis.\nBisaya: Butangi og Trichoderma aron mabatukan ang mga badlungong kagaw sa yuta ug tanom.",
            "Chemical Control: Apply fungicides (Azoxystrobin + Difenoconazole, Propiconazole) to reduce yield loss and improve harvestability.\nBisaya: Pag-spray og fungicide aron maminusan ang kadaot sa ani ug maprotektahan ang pananom.",
            "Field Monitoring: Inspect leaves regularly from vegetative growth through tasseling.\nBisaya: Kanunay nga i-check ang mga dahon samtang kini nagtubo ug nag-usbong."
        )

        "Southern Leaf Blight" -> listOf(
            "Cultural Control: Plant corn hybrids with documented resistance; rotate with non-host crops (legumes) for at least one cropping cycle.\nBisaya: Pagtanom og resistant nga klase sa mais; magpuli-puli og laing tanom sa uma.",
            "Field Sanitation & Fertilizer: Properly manage infected corn debris post-harvest; avoid excessive nitrogen application.\nBisaya: Hipusa ang mga dunot o nasakit nga dahon ug punoan; likayi ang sobra nga abono.",
            "Biological Control: Apply Trichoderma to suppress plant pathogens through competition and antibiosis.\nBisaya: Paggamit og Trichoderma aron mapatay ang mga kagaw sa tanom sa natural nga paagi.",
            "Chemical Control: Apply fungicides (Azoxystrobin + Difenoconazole, Propiconazole) early to reduce yield loss.\nBisaya: Pag-spray og angay nga fungicide sayo pa aron dili maalkanse ang ani.",
            "Field Monitoring: Inspect leaves regularly from vegetative growth through tasseling.\nBisaya: Susiha ang mga dahon kada semana gikan sa pagtubo hangtod sa pamulak."
        )

        "Healthy Leaves" -> listOf(
            "Regular Field Monitoring: Inspect leaves regularly from vegetative growth through tasseling.\nBisaya: Susiha gihapon ang mga dahon matag karon ug unya samtang nagtubo ang mais.",
            "Balanced Fertilization: Apply fertilizer based on soil conditions and crop requirements; avoid excessive nitrogen.\nBisaya: Hatagi og saktong abono ang yuta base sa panginahanglan sa tanom; ayaw palabihi ang nitroheno.",
            "Proper Planting Schedule: Avoid prolonged periods of leaf wetness where possible.\nBisaya: Pagtanom sa saktong panahon ug likayi ang basa kaayo nga palibot sa dahon."
        )

        "Uncertain Classification" -> listOf(
            "Low Confidence: The model could not classify this image reliably. Do not treat its predicted disease as confirmed.\nBisaya: Ubos ang confidence sa model. Ayaw sa isipon nga kumpirmado ang sakit sa dahon.",
            "Retake Photo: Use natural daylight, focus on a single corn leaf, and avoid blur and heavy shadows.\nBisaya: Pagkuha og bag-ong klarong hulagway sa usa ka dahon sa mais gamit ang natural nga kahayag.",
            "Seek Verification: If symptoms remain unclear, consult an agricultural technician.\nBisaya: Kung dili gihapon klaro ang sakit, pakonsulta sa agricultural technician."
        )

        else -> listOf(
            "Unclear Image Scan: Unable to detect a clear corn leaf disease from the photo.\nBisaya: Dili maklaro ang sakit sa dahon gikan sa gipasa nga hulagway.",
            "Lighting & Focus: Retake the photo in bright, natural daylight with the corn leaf in sharp focus.\nBisaya: Pictura og usab ubos sa hayag nga adlaw ug siguruha nga klaro ug dili libog ang dahon.",
            "Framing: Center a single corn leaf in the frame and avoid dark shadows or blurry backgrounds.\nBisaya: I-sentro ang usa ka dahon sa mais sa screen ug likayi ang ngitngit o blurry nga background."
        )
    }

    val isUndetermined = disease == "Corn Leaf Not Detected" ||
            disease == "Uncertain Classification"
    val severityText = if (isUndetermined) "N/A" else "Not assessed"
    val severityHex = if (isUndetermined) 0xFF9E9E9E else 0xFF757575

    return ScanResult(
        id = UUID.randomUUID().toString(),
        date = SimpleDateFormat(
            "MMM dd, yyyy • hh:mm a",
            Locale.getDefault()
        ).format(Date()),
        diseaseName = disease,
        scientificName = scientificName,
        confidence = confidence,
        severity = severityText,
        severityColorHex = severityHex,
        imageUrl = imageUrl,
        recommendations = recommendations,
        anfisFeatures = listOf(
            "MobileNetV2 CNN feature extraction",
            "Generalized Bell ANFIS classification"
        )
    )
}

// ============================================================
// CAMERA TEMPORARY IMAGE
// ============================================================

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "JPEG_${
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())
        }_",
        ".jpg",
        context.externalCacheDir ?: context.cacheDir
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}

// ============================================================
// TIPS ROW
// ============================================================

@Composable
private fun TipRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(
                    DetectAmber.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = DetectAmber,
                modifier = Modifier.size(12.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = text,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

// ============================================================
// PROCESS STEPPER
// ============================================================

@Composable
private fun ProcessStepper() {

    val steps = listOf(
        Triple(
            "Image",
            Icons.Default.Image,
            GreenPrimary
        ),
        Triple(
            "CNN",
            Icons.Default.Memory,
            GreenPrimary
        ),
        Triple(
            "ANFIS",
            Icons.Default.AccountTree,
            GreenPrimary
        ),
        Triple(
            "Result",
            Icons.Default.Insights,
            GreenPrimary
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, icon, color) ->

            val isLast = index == steps.lastIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLast) {
                                color
                            } else {
                                color.copy(alpha = 0.12f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isLast) {
                            Color.White
                        } else {
                            color
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isLast) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                    color = if (isLast) {
                        TextDark
                    } else {
                        TextSecondary
                    },
                    textAlign = TextAlign.Center
                )
            }

            if (!isLast) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = 4.dp,
                            vertical = 22.dp
                        ),
                    thickness = 2.dp,
                    color = GreenPrimary.copy(
                        alpha = 0.25f
                    )
                )
            }
        }
    }
}
