package fr.imacaron.torri.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.PriceListViewModel

@Composable
fun CommandDetailScreen(commandViewModel: CommandViewModel, priceListViewModel: PriceListViewModel) {
	val priceList = priceListViewModel.priceLists.find { it.priceList.idPriceList == commandViewModel.service?.idPriceList }
	LaunchedEffect(commandViewModel.service) {
		commandViewModel.loadHistory()
	}
	if(priceList == null) {
		Text("Erreur")
		return
	}
	if(commandViewModel.history.isEmpty()) {
		Text("Aucun historique de commande pour ce service")
	} else {
		LazyColumn {
			itemsIndexed(commandViewModel.history) { index, command ->
				Row {
					Text("Commande #${index + 1}, total ${command.total} ${priceList.priceList.currency}")
				}
			}
		}
	}
}