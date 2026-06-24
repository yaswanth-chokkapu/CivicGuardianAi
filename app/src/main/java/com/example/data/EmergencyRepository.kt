package com.example.data

import com.example.model.Contact
import com.example.model.HistoryEntry
import com.example.model.WitnessReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class EmergencyRepository {
    // Simulated Collections: contacts, history, alerts, witnessReports, settings
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    private val _witnessReports = MutableStateFlow<List<WitnessReport>>(emptyList())
    val witnessReports: StateFlow<List<WitnessReport>> = _witnessReports.asStateFlow()

    // App Setting States
    private val _locationPermissionGranted = MutableStateFlow(true)
    val locationPermissionGranted = _locationPermissionGranted.asStateFlow()

    private val _notificationPermissionGranted = MutableStateFlow(true)
    val notificationPermissionGranted = _notificationPermissionGranted.asStateFlow()

    private val _smsFallbackEnabled = MutableStateFlow(true)
    val smsFallbackEnabled = _smsFallbackEnabled.asStateFlow()

    init {
        // Seed default emergency contacts as required
        _contacts.value = listOf(
            Contact(name = "Mom (Guardian)", phone = "+91 98765 43210", relationship = "Mother", isGuardian = true),
            Contact(name = "Dad (Guardian)", phone = "+91 87654 32109", relationship = "Father", isGuardian = true),
            Contact(name = "Local Police Desk", phone = "100", relationship = "Emergency Service", isGuardian = false)
        )

        // Seed some history entries for visual realism
        _history.value = listOf(
            HistoryEntry(
                timestamp = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
                type = "Self Emergency",
                title = "Medical Distress Alert Sent",
                location = "Duvvada, Visakhapatnam",
                status = "Completed",
                details = "Ambulance dispatched from Visakha Hospital. Response completed in 14 minutes.",
                hospital = "Visakha Hospital, Duvvada, Visakhapatnam",
                eta = "Completed",
                incidentType = "Medical"
            ),
            HistoryEntry(
                timestamp = System.currentTimeMillis() - 86400000 * 5, // 5 days ago
                type = "Witness Report",
                title = "Minor Road Accident",
                location = "National Highway, Duvvada",
                status = "Verified",
                details = "Reported a minor collision between two cars. Traffic control and Visakha Hospital informed.",
                hospital = "Visakha Hospital, Duvvada",
                eta = "Completed",
                incidentType = "Accident"
            ),
            HistoryEntry(
                timestamp = System.currentTimeMillis() - 86400000 * 10, // 10 days ago
                type = "Self Emergency",
                title = "Panic Alert (False Alarm)",
                location = "Duvvada Station Road",
                status = "Cancelled",
                details = "Alert was triggered accidentally and cancelled within 30 seconds.",
                hospital = "Visakha Hospital, Duvvada",
                eta = "Cancelled",
                incidentType = "Medical"
            )
        )

        // Seed default witness reports in the area to showcase "Confirmed Crisis" (nearby alerts detected)
        _witnessReports.value = listOf(
            WitnessReport(
                incidentType = "Medical",
                riskLevel = "Critical",
                description = "Elderly person collapsed near Duvvada Railway Station.",
                location = "Duvvada Station, Visakhapatnam",
                isVerified = true,
                reportsNearby = 3
            ),
            WitnessReport(
                incidentType = "Fire",
                riskLevel = "High",
                description = "Transformer fire near Duvvada Main Junction.",
                location = "Junction Road, Duvvada",
                isVerified = true,
                reportsNearby = 3
            )
        )
    }

    // CONTACTS CRUD
    fun addContact(name: String, phone: String, relationship: String, isGuardian: Boolean) {
        val newContact = Contact(name = name, phone = phone, relationship = relationship, isGuardian = isGuardian)
        _contacts.update { currentList -> currentList + newContact }
    }

    fun updateContact(id: String, name: String, phone: String, relationship: String, isGuardian: Boolean) {
        _contacts.update { currentList ->
            currentList.map { contact ->
                if (contact.id == id) {
                    contact.copy(name = name, phone = phone, relationship = relationship, isGuardian = isGuardian)
                } else contact
            }
        }
    }

    fun deleteContact(id: String) {
        _contacts.update { currentList -> currentList.filter { it.id != id } }
    }

    // HISTORY CRUD
    fun addHistoryEntry(entry: HistoryEntry) {
        _history.update { currentList -> listOf(entry) + currentList }
    }

    fun updateHistoryEntryStatus(id: String, status: String) {
        _history.update { currentList ->
            currentList.map { entry ->
                if (entry.id == id) entry.copy(status = status) else entry
            }
        }
    }

    // WITNESS REPORTS CRUD
    fun addWitnessReport(report: WitnessReport) {
        // Add the report to witnessReports list
        _witnessReports.update { currentList -> listOf(report) + currentList }

        // Also record this in the general history timeline as a submission
        val historyEntry = HistoryEntry(
            type = "Witness Report",
            title = "Reported ${report.incidentType} (${report.riskLevel})",
            location = report.location,
            status = if (getWitnessReportCount() >= 3) "Verified" else "Sent",
            details = report.description.ifEmpty { "Incident reported with details." },
            hospital = if (report.incidentType == "Fire") "Duvvada Fire Station" else "Visakha Hospital, Duvvada",
            eta = when (report.incidentType) {
                "Fire" -> "3–5 mins"
                "Accident" -> "4 mins"
                else -> "~3:58 mins"
            },
            incidentType = report.incidentType
        )
        addHistoryEntry(historyEntry)
    }

    fun getWitnessReportCount(): Int {
        return _witnessReports.value.size
    }

    // SETTINGS CONTROL
    fun setLocationPermission(granted: Boolean) {
        _locationPermissionGranted.value = granted
    }

    fun setNotificationPermission(granted: Boolean) {
        _notificationPermissionGranted.value = granted
    }

    fun setSmsFallback(enabled: Boolean) {
        _smsFallbackEnabled.value = enabled
    }
}
