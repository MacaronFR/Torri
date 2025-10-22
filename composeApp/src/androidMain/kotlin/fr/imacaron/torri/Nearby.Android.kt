package fr.imacaron.torri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
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
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import okhttp3.internal.wait
import java.io.InputStream
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
		activity.checkPermission {
			if(it) {
				val options = AdvertisingOptions.Builder()
					.setStrategy(Strategy.P2P_POINT_TO_POINT).build()
				GoogleNearby.getConnectionsClient(activity)
					.startAdvertising(name, serviceId, Callback(), options)
					.addOnSuccessListener {
						advertising = true
					}.addOnFailureListener {
						println("startAdvertising:onResult:failure")
						it.printStackTrace()
					}
			} else {
				println("startAdvertising:onResult:permission")
			}
		}
	}

	override fun stopAdvertising() {
		GoogleNearby.getConnectionsClient(activity).stopAdvertising()
		advertising = false
	}

	override fun startDiscovery() {
		master = false
		activity.checkPermission {
			if(it) {
				val options = DiscoveryOptions.Builder()
					.setStrategy(Strategy.P2P_POINT_TO_POINT).build()
				GoogleNearby.getConnectionsClient(activity)
					.startDiscovery(serviceId, DiscoveryCallback(), options)
					.addOnSuccessListener {
						discovering = true
					}
					.addOnFailureListener {
						println("startDiscovery:onResult:failure")
						it.printStackTrace()
					}
			} else {
				println("startDiscovery:onResult:permission")
			}
		}
	}

	override fun stopDiscovery() {
		GoogleNearby.getConnectionsClient(activity).stopDiscovery()
		discovering = false
		discoveredDevices.clear()
	}

	override fun connect(device: Device) {
		stopDiscovery()
		connecting = device
		GoogleNearby.getConnectionsClient(activity)
			.requestConnection(name, device.id, Callback())
			.addOnFailureListener {
				println("requestConnection:onResult:failure")
				it.printStackTrace()
			}
	}

	override fun disconnect() {
		GoogleNearby.getConnectionsClient(activity).disconnectFromEndpoint(connected?.id ?: "")
		connecting = null
		connected = null
		discovering = false
		advertising = false
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
		println("Receive data")
		receiving = true
		var update: PayloadTransferUpdate?
		do {
			update = receivingUpdateChannel.receive()
			delay(100)
			println("Receive payload transfer update: ${update.status}")
		} while (update.status != PayloadTransferUpdate.Status.SUCCESS)
		println("payload full")
		return receivingPayload?.asBytes()?.also { receivingPayload = null; receiving = false }
	}

	private inner class Callback: ConnectionLifecycleCallback() {
		override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
			connecting = Device(connectionInfo.endpointName, endpointId)
			stopDiscovery()
			stopAdvertising()
			GoogleNearby.getConnectionsClient(activity).acceptConnection(endpointId, InputCallback())
		}

		override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
			stopDiscovery()
			stopAdvertising()
			when(result.status.statusCode) {
				ConnectionsStatusCodes.STATUS_OK -> {
					connected = connecting
					connecting = null
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
			discovering = false
			advertising = false
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
			println("Receive payload")
			receivingPayload = payload
		}

		override fun onPayloadTransferUpdate(endpointId: String, transferUpdate: PayloadTransferUpdate) {
			println("Receive payload transfer update")
			receivingUpdateChannel.trySend(transferUpdate).onFailure {
				it?.printStackTrace()
			}
		}

	}
}