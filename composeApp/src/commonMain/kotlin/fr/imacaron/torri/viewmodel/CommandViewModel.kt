package fr.imacaron.torri.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.torri.data.AppDataBase
import fr.imacaron.torri.data.CommandEntity
import fr.imacaron.torri.data.CommandPriceListItemEntity
import fr.imacaron.torri.data.CommandPriceListItemsWithPriceListItem
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.ServiceEntity
import kotlinx.coroutines.launch
import kotlin.collections.set

class CommandViewModel(
	private val db: AppDataBase,
): ViewModel() {
	val command = mutableStateMapOf<Long, Int>()
	val prices = mutableStateMapOf<Long, Double>()
	val history = mutableStateListOf<CommandEntity>()

	private var _service: ServiceEntity? by mutableStateOf(null)
	var service: ServiceEntity?
		get() = _service
		set(value) {
			_service = value
			command.clear()
			prices.clear()
			loadService()
		}

	val totalItem: Int
		get() = command.map { it.value }.sum()

	val totalPrice: Double
		get() = command.map { prices[it.key]!! * it.value }.sum()

	fun loadHistory() {
		_service?.let { s ->
			history.clear()
			viewModelScope.launch {
				history.addAll(db.commandDao().getByService(s.idService))
			}
		}
	}

	fun loadService() {
		_service?.let { s ->
			viewModelScope.launch {
				db.priceListItemDao().getAlByPriceList(s.idPriceList).forEach { prices[it.idPriceListItem] = it.price }
			}
		}
	}

	fun add(item: PriceListItemEntity) {
		command[item.idPriceListItem] = command[item.idPriceListItem]?.let { it + 1 } ?: 1
	}

	fun remove(item: PriceListItemEntity) {
		command[item.idPriceListItem]?.let {
			if(it > 0) {
				command[item.idPriceListItem] = it -1
			} else {
				command.remove(item.idPriceListItem)
			}
		}
	}

	fun pay(method: String) {
		if(command.isEmpty()) {
			return
		}
		_service?.let { s ->
			viewModelScope.launch {
				val id = db.commandDao().insert(
					CommandEntity(
						idService = s.idService,
						payementMethod = method,
						total = command.map { prices[it.key]!! * it.value }.sum()
					)
				)
				db.commandPriceListItemDao().insertAll(command.map { (idPriceListItem, quantity) ->
					CommandPriceListItemEntity(idCommand = id, idPriceListItem = idPriceListItem, quantity = quantity)
				})
				command.clear()
			}
		}
	}

	suspend fun loadCommandDetail(command: CommandEntity): List<CommandPriceListItemsWithPriceListItem> {
		return db.commandPriceListItemDao().getByCommand(command.idCommand)
	}
}