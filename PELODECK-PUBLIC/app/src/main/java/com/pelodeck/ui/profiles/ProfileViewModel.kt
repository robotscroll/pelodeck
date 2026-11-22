package com.pelodeck.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pelodeck.PeloDeckApp
import com.pelodeck.data.local.entity.RiderProfile
import com.pelodeck.data.repository.ActiveRideState
import com.pelodeck.data.repository.RideStatus
import com.pelodeck.domain.model.ThemeSkin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ProfileSwitchConflict {
    data class ActiveRideBlock(val targetProfile: RiderProfile) : ProfileSwitchConflict()
}

class ProfileViewModel(
    private val app: PeloDeckApp = PeloDeckApp.instance
) : ViewModel() {

    private val profileRepo = app.profileRepository
    private val prefRepo = app.preferencesRepository
    private val trackingRepo = app.rideTrackingRepository
    private val mockTelemetry = app.mockTelemetryProvider

    val allProfiles: StateFlow<List<RiderProfile>> = profileRepo.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfileId: StateFlow<Long?> = prefRepo.activeProfileId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeRide: StateFlow<ActiveRideState> = trackingRepo.rideState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveRideState())

    private val _conflictState = MutableStateFlow<ProfileSwitchConflict?>(null)
    val conflictState: StateFlow<ProfileSwitchConflict?> = _conflictState.asStateFlow()

    init {
        // Automatically sync and load profiles from profiles.json (external storage or assets)
        viewModelScope.launch {
            if (profileRepo.getCount() == 0) com.pelodeck.data.local.ProfileConfigManager.syncProfiles(
                app.applicationContext,
                profileRepo,
                prefRepo
            )
        }
    }

    /**
     * Re-reads profiles.json (e.g. after pushing via ADB or editing) and updates the database.
     */
    fun reloadProfilesFromJson() {
        viewModelScope.launch {
            com.pelodeck.data.local.ProfileConfigManager.syncProfiles(
                app.applicationContext,
                profileRepo,
                prefRepo
            )
        }
    }

    /**
     * Primary entry point: selects profile, applies skin & units, starts a ride session,
     * and navigates directly into the PeloDeck dashboard.
     */
    fun selectProfileAndStartRide(profile: RiderProfile, onRideStarted: () -> Unit) {
        val ride = activeRide.value
        // If this same rider already has a ride in progress or paused, simply resume into dashboard
        if ((ride.status == RideStatus.IN_PROGRESS || ride.status == RideStatus.PAUSED) && activeProfileId.value == profile.id) {
            onRideStarted()
            return
        }

        // If another rider has an active ride, require resolution first
        if (ride.status == RideStatus.IN_PROGRESS || ride.status == RideStatus.PAUSED) {
            _conflictState.value = ProfileSwitchConflict.ActiveRideBlock(profile)
            return
        }

        executeStartRideForProfile(profile, onRideStarted)
    }

    fun selectProfile(profile: RiderProfile) {
        val ride = activeRide.value
        if (ride.status == RideStatus.IN_PROGRESS || ride.status == RideStatus.PAUSED) {
            _conflictState.value = ProfileSwitchConflict.ActiveRideBlock(profile)
            return
        }
        applyProfileSelection(profile)
    }

    fun resolveConflictAndFinishRide(targetProfile: RiderProfile, onRideStarted: () -> Unit = {}) {
        viewModelScope.launch {
            trackingRepo.finishRide()
            executeStartRideForProfile(targetProfile, onRideStarted)
            _conflictState.value = null
        }
    }

    fun resolveConflictAndDiscardRide(targetProfile: RiderProfile, onRideStarted: () -> Unit = {}) {
        trackingRepo.discardRide()
        executeStartRideForProfile(targetProfile, onRideStarted)
        _conflictState.value = null
    }

    fun dismissConflict() {
        _conflictState.value = null
    }

    private fun executeStartRideForProfile(profile: RiderProfile, onRideStarted: () -> Unit = {}) {
        viewModelScope.launch {
            prefRepo.setActiveProfileId(profile.id)
            prefRepo.setPreferredUnits(profile.preferredUnits)
            prefRepo.setSelectedSkin(ThemeSkin.fromKey(profile.preferredSkin))
            mockTelemetry.resetDistance()
            app.hardwareTelemetryProvider.resetDistance()
            trackingRepo.startRide(profile.id, profile.weightKg)
            onRideStarted()
        }
    }

    private fun applyProfileSelection(profile: RiderProfile) {
        viewModelScope.launch {
            prefRepo.setActiveProfileId(profile.id)
            prefRepo.setPreferredUnits(profile.preferredUnits)
            prefRepo.setSelectedSkin(ThemeSkin.fromKey(profile.preferredSkin))
            trackingRepo.setRiderContext(profile.id, profile.weightKg)
        }
    }

    fun createProfile(
        name: String,
        weightKg: Double,
        imageUri: String?,
        skin: String,
        units: String
    ) {
        viewModelScope.launch {
            val newProfile = RiderProfile(
                name = name,
                weightKg = weightKg,
                profileImageUri = imageUri,
                preferredSkin = skin,
                preferredUnits = units
            )
            val newId = profileRepo.createProfile(newProfile)
            if (activeProfileId.value == null) {
                prefRepo.setActiveProfileId(newId)
            }
        }
    }

    fun updateProfile(profile: RiderProfile, name: String, weightKg: Double, imageUri: String?, skin: String, units: String) {
        viewModelScope.launch {
            val updated = profile.copy(name = name.trim(), weightKg = weightKg, profileImageUri = imageUri, preferredSkin = skin, preferredUnits = units)
            profileRepo.updateProfile(updated)
            if (activeProfileId.value == profile.id) applyProfileSelection(updated)
        }
    }

    fun deleteProfile(profile: RiderProfile) {
        viewModelScope.launch {
            profileRepo.deleteProfile(profile)
        }
    }
}
