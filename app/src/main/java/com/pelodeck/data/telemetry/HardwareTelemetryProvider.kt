package com.pelodeck.data.telemetry

import android.content.Context
import android.os.SystemClock
import com.pelodeck.domain.model.BikeTelemetry
import com.pelodeck.domain.model.TelemetrySource
import dev.digitalducktape.openride.core.sensor.AffernetBikeDataSource
import dev.digitalducktape.openride.core.sensor.ConnectionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** Reads the bike's on-device service. A live frame is required for connected status. */
class HardwareTelemetryProvider(context: Context) : TelemetryProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val source = AffernetBikeDataSource(context.applicationContext, scope)
    private val state = MutableStateFlow(BikeTelemetry(source = TelemetrySource.PELOTON_SERVICE))
    override val telemetry: Flow<BikeTelemetry> = state.asStateFlow()
    private var started = false
    private var observer: Job? = null
    private var distanceMiles = 0.0
    private var lastTick: Long? = null

    fun resetDistance() { distanceMiles = 0.0; lastTick = null }

    override suspend fun connect(): Boolean {
        if (!started) {
            started = true
            source.start()
            observer = scope.launch {
                while (isActive) {
                    val metrics = source.metrics.value
                    val connected = source.connectionState.value == ConnectionState.Connected
                    val now = SystemClock.elapsedRealtime()
                    val elapsed = lastTick?.let { (now - it).coerceIn(0, 2000) } ?: 0L
                    lastTick = if (connected) now else null
                    if (connected) distanceMiles += metrics.speedMph * elapsed / 3_600_000.0
                    state.value = BikeTelemetry(timestamp = System.currentTimeMillis(),
                        cadenceRpm = metrics.cadenceRpm.toDouble().takeIf { connected },
                        resistancePercent = metrics.resistancePercent.toDouble().takeIf { connected },
                        powerWatts = metrics.powerWatts.toDouble().takeIf { connected },
                        estimatedSpeedMph = metrics.speedMph.takeIf { connected },
                        estimatedDistanceMiles = distanceMiles.takeIf { connected },
                        source = TelemetrySource.PELOTON_SERVICE, isConnected = connected)
                    delay(1000)
                }
            }
        }
        return withTimeoutOrNull(6000) { state.first { it.isConnected }; true } ?: false
    }

    override suspend fun disconnect() {
        observer?.cancel()
        source.stop()
        started = false
        state.value = BikeTelemetry(source = TelemetrySource.PELOTON_SERVICE)
    }
}
