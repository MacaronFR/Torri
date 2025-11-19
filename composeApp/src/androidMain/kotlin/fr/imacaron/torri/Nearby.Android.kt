package fr.imacaron.torri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.launch
import com.google.android.gms.nearby.Nearby as GoogleNearby

class NearbyAndroid(
	private val activity: MainActivity,
): Nearby() {
	private val serviceId = "fr.imacaron.torri"
	private var name = "Android"

	override var advertising by mutableStateOf(false)

	override var discovering by mutableStateOf(false)

	override fun startAdvertising() {
		master = true
		activity.lifecycleScope.launch {
			activity.checkPermission {
				if(it) {
					val options = AdvertisingOptions.Builder()
						.setStrategy(Strategy.P2P_POINT_TO_POINT).build()
					GoogleNearby.getConnectionsClient(activity)
						.startAdvertising(name, serviceId, Callback(), options)
						.addOnSuccessListener {
							advertising = true
						}.addOnFailureListener {
							it.printStackTrace()
						}
				} else {
					println("startAdvertising:onResult:permission")
				}
			}
		}
	}

	override fun stopAdvertising() {
		GoogleNearby.getConnectionsClient(activity).stopAdvertising()
		advertising = false
	}

	override fun startDiscovery() {
		master = false
		activity.lifecycleScope.launch {
			activity.checkPermission {
				if (it) {
					val options = DiscoveryOptions.Builder()
						.setStrategy(Strategy.P2P_POINT_TO_POINT).build()
					GoogleNearby.getConnectionsClient(activity)
						.startDiscovery(serviceId, DiscoveryCallback(), options)
						.addOnSuccessListener {
							discovering = true
						}
						.addOnFailureListener {
							it.printStackTrace()
						}
				} else {
					println("startDiscovery:onResult:permission")
				}
			}
		}
	}

	override fun stopDiscovery() {
		GoogleNearby.getConnectionsClient(activity).stopDiscovery()
		discovering = false
		discoveredDevices.clear()
	}

	override fun connect(device: Device) {
		connecting = device
		GoogleNearby.getConnectionsClient(activity)
			.requestConnection(name, device.id, Callback())
			.addOnFailureListener {
				it.printStackTrace()
			}
	}

	override fun disconnect() {
		GoogleNearby.getConnectionsClient(activity).disconnectFromEndpoint(connected?.id ?: "")
		connecting = null
		connected = null
		discovering = false
		advertising = false
		stopDiscovery()
		stopAdvertising()
	}

	override fun sendData(data: ByteArray) {
		if(connected == null || !master) {
			return
		}
		val payload = Payload.fromBytes(data)
		GoogleNearby.getConnectionsClient(activity).sendPayload(connected?.id ?: "", payload).continueWith {

		}
	}

	private var receiving = false
	private var receivingPayload: Payload? = null
	private var receivingUpdateChannel: Channel<PayloadTransferUpdate> = Channel(capacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

	override suspend fun receiveData(): ByteArray? {
		if (receiving) {
			return null
		}
		receiving = true
		var update: PayloadTransferUpdate?
		do {
			update = receivingUpdateChannel.receive()
		} while (update.status != PayloadTransferUpdate.Status.SUCCESS)
		return receivingPayload?.asBytes()?.also { receivingPayload = null; receiving = false }
	}

	private inner class Callback: ConnectionLifecycleCallback() {
		override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
			connecting = Device(connectionInfo.endpointName, endpointId)
			GoogleNearby.getConnectionsClient(activity).acceptConnection(endpointId, InputCallback())
		}

		override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
			when(result.status.statusCode) {
				ConnectionsStatusCodes.STATUS_OK -> {
					connected = connecting
					connecting = null
					stopDiscovery()
					stopAdvertising()
				}
				ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
					connected = null
					connecting = null
				}
				ConnectionsStatusCodes.STATUS_ERROR -> {
					connected = null
					connecting = null
					error = "Error"
				}
				else -> {
					connected = null
					connecting = null
					error = "Unknown error"
				}
			}
		}

		override fun onDisconnected(endpointId: String) {
			connected = null
			connecting = null
			stopDiscovery()
			stopAdvertising()
		}
	}

	private inner class DiscoveryCallback: EndpointDiscoveryCallback() {
		override fun onEndpointFound(endpointId: String, discoveryEndpointInfo: DiscoveredEndpointInfo) {
			discoveredDevices.add(Device(discoveryEndpointInfo.endpointName, endpointId))
		}

		override fun onEndpointLost(endpointId: String) {
			discoveredDevices.removeIf { it.id == endpointId }
		}

	}

	private inner class InputCallback: PayloadCallback() {
		override fun onPayloadReceived(endpointId: String, payload: Payload) {
			receivingPayload = payload
		}

		override fun onPayloadTransferUpdate(endpointId: String, transferUpdate: PayloadTransferUpdate) {
			receivingUpdateChannel.trySend(transferUpdate).onFailure {
				it?.printStackTrace()
			}
		}

	}
}