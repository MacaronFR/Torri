package fr.imacaron.torri.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.imacaron.torri.Destination
import fr.imacaron.torri.components.CommandDetail
import fr.imacaron.torri.components.ItemSelection
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel

@Composable
fun CommandScreen(cols: Int, displaySidePanel: Boolean, serviceViewModel: ServiceViewModel, navController: NavController, priceListViewModel: PriceListViewModel, commandViewModel: CommandViewModel, portrait: Boolean) {
	commandViewModel.loadService()
	val service = serviceViewModel.currentService ?: return
	commandViewModel.service = service
	val priceList = priceListViewModel.priceLists.find { it.priceList.idPriceList == service.idPriceList }
	if(priceList == null) {
		navController.navigate(Destination.SERVICE.route)
		return
	}
	val items = priceList.items
	val prices = remember { mutableStateListOf<PriceListItemEntity>() }
	LaunchedEffect(priceListViewModel, priceList) {
		prices.addAll(priceListViewModel.getPriceListItems(priceList.priceList))
	}
	if(displaySidePanel) {
		if(portrait) {
			Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
				ItemSelection(cols, items, prices, priceList, commandViewModel, Modifier.weight(3f))
				CommandDetail(commandViewModel, items, priceList, prices, portrait, displaySidePanel, Modifier.weight(1f))
			}
		} else {
			Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				ItemSelection(cols, items, prices, priceList, commandViewModel, Modifier.weight(3f))
				CommandDetail(commandViewModel, items, priceList, prices, portrait, displaySidePanel, Modifier.weight(1f))
			}
		}
	} else {
		Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			CommandDetail(commandViewModel, items, priceList, prices, portrait, displaySidePanel)
			ItemSelection(cols, items, prices, priceList, commandViewModel)
		}
	}
}