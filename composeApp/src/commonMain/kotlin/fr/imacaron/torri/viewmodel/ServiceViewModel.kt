package fr.imacaron.torri.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.torri.data.AppDataBase
import fr.imacaron.torri.data.CommandEntity
import fr.imacaron.torri.data.ServiceEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ServiceViewModel(private val db: AppDataBase, private val commandViewModel: CommandViewModel, private val itemViewModel: SavedItemViewModel): ViewModel() {
	val services = mutableStateListOf<ServiceEntity>()
	var currentService by mutableStateOf<ServiceEntity?>(null)

	init {
		loadServices()
	}

	private fun loadServices(): Job {
		services.clear()
		currentService = null
		return viewModelScope.launch {
			services.addAll(db.serviceDao().getAllDone())
			currentService = db.serviceDao().getAllNotDone().firstOrNull()
		}
	}

	@OptIn(ExperimentalTime::class)
	fun create(priceList: Long) {
		viewModelScope.launch {
			db.serviceDao().create(ServiceEntity(
				date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
				idPriceList = priceList,
				pending = true
			))
			loadServices()
		}
	}

	fun setCurrentServiceDone() {
		val service = currentService ?: return
		viewModelScope.launch {
			val newService = service.copy(pending = false)
			db.serviceDao().update(newService)
			loadServices()
		}
	}

	fun delete(service: ServiceEntity) {
		viewModelScope.launch {
			db.serviceDao().delete(service)
			services.remove(service)
		}
	}

	suspend fun loadServiceCommand(service: ServiceEntity): List<CommandEntity> {
		return db.commandDao().getByService(service.idService)
	}

	suspend fun exportToCSV(service: ServiceEntity): String {
		val commands = loadServiceCommand(service)
		val csv = commands.map { command ->
			commandViewModel.loadCommandDetail(command).joinToString("\n") {
				if(it.priceListItem == null) {
					"${command.idCommand};Article inconnu;${it.commandPriceListItem.quantity};Prix inconnu;${command.payementMethod}"
				} else {
					"${command.idCommand};${itemViewModel.items.find { i -> i.idItem == it.priceListItem.idItem }?.name};${it.commandPriceListItem.quantity};${it.priceListItem.price * it.commandPriceListItem.quantity};${command.payementMethod}"
				}
			}
		}.joinToString("\n")
		return "ID;Article;Quantité;Prix;Méthode de paiement\n$csv"
	}
}