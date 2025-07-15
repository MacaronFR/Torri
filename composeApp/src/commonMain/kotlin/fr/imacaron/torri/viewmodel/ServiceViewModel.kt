package fr.imacaron.torri.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.torri.data.AppDataBase
import fr.imacaron.torri.data.ServiceEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ServiceViewModel(private val db: AppDataBase): ViewModel() {
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
}