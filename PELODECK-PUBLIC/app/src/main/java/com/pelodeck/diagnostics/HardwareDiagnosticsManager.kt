package com.pelodeck.diagnostics

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.webkit.WebViewCompat

data class DiagnosticReport(
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val buildFingerprint: String,
    val manufacturer: String,
    val model: String,
    val hardware: String,
    val board: String,
    val webViewPackage: String,
    val webViewVersion: String,
    val bluetoothStatus: String,
    val bondedBluetoothDevices: List<String>,
    val usbDevices: List<String>,
    val sensors: List<String>,
    val pelotonServicesDetected: List<String>
)

class HardwareDiagnosticsManager(private val context: Context) {

    fun generateReport(): DiagnosticReport {
        DiagnosticLogger.i("Diagnostics", "Gathering system hardware diagnostic report...")

        // OS and Build details
        val androidVersion = Build.VERSION.RELEASE ?: "Unknown"
        val apiLevel = Build.VERSION.SDK_INT
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH ?: "N/A"
        } else {
            "N/A"
        }
        val buildFingerprint = Build.FINGERPRINT ?: "Unknown"
        val manufacturer = Build.MANUFACTURER ?: "Unknown"
        val model = Build.MODEL ?: "Unknown"
        val hardware = Build.HARDWARE ?: "Unknown"
        val board = Build.BOARD ?: "Unknown"

        // WebView Provider
        val webViewPackageInfo = try {
            WebViewCompat.getCurrentWebViewPackage(context)
        } catch (e: Exception) {
            DiagnosticLogger.w("Diagnostics", "WebView query failed: ${e.message}")
            null
        }
        val webViewPackage = webViewPackageInfo?.packageName ?: "System Default"
        val webViewVersion = webViewPackageInfo?.versionName ?: "Unknown"

        // Bluetooth devices
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val btAdapter = btManager?.adapter
        val bluetoothStatus = when {
            btAdapter == null -> "Not Supported"
            btAdapter.isEnabled -> "Enabled (Ready)"
            else -> "Disabled"
        }

        val bondedDevices = mutableListOf<String>()
        try {
            if (btAdapter != null && btAdapter.isEnabled) {
                btAdapter.bondedDevices?.forEach { device ->
                    bondedDevices.add("${device.name ?: "Unknown"} [${device.address}]")
                }
            }
        } catch (e: SecurityException) {
            bondedDevices.add("Permission BLUETOOTH_CONNECT required to list bonded devices")
        }

        // USB devices
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
        val usbList = mutableListOf<String>()
        usbManager?.deviceList?.forEach { (name, device) ->
            val vId = "0x" + device.vendorId.toString(16).padStart(4, '0')
            val pId = "0x" + device.productId.toString(16).padStart(4, '0')
            usbList.add("$name (${device.productName ?: "Device"}, VID:$vId, PID:$pId, Class:${device.deviceClass})")
        }

        // Sensors
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensorList = mutableListOf<String>()
        sensorManager?.getSensorList(Sensor.TYPE_ALL)?.forEach { sensor ->
            sensorList.add("${sensor.name} (${sensor.stringType}, Vendor: ${sensor.vendor})")
        }

        // Peloton system services
        val pm = context.packageManager
        val checkedPackages = listOf(
            "com.peloton.service",
            "com.peloton.sensorservice",
            "com.peloton.otaserver",
            "com.peloton.updater",
            "dev.digitalducktape.openride",
            "dev.digitalducktape.openride.real",
            "org.cagnulen.qdomyoszwift"
        )
        val detectedServices = mutableListOf<String>()
        for (pkg in checkedPackages) {
            try {
                val info = pm.getPackageInfo(pkg, 0)
                detectedServices.add("$pkg (v${info.versionName})")
                DiagnosticLogger.i("Diagnostics", "Detected installed package: $pkg")
            } catch (_: Exception) {
                // Not found
            }
        }

        DiagnosticLogger.i("Diagnostics", "Report generated successfully: Model=$model, USB=${usbList.size}, Sensors=${sensorList.size}")

        return DiagnosticReport(
            androidVersion = androidVersion,
            apiLevel = apiLevel,
            securityPatch = securityPatch,
            buildFingerprint = buildFingerprint,
            manufacturer = manufacturer,
            model = model,
            hardware = hardware,
            board = board,
            webViewPackage = webViewPackage,
            webViewVersion = webViewVersion,
            bluetoothStatus = bluetoothStatus,
            bondedBluetoothDevices = bondedDevices,
            usbDevices = usbList,
            sensors = sensorList,
            pelotonServicesDetected = detectedServices
        )
    }
}
