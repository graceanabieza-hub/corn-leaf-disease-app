package com.example.cornleafdisease.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cornleafdisease.R
import com.example.cornleafdisease.ui.theme.*

private val GuidePageColor = Color(0xFFFAFBFA)

@Preview(showBackground = true, heightDp = 780)
@Composable
fun GuideScreenPreview() {
    GuideScreen()
}

/**
 * Guide tab, laid out to fit on a single screen with no scrolling.
 * The detect grid takes whatever height is left, so it adapts to the device.
 */
@Composable
fun GuideScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GuidePageColor)
    ) {
        // Cornfield banner behind the header (same look as Home)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color(0xFF0F2A14).copy(alpha = 0.65f)
                            )
                        )
                    )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ---- Header ------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Guide",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "How MaisScan works and what it can detect",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // ---- Content sheet fills the rest of the screen ------------------
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = GuidePageColor,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 16.dp)
                ) {
                    HowItWorksSection()

                    Spacer(Modifier.height(20.dp))

                    DetectSection(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// =============================================================================
// How it works
// =============================================================================

@Composable
private fun HowItWorksSection() {
    Column {
        GuideSectionHeader(title = "How it works")
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StepTile(
                number = 1,
                icon = Icons.Default.CameraAlt,
                title = "Capture",
                text = "Take or pick a leaf photo",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StepTile(
                number = 2,
                icon = Icons.Default.AutoAwesome,
                title = "Analyze",
                text = "The CNN-ANFIS model checks it",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            StepTile(
                number = 3,
                icon = Icons.Default.TaskAlt,
                title = "Diagnose",
                text = "See the result and advice",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun StepTile(
    number: Int,
    icon: ImageVector,
    title: String,
    text: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFFF1F5F1))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$number. $title",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================================
// What it can detect (2x2 grid that stretches to fill the remaining height)
// =============================================================================

@Composable
private fun DetectSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        GuideSectionHeader(title = "What it can detect", trailing = "4 classes")
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetectCard(
                    imageRes = R.drawable.healthy,
                    name = "Healthy",
                    severity = "Healthy",
                    color = SeverityLow,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                DetectCard(
                    imageRes = R.drawable.rust,
                    name = "Common Rust",
                    severity = "Moderate",
                    color = SeverityModerate,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetectCard(
                    imageRes = R.drawable.southern,
                    name = "Southern Leaf Blight",
                    severity = "Moderate",
                    color = SeverityModerate,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                DetectCard(
                    imageRes = R.drawable.northern,
                    name = "Northern Leaf Blight",
                    severity = "High",
                    color = SeverityHigh,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun DetectCard(
    imageRes: Int,
    name: String,
    severity: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = shape, clip = false)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE8ECE8), shape)
    ) {
        // Image takes all the height the text doesn't need
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Example of $name",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                color = color.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = severity,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
            }
        }
    }
}

// =============================================================================
// Section header
// =============================================================================

@Composable
private fun GuideSectionHeader(title: String, trailing: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GreenPrimary
            )
        }
    }
}