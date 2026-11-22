package com.pelodeck.ui.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pelodeck.PeloDeckApp
import com.pelodeck.data.telemetry.HardwareTelemetryProvider
import com.pelodeck.diagnostics.DiagnosticLogEntry
import com.pelodeck.diagnostics.DiagnosticLogger
import com.pelodeck.diagnostics.DiagnosticReport
import com.pelodeck.diagnostics.HardwareDiagnosticsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiagnosticsViewModel(
    private val app: PeloDeckApp = PeloDeckApp.instance
) : ViewModel() {

    private val diagnosticsManager: HardwareDiagnosticsManager = app.diagnosticsManager
    private val hardwareProvider: HardwareTelemetryProvider = app.hardwareTelemetryProvider

    private val _report = MutableStateFlow<DiagnosticReport?>(null)
    val report: StateFlow<DiagnosticReport?> = _report.asStateFlow()

    val logEntries: StateFlow<List<DiagnosticLogEntry>> = DiagnosticLogger.logsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _probeResult = MutableStateFlow<String?>(null)
    val probeResult: StateFlow<String?> = _probeResult.asStateFlow()

    init {
        refreshReport()
    }

    fun refreshReport() {
        viewModelScope.launch {
            _report.value = diagnosticsManager.generateReport()
        }
    }

    fun runHardwareProbe() {
        viewModelScope.launch {
            DiagnosticLogger.i("Diagnostics", "Initiating read-only hardware telemetry probe...")
            val connected = hardwareProvider.connect()
            _probeResult.value = if (connected) {
                val data = hardwareProvider.telemetry.first()
                "Bike data received: cadence ${data.cadenceRpm?.toInt()} rpm, resistance ${data.resistancePercent?.toInt()}%, power ${data.powerWatts?.toInt()} W."
            } else {
                "No bike data received. Check the bike connection and try again."
            }
            refreshReport()
        }
    }

    fun clearLogs() {
        DiagnosticLogger.clear()
    }
}
