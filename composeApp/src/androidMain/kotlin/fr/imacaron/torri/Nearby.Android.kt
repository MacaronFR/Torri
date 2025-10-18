package fr.imacaron.torri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.google.android.gms.nearby.Nearby as GoogleNearby

class NearbyAndroid(
	private val activity: MainActivity,
): Nearby() {
	private val serviceId = "fr.imacaron.torri"
	private var name = "Android"

	override var advertising by mutableStateOf(false)

	override var discovering by mutableStateOf(false)


	override fun startAdvertising() {
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
		discovering = false
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

	private inner class Callback: ConnectionLifecycleCallback() {
		override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
			connecting = Device(connectionInfo.endpointName, endpointId)
			discovering = false
			advertising = false
			GoogleNearby.getConnectionsClient(activity).acceptConnection(endpointId, InputCallback())
		}

		override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
			discovering = false
			advertising = false
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
		override fun onPayloadReceived(p0: String, p1: Payload) {
			println("Payload received from $p0: ${p1.type}")
		}

		override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
			println("Payload transfer update from $p0: ${p1.status}")
		}

	}
}