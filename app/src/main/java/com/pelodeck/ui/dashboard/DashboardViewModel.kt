package com.pelodeck.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pelodeck.PeloDeckApp
import com.pelodeck.data.local.entity.Bookmark
import com.pelodeck.data.local.entity.RideSession
import com.pelodeck.data.local.entity.RiderProfile
import com.pelodeck.data.repository.ActiveRideState
import com.pelodeck.data.repository.RideStatus
import com.pelodeck.data.telemetry.TelemetryProvider
import com.pelodeck.domain.model.BikeTelemetry
import com.pelodeck.domain.model.TelemetrySource
import com.pelodeck.domain.model.ThemeSkin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val app: PeloDeckApp = PeloDeckApp.instance
) : ViewModel() {

    private val prefRepo = app.preferencesRepository
    private val profileRepo = app.profileRepository
    private val trackingRepo = app.rideTrackingRepository
    private val bookmarkRepo = app.bookmarkRepository
    private val mockTelemetry = app.mockTelemetryProvider
    private val hardwareTelemetry = app.hardwareTelemetryProvider

    private var activeTelemetryProvider: TelemetryProvider = hardwareTelemetry

    val selectedSkin: StateFlow<ThemeSkin> = prefRepo.selectedSkin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeSkin.WINAMP)

    val preferredUnits: StateFlow<String> = prefRepo.preferredUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "IMPERIAL")

    val activeRide: StateFlow<ActiveRideState> = trackingRepo.rideState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveRideState())

    val bookmarks: StateFlow<List<Bookmark>> = bookmarkRepo.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _telemetry = MutableStateFlow(BikeTelemetry(source = TelemetrySource.PELOTON_SERVICE))
    val telemetry: StateFlow<BikeTelemetry> = _telemetry.asStateFlow()

    private val _activeProfile = MutableStateFlow<RiderProfile?>(null)
    val activeProfile: StateFlow<RiderProfile?> = _activeProfile.asStateFlow()

    init {
        // Observe profile changes and load active profile
        viewModelScope.launch {
            combine(prefRepo.activeProfileId, profileRepo.allProfiles) { id, profiles ->
                profiles.firstOrNull { it.id == id } ?: profiles.firstOrNull()
            }.collect { profile ->
                _activeProfile.value = profile
                profile?.let {
                    trackingRepo.setRiderContext(it.id, it.weightKg)
                }
            }
        }

        // Start the physical bike connection.
        startTelemetryEngine()
    }

    private fun startTelemetryEngine() {
        viewModelScope.launch {
            activeTelemetryProvider.connect()
            activeTelemetryProvider.telemetry.collect { telem ->
                _telemetry.value = telem
                trackingRepo.onTelemetryTick(telem)
            }
        }
    }

    fun ensureRideStarted() {
        if (activeRide.value.status == RideStatus.IDLE) {
            startRide()
        }
    }

    fun setSelectedSkin(skin: ThemeSkin) {
        viewModelScope.launch {
            prefRepo.setSelectedSkin(skin)
        }
    }

    fun startRide() {
        val profile = _activeProfile.value ?: RiderProfile(id = 1, name = "Peloton Rider")
        mockTelemetry.resetDistance()
        hardwareTelemetry.resetDistance()
        trackingRepo.startRide(profile.id, profile.weightKg)
    }

    fun pauseRide() {
        trackingRepo.pauseRide()
    }

    fun resumeRide() {
        trackingRepo.resumeRide()
    }

    fun finishRide(onFinished: ((RideSession?) -> Unit)? = null) {
        viewModelScope.launch {
            val session = trackingRepo.finishRide()
            onFinished?.invoke(session)
        }
    }

    fun addBookmark(title: String, url: String) {
        viewModelScope.launch {
            bookmarkRepo.addBookmark(title, url)
        }
    }
}
