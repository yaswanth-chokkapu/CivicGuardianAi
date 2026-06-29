package com.example.patent

import com.example.model.WitnessReport
import java.util.UUID
import kotlin.math.abs

interface IWitnessVerificationEngine {
    fun processAndVerify(
        newReport: WitnessReport,
        existingReports: List<WitnessReport>
    ): VerificationResult
}

data class VerificationResult(
    val isMerged: Boolean,
    val mergedIncidentId: String,
    val witnessCount: Int,
    val verificationConfidence: Int, // 0 - 100
    val summary: String
)

class WitnessVerificationEngineImpl : IWitnessVerificationEngine {
    override fun processAndVerify(
        newReport: WitnessReport,
        existingReports: List<WitnessReport>
    ): VerificationResult {
        // Find if there is any existing report that matches closely in spatial, temporal, and semantic bounds
        val thresholdMinutes = 30
        val maxTimeDifferenceMs = thresholdMinutes * 60 * 1000L
        
        var matchFound: WitnessReport? = null
        var highestSimilarityScore = 0f
        val similarReports = mutableListOf<WitnessReport>()

        for (report in existingReports) {
            // 1. Time Check (within 30 mins)
            val timeDiff = abs(newReport.timestamp - report.timestamp)
            if (timeDiff > maxTimeDifferenceMs) continue

            // 2. Incident Type Check
            if (newReport.incidentType != report.incidentType) continue

            // 3. Location Semantic Similarity or simple distance
            val locationMatch = newReport.location.substringBefore(",").trim().lowercase() == 
                                report.location.substringBefore(",").trim().lowercase()

            // 4. Word overlap / semantic overlap in descriptions
            val words1 = newReport.description.lowercase().split("\\s+".toRegex()).toSet()
            val words2 = report.description.lowercase().split("\\s+".toRegex()).toSet()
            val intersection = words1.intersect(words2)
            val jaccardSimilarity = if (words1.isEmpty() || words2.isEmpty()) 0f 
                                    else intersection.size.toFloat() / (words1.size + words2.size - intersection.size)

            // Let's compute a match probability
            var similarity = 0f
            if (locationMatch) similarity += 0.4f
            similarity += (jaccardSimilarity * 0.4f)
            
            // Photo analysis alignment
            if (newReport.photoUri != null && report.photoUri != null) {
                if (newReport.photoUri == report.photoUri) {
                    similarity += 0.2f
                }
            }

            if (similarity >= 0.35f) {
                similarReports.add(report)
                if (similarity > highestSimilarityScore) {
                    highestSimilarityScore = similarity
                    matchFound = report
                }
            }
        }

        return if (matchFound != null) {
            val totalWitnesses = similarReports.size + 1 // including the new report
            // Compute a verification confidence score
            val confidence = (75 + (totalWitnesses * 8)).coerceAtMost(99)
            VerificationResult(
                isMerged = true,
                mergedIncidentId = matchFound.id,
                witnessCount = totalWitnesses,
                verificationConfidence = confidence,
                summary = "Community Verified: Merged $totalWitnesses report(s) near ${newReport.location.substringBefore(",")} with ${confidence}% confidence."
            )
        } else {
            VerificationResult(
                isMerged = false,
                mergedIncidentId = newReport.id,
                witnessCount = 1,
                verificationConfidence = 60, // single witness base confidence
                summary = "Single witness report registered. Awaiting community verification."
            )
        }
    }
}
