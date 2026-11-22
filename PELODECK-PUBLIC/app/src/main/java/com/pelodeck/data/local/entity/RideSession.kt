package com.pelodeck.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_sessions")
data class RideSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val riderProfileId: Long,
    val startTime: Long,
    val endTime: Long? = null,
    val elapsedActiveSeconds: Long = 0,
    val state: String = "FINISHED", // IN_PROGRESS, PAUSED, FINISHED
    val totalDistanceMiles: Double = 0.0,
    val avgCadence: Double = 0.0,
    val maxCadence: Double = 0.0,
    val avgPowerWatts: Double = 0.0,
    val maxPowerWatts: Double = 0.0,
    val avgResistance: Double = 0.0,
    val maxResistance: Double = 0.0,
    val caloriesKcal: Double = 0.0,
    val isSimulated: Boolean = true // Flags mock/simulated telemetry vs verified physical sensor
)
