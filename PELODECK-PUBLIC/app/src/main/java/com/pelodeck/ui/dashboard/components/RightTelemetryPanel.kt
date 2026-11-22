package com.pelodeck.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.pelodeck.data.repository.ActiveRideState
import com.pelodeck.domain.model.BikeTelemetry
import com.pelodeck.ui.theme.PeloDeckTheme

@Composable
fun RightTelemetryPanel(
    telemetry: BikeTelemetry,
    activeRide: ActiveRideState,
    modifier: Modifier = Modifier
) {
    val colors = PeloDeckTheme.colors

    val resistance = telemetry.resistancePercent ?: 0.0
    val resistanceProgress = (resistance / 100.0).toFloat()

    val elapsedSec = activeRide.elapsedSeconds
    val hours = elapsedSec / 3600
    val minutes = (elapsedSec % 3600) / 60
    val seconds = elapsedSec % 60
    val formattedDuration = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    val calories = activeRide.estimatedCaloriesKcal

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
            text = "EFFORT & WORK",
            color = colors.textSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        MetricCard(
            label = "Resistance",
            value = telemetry.resistancePercent?.let { String.format("%.0f", it) } ?: "--",
            unit = "%",
            progress = resistanceProgress,
            progressColor = colors.secondary,
            modifier = Modifier.weight(1f),
            subtitle = "MANUAL KNOB"
        )

        MetricCard(
            label = "Est. Calories",
            value = String.format("%.0f", calories),
            unit = "KCAL",
            progress = null,
            modifier = Modifier.weight(1f),
            subtitle = if (activeRide.totalWorkKiloJoules > 0.1) {
                String.format("%.0f kJ WORK", activeRide.totalWorkKiloJoules)
            } else {
                "MET ESTIMATE"
            }
        )

        MetricCard(
            label = "Ride Duration",
            value = formattedDuration,
            unit = if (hours > 0) "HR:MIN:SEC" else "MIN:SEC",
            progress = null,
            modifier = Modifier.weight(1f),
            subtitle = activeRide.status.name
        )
    }
}
