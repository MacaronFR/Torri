package fr.imacaron.torri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Nearbyios: Nearby() {
	override fun startAdvertising() {
		TODO("Not yet implemented")
	}

	override fun stopAdvertising() {
		TODO("Not yet implemented")
	}

	override fun startDiscovery() {
		TODO("Not yet implemented")
	}

	override fun stopDiscovery() {
		TODO("Not yet implemented")
	}

	override fun connect(device: Device) {
		TODO("Not yet implemented")
	}

	override fun disconnect() {
		TODO("Not yet implemented")
	}

	override fun sendData(data: ByteArray) {
		TODO("Not yet implemented")
	}

	override suspend fun receiveData(): ByteArray? {
		TODO("Not yet implemented")
	}

	override var advertising by mutableStateOf(false)
	override var discovering by mutableStateOf(false)

}