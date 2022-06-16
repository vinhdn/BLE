package com.beepiz.blegattcoroutines.sample.common

import android.Manifest
import android.bluetooth.BluetoothGattCharacteristic
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beepiz.blegattcoroutines.genericaccess.GenericAccess
import com.beepiz.blegattcoroutines.sample.common.extensions.deviceFor
import com.beepiz.blegattcoroutines.sample.common.extensions.print
import com.beepiz.blegattcoroutines.sample.common.extensions.printWithCharacteristics
import com.beepiz.blegattcoroutines.sample.common.extensions.useBasic
import com.beepiz.bluetooth.gattcoroutines.BGD
import kotlinx.coroutines.*
import splitties.toast.UnreliableToastApi
import splitties.toast.toast
import timber.log.Timber
import java.util.*

@Suppress("InlinedApi")
class MainViewModel : ViewModel() {

    private val myEddystoneUrlBeaconMacAddress = "F2:D6:43:93:70:7A"
    private val iBks12MacAddress = "F6:61:CF:AF:D0:07"
    private val iBksPlusMacAddress = "BA:03:4C:42:37:81"
    private val defaultDeviceMacAddress = iBksPlusMacAddress

    private var operationAttempt: Job? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @RequiresApi(18)
    @OptIn(UnreliableToastApi::class)
    fun logNameAndAppearance(
        deviceMacAddress: String = defaultDeviceMacAddress,
        connectionTimeoutInMillis: Long = 15000L
    ) {
        operationAttempt?.cancel()
        operationAttempt = viewModelScope.launch(Dispatchers.Main) {
            deviceFor(deviceMacAddress).useBasic(connectionTimeoutInMillis) { device, services ->
                services.forEach { Timber.d("Service found with UUID: ${it.uuid}") }
                services.forEach { Timber.d(it.printWithCharacteristics()) }
                val a = with(GenericAccess) {
                    device.readAppearance()
                    Timber.d("Device appearance: ${device.appearance}")
                    device.readDeviceName()
                    Timber.d("Device name: ${device.deviceName}".also { toast(it) })
                    device.allNotifications.collect {
                        Timber.i(it.print())
                    }
                    device.readCharacteristic(BluetoothGattCharacteristic(UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"), 0x01, 0).apply {
                        value = "57480D30013430322E3437353000000000".toByteArray()
                    }).apply {
                        Timber.d("data ${this.value}")
                    }
                    device.writeCharacteristic(BluetoothGattCharacteristic(UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"), 0x01, 0).apply {
                        value = "57480D30013430322E3437353000000000".toByteArray()
                    }).apply {
                        Timber.d("data ${this.print()}")
                    }
                    device.readDescriptor(BGD(UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"), 0).apply {
                        value = "57480D30013430322E3437353000000000".toByteArray()
                    }).apply {
                        Timber.d("data ${this.print()}")
                    }
                }
            }
            operationAttempt = null
        }
    }
}
