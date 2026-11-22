package com.pelodeck.data.telemetry

import com.pelodeck.domain.model.BikeTelemetry
import com.pelodeck.domain.model.TelemetrySource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cbrt
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * Realistic Mock Telemetry Provider for cycling simulation.
 * Emits dynamic, physically consistent telemetry updates approximately once per second.
 */
class MockTelemetryProvider : TelemetryProvider {

    private val _telemetry = MutableStateFlow(
        BikeTelemetry(
            source = TelemetrySource.MOCK,
            isConnected = false
        )
    )
    override val telemetry: Flow<BikeTelemetry> = _telemetry.asStateFlow()

    private var simulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Simulation state
    private var baseCadence = 80.0
    private var baseResistance = 42.0
    private var accumulatedDistanceMiles = 0.0
    private var tick = 0

    override suspend fun connect(): Boolean {
        if (simulationJob?.isActive == true) return true

        _telemetry.value = _telemetry.value.copy(
            isConnected = true,
            source = TelemetrySource.MOCK
        )

        simulationJob = scope.launch {
            while (isActive) {
                tick++
                // Gentle sinusoidal wave with realistic pedal jitter
                val cadenceVariance = sin(tick * 0.15) * 8.0 + (Random.nextDouble(-1.5, 1.5))
                val cadence = (baseCadence + cadenceVariance).coerceIn(40.0, 115.0)

                // Resistance drifts slowly or stays stable like a rider adjusting knob
                val resistanceVariance = sin(tick * 0.05) * 3.0 + (Random.nextDouble(-0.3, 0.3))
                val resistance = (baseResistance + resistanceVariance).coerceIn(20.0, 85.0)

                // Realistic cycling power estimate (watts) derived from cadence & resistance
                // Standard calibration model for magnetic resistance indoor flywheel
                val power = calculateSimulatedPower(cadence, resistance)

                // Aerodynamic speed model: speed ≈ k * cbrt(power)
                val speedMph = if (power > 5.0) {
                    (3.55 * cbrt(power)).coerceIn(0.0, 35.0)
                } else {
                    0.0
                }

                // Distance accumulation: speed in miles/hour divided by 3600 seconds
                accumulatedDistanceMiles += (speedMph / 3600.0)

                _telemetry.value = BikeTelemetry(
                    timestamp = System.currentTimeMillis(),
                    cadenceRpm = (cadence * 10).toInt() / 10.0,
                    resistancePercent = (resistance * 10).toInt() / 10.0,
                    powerWatts = (power * 10).toInt() / 10.0,
                    estimatedSpeedMph = (speedMph * 10).toInt() / 10.0,
                    estimatedDistanceMiles = (accumulatedDistanceMiles * 100).toInt() / 100.0,
                    source = TelemetrySource.MOCK,
                    isConnected = true
                )

                delay(1000L)
            }
        }
        return true
    }

    override suspend fun disconnect() {
        simulationJob?.cancel()
        simulationJob = null
        _telemetry.value = _telemetry.value.copy(
            isConnected = false
        )
    }

    fun resetDistance() {
        accumulatedDistanceMiles = 0.0
    }

    companion object {
        /**
         * Simulates bike power curve based on cadence and magnetic resistance.
         * Modeled after typical eddy current brake resistance curves.
         */
        fun calculateSimulatedPower(cadence: Double, resistance: Double): Double {
            if (cadence < 10.0) return 0.0
            val normalizedResistance = resistance / 100.0
            // Empirical indoor cycling wattage curve
            val resistanceFactor = normalizedResistance * normalizedResistance * 350.0 + (normalizedResistance * 130.0)
            val cadenceFactor = cadence / 80.0
            val power = resistanceFactor * cadenceFactor
            return max(0.0, power)
        }
    }
}
