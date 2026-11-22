package com.pelodeck.ui.diagnostics

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pelodeck.diagnostics.DiagnosticReport
import com.pelodeck.diagnostics.LogLevel
import com.pelodeck.ui.theme.PeloDeckTheme

@Composable
fun DiagnosticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiagnosticsViewModel = viewModel()
) {
    val report by viewModel.report.collectAsState()
    val logEntries by viewModel.logEntries.collectAsState()
    val probeResult by viewModel.probeResult.collectAsState()
    val colors = PeloDeckTheme.colors

    var selectedTab by remember { mutableIntStateOf(0) }

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
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HARDWARE DIAGNOSTICS & SYSTEM DISCOVERY",
                    color = colors.primary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.runHardwareProbe() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.buttonBackground,
                        contentColor = colors.primary
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PROBE SENSORS", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }

                IconButton(onClick = { viewModel.refreshReport() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.textPrimary)
                }
            }
        }

        if (probeResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.accent, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "PROBE RESULT: $probeResult",
                    color = colors.accent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }

        // Tabs: System Specs vs Live Diagnostic Logs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colors.surface,
            contentColor = colors.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = colors.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("HARDWARE SPECS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("DIAGNOSTIC LOGS (${logEntries.size})", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> HardwareSpecsView(report = report)
            1 -> DiagnosticLogsView(
                logEntries = logEntries,
                onClear = { viewModel.clearLogs() }
            )
        }
    }
}

@Composable
fun HardwareSpecsView(report: DiagnosticReport?) {
    val colors = PeloDeckTheme.colors

    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("PROBING HARDWARE INTERFACES...", color = colors.textSecondary, fontFamily = FontFamily.Monospace)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DiagnosticSection("OPERATING SYSTEM & TABLET BUILD") {
            DiagRow("Android OS Version", "${report.androidVersion} (API ${report.apiLevel})")
            DiagRow("Security Patch", report.securityPatch)
            DiagRow("Device Manufacturer", report.manufacturer)
            DiagRow("Model", report.model)
            DiagRow("Board / Hardware", "${report.board} / ${report.hardware}")
            DiagRow("Build Fingerprint", report.buildFingerprint)
        }

        DiagnosticSection("WEBVIEW RUNTIME PROVIDER") {
            DiagRow("WebView Package", report.webViewPackage)
            DiagRow("WebView Engine Version", report.webViewVersion)
        }

        DiagnosticSection("CONNECTED PERIPHERALS & INTERFACES") {
            DiagRow("Bluetooth Adapter Status", report.bluetoothStatus)
            DiagRow(
                "Bonded Bluetooth Devices",
                if (report.bondedBluetoothDevices.isEmpty()) "None paired" else report.bondedBluetoothDevices.joinToString("\n")
            )
            DiagRow(
                "Connected USB Peripherals",
                if (report.usbDevices.isEmpty()) "No external USB devices detected" else report.usbDevices.joinToString("\n")
            )
        }

        DiagnosticSection("ACCESSIBLE SENSORS & SYSTEM SERVICES") {
            DiagRow(
                "Hardware Sensors",
                if (report.sensors.isEmpty()) "No standard Android sensors exposed" else report.sensors.take(8).joinToString("\n") + if (report.sensors.size > 8) "\n...and ${report.sensors.size - 8} more" else ""
            )
            DiagRow(
                "Detected Peloton Packages",
                if (report.pelotonServicesDetected.isEmpty()) "None detected (Stock/Open Android environment)" else report.pelotonServicesDetected.joinToString("\n")
            )
        }
    }
}

@Composable
fun DiagnosticLogsView(
    logEntries: List<com.pelodeck.diagnostics.DiagnosticLogEntry>,
    onClear: () -> Unit
) {
    val colors = PeloDeckTheme.colors

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onClear) {
                Text("CLEAR LOGS", color = colors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surface)
                .border(1.dp, colors.panelBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logEntries) { entry ->
                    val levelColor = when (entry.level) {
                        LogLevel.DEBUG -> colors.textSecondary
                        LogLevel.INFO -> colors.primary
                        LogLevel.WARN -> colors.secondary
                        LogLevel.ERROR -> colors.accent
                    }
                    Text(
                        text = "[${entry.formattedTime}] [${entry.level.name}] [${entry.tag}] ${entry.message}",
                        color = levelColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticSection(title: String, content: @Composable () -> Unit) {
    val colors = PeloDeckTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.panelBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = colors.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    val colors = PeloDeckTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.65f)
        )
    }
}
