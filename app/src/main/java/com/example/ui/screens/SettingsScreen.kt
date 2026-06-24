package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.EmergencyViewModel

@Composable
fun SettingsScreen(
    viewModel: EmergencyViewModel,
    onNavigateToContacts: () -> Unit
) {
    val locationGranted by viewModel.locationPermission.collectAsState()
    val notificationGranted by viewModel.notificationPermission.collectAsState()
    val smsFallbackEnabled by viewModel.smsFallback.collectAsState()

    val context = LocalContext.current
    var isPrivacyExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
            // Header Title
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "Application Settings",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure system connections, permissions, and fallback rules",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Permissions Toggles Section Card
            Text(
                text = "System Integrations",
                color = AccentTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    // Location Switch Item
                    SettingsSwitchRow(
                        title = "Location Access (Simulated)",
                        subtitle = "Required for nearest hospital search & tracking",
                        icon = Icons.Outlined.GpsFixed,
                        iconColor = AccentTeal,
                        checked = locationGranted,
                        onCheckedChange = { viewModel.setLocationPermissionGranted(it) },
                        modifier = Modifier.testTag("setting_switch_location")
                    )

                    Divider(color = GlassBorder, thickness = 1.dp)

                    // Notification Switch Item
                    SettingsSwitchRow(
                        title = "Push Notifications",
                        subtitle = "Receive localized nearby hazard warnings",
                        icon = Icons.Outlined.NotificationsActive,
                        iconColor = AccentTeal,
                        checked = notificationGranted,
                        onCheckedChange = { viewModel.setNotificationPermissionGranted(it) },
                        modifier = Modifier.testTag("setting_switch_notifications")
                    )

                    Divider(color = GlassBorder, thickness = 1.dp)

                    // SMS Fallback Switch Item
                    SettingsSwitchRow(
                        title = "SMS Offline Fallback",
                        subtitle = "Sends SOS coordinate text to Guardians if offline",
                        icon = Icons.Outlined.Sms,
                        iconColor = AccentTeal,
                        checked = smsFallbackEnabled,
                        onCheckedChange = { viewModel.setSmsFallbackEnabled(it) },
                        modifier = Modifier.testTag("setting_switch_sms")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Shortcuts Section Card
            Text(
                text = "Quick Shortcuts",
                color = AccentTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    SettingsNavigationRow(
                        title = "Configure Emergency Guardians",
                        subtitle = "Add, edit, or delete primary responders",
                        icon = Icons.Outlined.Group,
                        onClick = onNavigateToContacts,
                        modifier = Modifier.testTag("setting_nav_contacts")
                    )

                    Divider(color = GlassBorder, thickness = 1.dp)

                    SettingsNavigationRow(
                        title = "Test SOS SMS Fallback Alert",
                        subtitle = "Trigger mock offline SMS warning text",
                        icon = Icons.Outlined.SettingsCell,
                        onClick = {
                            Toast.makeText(context, "Mock SOS SMS alert broadcasted to 3 Guardians!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.testTag("setting_test_sms")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy and Policy Section Card (Expandable)
            Text(
                text = "Legal & Security",
                color = AccentTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .clickable { isPrivacyExpanded = !isPrivacyExpanded }
                    .padding(16.dp)
                    .testTag("setting_privacy_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x11FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PrivacyTip,
                                    contentDescription = null,
                                    tint = AccentTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Privacy Policy",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Your device telemetry is strictly local",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isPrivacyExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "Expand",
                            tint = Color.Gray
                        )
                    }

                    AnimatedVisibility(
                        visible = isPrivacyExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Divider(color = GlassBorder, thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))
                            Text(
                                text = "CrisisSense AI is designed under strict Privacy-by-Default architecture:\n\n" +
                                        "1. Offline Processing First: Location analysis and route tracing occur locally on your device.\n\n" +
                                        "2. No Tracking: Location telemetry is shared with Guardians only upon explicit emergency activation.\n\n" +
                                        "3. Anonymous Auth: Cloud database collections are protected under Anonymous Firebase credentials. We do not persist real identity parameters, names, or addresses.",
                                color = SecondaryWhite,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About CSP Section Card
            Text(
                text = "Community Service Project (CSP)",
                color = AccentTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CrisisSense AI",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Version 1.0.0 (Native Android Build)",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "This application is developed as a Community Service Project (CSP) to provide proactive emergency SOS dispatches, real-time hospital route navigation, and collaborative crowdsourced local witness reports to bolster safety resilience.",
                        color = SecondaryWhite,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Built on Jetpack Compose, Kotlin, and MVVM Architecture.",
                        color = AccentTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x11FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentTeal,
                checkedTrackColor = AccentTeal.copy(alpha = 0.4f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x11FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Open shortcut",
            tint = Color.Gray
        )
    }
}
