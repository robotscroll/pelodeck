package com.pelodeck.domain.model

enum class TelemetrySource(val label: String) {
    MOCK("Mock Simulation"),
    PELOTON_SERVICE("Peloton System Service"),
    SERIAL_RS232("Internal Serial (RS-232)"),
    BLE_CSC("Bluetooth Smart Trainer"),
    SIMULATED("Simulated Engine")
}

data class BikeTelemetry(
    val timestamp: Long = System.currentTimeMillis(),
    val cadenceRpm: Double? = null,
    val resistancePercent: Double? = null,
    val powerWatts: Double? = null,
    val estimatedSpeedMph: Double? = null,
    val estimatedDistanceMiles: Double? = null,
    val source: TelemetrySource = TelemetrySource.MOCK,
    val isConnected: Boolean = false
)
