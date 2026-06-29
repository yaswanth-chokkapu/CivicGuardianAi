package com.example.patent

interface IConfidenceEngine {
    fun evaluateEmergency(
        emergencyType: String,
        gpsAccuracyMeters: Float,
        userDescription: String,
        imageAnalysisLabel: String?,
        voiceTranscription: String?,
        nearbyWitnessCount: Int,
        timestamp: Long = System.currentTimeMillis(),
        isFallDetected: Boolean = false
    ): ConfidenceEvaluationResult
}

data class ConfidenceEvaluationResult(
    val confidenceScore: Int, // 0 - 100
    val priorityLevel: PriorityLevel,
    val aiSummary: String,
    val explanation: String
)

class ConfidenceEngineImpl : IConfidenceEngine {
    override fun evaluateEmergency(
        emergencyType: String,
        gpsAccuracyMeters: Float,
        userDescription: String,
        imageAnalysisLabel: String?,
        voiceTranscription: String?,
        nearbyWitnessCount: Int,
        timestamp: Long,
        isFallDetected: Boolean
    ): ConfidenceEvaluationResult {
        var score = 50 // Base score

        val reasons = mutableListOf<String>()

        // 1. GPS Accuracy Weight
        if (gpsAccuracyMeters <= 10f) {
            score += 15
            reasons.add("High GPS precision verified (< 10m)")
        } else if (gpsAccuracyMeters <= 30f) {
            score += 8
            reasons.add("Standard GPS precision locked")
        } else {
            score -= 10
            reasons.add("Degraded GPS signal accuracy")
        }

        // 2. Image Analysis Weight
        if (!imageAnalysisLabel.isNullOrBlank() && !imageAnalysisLabel.contains("Analyzing", ignoreCase = true)) {
            score += 20
            reasons.add("Physical evidence detected via Gemini computer vision: '$imageAnalysisLabel'")
        } else {
            reasons.add("No secondary visual confirmation uploaded")
        }

        // 3. Voice Analysis & Distress Keywords
        val textToAnalyze = "${userDescription.lowercase()} ${voiceTranscription?.lowercase() ?: ""}"
        val distressKeywords = listOf(
            "chest pain", "heart", "breathing", "unconscious", "collapse", "bleed", "blood",
            "fire", "smoke", "explode", "trapped", "accident", "crash", "collision", "hit", "help",
            "critical", "violence", "attack", "weapon", "hurt", "pain", "choke"
        )
        val matchedKeywords = distressKeywords.filter { textToAnalyze.contains(it) }
        
        if (matchedKeywords.isNotEmpty()) {
            val kwBonus = (matchedKeywords.size * 5).coerceAtMost(20)
            score += kwBonus
            reasons.add("Acoustic / linguistic distress flags: ${matchedKeywords.take(3).joinToString(", ")}")
        }

        // 4. Nearby Witness Verification weight
        if (nearbyWitnessCount > 0) {
            val witnessBonus = (nearbyWitnessCount * 10).coerceAtMost(25)
            score += witnessBonus
            reasons.add("Corroborated by $nearbyWitnessCount nearby independent witness report(s)")
        }

        // 5. Fall Detection support (Future Support / Sensor telemetry)
        if (isFallDetected) {
            score += 20
            reasons.add("Automatic micro-accelerometer fall trigger detected")
        }

        // Constrain final score to 0..100
        val finalScore = score.coerceIn(5, 100)

        // Resolve Priority Level
        val priority = when {
            finalScore >= 90 || isFallDetected || emergencyType == "Fire" -> PriorityLevel.CRITICAL
            finalScore >= 75 -> PriorityLevel.HIGH
            finalScore >= 50 -> PriorityLevel.MEDIUM
            else -> PriorityLevel.LOW
        }

        // Generate AI Summary and Detailed Explanation
        val typeLabel = when (emergencyType) {
            "Medical" -> "Medical Crisis"
            "Fire" -> "Structural / Active Fire Threat"
            "Accident" -> "Major Vehicular Collision"
            "Violence" -> "Physical Safety Incident"
            "Natural Disaster" -> "Natural Disaster State"
            else -> "$emergencyType Incident"
        }

        val summary = "Evaluated $typeLabel. System has computed a $finalScore% confidence match for direct dispatch routing."
        
        val explanationBuilder = StringBuilder()
        explanationBuilder.append("Confidence Engine parameters verified:\n")
        reasons.forEach { r ->
            explanationBuilder.append("• $r\n")
        }

        return ConfidenceEvaluationResult(
            confidenceScore = finalScore,
            priorityLevel = priority,
            aiSummary = summary,
            explanation = explanationBuilder.toString().trim()
        )
    }
}
