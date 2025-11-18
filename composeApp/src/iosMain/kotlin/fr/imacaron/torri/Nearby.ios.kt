package fr.imacaron.torri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.imacaron.torri.ios.NearbySwift
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel
import org.publicvalue.multiplatform.oidc.toByteArray
import org.publicvalue.multiplatform.oidc.toNSData

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
					connecting = Device(endpointId ?: "", endpointId ?: "")
				}
				"CONNECTED" -> {
					println("CONNECTED")
					connected = connecting
					connecting = null
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
	}

	@OptIn(ExperimentalForeignApi::class)
	override fun startAdvertising() {
		master = true
		nearbySwift.startAdvertising()
		advertising = true
	}

	@OptIn(ExperimentalForeignApi::class)
	override fun stopAdvertising() {
		nearbySwift.stopAdvertising()
		advertising = false
	}

	@OptIn(ExperimentalForeignApi::class)
	override fun startDiscovery() {
		master = false
		nearbySwift.startDiscovery()
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

	override fun disconnect() {
		val id = connecting?.id ?: connected?.id ?: return
		nearbySwift.disconnectWithEndpointId(id)
		connecting = null
		connected = null
		stopDiscovery()
		stopAdvertising()
	}

	override fun sendData(data: ByteArray) {
		if(connected == null || !master) {
			return
		}
		nearbySwift.sendDataWithData(data.toNSData(), connected!!.id)
	}

	override suspend fun receiveData(): ByteArray? {
		val channel = Channel<ByteArray>(1)
		nearbySwift.receiveDataWithCompletionHandler { data, _ ->
			data?.let { channel.trySend(it.toByteArray()) }
		}
		return channel.receive()
	}

	override var advertising by mutableStateOf(false)
	override var discovering by mutableStateOf(false)

}