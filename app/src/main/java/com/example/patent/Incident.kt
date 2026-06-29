package com.example.patent

import java.util.UUID

enum class PriorityLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class NetworkMode {
    CLOUD, OFFLINE_SMS
}

data class Incident(
    val id: String = "INC-" + UUID.randomUUID().toString().take(8).uppercase(),
    val emergencyType: String, // "Medical", "Fire", "Accident", "Violence", "Natural Disaster"
    val confidenceScore: Int, // 0 to 100
    val priority: PriorityLevel,
    val aiSummary: String,
    val confidenceExplanation: String,
    val witnessCount: Int,
    val verificationStatus: String, // "Self Declared", "Single Report", "Community Verified"
    val verificationConfidence: Int, // 0 to 100%
    val dispatchRoute: List<String>, // e.g. ["Hospital", "Police", "Fire Department", "Disaster Response Team"]
    val dispatchStatus: String, // "Delivered" or "Queued"
    val gpsCoordinates: String = "17.7123° N, 83.1782° E", // Duvvada, Visakhapatnam area
    val timestamp: Long = System.currentTimeMillis(),
    val networkMode: NetworkMode
)
