package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EmergencyRepository
import com.example.model.Contact
import com.example.model.HistoryEntry
import com.example.model.WitnessReport
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.patent.*

class EmergencyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmergencyRepository()

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    // Patent Engines
    private val confidenceEngine: IConfidenceEngine = ConfidenceEngineImpl()
    private val witnessVerificationEngine: IWitnessVerificationEngine = WitnessVerificationEngineImpl()
    private val smartDispatchEngine: ISmartDispatchEngine = SmartDispatchEngineImpl()
    private val offlineRelayEngine: IOfflineRelayEngine = OfflineRelayEngineImpl()

    // Standarized Incident Intelligence Object
    private val _activeIncident = MutableStateFlow<Incident?>(null)
    val activeIncident = _activeIncident.asStateFlow()

    val syncStatusMessage: StateFlow<String?> = offlineRelayEngine.syncStatusMessage

    // Exposed flows from repository
    val contacts: StateFlow<List<Contact>> = repository.contacts
    val history: StateFlow<List<HistoryEntry>> = repository.history
    val witnessReports: StateFlow<List<WitnessReport>> = repository.witnessReports
    val locationPermission: StateFlow<Boolean> = repository.locationPermissionGranted
    val notificationPermission: StateFlow<Boolean> = repository.notificationPermissionGranted
    val smsFallback: StateFlow<Boolean> = repository.smsFallbackEnabled

    // UI state for My Safety hold button
    private val _holdProgress = MutableStateFlow(0f)
    val holdProgress = _holdProgress.asStateFlow()

    private val _isHolding = MutableStateFlow(false)
    val isHolding = _isHolding.asStateFlow()

    private val _emergencyTriggered = MutableStateFlow(false)
    val emergencyTriggered = _emergencyTriggered.asStateFlow()

    // Mode Toggle: "My Safety" = 0, "Witness" = 1
    private val _selectedMode = MutableStateFlow(0)
    val selectedMode = _selectedMode.asStateFlow()

    // Witness Mode Fields
    private val _incidentType = MutableStateFlow("Medical") // Medical, Fire, Accident
    val incidentType = _incidentType.asStateFlow()

    private val _riskLevel = MutableStateFlow("Medium") // Defaults to Medium
    val riskLevel = _riskLevel.asStateFlow()

    private val _descriptionText = MutableStateFlow("")
    val descriptionText = _descriptionText.asStateFlow()

    // Dummy AI Photo upload state
    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto = _isUploadingPhoto.asStateFlow()

    private val _uploadedPhotoLabel = MutableStateFlow<String?>(null)
    val uploadedPhotoLabel = _uploadedPhotoLabel.asStateFlow()

    // Dummy AI Voice record state
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice = _isRecordingVoice.asStateFlow()

    private val _voiceTranscription = MutableStateFlow<String?>(null)
    val voiceTranscription = _voiceTranscription.asStateFlow()

    // Camera & Photo Support
    private val _capturedImage = MutableStateFlow<android.graphics.Bitmap?>(null)
    val capturedImage = _capturedImage.asStateFlow()

    private val _galleryImageUri = MutableStateFlow<android.net.Uri?>(null)
    val galleryImageUri = _galleryImageUri.asStateFlow()

    private val _simulatedImageName = MutableStateFlow<String?>(null) // "medical", "fire", "accident"
    val simulatedImageName = _simulatedImageName.asStateFlow()

    // SOS Stepper Workflow Support
    private val _sosStep = MutableStateFlow(-1)
    val sosStep = _sosStep.asStateFlow()

    // Simulated weak connectivity for map fallback demo
    private val _isWeakConnectivity = MutableStateFlow(false)
    val isWeakConnectivity = _isWeakConnectivity.asStateFlow()

    fun toggleConnectivity() {
        _isWeakConnectivity.value = !_isWeakConnectivity.value
        val isWeak = _isWeakConnectivity.value
        val incident = _activeIncident.value
        if (incident != null) {
            if (isWeak) {
                val updated = incident.copy(
                    networkMode = NetworkMode.OFFLINE_SMS,
                    dispatchStatus = "Queued"
                )
                _activeIncident.value = updated
                offlineRelayEngine.queueOfflineIncident(updated)
            } else {
                // Connectivity returned! Sync offline queue with Firebase simulation
                offlineRelayEngine.triggerConnectivityReturn()
                val updated = incident.copy(
                    networkMode = NetworkMode.CLOUD,
                    dispatchStatus = "Delivered"
                )
                _activeIncident.value = updated
                
                viewModelScope.launch {
                    delay(3500)
                    offlineRelayEngine.clearSyncStatus()
                }
            }
        }
    }

    fun setWeakConnectivity(weak: Boolean) {
        if (_isWeakConnectivity.value != weak) {
            toggleConnectivity()
        }
    }

    val sosWorkflowSteps = listOf(
        "GPS Locked",
        "Location Shared",
        "Emergency Contacts Notified",
        "Police Alerted",
        "Visakha Hospital Alerted",
        "Emergency Response Activated",
        "Help is on the way"
    )

    // Source of Emergency ("self" or "witness")
    private val _emergencySource = MutableStateFlow("self")
    val emergencySource = _emergencySource.asStateFlow()

    // Community alerts count
    val communityAlertsNearbyCount = witnessReports.map { list ->
        list.size
    }.stateIn(viewModelScope, SharingStarted.Lazily, 3)

    // Confirmation screen state variables
    private val _countdownSeconds = MutableStateFlow(8)
    val countdownSeconds = _countdownSeconds.asStateFlow()

    private val _callCancelled = MutableStateFlow(false)
    val callCancelled = _callCancelled.asStateFlow()

    private val _selectedHospital = MutableStateFlow("Visakha Hospital, Duvvada, Visakhapatnam")
    val selectedHospital = _selectedHospital.asStateFlow()

    private val _selectedIncidentTypeForConfirmation = MutableStateFlow("Medical")
    val selectedIncidentTypeForConfirmation = _selectedIncidentTypeForConfirmation.asStateFlow()

    private var holdJob: Job? = null
    private var countdownJob: Job? = null

    init {
        // Automatically link hospital or fire station to incident types
        viewModelScope.launch {
            combine(_incidentType, _selectedIncidentTypeForConfirmation, _emergencyTriggered, _emergencySource) { type, confType, triggered, source ->
                val activeType = if (triggered) confType else type
                if (activeType == "Fire") {
                    "Duvvada Fire Station, Visakhapatnam"
                } else {
                    "Visakha Hospital, Duvvada, Visakhapatnam"
                }
            }.collect { resolvedHospital ->
                _selectedHospital.value = resolvedHospital
            }
        }
    }

    // Toggle Mode
    fun setMode(modeIndex: Int) {
        _selectedMode.value = modeIndex
    }

    // Set Incident Type
    fun setIncidentType(type: String) {
        _incidentType.value = type
    }

    // Set Risk Level
    fun setRiskLevel(level: String) {
        _riskLevel.value = level
    }

    // Set Description Box
    fun setDescriptionText(text: String) {
        _descriptionText.value = text
    }

    // Start Hold Alert Timer
    fun startEmergencyHold() {
        if (_emergencyTriggered.value) return
        _isHolding.value = true
        _holdProgress.value = 0f
        
        holdJob = viewModelScope.launch {
            val totalSteps = 20
            val stepDelay = 100L // 2.0 seconds total (20 * 100ms)
            
            for (step in 1..totalSteps) {
                delay(stepDelay)
                _holdProgress.value = step.toFloat() / totalSteps
                // Vibrate during hold
                triggerVibration(100L)
            }
            
            // Hold complete -> Trigger emergency workflow
            triggerVibration(500L)
            triggerSelfEmergency()
        }
    }

    // Cancel Hold Alert Timer
    fun cancelEmergencyHold() {
        if (_emergencyTriggered.value) return
        holdJob?.cancel()
        _isHolding.value = false
        _holdProgress.value = 0f
    }

    // Trigger Self Emergency Workflow
    private fun triggerSelfEmergency() {
        _isHolding.value = false
        _holdProgress.value = 0f
        _emergencySource.value = "self"
        _selectedIncidentTypeForConfirmation.value = "Medical"
        
        // Start the SOS Stepper Workflow animation
        viewModelScope.launch {
            for (step in 0..6) {
                _sosStep.value = step
                delay(750) // Smooth animation steps (approx 5.2 seconds total)
            }
            
            // Steps completed! Navigate to Confirmation
            _sosStep.value = -1 // reset overlay
            _emergencyTriggered.value = true
            
            // 1. AI Emergency Confidence Engine (ECE) Evaluation
            val isWeak = _isWeakConnectivity.value
            val eval = confidenceEngine.evaluateEmergency(
                emergencyType = "Medical",
                gpsAccuracyMeters = if (isWeak) 45f else 4.2f,
                userDescription = "Emergency SOS Hold triggered.",
                imageAnalysisLabel = _uploadedPhotoLabel.value,
                voiceTranscription = _voiceTranscription.value,
                nearbyWitnessCount = repository.witnessReports.value.filter { it.incidentType == "Medical" }.size,
                isFallDetected = false
            )

            // 2. Smart Dispatch Decision Engine Routing
            val dispatchTargets = smartDispatchEngine.resolveDispatchRoute("Medical")
            val dispatchRouteNames = dispatchTargets.map { it.displayName }

            // 3. Adaptive Offline Emergency Relay Activation
            val offlineActive = offlineRelayEngine.isOfflineActive(isWeak)
            val networkMode = if (offlineActive) NetworkMode.OFFLINE_SMS else NetworkMode.CLOUD
            val initialDispatchStatus = if (offlineActive) "Queued" else "Delivered"

            // 4. Create Standardized Incident Object
            val incident = Incident(
                emergencyType = "Medical",
                confidenceScore = eval.confidenceScore,
                priority = eval.priorityLevel,
                aiSummary = eval.aiSummary,
                confidenceExplanation = eval.explanation,
                witnessCount = 1,
                verificationStatus = "Self Declared",
                verificationConfidence = 100,
                dispatchRoute = dispatchRouteNames,
                dispatchStatus = initialDispatchStatus,
                networkMode = networkMode
            )
            _activeIncident.value = incident

            if (offlineActive) {
                offlineRelayEngine.queueOfflineIncident(incident)
            }

            // 5. Log Custom History Entry
            val newAlert = HistoryEntry(
                id = incident.id,
                type = "Self Emergency",
                title = "SOS Emergency Initiated (${incident.id})",
                location = "Duvvada, Visakhapatnam",
                status = if (offlineActive) "Queued" else "En Route",
                details = "${eval.aiSummary}\n\nPriority: ${eval.priorityLevel.name}\nConfidence: ${eval.confidenceScore}%\n\n${eval.explanation}",
                hospital = "Visakha Hospital, Duvvada, Visakhapatnam",
                eta = "~3:58 mins",
                incidentType = "Medical"
            )
            repository.addHistoryEntry(newAlert)
            
            // Start Call Countdown
            startCallCountdown()
        }
    }

    // Camera & Photo management methods
    fun setCapturedImage(bitmap: android.graphics.Bitmap?) {
        _capturedImage.value = bitmap
        _galleryImageUri.value = null
        _simulatedImageName.value = null
        if (bitmap != null) {
            simulatePhotoAnalysis("Captured Image")
        }
    }

    fun setGalleryImageUri(uri: android.net.Uri?) {
        _galleryImageUri.value = uri
        _capturedImage.value = null
        _simulatedImageName.value = null
        if (uri != null) {
            simulatePhotoAnalysis("Gallery Photo")
        }
    }

    fun setSimulatedImage(name: String?) {
        _simulatedImageName.value = name
        _capturedImage.value = null
        _galleryImageUri.value = null
        if (name != null) {
            simulatePhotoAnalysis(name)
        }
    }

    fun clearImage() {
        _capturedImage.value = null
        _galleryImageUri.value = null
        _simulatedImageName.value = null
        _uploadedPhotoLabel.value = null
    }

    private fun simulatePhotoAnalysis(source: String) {
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            _uploadedPhotoLabel.value = "Analyzing image via CrisisSense AI..."
            delay(1500)
            val label = when {
                source.contains("fire", ignoreCase = true) || _incidentType.value == "Fire" -> "Verified: Fire / Smoke Hazard"
                source.contains("accident", ignoreCase = true) || _incidentType.value == "Accident" -> "Verified: Vehicular Collision"
                else -> "Verified: Medical Distress"
            }
            _uploadedPhotoLabel.value = label
            _isUploadingPhoto.value = false
        }
    }

    // Directly trigger alert (e.g. bypass hold or voice trigger)
    fun instantTriggerEmergency() {
        triggerSelfEmergency()
    }

    // Start Confirmation Call Countdown (8 seconds)
    private fun startCallCountdown() {
        _countdownSeconds.value = 8
        _callCancelled.value = false
        
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_countdownSeconds.value > 0 && !_callCancelled.value) {
                delay(1000L)
                if (!_callCancelled.value) {
                    _countdownSeconds.value -= 1
                }
            }
        }
    }

    // Cancel Dial/Call Emergency Services
    fun cancelCall() {
        _callCancelled.value = true
        countdownJob?.cancel()
    }

    // Cancel Entire Alert Workflow (Back to home)
    fun cancelEntireAlert() {
        _emergencyTriggered.value = false
        _callCancelled.value = false
        _countdownSeconds.value = 8
        _sosStep.value = -1
        countdownJob?.cancel()
        
        // Add cancel to history log
        val lastSOS = repository.history.value.firstOrNull { it.title.contains("SOS") }
        if (lastSOS != null) {
            repository.updateHistoryEntryStatus(lastSOS.id, "Cancelled")
        }
    }

    // Dummy AI Photo Upload Action
    fun simulatePhotoUpload() {
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            _uploadedPhotoLabel.value = "Analyzing image..."
            delay(2000) // Simulate processing delay
            
            // Returns Medical, Fire, or Accident based on selected incident or randomized
            val responseLabel = when (_incidentType.value) {
                "Fire" -> "Verified: Fire / Smoke Hazard"
                "Accident" -> "Verified: Vehicular Collision"
                else -> "Verified: Medical Distress"
            }
            _uploadedPhotoLabel.value = responseLabel
            _isUploadingPhoto.value = false
            
            // Automatically set incident type to match predicted AI output
            val matchedType = when (_incidentType.value) {
                "Fire" -> "Fire"
                "Accident" -> "Accident"
                else -> "Medical"
            }
            _incidentType.value = matchedType
        }
    }

    // Dummy AI Voice Recording Action
    fun simulateVoiceRecording() {
        viewModelScope.launch {
            _isRecordingVoice.value = true
            _voiceTranscription.value = "Listening to voice alert..."
            delay(3000) // 3 seconds simulation
            
            val responseText = when (_incidentType.value) {
                "Fire" -> "HELP! Heavy fire and smoke rising from building, send firefighters!"
                "Accident" -> "Emergency! Two cars collided at the junction, passengers are stuck!"
                else -> "Help me, I am feeling severe chest pain. Send an ambulance to Duvvada."
            }
            _voiceTranscription.value = responseText
            _isRecordingVoice.value = false
            _descriptionText.value = responseText
        }
    }

    // Submit Witness Emergency Report
    fun submitWitnessReport() {
        val description = _descriptionText.value.ifEmpty { "Incident reported at Duvvada." }
        val type = _incidentType.value
        val risk = _riskLevel.value.ifEmpty { "Medium" }
        
        val newReport = WitnessReport(
            incidentType = type,
            riskLevel = risk,
            description = description,
            photoUri = _uploadedPhotoLabel.value,
            voiceTranscription = _voiceTranscription.value,
            isVerified = true,
            reportsNearby = repository.getWitnessReportCount() + 1
        )

        // 1. Community Witness Verification Engine (CWVE)
        val verifyResult = witnessVerificationEngine.processAndVerify(
            newReport,
            repository.witnessReports.value
        )

        // 2. AI Emergency Confidence Engine (ECE) Evaluation
        val isWeak = _isWeakConnectivity.value
        val eval = confidenceEngine.evaluateEmergency(
            emergencyType = type,
            gpsAccuracyMeters = if (isWeak) 85f else 12f,
            userDescription = description,
            imageAnalysisLabel = _uploadedPhotoLabel.value,
            voiceTranscription = _voiceTranscription.value,
            nearbyWitnessCount = verifyResult.witnessCount - 1,
            isFallDetected = false
        )

        // 3. Smart Dispatch Decision Engine Routing
        val dispatchTargets = smartDispatchEngine.resolveDispatchRoute(type)
        val dispatchRouteNames = dispatchTargets.map { it.displayName }

        // 4. Adaptive Offline Emergency Relay Activation
        val offlineActive = offlineRelayEngine.isOfflineActive(isWeak)
        val networkMode = if (offlineActive) NetworkMode.OFFLINE_SMS else NetworkMode.CLOUD
        val initialDispatchStatus = if (offlineActive) "Queued" else "Delivered"

        // 5. Standardized Incident Object
        val incident = Incident(
            emergencyType = type,
            confidenceScore = eval.confidenceScore,
            priority = eval.priorityLevel,
            aiSummary = eval.aiSummary,
            confidenceExplanation = eval.explanation,
            witnessCount = verifyResult.witnessCount,
            verificationStatus = if (verifyResult.isMerged) "Community Verified" else "Single Report",
            verificationConfidence = verifyResult.verificationConfidence,
            dispatchRoute = dispatchRouteNames,
            dispatchStatus = initialDispatchStatus,
            networkMode = networkMode
        )
        _activeIncident.value = incident

        if (offlineActive) {
            offlineRelayEngine.queueOfflineIncident(incident)
        }

        // 6. Log Custom History Entry via Repository
        val customHistoryEntry = HistoryEntry(
            id = incident.id,
            type = "Witness Report",
            title = "Reported $type (${incident.id})",
            location = newReport.location,
            status = if (offlineActive) "Queued" else if (verifyResult.isMerged) "Verified" else "Sent",
            details = "${eval.aiSummary}\n\nVerification: ${incident.verificationStatus} (${verifyResult.witnessCount} Witnesses)\nPriority: ${eval.priorityLevel.name}\nConfidence Score: ${eval.confidenceScore}%\n\nRouting Path:\n${dispatchRouteNames.joinToString("\n")}",
            hospital = if (type == "Fire") "Duvvada Fire Station" else "Visakha Hospital, Duvvada",
            eta = when (type) {
                "Fire" -> "3–5 mins"
                "Accident" -> "4 mins"
                else -> "~3:58 mins"
            },
            incidentType = type
        )

        repository.addWitnessReport(
            report = newReport.copy(
                isVerified = verifyResult.isMerged || verifyResult.witnessCount > 1,
                reportsNearby = verifyResult.witnessCount
            ),
            customHistoryEntry = customHistoryEntry
        )
        
        // Setup Witness Confirmation Navigation
        _selectedIncidentTypeForConfirmation.value = type
        _emergencySource.value = "witness"
        _emergencyTriggered.value = true
        
        // Start Call Countdown for safety
        startCallCountdown()
        
        // Clear Witness Fields for next report
        _descriptionText.value = ""
        _uploadedPhotoLabel.value = null
        _voiceTranscription.value = null
        clearImage()
    }

    // Contacts Controls
    fun addContact(name: String, phone: String, relationship: String, isGuardian: Boolean) {
        repository.addContact(name, phone, relationship, isGuardian)
    }

    fun updateContact(id: String, name: String, phone: String, relationship: String, isGuardian: Boolean) {
        repository.updateContact(id, name, phone, relationship, isGuardian)
    }

    fun deleteContact(id: String) {
        repository.deleteContact(id)
    }

    // Settings Controls
    fun setLocationPermissionGranted(granted: Boolean) {
        repository.setLocationPermission(granted)
    }

    fun setNotificationPermissionGranted(granted: Boolean) {
        repository.setNotificationPermission(granted)
    }

    fun setSmsFallbackEnabled(enabled: Boolean) {
        repository.setSmsFallback(enabled)
    }

    // Local Haptic Feedback service
    private fun triggerVibration(durationMs: Long) {
        try {
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            // Log/ignore vibration errors gracefully so that the core SOS holding animation works seamlessly
            e.printStackTrace()
        }
    }
}
