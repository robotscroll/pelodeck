package com.pelodeck

import android.app.Application
import com.pelodeck.data.local.PeloDeckDatabase
import com.pelodeck.data.local.preferences.UserPreferencesRepository
import com.pelodeck.data.repository.BookmarkRepository
import com.pelodeck.data.repository.RideTrackingRepository
import com.pelodeck.data.repository.RiderProfileRepository
import com.pelodeck.data.telemetry.HardwareTelemetryProvider
import com.pelodeck.data.telemetry.MockTelemetryProvider
import com.pelodeck.diagnostics.HardwareDiagnosticsManager

class PeloDeckApp : Application() {

    lateinit var database: PeloDeckDatabase
        private set

    lateinit var profileRepository: RiderProfileRepository
        private set

    lateinit var rideTrackingRepository: RideTrackingRepository
        private set

    lateinit var bookmarkRepository: BookmarkRepository
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var mockTelemetryProvider: MockTelemetryProvider
        private set

    lateinit var hardwareTelemetryProvider: HardwareTelemetryProvider
        private set

    lateinit var diagnosticsManager: HardwareDiagnosticsManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = PeloDeckDatabase.getInstance(this)
        profileRepository = RiderProfileRepository(database.riderProfileDao())
        rideTrackingRepository = RideTrackingRepository(database.rideSessionDao())
        bookmarkRepository = BookmarkRepository(database.bookmarkDao())
        preferencesRepository = UserPreferencesRepository(this)

        mockTelemetryProvider = MockTelemetryProvider()
        hardwareTelemetryProvider = HardwareTelemetryProvider(this)
        diagnosticsManager = HardwareDiagnosticsManager(this)
    }

    companion object {
        lateinit var instance: PeloDeckApp
            private set
    }
}
