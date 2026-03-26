package fr.imacaron.torri.viewmodel

import androidx.lifecycle.viewModelScope
import fr.imacaron.torri.Nearby
import fr.imacaron.torri.data.AppDataBase
import fr.imacaron.torri.data.CommandEntity
import fr.imacaron.torri.data.CommandPriceListItemEntity
import fr.imacaron.torri.data.CommandPriceListItemsWithPriceListItem
import fr.imacaron.torri.data.ServiceEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.collections.set

class CommandViewModel(
	private val db: AppDataBase,
	private val nearby: Nearby
): BaseCommandViewModel() {
	fun startMasterCommand() {
		viewModelScope.launch {
			receiver()
		}
		nearby.onConnected = { device ->
			viewModelScope.launch {
				db.serviceDao().getAllNotDone().firstOrNull()?.let { s ->
					service = s
					sendDataTo("service:${Json.encodeToString(s)}".encodeToByteArray(), device.id)
					val priceList = db.priceListDao().getById(s.idPriceList)
					sendDataTo("priceList:${Json.encodeToString(priceList)}".encodeToByteArray(), device.id)
					val prices = db.priceListItemDao().getAlByPriceList(priceList?.priceList?.idPriceList ?: return@let)
					sendDataTo("prices:${Json.encodeToString(prices)}".encodeToByteArray(), device.id)
				}
			}
		}
		nearby.startAdvertising(true)
	}

	val connectedDevices: List<Nearby.Device> = nearby.connectedList

	fun disconnectDevice(device: Nearby.Device) {
		nearby.disconnectDevice(device.id)
	}

	fun disconnectAll() {
		nearby.disconnectAll()
	}

	val isOnline: Boolean
		get() = nearby.advertising

	@OptIn(DelicateCoroutinesApi::class)
	suspend fun receiver() {
		val receiverChannel = nearby.startReceive()
		while(!receiverChannel.isClosedForReceive) {
			val message = receiverChannel.receive()
			val (action, data) = message.data.decodeToString().split(":", limit = 2)
			when(action) {
				"payCommand" -> paySlave(data, message.id)
				"payCommandDetail" -> paySlaveDetail(data)
				else -> println("Unknown action: $action")
			}
		}
	}

	suspend fun paySlave(data: String, senderId: String) {
		val command = Json.decodeFromString<CommandEntity?>(data) ?: return
		val id = db.commandDao().insert(command)
		nearby.sendDataTo("commandId:${id}".encodeToByteArray(), senderId)
	}

	suspend fun paySlaveDetail(data: String) {
		val commandDetail: List<CommandPriceListItemEntity> = Json.decodeFromString(data)
		db.commandPriceListItemDao().insertAll(commandDetail)
	}

	override var service: ServiceEntity?
		get() = _service
		set(value) {
			_service = value
			command.clear()
			prices.clear()
			loadService()
		}

	override fun loadHistory() {
		_service?.let { s ->
			history.clear()
			viewModelScope.launch {
				history.addAll(db.commandDao().getByService(s.idService))
			}
		}
	}

	fun loadService(): Job? {
		return _service?.let { s ->
			viewModelScope.launch {
				db.priceListItemDao().getAlByPriceList(s.idPriceList).forEach { prices[it.idPriceListItem] = it.price }
			}
		}
	}

	override fun removeFromHistory(command: CommandEntity) {
		history.remove(command)
		viewModelScope.launch {
			db.commandDao().delete(command.idCommand)
		}

	}

	override fun pay(method: String) {
		if(command.isEmpty()) {
			return
		}
		prepareCommandEntity(method)?.let { commandEntity ->
			viewModelScope.launch {
				val id = db.commandDao().insert(commandEntity)
				db.commandPriceListItemDao().insertAll(prepareCommandDetail(id))
				command.clear()
			}
		}
	}

	override suspend fun loadCommandDetail(command: CommandEntity): List<CommandPriceListItemsWithPriceListItem> {
		return db.commandPriceListItemDao().getByCommand(command.idCommand)
	}
}