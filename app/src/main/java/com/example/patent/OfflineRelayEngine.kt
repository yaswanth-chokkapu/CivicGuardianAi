package com.example.patent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface IOfflineRelayEngine {
    val syncStatusMessage: StateFlow<String?>
    fun isOfflineActive(isWeakConnectivity: Boolean): Boolean
    fun generateOfflineSMS(incident: Incident): String
    fun queueOfflineIncident(incident: Incident)
    fun triggerConnectivityReturn(): List<Incident> // Returns synced incidents
    fun clearSyncStatus()
}

class OfflineRelayEngineImpl : IOfflineRelayEngine {
    private val pendingQueue = mutableListOf<Incident>()
    
    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    override val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    override fun isOfflineActive(isWeakConnectivity: Boolean): Boolean {
        return isWeakConnectivity
    }

    override fun generateOfflineSMS(incident: Incident): String {
        // Build a highly compact, standards-compliant emergency transmission packet
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formattedTime = sdf.format(Date(incident.timestamp))
        
        // Coordinates extracted to construct google maps link
        // Base coordinates: Duvvada Station area (17.7123, 83.1782)
        val lat = 17.7123
        val lon = 83.1782
        val mapsLink = "https://maps.google.com/?q=$lat,$lon"

        return """
[CRISIS_SENSE_PATENT_AOER]
ID: ${incident.id}
TYPE: ${incident.emergencyType}
PRIORITY: ${incident.priority.name}
CONFIDENCE: ${incident.confidenceScore}%
LOC: Duvvada ($lat, $lon)
MAPS: $mapsLink
AI SUMMARY: ${incident.aiSummary.take(75)}...
TIME: $formattedTime
[OFFLINE EMERGENCY RELAY ACTIVE]
        """.trimIndent()
    }

    override fun queueOfflineIncident(incident: Incident) {
        synchronized(pendingQueue) {
            if (pendingQueue.none { it.id == incident.id }) {
                pendingQueue.add(incident.copy(dispatchStatus = "Queued", networkMode = NetworkMode.OFFLINE_SMS))
            }
        }
        _syncStatusMessage.value = "Offline Emergency Mode Activated"
    }

    override fun triggerConnectivityReturn(): List<Incident> {
        val syncedList = mutableListOf<Incident>()
        synchronized(pendingQueue) {
            if (pendingQueue.isNotEmpty()) {
                syncedList.addAll(pendingQueue.map { it.copy(dispatchStatus = "Delivered", networkMode = NetworkMode.CLOUD) })
                pendingQueue.clear()
                _syncStatusMessage.value = "Cloud Synchronization Complete"
            }
        }
        return syncedList
    }

    override fun clearSyncStatus() {
        _syncStatusMessage.value = null
    }
}
