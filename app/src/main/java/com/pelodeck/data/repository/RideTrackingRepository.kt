package com.pelodeck.data.repository

import android.os.SystemClock
import com.pelodeck.data.local.dao.RideSessionDao
import com.pelodeck.data.local.entity.RideSession
import com.pelodeck.domain.model.BikeTelemetry
import com.pelodeck.domain.model.CalorieCalculator
import com.pelodeck.domain.model.TelemetrySource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

enum class RideStatus {
    IDLE,
    IN_PROGRESS,
    PAUSED,
    FINISHED
}

data class ActiveRideState(
    val status: RideStatus = RideStatus.IDLE,
    val elapsedSeconds: Long = 0,
    val distanceMiles: Double = 0.0,
    val currentCadence: Double = 0.0,
    val avgCadence: Double = 0.0,
    val maxCadence: Double = 0.0,
    val currentPowerWatts: Double = 0.0,
    val avgPowerWatts: Double = 0.0,
    val maxPowerWatts: Double = 0.0,
    val currentResistance: Double = 0.0,
    val avgResistance: Double = 0.0,
    val maxResistance: Double = 0.0,
    val totalWorkKiloJoules: Double = 0.0,
    val estimatedCaloriesKcal: Double = 0.0,
    val isSimulated: Boolean = true
)

class RideTrackingRepository(
    private val rideSessionDao: RideSessionDao
) {
    private val _rideState = MutableStateFlow(ActiveRideState())
    val rideState: Flow<ActiveRideState> = _rideState.asStateFlow()

    // Monotonic clock tracking
    private var segmentStartRealtime: Long = 0
    private var accumulatedElapsedMillis: Long = 0
    private var rideStartWallTime: Long = 0

    // Metric accumulators
    private val cadenceSamples = mutableListOf<Double>()
    private val powerSamples = mutableListOf<Double>()
    private val resistanceSamples = mutableListOf<Double>()
    private var accumulatedWorkJoules = 0.0
    private var currentRiderWeightKg = 75.0
    private var currentProfileId: Long = 1

    fun setRiderContext(profileId: Long, weightKg: Double) {
        currentProfileId = profileId
        currentRiderWeightKg = weightKg
    }

    fun startRide(profileId: Long, weightKg: Double) {
        currentProfileId = profileId
        currentRiderWeightKg = weightKg
        rideStartWallTime = System.currentTimeMillis()
        segmentStartRealtime = SystemClock.elapsedRealtime()
        accumulatedElapsedMillis = 0
        cadenceSamples.clear()
        powerSamples.clear()
        resistanceSamples.clear()
        accumulatedWorkJoules = 0.0

        _rideState.value = ActiveRideState(
            status = RideStatus.IN_PROGRESS,
            elapsedSeconds = 0,
            isSimulated = true
        )
    }

    fun pauseRide() {
        if (_rideState.value.status == RideStatus.IN_PROGRESS) {
            val now = SystemClock.elapsedRealtime()
            accumulatedElapsedMillis += (now - segmentStartRealtime)
            _rideState.value = _rideState.value.copy(
                status = RideStatus.PAUSED,
                elapsedSeconds = accumulatedElapsedMillis / 1000
            )
        }
    }

    fun resumeRide() {
        if (_rideState.value.status == RideStatus.PAUSED) {
            segmentStartRealtime = SystemClock.elapsedRealtime()
            _rideState.value = _rideState.value.copy(
                status = RideStatus.IN_PROGRESS
            )
        }
    }

    fun onTelemetryTick(telemetry: BikeTelemetry) {
        val current = _rideState.value
        if (current.status != RideStatus.IN_PROGRESS) return

        // Monotonic elapsed time calculation
        val now = SystemClock.elapsedRealtime()
        val totalMillis = accumulatedElapsedMillis + (now - segmentStartRealtime)
        val elapsedSec = totalMillis / 1000

        // Sample metrics
        val cad = telemetry.cadenceRpm ?: 0.0
        val pwr = telemetry.powerWatts ?: 0.0
        val res = telemetry.resistancePercent ?: 0.0
        val isSim = (telemetry.source == TelemetrySource.MOCK || telemetry.source == TelemetrySource.SIMULATED)

        if (cad > 0.0) cadenceSamples.add(cad)
        if (pwr > 0.0) powerSamples.add(pwr)
        if (res > 0.0) resistanceSamples.add(res)

        // Work in Joules: Watts * seconds
        accumulatedWorkJoules += pwr
        val totalKj = accumulatedWorkJoules / 1000.0

        // Calorie calculation with clear physical estimation model
        val calories = if (powerSamples.isNotEmpty() && totalKj > 0.1) {
            CalorieCalculator.calculateFromWork(totalKj)
        } else {
            val hours = elapsedSec / 3600.0
            val avgCad = if (cadenceSamples.isNotEmpty()) cadenceSamples.average() else 0.0
            CalorieCalculator.calculateFromMet(currentRiderWeightKg, hours, avgCad)
        }

        val avgCad = if (cadenceSamples.isNotEmpty()) cadenceSamples.average() else 0.0
        val maxCad = if (cadenceSamples.isNotEmpty()) cadenceSamples.maxOrNull() ?: 0.0 else 0.0

        val avgPwr = if (powerSamples.isNotEmpty()) powerSamples.average() else 0.0
        val maxPwr = if (powerSamples.isNotEmpty()) powerSamples.maxOrNull() ?: 0.0 else 0.0

        val avgRes = if (resistanceSamples.isNotEmpty()) resistanceSamples.average() else 0.0
        val maxRes = if (resistanceSamples.isNotEmpty()) resistanceSamples.maxOrNull() ?: 0.0 else 0.0

        _rideState.value = current.copy(
            elapsedSeconds = elapsedSec,
            distanceMiles = telemetry.estimatedDistanceMiles ?: current.distanceMiles,
            currentCadence = cad,
            avgCadence = (avgCad * 10).toInt() / 10.0,
            maxCadence = (maxCad * 10).toInt() / 10.0,
            currentPowerWatts = pwr,
            avgPowerWatts = (avgPwr * 10).toInt() / 10.0,
            maxPowerWatts = (maxPwr * 10).toInt() / 10.0,
            currentResistance = res,
            avgResistance = (avgRes * 10).toInt() / 10.0,
            maxResistance = (maxRes * 10).toInt() / 10.0,
            totalWorkKiloJoules = (totalKj * 10).toInt() / 10.0,
            estimatedCaloriesKcal = (calories * 10).toInt() / 10.0,
            isSimulated = isSim
        )
    }

    suspend fun finishRide(): RideSession? {
        val current = _rideState.value
        if (current.status == RideStatus.IDLE) return null

        val finalElapsedSec = if (current.status == RideStatus.IN_PROGRESS) {
            val now = SystemClock.elapsedRealtime()
            (accumulatedElapsedMillis + (now - segmentStartRealtime)) / 1000
        } else {
            accumulatedElapsedMillis / 1000
        }

        val session = RideSession(
            riderProfileId = currentProfileId,
            startTime = rideStartWallTime,
            endTime = System.currentTimeMillis(),
            elapsedActiveSeconds = finalElapsedSec,
            state = "FINISHED",
            totalDistanceMiles = current.distanceMiles,
            avgCadence = current.avgCadence,
            maxCadence = current.maxCadence,
            avgPowerWatts = current.avgPowerWatts,
            maxPowerWatts = current.maxPowerWatts,
            avgResistance = current.avgResistance,
            maxResistance = current.maxResistance,
            caloriesKcal = current.estimatedCaloriesKcal,
            isSimulated = current.isSimulated
        )

        val id = rideSessionDao.insert(session)
        val savedSession = session.copy(id = id)

        _rideState.value = ActiveRideState(status = RideStatus.IDLE)
        accumulatedElapsedMillis = 0
        return savedSession
    }

    fun discardRide() {
        _rideState.value = ActiveRideState(status = RideStatus.IDLE)
        accumulatedElapsedMillis = 0
    }

    fun getAllSessions(): Flow<List<RideSession>> = rideSessionDao.getAllSessions()

    fun getSessionsForProfile(profileId: Long): Flow<List<RideSession>> =
        rideSessionDao.getSessionsForProfile(profileId)
}
