package fr.imacaron.torri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.imacaron.torri.ios.NearbySwift
import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel

@OptIn(ExperimentalForeignApi::class)
class Nearbyios: Nearby() {
	@OptIn(ExperimentalForeignApi::class)
	val nearbySwift = NearbySwift()

	init {
		nearbySwift.onDiscoverDevice = { endpointId, endpointName ->
			discoveredDevices.add(Device(endpointName ?: "", endpointId ?: ""))
		}
		nearbySwift.onDeviceLost = { endpointId ->
			discoveredDevices.removeAll { it.id == endpointId }
		}
		nearbySwift.onConnectionStateChange = { state, endpointId ->
			when(state) {
				"CONNECTING" -> {
					connecting = discoveredDevices.find { it.id == endpointId } ?: Device(endpointId ?: "", endpointId ?: "")
				}
				"CONNECTED" -> {
					val device = if(star && master) {
						connecting?.let { connectedList.add(it); it }
					} else {
						connected = connecting
						connected?.let {
							sendDataTo("Type:${this.type}".toByteArray(), it.id)
						}
						connecting
					}
					connecting = null
					device?.let {
						onConnected(it)
					}
				}
				"DISCONNECTED" -> {
					connected = null
				}
				"REJECTED" -> {
					connected = null
					connecting = null
				}
			}
		}
		nearbySwift.onReceiveData = { data, endpointId ->
			val message = data?.toByteArray()?.decodeToString() ?: ""
			if(message.startsWith("Type:")) {
				if(master) {
					connectedList.find { it.id == endpointId }?.let {
						connectedList.remove(it)
						it.type = Type.valueOf(message.substringAfter("Type:"))
						connectedList.add(it)
					}
				} else {
					connected?.let {
						it.type = Type.valueOf(message.substringAfter("Type:"))
					}
				}
				false
			} else {
				true
			}
		}
	}

	@OptIn(ExperimentalForeignApi::class)
	override fun startAdvertising(star: Boolean) {
		this.star = star
		master = true
		type = Type.MASTER
		nearbySwift.startAdvertisingWithStar(star)
		advertising = true
	}

	@OptIn(ExperimentalForeignApi::class)
	override fun stopAdvertising() {
		nearbySwift.stopAdvertising()
		advertising = false
	}

	@OptIn(ExperimentalForeignApi::class)
	override fun startDiscovery(star: Boolean, type: Type) {
		this.star = star
		master = false
		nearbySwift.startDiscoveryWithStar(star)
		this.type = type
		discovering = true
	}

	@OptIn(ExperimentalForeignApi::class)
	override fun stopDiscovery() {
		nearbySwift.stopDiscovery()
		discoveredDevices.clear()
		discovering = false
	}

	override fun connect(device: Device) {
		connecting = device
		nearbySwift.connectWithEndpointId(device.id)
	}

	override fun disconnectDevice(id: String) {
		if(connected?.id == id || connecting?.id == id) {
			nearbySwift.disconnectWithEndpointId(id)
			connected = null
			connecting = null
		} else if(connectedList.find { it.id == id } != null) {
			nearbySwift.disconnectWithEndpointId(id)
			connectedList.removeAll { it.id == id }
		}
	}

	override fun disconnectAll() {
		(connecting?.id ?: connected?.id)?.let { id ->
			nearbySwift.disconnectWithEndpointId(id)
		}
		connectedList.forEach {
			nearbySwift.disconnectWithEndpointId(it.id)
		}
		connecting = null
		connected = null
		connectedList.clear()
		stopDiscovery()
		stopAdvertising()
	}

	override fun sendData(data: ByteArray) {
		println("send data ${connected?.id}")
		if(connected == null) {
			return
		}
		nearbySwift.sendDataWithData(data.toNSData(), connected!!.id)
	}

	override fun sendDataTo(data: ByteArray, id: String) {
		nearbySwift.sendDataWithData(data.toNSData(), id)
	}

	override suspend fun receiveData(): ByteArray {
		val channel = Channel<ByteArray>(1)
		nearbySwift.receiveDataWithCompletionHandler { data, _ ->
			data?.let { channel.trySend(it.message.toByteArray()) }
		}
		return channel.receive()
	}

	override suspend fun startReceive(): Channel<Message> {
		val channel = Channel<Message>(10)
		receiveData(channel)
		return channel
	}

	private fun receiveData(channel: Channel<Message>) {
		nearbySwift.receiveDataWithCompletionHandler { data, _ ->
			println(data)
			data?.let { println(it.message) }
			data?.let { channel.trySend(Message(it.message.toByteArray(), it.id)) }
			receiveData(channel)
		}
	}

	override var advertising by mutableStateOf(false)
	override var discovering by mutableStateOf(false)

}