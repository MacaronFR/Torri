package fr.imacaron.torri.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import fr.imacaron.torri.Destination
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel

@Composable
fun ServiceAddScreen(priceListViewModel: PriceListViewModel, serviceViewModel: ServiceViewModel, navController: NavController) {
	Column {
		Text("Nouveau service")
		LazyColumn {
			items(priceListViewModel.priceLists) {
				Row(Modifier.clickable {
					serviceViewModel.create(it.priceList.idPriceList)
					navController.navigate(Destination.SERVICE_COMMAND.route)
				}) {
					Text("${it.priceList.name} (${it.priceList.currency})")
				}
			}
		}
	}
}