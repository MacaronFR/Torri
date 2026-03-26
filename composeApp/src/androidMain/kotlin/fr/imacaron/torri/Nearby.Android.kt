package fr.imacaron.torri

import android.provider.Settings
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
import kotlin.emptyArray
import com.google.android.gms.nearby.Nearby as GoogleNearby

class NearbyAndroid(
	private val activity: MainActivity,
): Nearby() {
	private val serviceId = "fr.imacaron.torri"
	private val name: String
		get() = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
			Settings.System.getString(activity.contentResolver, Settings.Global.DEVICE_NAME) ?: run {
				Settings.Secure.getString(activity.contentResolver, "bluetooth_name")
			}
		} else {
			Settings.Secure.getString(activity.contentResolver, "bluetooth_name")
		}

	override var advertising by mutableStateOf(false)

	override var discovering by mutableStateOf(false)

	override fun startAdvertising(star: Boolean) {
		this.star = star
		master = true
		type = Type.MASTER
		activity.lifecycleScope.launch {
			activity.checkPermission {
				if(it) {
					val options = AdvertisingOptions.Builder()
						.setStrategy(if(star) Strategy.P2P_STAR else Strategy.P2P_POINT_TO_POINT).build()
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

	override fun startDiscovery(star: Boolean, type: Type) {
		master = false
		this.type = type
		this.star = star
		activity.lifecycleScope.launch {
			activity.checkPermission {
				if (it) {
					val options = DiscoveryOptions.Builder()
						.setStrategy(if(star) Strategy.P2P_STAR else Strategy.P2P_POINT_TO_POINT).build()
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

	override fun disconnectDevice(id: String) {
		if(connected?.id == id || connecting?.id == id) {
			GoogleNearby.getConnectionsClient(activity).disconnectFromEndpoint(id)
			connected = null
			connecting = null
		} else {
			connectedList.find { it.id == id }?.let {
				GoogleNearby.getConnectionsClient(activity).disconnectFromEndpoint(id)
				connectedList.remove(it)
			}
		}
	}

	override fun disconnectAll() {
		(connected?.id ?: connecting?.id)?.let { id ->
			GoogleNearby.getConnectionsClient(activity).disconnectFromEndpoint(id)
		}
		connectedList.forEach {
			GoogleNearby.getConnectionsClient(activity).disconnectFromEndpoint(it.id)
		}
		connecting = null
		connected = null
		connectedList.clear()
		stopDiscovery()
		stopAdvertising()

	}

	override fun sendData(data: ByteArray) {
		if (connected == null || (!master && !star)) {
			return
		}
		val payload = Payload.fromBytes(data)
		GoogleNearby.getConnectionsClient(activity).sendPayload(connected?.id ?: "", payload).continueWith {

		}
	}

	override fun sendDataTo(data: ByteArray, id: String) {
		GoogleNearby.getConnectionsClient(activity).sendPayload(id, Payload.fromBytes(data)).continueWith {

		}
	}

	private var receiving = false
	private var receivingPayload: Payload? = null
	private var receivingUpdateChannel: Channel<PayloadTransferUpdate> = Channel(capacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

	private val receivingMessageChannel: Channel<Message> = Channel(capacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

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

	override suspend fun startReceive(): Channel<Message> {
		return receivingMessageChannel
	}

	private inner class Callback: ConnectionLifecycleCallback() {
		override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
			connecting = Device(connectionInfo.endpointName, endpointId)
			GoogleNearby.getConnectionsClient(activity).acceptConnection(endpointId, InputCallback())
		}

		override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
			when(result.status.statusCode) {
				ConnectionsStatusCodes.STATUS_OK -> {
					val device = if(star && master) {
						connecting?.let {
							connectedList.add(it)
							it
						}
					} else {
						connected = connecting
						sendDataTo("Type:${type}".toByteArray(), connected!!.id)
						connecting
					}
					connecting = null
					stopDiscovery()
					if(!star) {
						stopAdvertising()
					}
					device?.let {
						onConnected(it)
					}
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
			connectedList.removeIf { it.id == endpointId }
			connected = null
			connecting = null
			if(!star) {
				stopDiscovery()
				stopAdvertising()
			}
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
			println("onPayloadReceived")
			if(star) {
				println("star")
				val message = payload.asBytes() ?: ByteArray(0)
				if(message.decodeToString().startsWith("Type:")) {
					if(master) {
						connectedList.find { it.id == endpointId }?.let {
							connectedList.remove(it)
							it.type = Type.valueOf(message.decodeToString().substringAfter("Type:"))
							connectedList.add(it)
						}
					} else {
						connected?.let {
							it.type = Type.valueOf(message.decodeToString().substringAfter("Type:"))
						}
					}
				} else {
					println("message channel, ")
					println("message channel, ${message.decodeToString()}")
					receivingMessageChannel.trySend(Message(message, endpointId))
				}
			} else {
				println("not star")
				receivingPayload = payload
			}
		}

		override fun onPayloadTransferUpdate(endpointId: String, transferUpdate: PayloadTransferUpdate) {
			if(!star) {
				receivingUpdateChannel.trySend(transferUpdate).onFailure {
					it?.printStackTrace()
				}
			}
		}

	}
}