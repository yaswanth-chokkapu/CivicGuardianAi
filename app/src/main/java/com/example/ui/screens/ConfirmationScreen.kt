package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CrisisSenseMap
import com.example.ui.theme.*
import com.example.viewmodel.EmergencyViewModel
import com.example.patent.*

@Composable
fun ConfirmationScreen(
    viewModel: EmergencyViewModel,
    onNavigateBack: () -> Unit
) {
    val countdownSeconds by viewModel.countdownSeconds.collectAsState()
    val callCancelled by viewModel.callCancelled.collectAsState()
    val selectedHospital by viewModel.selectedHospital.collectAsState()
    val selectedIncidentType by viewModel.selectedIncidentTypeForConfirmation.collectAsState()
    val isWeakConnectivity by viewModel.isWeakConnectivity.collectAsState()
    
    val activeIncident by viewModel.activeIncident.collectAsState()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsState()

    // Configuration depending on the type of emergency (Medical, Fire, Accident)
    val activeEta = when (selectedIncidentType) {
        "Fire" -> "3–5 mins"
        "Accident" -> "4 mins"
        else -> "~3:58"
    }

    val progressLabel = when (selectedIncidentType) {
        "Fire" -> "🚒 En route – Help is approaching"
        else -> "🚑 En route – Help is approaching"
    }

    val activeHospitalTitle = when (selectedIncidentType) {
        "Fire" -> "Nearest Fire Station"
        else -> "Nearest Hospital"
    }

    val activeHospitalName = when (selectedIncidentType) {
        "Fire" -> "Duvvada Fire Station"
        else -> "Visakha Hospital"
    }

    val activeHospitalLoc = when (selectedIncidentType) {
        "Fire" -> "Visakhapatnam"
        "Accident" -> "Duvvada"
        else -> "Duvvada, Visakhapatnam"
    }

    val activeHospitalDist = when (selectedIncidentType) {
        "Fire" -> "2.4 km • 3–5 mins"
        "Accident" -> "1.5 km • 4 mins"
        else -> "1.2 km • ~3:58 mins"
    }

    val bottomAlertMessage = when (selectedIncidentType) {
        "Fire" -> "Fire Department and Police have been alerted."
        "Accident" -> "Police, Ambulance, and Road Emergency Unit have been alerted."
        else -> "Police and Visakha Hospital have been alerted."
    }

    // Slide/Pulse animation for check icon
    val checkTransition = rememberInfiniteTransition(label = "check_icon")
    val checkGlowScale by checkTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "check_scale"
    )

    // Smooth scroll state
    val scrollState = rememberScrollState()

    // Handle back state if user cancels alert
    val emergencyTriggered by viewModel.emergencyTriggered.collectAsState()
    LaunchedEffect(emergencyTriggered) {
        if (!emergencyTriggered) {
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Header spacer
            Spacer(modifier = Modifier.height(24.dp))

            // Floating Adaptive Sync Banner
            syncStatusMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (msg.contains("Activated", ignoreCase = true)) Color(0x26FFB300) else Color(0x1A27E1C1))
                        .border(
                            1.dp,
                            if (msg.contains("Activated", ignoreCase = true)) Color(0xFFFFB300).copy(alpha = 0.5f) else AccentTeal.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                        .testTag("sync_status_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (msg.contains("Activated", ignoreCase = true)) Icons.Filled.WifiOff else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (msg.contains("Activated", ignoreCase = true)) Color(0xFFFFB300) else AccentTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Large Green Check Icon & Help Confirmation
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(SuccessGreenBg)
                        .border(1.5.dp, SuccessGreen.copy(alpha = checkGlowScale), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Success",
                        tint = SuccessGreen,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Help is on the way",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Dispatch is locked on your Duvvada GPS coordinates",
                    color = SecondaryWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Cards for Alert Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Alert Status Card
                StatusCard(
                    title = "Alert Status",
                    value = "Sent ✅",
                    icon = Icons.AutoMirrored.Filled.Send,
                    iconColor = AccentTeal,
                    modifier = Modifier.weight(1f)
                )

                // Location Card
                StatusCard(
                    title = "Location",
                    value = "Shared 📍",
                    icon = Icons.Filled.MyLocation,
                    iconColor = AccentTeal,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Contacts Card
                StatusCard(
                    title = "Contacts",
                    value = "3 Notified 👥",
                    icon = Icons.Filled.Group,
                    iconColor = AccentTeal,
                    modifier = Modifier.weight(1f)
                )

                // ETA Card
                StatusCard(
                    title = "ETA",
                    value = activeEta,
                    icon = Icons.Filled.Timer,
                    iconColor = PrimaryRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Patent Features Details Card
            Text(
                text = "AI PATENT INTELLIGENCE STATS",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .testTag("patent_stats_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Row 1: Confidence score & priority level
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Confidence (ECE)",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${activeIncident?.confidenceScore ?: 97}% Match",
                                color = SuccessGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Priority Level",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeIncident?.priority?.name ?: "CRITICAL",
                                color = if ((activeIncident?.priority ?: PriorityLevel.CRITICAL) == PriorityLevel.CRITICAL) PrimaryRed else AccentTeal,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    HorizontalDivider(color = GlassBorder, thickness = 1.dp)

                    // Row 2: Verification Status & Witness count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verification (CWVE)",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeIncident?.verificationStatus ?: "Community Verified",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Witness Count",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${activeIncident?.witnessCount ?: 3} Witnesses",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = GlassBorder, thickness = 1.dp)

                    // Row 3: Dispatch mode & status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = "Smart Dispatch Route",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeIncident?.dispatchRoute?.joinToString(" + ") ?: bottomAlertMessage.substringBefore(" have"),
                                color = AccentTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dispatch Mode",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (activeIncident?.networkMode == NetworkMode.OFFLINE_SMS) "Offline SMS ⏳" else "Cloud Dispatch ✅",
                                color = if (activeIncident?.networkMode == NetworkMode.OFFLINE_SMS) Color(0xFFFFB300) else SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ambulance/Fire Truck Progress Line (Horizontal progress bar with traveling vehicle icon)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedIncidentType == "Fire") "Fire Truck Progress" else "Ambulance Progress",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "En Route",
                            color = AccentTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vehicle Travel Rail
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B1B2B))
                    ) {
                        // Slider progress animation
                        val travelTransition = rememberInfiniteTransition(label = "travel")
                        val progressOffset by travelTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "car_progress"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressOffset)
                                .background(AccentTeal)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = progressLabel,
                        color = SecondaryWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nearest Service details (Hospital/Fire Station)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Hospital/Fire station Item
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = if (selectedIncidentType == "Fire") Icons.Outlined.LocalFireDepartment else Icons.Outlined.LocalHospital,
                            contentDescription = activeHospitalTitle,
                            tint = AccentTeal,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activeHospitalTitle,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeHospitalName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeHospitalLoc,
                                color = SecondaryWhite,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ETA: $activeHospitalDist",
                                color = AccentTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = GlassBorder, thickness = 1.dp)

                    // User Location Coordinates
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "My coordinates",
                            tint = PrimaryRed,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Emergency Coordinates",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Duvvada, Visakhapatnam, Andhra Pradesh",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Header Row with Map Title and Connectivity Selector Pill
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Map Route",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Interactive Connection Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isWeakConnectivity) Color(0x26FFB300) else Color(0x1A27E1C1))
                        .border(
                            1.dp,
                            if (isWeakConnectivity) Color(0xFFFFB300).copy(alpha = 0.4f) else AccentTeal.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.toggleConnectivity() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("toggle_connectivity_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isWeakConnectivity) Color(0xFFFFB300) else AccentTeal)
                        )
                        Text(
                            text = if (isWeakConnectivity) "Weak Signal (Offline)" else "Strong Signal (Online)",
                            color = if (isWeakConnectivity) Color(0xFFFFB300) else AccentTeal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Embedded Custom CrisisSense Map with connectivity parameters
            CrisisSenseMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .testTag("embedded_map"),
                hospitalName = activeHospitalName,
                isWeakConnectivity = isWeakConnectivity,
                onConnectivityToggle = { viewModel.toggleConnectivity() }
            )

            // Warning Banner for local offline vector cache
            if (isWeakConnectivity) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x16FFB300))
                        .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                        .testTag("weak_connectivity_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WifiOff,
                            contentDescription = "Signal Weak",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp).padding(top = 1.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "Emergency Fallback Map Active",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Weak mobile signal detected. Pinned coordinates are running on a local vector tile cache (Duvvada-Visakhapatnam area). No active network connection is required.",
                                color = SecondaryWhite,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dial Countdown section & Custom Alerts Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF140D11))
                    .border(1.dp, PrimaryRed.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Alert Confirmation text
                    Text(
                        text = bottomAlertMessage,
                        color = SuccessGreen,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Emergency services are being contacted...",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (countdownSeconds > 0 && !callCancelled) {
                        Text(
                            text = "$countdownSeconds seconds remaining",
                            color = PrimaryRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.cancelCall() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1218)),
                            border = BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("cancel_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Call,
                                contentDescription = "Hold phone dialing",
                                tint = PrimaryRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Cancel Call", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (callCancelled) Icons.Filled.PhoneDisabled else Icons.Filled.PhoneInTalk,
                                contentDescription = "Phone dialing status",
                                tint = if (callCancelled) Color.Gray else SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (callCancelled) "Phone Dialing Suspended" else "Emergency Line Dialed!",
                                color = if (callCancelled) Color.Gray else SuccessGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel Alert Button (Large Red bottom)
            Button(
                onClick = { viewModel.cancelEntireAlert() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("cancel_alert_button")
            ) {
                Text(
                    text = "Cancel Alert",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(GlassBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
