package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HistoryEntry
import com.example.ui.theme.*
import com.example.viewmodel.EmergencyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: EmergencyViewModel
) {
    val historyList by viewModel.history.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Title
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "Activity History",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Review logs of local emergency dispatches & reports",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Empty",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Activity Logged",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Initiated alerts and reports will appear here.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(historyList, key = { it.id }) { entry ->
                        TimelineItem(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItem(entry: HistoryEntry) {
    val isSelfEmergency = entry.type == "Self Emergency"
    
    // Status color mapping
    val statusColor = when (entry.status) {
        "Completed" -> SuccessGreen
        "Verified" -> AccentTeal
        "Cancelled" -> Color.Gray
        "En Route" -> WarningOrange
        else -> PrimaryRed
    }

    val statusBgColor = when (entry.status) {
        "Completed" -> SuccessGreenBg
        "Verified" -> Color(0x1F27E1C1)
        "Cancelled" -> Color(0x11FFFFFF)
        "En Route" -> WarningOrangeBg
        else -> Color(0x1FFF3B4A)
    }

    val icon = when {
        isSelfEmergency -> Icons.Outlined.Warning
        entry.title.contains("Medical", ignoreCase = true) -> Icons.Outlined.LocalHospital
        entry.title.contains("Fire", ignoreCase = true) -> Icons.Outlined.LocalFireDepartment
        entry.title.contains("Accident", ignoreCase = true) -> Icons.Outlined.DirectionsCar
        else -> Icons.Outlined.Feedback
    }

    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val formattedTime = sdf.format(Date(entry.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Solves vertical line stretching
    ) {
        // Left Column: Timeline Indicator Line and Node
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Node point
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isSelfEmergency) PrimaryRed else AccentTeal)
                    .border(3.dp, BackgroundDark, CircleShape)
            )

            // Dynamic connector line extending downwards
            Divider(
                color = Color(0xFF1B1B2B),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right Column: Card Layout Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    // Header Row: Badge Type and Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge Node Type
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelfEmergency) PrimaryRed else AccentTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = entry.type,
                                color = if (isSelfEmergency) PrimaryRed else AccentTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Status Badge Row
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(statusBgColor)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = entry.status,
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Event Title
                    Text(
                        text = entry.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Event Description
                    Text(
                        text = entry.details,
                        color = SecondaryWhite,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        lineHeight = 17.sp
                    )

                    // Hospital, ETA and Incident details block
                    if (!entry.hospital.isNullOrEmpty() || !entry.eta.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C0C14))
                                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!entry.hospital.isNullOrEmpty()) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Text("RESPONDER UNIT", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(entry.hospital?.substringBefore(",") ?: "", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            if (!entry.eta.isNullOrEmpty()) {
                                Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.End) {
                                    Text("EST. ETA", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(entry.eta ?: "", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Event Location & Time stamp footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Coordinates",
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.location,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = formattedTime,
                            color = Color.DarkGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
