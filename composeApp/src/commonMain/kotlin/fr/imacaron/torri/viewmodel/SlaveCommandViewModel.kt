package fr.imacaron.torri.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import fr.imacaron.torri.Nearby
import fr.imacaron.torri.Nearby.Type
import fr.imacaron.torri.SumUp
import fr.imacaron.torri.data.CommandEntity
import fr.imacaron.torri.data.CommandPriceListItemsWithPriceListItem
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SlaveCommandViewModel(
	private val nearby: Nearby
): BaseCommandViewModel() {

	init {
		nearby.onConnected = {
			viewModelScope.launch {
				receiver()
			}
		}
	}

	fun startSlaveCommand() {
		nearby.startDiscovery(true, Type.SLAVE_COMMAND)
	}

	val detectedDevices: List<Nearby.Device> = nearby.discoveredDevices

	fun connect(device: Nearby.Device) {
		nearby.connect(device)
	}

	val connected: Boolean
		get() = nearby.connected != null

	fun disconnect() {
		nearby.disconnectAll()
	}

	val isOnline: Boolean
		get() = nearby.discovering || nearby.connecting != null || nearby.connected != null

	private val commandIdChannel = Channel<Long>(2, BufferOverflow.SUSPEND)
	private val historyDetailChannel = Channel<List<CommandPriceListItemsWithPriceListItem>>(2, BufferOverflow.SUSPEND)

	@OptIn(DelicateCoroutinesApi::class)
	suspend fun receiver() {
		val receiverChannel = nearby.startReceive()
		while(!receiverChannel.isClosedForReceive) {
			val message = receiverChannel.receive()
			val (action, data) = message.data.decodeToString().split(":", limit = 2)
			when(action) {
				"service" -> receiveService(data)
				"priceList" -> receivePriceList(data)
				"prices" -> receivePrices(data)
				"commandId" -> commandIdChannel.send(data.toLong())
				"history" -> receiveHistory(data)
				"commandDetail" -> receiveHistoryDetail(data)
				"removeFromHistory" -> masterRemoveFromHistory(data.toLong())
				"sumupLogin" -> sumupLogin(data)
				else -> println("Unknown action: $action")
			}
		}
	}

	private fun receiveService(data: String) {
		service = Json.decodeFromString(data)
	}

	private fun receivePriceList(data: String) {
		priceList = Json.decodeFromString<PriceListWithItem>(data)
		priceList?.items?.let {
			items.clear()
			items.addAll(it)
		}
	}

	private fun receivePrices(data: String) {
		val p = Json.decodeFromString<List<PriceListItemEntity>>(data)
		pricesScreen.clear()
		p.forEach {
			pricesScreen.add(it)
			prices[it.idPriceListItem] = it.price
		}
	}

	private suspend fun receiveHistoryDetail(data: String) {
		historyDetailChannel.send(Json.decodeFromString<List<CommandPriceListItemsWithPriceListItem>>(data))
	}

	private fun masterRemoveFromHistory(id: Long) {
		history.removeAll { it.idCommand == id }
	}

	private fun sumupLogin(data: String) {
		SumUp.login(data)
	}

	var priceList by mutableStateOf<PriceListWithItem?>(null)

	val items = mutableStateListOf<ItemEntity>()

	val pricesScreen = mutableStateListOf<PriceListItemEntity>()

	override fun pay(method: String) {
		if(command.isEmpty()) {
			return
		}
		viewModelScope.launch {
			nearby.sendData("payCommand:${Json.encodeToString(prepareCommandEntity(method))}".toByteArray())
			val id = commandIdChannel.receive()
			nearby.sendData("payCommandDetail:${Json.encodeToString(prepareCommandDetail(id))}".toByteArray())
			command.clear()
		}
	}

	override fun loadHistory() {
		nearby.sendData("getHistory:svp".toByteArray())
	}

	fun receiveHistory(data: String) {
		history.addAll(Json.decodeFromString<List<CommandEntity>>(data))
	}

	override fun removeFromHistory(command: CommandEntity) {
		nearby.sendData("removeHistory:${command.idCommand}".toByteArray())
	}

	override suspend fun loadCommandDetail(command: CommandEntity): List<CommandPriceListItemsWithPriceListItem> {
		nearby.sendData("getCommandDetail:${command.idCommand}".toByteArray())
		return historyDetailChannel.receive()
	}
}