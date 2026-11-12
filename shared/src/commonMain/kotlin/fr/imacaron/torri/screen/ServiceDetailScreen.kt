package fr.imacaron.torri.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import fr.imacaron.torri.components.CommandList
import fr.imacaron.torri.data.CommandEntity
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.SavedItemViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel

@Composable
fun ServiceDetailScreen(serviceViewModel: ServiceViewModel, commandViewModel: CommandViewModel, savedItemsViewModel: SavedItemViewModel, priceListViewModel: PriceListViewModel, id: Long) {
	val service = remember { serviceViewModel.services.find { it.idService == id } } ?: return
	val priceList = remember { priceListViewModel.priceLists.find { it.priceList.idPriceList == service.idPriceList } } ?: return
	val commands = mutableStateListOf<CommandEntity>()
	LaunchedEffect(serviceViewModel, service) {
		commands.addAll(serviceViewModel.loadServiceCommand(service))
	}
	CommandList(commands, commandViewModel, priceList.priceList, savedItemsViewModel)
}