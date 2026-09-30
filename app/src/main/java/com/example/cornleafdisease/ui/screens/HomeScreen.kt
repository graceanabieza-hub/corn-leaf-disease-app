package com.example.cornleafdisease.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornleafdisease.R
import com.example.cornleafdisease.ui.theme.*

import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onCaptureClick = {}
    )
}

@Composable
fun HomeScreen(
    onCaptureClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // 1) Background cornfield image - Full Screen
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2) Soft gradient scrim — darker at top & bottom for text contrast,
        //    lighter in the middle so the cornfield photo still shows through
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = 0.55f),
                            0.35f to Color.White.copy(alpha = 0.35f),
                            0.75f to Color.White.copy(alpha = 0.55f),
                            1.0f to Color.White.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3) Logo — circular corn icon with a soft glow ring
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(elevation = 12.dp, shape = CircleShape, clip = false)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Corn Leaf Disease Classification logo",
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Corn Leaf Disease\nClassification",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GreenPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(Modifier.height(6.dp))

            // Small pill badge instead of plain text — reads as a polished tagline
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(GreenPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CNN-ANFIS · Hybrid Approach",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GreenPrimary
                )
            }

            Spacer(Modifier.height(26.dp))

            // Welcome message on a soft glass card for better legibility
            // over the busy background photo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.55f))
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome! \uD83C\uDF3D",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Detect and classify corn leaf diseases\nusing deep learning and a fuzzy\ninference system.",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Capture Image Card — richer visual treatment with a gradient
            // background, icon badge and trailing chevron for affordance
            HomeCard(
                title = "Capture / Upload Image",
                subtitle = "Take a photo or pick one from your gallery",
                icon = Icons.Default.CameraAlt,
                modifier = Modifier.fillMaxWidth(),
                onClick = onCaptureClick
            )

            Spacer(Modifier.height(28.dp))

            // "What can it detect?" section — 2x2 grid of reference leaf photos
            Text(
                text = "What can it detect?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiseaseChip(
                    imageRes = R.drawable.healthy,
                    label = "Healthy",
                    accent = GreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                DiseaseChip(
                    imageRes = R.drawable.rust,
                    label = "Common Rust",
                    accent = Color(0xFFB0562A),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiseaseChip(
                    imageRes = R.drawable.southern,
                    label = "Southern Leaf Spot",
                    accent = Color(0xFF8C6A2F),
                    modifier = Modifier.weight(1f)
                )
                DiseaseChip(
                    imageRes = R.drawable.northern,
                    label = "Northern Leaf Blight",
                    accent = Color(0xFF6B7A3A),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Small reference card used in the "What can it detect?" grid.
 * Add the following drawables to res/drawable (any real leaf photos you have):
 *   - leaf_healthy
 *   - leaf_common_rust
 *   - leaf_southern_leaf_spot
 *   - leaf_northern_leaf_blight
 */
@Composable
fun DiseaseChip(
    imageRes: Int,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun HomeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp), clip = false)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(GreenPrimary.copy(alpha = 0.06f), Color.White)
                    )
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}