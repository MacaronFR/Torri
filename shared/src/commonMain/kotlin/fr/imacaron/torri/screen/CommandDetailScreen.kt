package fr.imacaron.torri.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import fr.imacaron.torri.components.CommandList
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.SavedItemViewModel

@Composable
fun CommandDetailScreen(commandViewModel: CommandViewModel, priceListViewModel: PriceListViewModel, savedItemsViewModel: SavedItemViewModel) {
	val priceList = priceListViewModel.priceLists.find { it.priceList.idPriceList == commandViewModel.service?.idPriceList }
	LaunchedEffect(commandViewModel.service) {
		commandViewModel.loadHistory()
	}
	if(priceList == null) {
		Text("Erreur")
		return
	}
	CommandList(commandViewModel.history, commandViewModel, priceList.priceList, savedItemsViewModel)
}