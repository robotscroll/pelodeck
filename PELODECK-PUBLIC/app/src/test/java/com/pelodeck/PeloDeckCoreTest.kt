package com.pelodeck

import com.pelodeck.data.telemetry.MockTelemetryProvider
import com.pelodeck.domain.model.CalorieCalculator
import com.pelodeck.domain.model.ThemeSkin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PeloDeckCoreTest {

    @Test
    fun testThemeSkinKeyResolution() {
        assertEquals(ThemeSkin.WINAMP, ThemeSkin.fromKey("winamp"))
        assertEquals(ThemeSkin.CYBERPUNK, ThemeSkin.fromKey("cyberpunk"))
        assertEquals(ThemeSkin.MINIMAL, ThemeSkin.fromKey("minimal"))
        assertEquals(ThemeSkin.MATRIX, ThemeSkin.fromKey("matrix"))
        assertEquals(ThemeSkin.CINNAMOROLL, ThemeSkin.fromKey("cinnamoroll"))
        // Fallback default
        assertEquals(ThemeSkin.WINAMP, ThemeSkin.fromKey("unknown_skin"))
        assertEquals(ThemeSkin.WINAMP, ThemeSkin.fromKey(null))
    }

    @Test
    fun testCalorieCalculationFromWork() {
        // 100 kJ of mechanical work at ~24% gross efficiency ≈ 99.5 kcal
        val kcal = CalorieCalculator.calculateFromWork(100.0)
        assertEquals(99.5, kcal, 0.5)

        // Zero work
        assertEquals(0.0, CalorieCalculator.calculateFromWork(0.0), 0.01)
    }

    @Test
    fun testCalorieCalculationFromMet() {
        // 75kg rider, 1 hour, 80 rpm (MET 8.5) -> 8.5 * 75 * 1.0 = 637.5 kcal
        val kcal = CalorieCalculator.calculateFromMet(75.0, 1.0, 80.0)
        assertEquals(637.5, kcal, 1.0)
    }

    @Test
    fun testMockPowerPhysics() {
        // Cadence 80, Resistance 40
        val power = MockTelemetryProvider.calculateSimulatedPower(80.0, 40.0)
        assertTrue("Simulated power should be realistic (90W-250W)", power in 90.0..250.0)

        // Cadence 0 -> 0 Watts
        val zeroPower = MockTelemetryProvider.calculateSimulatedPower(0.0, 50.0)
        assertEquals(0.0, zeroPower, 0.01)
    }

    @Test
    fun testMockTelemetryProviderFlow() = runBlocking {
        val provider = MockTelemetryProvider()
        val initial = provider.telemetry.first()
        assertNotNull(initial)

        val connected = provider.connect()
        assertTrue(connected)

        val active = provider.telemetry.first()
        assertTrue("Telemetry provider should report connected", active.isConnected)

        provider.disconnect()
        val disconnected = provider.telemetry.first()
        assertTrue("Telemetry provider should report disconnected", !disconnected.isConnected)
    }
}
