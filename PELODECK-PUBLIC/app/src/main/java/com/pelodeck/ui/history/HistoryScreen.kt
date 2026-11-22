package com.pelodeck.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pelodeck.data.local.entity.RideSession
import com.pelodeck.ui.theme.PeloDeckTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val sessions by viewModel.allSessions.collectAsState()
    val preferredUnits by viewModel.preferredUnits.collectAsState()
    val colors = PeloDeckTheme.colors
    val isMetric = preferredUnits.equals("METRIC", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WORKOUT HISTORY",
                color = colors.primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "(${sessions.size} rides recorded)",
                color = colors.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO COMPLETED RIDES YET. START A WORKOUT FROM THE DASHBOARD!",
                    color = colors.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    RideHistoryCard(
                        session = session,
                        isMetric = isMetric,
                        onDelete = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RideHistoryCard(
    session: RideSession,
    isMetric: Boolean,
    onDelete: () -> Unit
) {
    val colors = PeloDeckTheme.colors
    val dateFormat = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.US)
    val formattedDate = dateFormat.format(Date(session.startTime))

    val hours = session.elapsedActiveSeconds / 3600
    val minutes = (session.elapsedActiveSeconds % 3600) / 60
    val seconds = session.elapsedActiveSeconds % 60
    val formattedDuration = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    val displayDistance = if (isMetric) session.totalDistanceMiles * 1.60934 else session.totalDistanceMiles
    val unitStr = if (isMetric) "km" else "mi"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.panelBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (session.isSimulated) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.secondary.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SIMULATED",
                                color = colors.secondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn("DURATION", formattedDuration, "")
                MetricColumn("DISTANCE", String.format("%.2f", displayDistance), unitStr)
                MetricColumn("AVG CADENCE", String.format("%.0f", session.avgCadence), "rpm")
                MetricColumn("AVG POWER", String.format("%.0f", session.avgPowerWatts), "w")
                MetricColumn("AVG RESIST", String.format("%.0f", session.avgResistance), "%")
                MetricColumn("EST. CALS", String.format("%.0f", session.caloriesKcal), "kcal")
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String, unit: String) {
    val colors = PeloDeckTheme.colors
    Column {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = colors.digitalDisplay,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    color = colors.secondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
