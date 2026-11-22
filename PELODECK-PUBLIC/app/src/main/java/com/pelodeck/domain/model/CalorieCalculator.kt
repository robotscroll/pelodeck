package com.pelodeck.domain.model

object CalorieCalculator {
    /**
     * Calculates estimated calories from mechanical work (kJ).
     * Human mechanical gross efficiency (GE) during cycling is ~24%.
     * 1 kcal = 4.184 kJ.
     * Estimated kcal = Work (kJ) / (4.184 * 0.24) ≈ Work (kJ) * 0.995 ≈ Work (kJ).
     */
    fun calculateFromWork(totalKiloJoules: Double): Double {
        if (totalKiloJoules <= 0.0) return 0.0
        return totalKiloJoules * 0.995
    }

    /**
     * Fallback calculation using Metabolic Equivalent of Task (MET)
     * if power output is not available.
     */
    fun calculateFromMet(weightKg: Double, elapsedHours: Double, avgCadence: Double): Double {
        if (elapsedHours <= 0.0 || weightKg <= 0.0) return 0.0
        val met = when {
            avgCadence <= 0.0 -> 1.3 // resting
            avgCadence < 50.0 -> 4.0 // light cycling
            avgCadence < 70.0 -> 6.8 // moderate cycling
            avgCadence < 90.0 -> 8.5 // vigorous cycling
            else -> 10.5             // racing effort
        }
        return met * weightKg * elapsedHours
    }
}
