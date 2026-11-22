package com.pelodeck.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pelodeck.PeloDeckApp
import com.pelodeck.data.local.entity.RideSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val app: PeloDeckApp = PeloDeckApp.instance
) : ViewModel() {

    private val trackingRepo = app.rideTrackingRepository
    private val prefRepo = app.preferencesRepository
    private val database = app.database

    val allSessions: StateFlow<List<RideSession>> = trackingRepo.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferredUnits: StateFlow<String> = prefRepo.preferredUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "IMPERIAL")

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            database.rideSessionDao().deleteById(sessionId)
        }
    }
}
