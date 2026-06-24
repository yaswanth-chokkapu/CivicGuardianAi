package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.EmergencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: EmergencyViewModel,
    onNavigateToConfirmation: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val emergencyTriggered by viewModel.emergencyTriggered.collectAsState()
    val isLocationPermissionGranted by viewModel.locationPermission.collectAsState()

    // Trigger navigation if emergency state is active
    LaunchedEffect(emergencyTriggered) {
        if (emergencyTriggered) {
            onNavigateToConfirmation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Logo and Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top Left: CrisisSense AI Logo with Sophisticated Glow
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val redDotTransition = rememberInfiniteTransition(label = "red_dot")
                    val dotAlpha by redDotTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(PrimaryRed)
                    ) {
                        // Pulsating background glow ring around red dot
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(1.8f)
                                .clip(CircleShape)
                                .background(PrimaryRed.copy(alpha = dotAlpha * 0.4f))
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "CrisisSense AI",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Top Right: Profile Circular Badge (Sophisticated Dark Border)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x0DFFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                        .clickable(onClick = onOpenProfile)
                        .testTag("profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Minimalist head/shoulder profile element outline representation
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(2.dp, Color(0xFF94A3B8), CircleShape)
                    )
                }
            }

            // Segment Control (Adaptive, Multi-window safe layout with 0xFF09090F background and glass)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x0DFFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                val animatedOffsetPercent by animateFloatAsState(
                    targetValue = if (selectedMode == 0) 0f else 1f,
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium),
                    label = "segment_slider"
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    if (animatedOffsetPercent > 0f) {
                        Spacer(modifier = Modifier.weight(animatedOffsetPercent))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedMode == 0) PrimaryRed else AccentTeal)
                    )
                    if (1f - animatedOffsetPercent > 0f) {
                        Spacer(modifier = Modifier.weight(1f - animatedOffsetPercent))
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    // My Safety Segment
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setMode(0) }
                            .testTag("segment_my_safety"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "My Safety",
                            color = if (selectedMode == 0) Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }

                    // Witness Segment
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setMode(1) }
                            .testTag("segment_witness"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Witness",
                            color = if (selectedMode == 1) Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }

            // Content Area based on mode selection
            AnimatedContent(
                targetState = selectedMode,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "mode_navigation"
            ) { mode ->
                if (mode == 0) {
                    MySafetyModeScreen(viewModel)
                } else {
                    WitnessModeScreen(viewModel)
                }
            }
        }

        // Animated SOS workflow overlay
        SosWorkflowOverlay(viewModel = viewModel)
    }
}

// ==========================================================
// MY SAFETY MODE SCREEN (Mode 1: Self Emergency)
// ==========================================================
@Composable
fun MySafetyModeScreen(viewModel: EmergencyViewModel) {
    val isHolding by viewModel.isHolding.collectAsState()
    val progress by viewModel.holdProgress.collectAsState()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val voiceTranscription by viewModel.voiceTranscription.collectAsState()

    // Outer glow pulse animations for central emergency button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_button")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale_1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha_1"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Tagline & Context
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "\"Action First. Context Second.\"",
                color = AccentTeal,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Hold center button to trigger immediate SOS dispatch",
                color = SecondaryWhite,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Center: Glowing SOS Circular Button Container
        Box(
            modifier = Modifier
                .size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient red blurred backdrop glow representing `blur-3xl opacity-20 scale-150`
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryRed.copy(alpha = 0.22f), Color.Transparent),
                            radius = 280f
                        ),
                        shape = CircleShape
                    )
            )

            // Outer boundary ring: `w-56 h-56 rounded-full border-2 border-white/5`
            Box(
                modifier = Modifier
                    .size(224.dp)
                    .border(2.dp, Color(0x0DFFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Pulsing outer glow ring: `border border-[#FF3B4A]/30`
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(if (!isHolding) pulseScale1 else 1.0f)
                        .border(
                            1.2.dp,
                            if (isHolding) PrimaryRed.copy(alpha = 0.7f) else PrimaryRed.copy(alpha = pulseAlpha1 * 0.4f),
                            CircleShape
                        )
                )

                // Hold progress indicator ring (around the button)
                Canvas(modifier = Modifier.size(192.dp)) {
                    if (progress > 0) {
                        drawArc(
                            color = PrimaryRed,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            topLeft = Offset(center.x - 78.dp.toPx(), center.y - 78.dp.toPx()),
                            size = Size(156.dp.toPx(), 156.dp.toPx()),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Central Active Touch Button Trigger
                Box(
                    modifier = Modifier
                        .size(144.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(PrimaryRed, Color(0xFFB91C1C)),
                                radius = 220f
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape) // white highlight border
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    viewModel.startEmergencyHold()
                                    try {
                                        awaitRelease()
                                    } finally {
                                        viewModel.cancelEmergencyHold()
                                    }
                                }
                            )
                        }
                        .testTag("emergency_sos_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Emoji icon (SOS representation from design)
                        Text(
                            text = if (isHolding) "⏳" else "🆘",
                            fontSize = 30.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = if (isHolding) "HOLDING" else "HOLD 2S",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            textAlign = TextAlign.Center
                        )
                        if (isHolding) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Voice Command Capsule Trigger below button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
                    .clip(CircleShape)
                    .background(Color(0x0DFFFFFF)) // bg-white/5
                    .border(
                        1.dp,
                        if (isRecordingVoice) PrimaryRed.copy(alpha = 0.5f) else Color(0x1AFFFFFF), // border-white/10
                        CircleShape
                    )
                    .clickable { viewModel.simulateVoiceRecording() }
                    .testTag("voice_command_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                ) {
                    // Accent circular badge representing `bg-[#27E1C1] rounded-full`
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isRecordingVoice) PrimaryRed else AccentTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecordingVoice) Icons.Filled.Mic else Icons.Filled.MicNone,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (isRecordingVoice) "LISTENING..." else "VOICE COMMAND",
                        color = Color(0xFFE2E8F0), // Slate-200
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (voiceTranscription != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = voiceTranscription ?: "",
                    color = AccentTeal,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // Bottom System Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0x08FFFFFF)) // bg-white/[0.03]
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(24.dp)) // border-white/[0.08]
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Pulsating Green/Teal Icon representation
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentTeal.copy(alpha = 0.1f))
                            .border(1.dp, AccentTeal.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val statusPulseTransition = rememberInfiniteTransition(label = "status_pulse")
                        val pulseAlpha by statusPulseTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_alpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(AccentTeal.copy(alpha = pulseAlpha))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.8f)
                                    .clip(CircleShape)
                                    .background(AccentTeal.copy(alpha = pulseAlpha * 0.4f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "SYSTEM STATUS",
                            color = Color(0xFF94A3B8), // slate-400
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Secured & Locked",
                            color = Color(0xFFF1F5F9), // slate-100
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Guardians overlap avatars
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.5.dp, BackgroundDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JD", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                            .border(1.5.dp, BackgroundDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AM", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AccentTeal)
                            .border(1.5.dp, BackgroundDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+1", color = BackgroundDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================================
// WITNESS MODE SCREEN (Mode 2: Witness Report Emergency)
// ==========================================================
@Composable
fun WitnessModeScreen(viewModel: EmergencyViewModel) {
    val incidentType by viewModel.incidentType.collectAsState()
    val riskLevel by viewModel.riskLevel.collectAsState()
    val descriptionText by viewModel.descriptionText.collectAsState()
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsState()
    val photoLabel by viewModel.uploadedPhotoLabel.collectAsState()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val voiceTranscription by viewModel.voiceTranscription.collectAsState()
    val reportsNearby by viewModel.communityAlertsNearbyCount.collectAsState()

    val capturedImage by viewModel.capturedImage.collectAsState()
    val galleryImageUri by viewModel.galleryImageUri.collectAsState()
    val simulatedImageName by viewModel.simulatedImageName.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showCameraDialog by remember { mutableStateOf(false) }
    var showGalleryDialog by remember { mutableStateOf(false) }

    // Real camera & gallery launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.setCapturedImage(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.setGalleryImageUri(uri)
        }
    }

    // Camera Choice Modal
    if (showCameraDialog) {
        AlertDialog(
            onDismissRequest = { showCameraDialog = false },
            title = { Text("Take Photo Option", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Choose between real device camera capture or a fully simulated emergency image.", color = SecondaryWhite, fontSize = 13.sp) },
            containerColor = Color(0xFF0F0F19),
            confirmButton = {
                TextButton(
                    onClick = {
                        showCameraDialog = false
                        try {
                            cameraLauncher.launch(null)
                        } catch (e: Exception) {
                            viewModel.setSimulatedImage(incidentType.lowercase())
                        }
                    }
                ) {
                    Text("Device Camera", color = AccentTeal, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCameraDialog = false
                        viewModel.setSimulatedImage(incidentType.lowercase())
                    }
                ) {
                    Text("Simulate Demo", color = PrimaryRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Gallery Choice Modal
    if (showGalleryDialog) {
        AlertDialog(
            onDismissRequest = { showGalleryDialog = false },
            title = { Text("Upload Photo Option", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Choose between selecting a real image file from your device or a fully simulated emergency photo.", color = SecondaryWhite, fontSize = 13.sp) },
            containerColor = Color(0xFF0F0F19),
            confirmButton = {
                TextButton(
                    onClick = {
                        showGalleryDialog = false
                        try {
                            galleryLauncher.launch("image/*")
                        } catch (e: Exception) {
                            viewModel.setSimulatedImage(incidentType.lowercase())
                        }
                    }
                ) {
                    Text("Device Gallery", color = AccentTeal, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showGalleryDialog = false
                        viewModel.setSimulatedImage(incidentType.lowercase())
                    }
                ) {
                    Text("Simulate Demo", color = PrimaryRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "Report Emergency",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Orange Warning Banner (Community verification)
        if (reportsNearby >= 3) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarningOrangeBg)
                        .border(1.dp, WarningOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Community Alert",
                            tint = WarningOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Confirmed Crisis in Area",
                                color = WarningOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "3 reports nearby in last 5 minutes",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Incident Type selection (Medical, Fire, Accident)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Incident Type",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Medical type
                    IncidentTypeCard(
                        title = "Medical",
                        icon = Icons.Outlined.LocalHospital,
                        isSelected = incidentType == "Medical",
                        onSelect = { viewModel.setIncidentType("Medical") },
                        modifier = Modifier.weight(1f)
                    )

                    // Fire type
                    IncidentTypeCard(
                        title = "Fire",
                        icon = Icons.Outlined.LocalFireDepartment,
                        isSelected = incidentType == "Fire",
                        onSelect = { viewModel.setIncidentType("Fire") },
                        modifier = Modifier.weight(1f)
                    )

                    // Accident type
                    IncidentTypeCard(
                        title = "Accident",
                        icon = Icons.Outlined.DirectionsCar,
                        isSelected = incidentType == "Accident",
                        onSelect = { viewModel.setIncidentType("Accident") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Risk Level selection (Low, Medium, High, Critical)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Risk Level",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Low", "Medium", "High", "Critical").forEach { level ->
                        val isSelected = riskLevel == level
                        val activeColor = when (level) {
                            "Low" -> AccentTeal
                            "Medium" -> WarningOrange
                            "High" -> PrimaryRed
                            else -> Color(0xFFFF0033)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.2f) else GlassBg)
                                .border(
                                    1.dp,
                                    if (isSelected) activeColor else GlassBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setRiskLevel(level) }
                                .padding(vertical = 10.dp)
                                .testTag("risk_level_$level"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level,
                                color = if (isSelected) activeColor else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Description Box
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Description Box",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { viewModel.setDescriptionText(it) },
                    placeholder = { Text("Summary of the issue...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = Color(0xFF141424),
                        unfocusedContainerColor = Color(0xFF11111B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("description_box"),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                )
            }
        }

        // Take Photo, Upload Photo & Record Voice Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Take Photo
                Button(
                    onClick = { showCameraDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBg),
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("take_photo_button")
                ) {
                    Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = "Camera", tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Take Photo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Upload Photo
                Button(
                    onClick = { showGalleryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBg),
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("upload_photo_button")
                ) {
                    Icon(imageVector = Icons.Filled.Photo, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload Photo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Record Voice Button
                Button(
                    onClick = { viewModel.simulateVoiceRecording() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (voiceTranscription != null) SuccessGreenBg else GlassBg
                    ),
                    border = BorderStroke(1.dp, if (voiceTranscription != null) SuccessGreen else GlassBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("record_voice_button")
                ) {
                    if (isRecordingVoice) {
                        CircularProgressIndicator(color = AccentTeal, modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                    } else {
                        Icon(
                            imageVector = if (voiceTranscription != null) Icons.Filled.CheckCircle else Icons.Filled.Mic,
                            contentDescription = "Voice Record",
                            tint = if (voiceTranscription != null) SuccessGreen else Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (voiceTranscription != null) "Recorded" else "Record Voice",
                            color = if (voiceTranscription != null) SuccessGreen else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Real-time voice fluctuating animated waveform
        if (isRecordingVoice) {
            item {
                VoiceRecordingWaveform()
            }
        }

        // Image Preview & Remove Card
        val hasImage = capturedImage != null || galleryImageUri != null || simulatedImageName != null
        if (hasImage) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141424))
                        .border(1.dp, AccentTeal.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    if (capturedImage != null) {
                        Image(
                            bitmap = capturedImage!!.asImageBitmap(),
                            contentDescription = "Captured Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (galleryImageUri != null) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A30)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Photo, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Gallery Photo Loaded", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(galleryImageUri.toString().takeLast(30), color = Color.Gray, fontSize = 9.sp)
                            }
                        }
                    } else if (simulatedImageName != null) {
                        val themeColor = when (simulatedImageName) {
                            "fire" -> PrimaryRed
                            "accident" -> WarningOrange
                            else -> AccentTeal
                        }
                        val title = when (simulatedImageName) {
                            "fire" -> "Simulated Fire Hazard"
                            "accident" -> "Simulated Accident Scene"
                            else -> "Simulated Medical Distress"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(themeColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = when (simulatedImageName) {
                                        "fire" -> Icons.Filled.LocalFireDepartment
                                        "accident" -> Icons.Filled.DirectionsCar
                                        else -> Icons.Filled.LocalHospital
                                    },
                                    contentDescription = null,
                                    tint = themeColor,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("CrisisSense AI Verified ✔", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Remove/Delete Floating X Icon button on top-right corner
                    IconButton(
                        onClick = { viewModel.clearImage() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .background(Color(0xE6000000), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .testTag("remove_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Display AI transcriptions or analysis outcomes
        if (photoLabel != null || voiceTranscription != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF11111B))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        photoLabel?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Photo AI Analysis: $it",
                                    color = SuccessGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        voiceTranscription?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Hearing, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Voice Transcription: \"$it\"",
                                    color = AccentTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Submit Report Large Bottom Button
        item {
            Button(
                onClick = {
                    viewModel.submitWitnessReport()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp)
                    .testTag("submit_report_button")
            ) {
                Text(
                    text = "Submit Report",
                    color = BackgroundDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun IncidentTypeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AccentTeal.copy(alpha = 0.15f) else GlassBg)
            .border(
                1.dp,
                if (isSelected) AccentTeal else GlassBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .testTag("incident_type_$title"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) AccentTeal else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = if (isSelected) AccentTeal else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================================
// VOICE RECORDING FLUTTER WAVEFORM COMPONENT
// ==========================================================
@Composable
fun VoiceRecordingWaveform() {
    val transition = rememberInfiniteTransition(label = "waveform")
    val heights = listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 0.4f, 0.7f, 0.3f)
    
    val pulseRates = heights.map { baseHeight ->
        transition.animateFloat(
            initialValue = baseHeight * 0.4f,
            targetValue = baseHeight,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = (500 + baseHeight * 600).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0C0C14))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Audio ",
                color = AccentTeal,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.width(6.dp))
            pulseRates.forEach { value ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(28.dp * value.value)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(AccentTeal)
                )
            }
        }
    }
}

// ==========================================================
// SOS WORKFLOW STEPPER OVERLAY COMPONENT
// ==========================================================
@Composable
fun SosWorkflowOverlay(viewModel: EmergencyViewModel) {
    val sosStep by viewModel.sosStep.collectAsState()

    if (sosStep >= 0) {
        // Full screen dark premium glass slate overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFA09090F)) // 98% deep dark contrast
                .clickable(enabled = true) {} // consume touch inputs
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F0F1A))
                    .border(1.dp, PrimaryRed.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Top header: Live Status Alert Beacon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val pulseTransition = rememberInfiniteTransition(label = "beacon")
                    val pulseAlpha by pulseTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "beacon_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PrimaryRed.copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EMERGENCY ALERT ACTIVATED",
                        color = PrimaryRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Circular Progress Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { (sosStep + 1) / 7f },
                        color = PrimaryRed,
                        strokeWidth = 6.dp,
                        trackColor = Color(0x1AFFFFFF),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${((sosStep + 1) * 100) / 7}%",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "DISPATCHING",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable/structured 7-step interactive progress checklist
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val stepsList = listOf(
                        "Detecting Emergency Distress Signature",
                        "Securing GPS Location Lock (Duvvada Area)",
                        "Notifying Nearest Registered Rescuers",
                        "Sharing Digital Medical/Health Profile",
                        "Dispatching Visakha Hospital Ambulance Unit",
                        "Establishing Audio & Camera Streaming Link",
                        "Emergency Dispatch Loop Fully Synced"
                    )

                    stepsList.forEachIndexed { index, title ->
                        val isCompleted = index < sosStep
                        val isActive = index == sosStep
                        val isPending = index > sosStep

                        val stateColor = when {
                            isCompleted -> SuccessGreen
                            isActive -> AccentTeal
                            else -> Color.Gray.copy(alpha = 0.4f)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isActive) Color(0x1427E1C1) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            // Checkmark/Icon indicator
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isActive) {
                                CircularProgressIndicator(
                                    color = AccentTeal,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = "Pending",
                                    tint = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = title,
                                color = if (isActive) Color.White else stateColor,
                                fontSize = 12.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Glowing "CANCEL DISPATCH" button
                Button(
                    onClick = { viewModel.cancelEntireAlert() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1215)),
                    border = BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("cancel_sos_dispatch_button")
                ) {
                    Text(
                        text = "CANCEL DISPATCH LOOP",
                        color = PrimaryRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
