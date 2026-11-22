package com.pelodeck.data.telemetry

import com.pelodeck.domain.model.BikeTelemetry
import kotlinx.coroutines.flow.Flow

interface TelemetryProvider {
    val telemetry: Flow<BikeTelemetry>
    suspend fun connect(): Boolean
    suspend fun disconnect()
}
