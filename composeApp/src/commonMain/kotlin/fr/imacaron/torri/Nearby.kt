package fr.imacaron.torri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

abstract class Nearby {
	abstract fun startAdvertising()

	abstract fun stopAdvertising()

	abstract fun startDiscovery()

	abstract fun stopDiscovery()

	abstract fun connect(device: Device)

	abstract fun disconnect()

	abstract fun sendData(data: ByteArray)

	abstract suspend fun receiveData(): ByteArray?

	abstract var advertising: Boolean
		protected set

	abstract var discovering: Boolean
		protected set

	var connected: Device? by mutableStateOf(null)
		protected set

	var connecting: Device? by mutableStateOf(null)
		protected set

	var error: String? by mutableStateOf(null)
		protected set

	var master: Boolean by mutableStateOf(false)
		protected set

	val discoveredDevices: MutableList<Device> = mutableStateListOf()

	data class Device(val name: String, val id: String)
}
