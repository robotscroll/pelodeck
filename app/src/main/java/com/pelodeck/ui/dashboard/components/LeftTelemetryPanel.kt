package com.pelodeck.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pelodeck.domain.model.BikeTelemetry
import com.pelodeck.ui.theme.PeloDeckTheme

@Composable
fun LeftTelemetryPanel(
    telemetry: BikeTelemetry,
    preferredUnits: String,
    modifier: Modifier = Modifier
) {
    val colors = PeloDeckTheme.colors
    val isMetric = preferredUnits.equals("METRIC", ignoreCase = true)

    val cadence = telemetry.cadenceRpm ?: 0.0
    val cadenceProgress = (cadence / 130.0).toFloat()
    val cadenceColor = when {
        cadence < 60 -> colors.secondary
        cadence <= 90 -> colors.primary
        cadence <= 110 -> colors.accent
        else -> colors.secondary
    }

    val power = telemetry.powerWatts ?: 0.0
    val powerProgress = (power / 400.0).toFloat()

    val distance = telemetry.estimatedDistanceMiles ?: 0.0
    val displayDistance = if (isMetric) distance * 1.60934 else distance
    val distanceUnit = if (isMetric) "KM" else "MI"

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.background)
            .border(1.dp, colors.panelBorder, RoundedCornerShape(8.dp))
            .padding(6.dp)
            ,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "OUTPUT METRICS",
            color = colors.textSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        MetricCard(
            label = "Cadence",
            value = telemetry.cadenceRpm?.let { String.format("%.0f", it) } ?: "--",
            unit = "RPM",
            progress = cadenceProgress,
            progressColor = cadenceColor,
            modifier = Modifier.weight(1f),
            subtitle = when {
                cadence <= 0.0 -> "IDLE"
                cadence < 70 -> "WARMUP"
                cadence in 70.0..90.0 -> "ENDURANCE"
                cadence in 90.1..110.0 -> "TEMPO"
                else -> "SPRINT"
            }
        )

        MetricCard(
            label = "Power Output",
            value = telemetry.powerWatts?.let { String.format("%.0f", it) } ?: "--",
            unit = "WATTS",
            progress = powerProgress,
            progressColor = colors.accent,
            modifier = Modifier.weight(1f),
            subtitle = if (power > 0.0) String.format("%.0f%% REF", (power / 200.0) * 100) else null
        )

        MetricCard(
            label = "Distance",
            value = telemetry.estimatedDistanceMiles?.let { String.format("%.2f", displayDistance) } ?: "--",
            unit = distanceUnit,
            subtitle = if (isMetric) "METRIC" else "IMPERIAL",
            footer = "Est. speed: " + (telemetry.estimatedSpeedMph?.let {
                String.format("%.1f %s", if (isMetric) it * 1.60934 else it, if (isMetric) "km/h" else "mph")
            } ?: "--"),
            modifier = Modifier.weight(1f)
        )
    }
}
