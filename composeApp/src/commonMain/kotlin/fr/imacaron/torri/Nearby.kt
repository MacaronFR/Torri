package fr.imacaron.torri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.Channel

abstract class Nearby {
	abstract fun startAdvertising(star: Boolean)

	abstract fun stopAdvertising()

	abstract fun startDiscovery(star: Boolean, type: Type = Type.UNKNOWN)

	abstract fun stopDiscovery()

	abstract fun connect(device: Device)

	abstract fun disconnectDevice(id: String)

	abstract fun disconnectAll()

	abstract fun sendData(data: ByteArray)

	abstract fun sendDataTo(data: ByteArray, id: String)

	abstract suspend fun receiveData(): ByteArray?

	abstract suspend fun startReceive(): Channel<Message>

	abstract var advertising: Boolean
		protected set

	abstract var discovering: Boolean
		protected set

	var connected: Device? by mutableStateOf(null)
		protected set

	var type: Type by mutableStateOf(Type.UNKNOWN)
		protected set

	val connectedList: MutableList<Device> = mutableStateListOf()

	var connecting: Device? by mutableStateOf(null)
		protected set

	var error: String? by mutableStateOf(null)
		protected set

	var master: Boolean by mutableStateOf(false)
		protected set

	val discoveredDevices: MutableList<Device> = mutableStateListOf()

	protected var star by mutableStateOf(false)

	var onConnected: Nearby.(Device) -> Unit = {}

	data class Device(val name: String, val id: String, var type: Type = Type.UNKNOWN)

	data class Message(val data: ByteArray, val id: String)

	enum class Type {
		UNKNOWN,
		MASTER,
		SLAVE_COMMAND,
		SLAVE_KITCHEN
	}
}
