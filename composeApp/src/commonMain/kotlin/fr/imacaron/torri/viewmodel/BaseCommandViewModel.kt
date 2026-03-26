package fr.imacaron.torri.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import fr.imacaron.torri.data.CommandEntity
import fr.imacaron.torri.data.CommandPriceListItemEntity
import fr.imacaron.torri.data.CommandPriceListItemsWithPriceListItem
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.ServiceEntity

abstract class BaseCommandViewModel: ViewModel() {
	protected var _service: ServiceEntity? by mutableStateOf(null)
	open var service: ServiceEntity?
		get() = _service
		set(value) {
			_service = value
			command.clear()
			prices.clear()
		}

	val command = mutableStateMapOf<Long, Int>()
	val prices = mutableStateMapOf<Long, Double>()
	val history = mutableStateListOf<CommandEntity>()

	val totalItem: Int
		get() = command.map { it.value }.sum()

	val totalPrice: Double
		get() {
			println(prices.toMap())
			command.map {
				println(it.key)
				println(prices[it.key])
			}
			return command.map { prices[it.key]!! * it.value }.sum()
		}

	fun add(item: PriceListItemEntity) {
		command[item.idPriceListItem] = command[item.idPriceListItem]?.let { it + 1 } ?: 1
	}

	fun remove(item: PriceListItemEntity) {
		command[item.idPriceListItem]?.let {
			if(it > 1) {
				command[item.idPriceListItem] = it - 1
			} else {
				command.remove(item.idPriceListItem)
			}
		}
	}

	abstract fun pay(method: String)

	abstract fun loadHistory()

	abstract fun removeFromHistory(command: CommandEntity)

	abstract suspend fun loadCommandDetail(command: CommandEntity): List<CommandPriceListItemsWithPriceListItem>

	protected fun prepareCommandEntity(method: String): CommandEntity? = service?.let { s ->
		CommandEntity(idService = s.idService, payementMethod = method, total = command.map { prices[it.key]!! * it.value }.sum(), done = false)
	}

	protected fun prepareCommandDetail(idCommand: Long): List<CommandPriceListItemEntity> = command.map { (idPriceListItem, quantity) ->
		CommandPriceListItemEntity(idCommand = idCommand, idPriceListItem = idPriceListItem, quantity = quantity)
	}
}