package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.PrimaryRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CrisisSenseMap(
    modifier: Modifier = Modifier,
    hospitalName: String = "King George Hospital (KGH)",
    isWeakConnectivity: Boolean = false,
    onConnectivityToggle: ((Boolean) -> Unit)? = null,
    onMapInteraction: () -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }

    // Pulsing animations for coordinates/markers
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val mapBgColor = if (isWeakConnectivity) Color(0xFF0C0C12) else Color(0xFF07070B)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(mapBgColor)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                    offset += pan
                    onMapInteraction()
                }
            }
    ) {
        // Draw map elements using Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f + offset.x
            val centerY = height / 2f + offset.y

            // Draw grid lines (cyberpunk tech map style)
            val gridSize = 60f * scale
            val gridColor = if (isWeakConnectivity) Color(0x06FFB300) else Color(0x0CFFFFFF)
            val strokeWidth = 1f * scale

            // Vertical grid lines
            var x = centerX % gridSize
            while (x < width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth)
                x += gridSize
            }
            // Horizontal grid lines
            var y = centerY % gridSize
            while (y < height) {
                drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth)
                y += gridSize
            }

            // Draw background features (water body / cached coastline boundary)
            if (!isWeakConnectivity) {
                // Water bay (representing Visakhapatnam Coastline)
                val waterPath = Path().apply {
                    moveTo(centerX + 300f * scale, centerY + 300f * scale)
                    quadraticTo(
                        centerX + 500f * scale, centerY + 150f * scale,
                        centerX + 600f * scale, centerY + 500f * scale
                    )
                    lineTo(width, height)
                    lineTo(centerX + 300f * scale, height)
                    close()
                }
                drawPath(waterPath, color = Color(0x1127E1C1))
            } else {
                // Draw a simple dashed cached coast boundary segment instead of high-fidelity water fill
                val waterPath = Path().apply {
                    moveTo(centerX + 300f * scale, centerY + 300f * scale)
                    quadraticTo(
                        centerX + 500f * scale, centerY + 150f * scale,
                        centerX + 600f * scale, centerY + 500f * scale
                    )
                }
                drawPath(
                    path = waterPath,
                    color = Color(0x22FFB300),
                    style = Stroke(
                        width = 1.5f * scale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f * scale, 6f * scale), 0f)
                    )
                )
            }

            // Draw roads network (simplified dotted/dashed if offline fallback)
            val roadColor = if (isWeakConnectivity) Color(0x08FFFFFF) else Color(0x12FFFFFF)
            val mainRoadColor = if (isWeakConnectivity) Color(0x15FFFFFF) else Color(0x22FFFFFF)
            val mainRoadEffect = if (isWeakConnectivity) PathEffect.dashPathEffect(floatArrayOf(10f * scale, 10f * scale), 0f) else null
            val roadEffect = if (isWeakConnectivity) PathEffect.dashPathEffect(floatArrayOf(6f * scale, 6f * scale), 0f) else null

            // Grid roads
            for (i in -4..4) {
                // Diagonal main arteries
                drawLine(
                    color = mainRoadColor,
                    start = Offset(centerX + i * 200f * scale, centerY - 600f * scale),
                    end = Offset(centerX + i * 200f * scale + 300f * scale, centerY + 600f * scale),
                    strokeWidth = 2.5f * scale,
                    pathEffect = mainRoadEffect
                )
                drawLine(
                    color = roadColor,
                    start = Offset(centerX - 800f * scale, centerY + i * 150f * scale),
                    end = Offset(centerX + 800f * scale, centerY + i * 150f * scale + 50f * scale),
                    strokeWidth = 1.2f * scale,
                    pathEffect = roadEffect
                )
            }

            // User location coordinates (simulated Duvvada, Visakhapatnam)
            val userPos = Offset(centerX - 100f * scale, centerY + 120f * scale)
            // Hospital position coordinates
            val hospitalPos = Offset(centerX + 180f * scale, centerY - 150f * scale)

            // Draw offline boundary limits box showing cached vector sectors
            if (isWeakConnectivity) {
                val minX = minOf(userPos.x, hospitalPos.x) - 80f * scale
                val maxX = maxOf(userPos.x, hospitalPos.x) + 80f * scale
                val minY = minOf(userPos.y, hospitalPos.y) - 80f * scale
                val maxY = maxOf(userPos.y, hospitalPos.y) + 80f * scale

                drawRect(
                    color = Color(0x05FFB300),
                    topLeft = Offset(minX, minY),
                    size = Size(maxX - minX, maxY - minY)
                )
                drawRect(
                    color = Color(0x33FFB300),
                    topLeft = Offset(minX, minY),
                    size = Size(maxX - minX, maxY - minY),
                    style = Stroke(
                        width = 1.5f * scale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f * scale, 8f * scale), 0f)
                    )
                )
            }

            // Draw connecting EMERGENCY ROUTE
            if (isWeakConnectivity) {
                // Flat, straight dashed orange/amber cached estimated path (no live telemetry coordinates)
                drawLine(
                    color = Color(0xFFFFB300),
                    start = userPos,
                    end = hospitalPos,
                    strokeWidth = 3.5f * scale,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f * scale, 8f * scale), 0f)
                )
            } else {
                val routePath = Path().apply {
                    moveTo(userPos.x, userPos.y)
                    // Curve road route layout
                    cubicTo(
                        userPos.x + 50f * scale, userPos.y - 40f * scale,
                        userPos.x + 120f * scale, userPos.y + 10f * scale,
                        userPos.x + 150f * scale, userPos.y - 60f * scale
                    )
                    lineTo(hospitalPos.x - 40f * scale, hospitalPos.y + 40f * scale)
                    quadraticTo(
                        hospitalPos.x - 20f * scale, hospitalPos.y + 20f * scale,
                        hospitalPos.x, hospitalPos.y
                    )
                }

                // Outer Glow of route
                drawPath(
                    path = routePath,
                    color = PrimaryRed.copy(alpha = 0.25f),
                    style = Stroke(width = 12f * scale, miter = 4f, pathEffect = PathEffect.cornerPathEffect(40f))
                )
                // Middle route line
                drawPath(
                    path = routePath,
                    color = PrimaryRed.copy(alpha = 0.6f),
                    style = Stroke(width = 6f * scale, pathEffect = PathEffect.cornerPathEffect(40f))
                )
                // Core laser line
                drawPath(
                    path = routePath,
                    color = PrimaryRed,
                    style = Stroke(width = 2.5f * scale, pathEffect = PathEffect.cornerPathEffect(40f))
                )
            }

            // DRAW MARKERS
            if (isWeakConnectivity) {
                // Static amber local memory markers
                drawCircle(
                    color = Color(0xFFFFB300).copy(alpha = 0.25f),
                    radius = 16f * scale,
                    center = userPos
                )
                drawCircle(
                    color = Color(0xFFFFB300),
                    radius = 7f * scale,
                    center = userPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f * scale,
                    center = userPos
                )

                // Static teal hospital marker
                drawCircle(
                    color = AccentTeal.copy(alpha = 0.25f),
                    radius = 20f * scale,
                    center = hospitalPos
                )
                drawCircle(
                    color = AccentTeal,
                    radius = 10f * scale,
                    center = hospitalPos
                )
            } else {
                // 1. User Position pulse
                drawCircle(
                    color = PrimaryRed.copy(alpha = pulseAlpha),
                    radius = pulseRadius * scale,
                    center = userPos
                )
                drawCircle(
                    color = PrimaryRed,
                    radius = 7f * scale,
                    center = userPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f * scale,
                    center = userPos
                )

                // 2. Hospital Marker Anchor
                drawCircle(
                    color = AccentTeal.copy(alpha = 0.2f),
                    radius = 24f * scale,
                    center = hospitalPos
                )
                drawCircle(
                    color = AccentTeal,
                    radius = 12f * scale,
                    center = hospitalPos
                )
            }
        }

        // Overlay Interactive Icons & UI Elements

        // Top Banner for Offline Mode
        if (isWeakConnectivity) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color(0xCCFFB300))
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.WifiOff,
                        contentDescription = "Offline Cache",
                        tint = Color.Black,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RUNNING ON LOCAL OFFLINE VECTOR CACHE",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Current User Marker Badge (floating text)
        val userBadgeBgColor = if (isWeakConnectivity) Color(0xF216120F) else Color(0xE60D0D14)
        val userBadgeBorderColor = if (isWeakConnectivity) Color(0x4DFFB300) else Color.Transparent
        val userBadgeText = if (isWeakConnectivity) "You (Duvvada) • Cached" else "You (Duvvada)"
        val userIconColor = if (isWeakConnectivity) Color(0xFFFFB300) else PrimaryRed

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-100).dp + (offset.x / 2.5f).dp, y = 80.dp + (offset.y / 2.5f).dp)
                .background(userBadgeBgColor, RoundedCornerShape(8.dp))
                .border(1.dp, userBadgeBorderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "My Position",
                    tint = userIconColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = userBadgeText,
                    color = Color.White,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Hospital Marker Badge (floating text)
        val hospitalBadgeBgColor = if (isWeakConnectivity) Color(0xF20B1516) else Color(0xE60D0D14)
        val hospitalBadgeBorderColor = if (isWeakConnectivity) Color(0x4D27E1C1) else Color.Transparent
        val hospitalBadgeText = if (isWeakConnectivity) "${hospitalName.substringBefore(",")} • Cached" else hospitalName.substringBefore(",")

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 180.dp + (offset.x / 2.5f).dp, y = (-190).dp + (offset.y / 2.5f).dp)
                .background(hospitalBadgeBgColor, RoundedCornerShape(8.dp))
                .border(1.dp, hospitalBadgeBorderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hospitalName.contains("Fire", ignoreCase = true)) {
                        Icons.Filled.LocalFireDepartment
                    } else {
                        Icons.Filled.LocalHospital
                    },
                    contentDescription = "Service Location",
                    tint = AccentTeal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = hospitalBadgeText,
                    color = Color.White,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Zoom Controls & Map Stats Overlay (Bottom Left)
        val bottomLabelText = if (isWeakConnectivity) "⚠️ OFFLINE VECTOR FALLBACK MAP" else "GPS SECURE • HIGH CONFIDENCE"
        val bottomLabelColor = if (isWeakConnectivity) Color(0xFFFFB300) else AccentTeal
        val bottomLabelBg = if (isWeakConnectivity) Color(0xE614100D) else Color(0xCC07070B)
        val bottomLabelBorder = if (isWeakConnectivity) Color(0x33FFB300) else Color.Transparent

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(bottomLabelBg, RoundedCornerShape(12.dp))
                .border(1.dp, bottomLabelBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = bottomLabelText,
                color = bottomLabelColor,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Zoom Controls & Offline Toggler (Bottom Right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(Color(0xCC07070B), RoundedCornerShape(12.dp))
                .padding(2.dp)
        ) {
            IconButton(
                onClick = { scale = (scale + 0.2f).coerceAtMost(3.0f) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Zoom In",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = { scale = (scale - 0.2f).coerceAtLeast(0.5f) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Zoom Out",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (onConnectivityToggle != null) {
                IconButton(
                    onClick = { onConnectivityToggle(!isWeakConnectivity) },
                    modifier = Modifier.size(36.dp).testTag("map_connectivity_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Filled.WifiOff,
                        contentDescription = "Toggle Offline Fallback",
                        tint = if (isWeakConnectivity) Color(0xFFFFB300) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
