package com.beepiz.blegattcoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.beepiz.blegattcoroutines.sample.common.BleScanHeater
import com.beepiz.blegattcoroutines.sample.common.MainViewModel
import com.beepiz.blegattcoroutines.sample.common.bluetoothLeScanner
import com.beepiz.blegattcoroutines.sample.common.scanFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import splitties.dimensions.dip
import splitties.views.dsl.core.add
import splitties.views.dsl.core.button
import splitties.views.dsl.core.contentView
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.verticalLayout
import splitties.views.gravityCenterHorizontal
import splitties.views.onClick
import splitties.views.padding
import timber.log.Timber

@SuppressLint("SetTextI18n") // This is just a sample, English is enough.
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {

            ensureBlePermissionsOrFinishActivity()

            @Suppress("MissingPermission")
            if (SDK_INT >= 21) launch { BleScanHeater.heatUpWhileStarted(lifecycle) }

            @Suppress("MissingPermission")
            if (SDK_INT >= 21) launch {
                val scanSettings = ScanSettings.Builder().apply {
                    setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                }.build()
                bluetoothLeScanner?.scanFlow(listOf(ScanFilter.Builder()
                    .build()), scanSettings)?.collect {
                    if (it.device.type == BluetoothDevice.DEVICE_TYPE_LE)
                    Timber.d("${it.device.address} ${it.device.name} ${it.device.type}")
                }
            }

            @Suppress("MissingPermission")
            contentView = verticalLayout {
                padding = dip(16)
                val lp = lParams(gravity = gravityCenterHorizontal)
                add(button {
                    text = "Log name and appearance of default device"
                    onClick {
                        viewModel.logNameAndAppearance()
                    }
                }, lp)
            }
        }
    }

    private suspend fun ensureBlePermissionsOrFinishActivity() = ensureAllPermissions(
        permissionNames = when {
            SDK_INT >= 31 -> listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            else -> listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        },
        askDialogTitle = "Location permission required",
        askDialogMessage = "Bluetooth Low Energy can be used for location, " +
                "so the permission is required."
    ) { finish(); awaitCancellation() }
}
