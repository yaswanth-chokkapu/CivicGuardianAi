package com.example.model

import java.util.UUID

data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val relationship: String,
    val isGuardian: Boolean = true
)

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "Self Emergency" or "Witness Report"
    val title: String,
    val location: String = "Duvvada, Visakhapatnam",
    val status: String, // "Sent", "En Route", "Completed", "Cancelled", "Verified"
    val details: String,
    val hospital: String? = null,
    val eta: String? = null,
    val incidentType: String? = null
)

data class WitnessReport(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val incidentType: String, // "Medical", "Fire", "Accident"
    val riskLevel: String, // "Low", "Medium", "High", "Critical"
    val description: String,
    val location: String = "Duvvada, Visakhapatnam",
    val photoUri: String? = null,
    val voiceTranscription: String? = null,
    val isVerified: Boolean = false,
    val reportsNearby: Int = 1
)
