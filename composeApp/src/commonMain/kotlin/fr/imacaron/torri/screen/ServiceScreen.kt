package fr.imacaron.torri.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.Destination
import fr.imacaron.torri.components.ServiceItem
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel

@Composable
fun ServiceScreen(serviceViewModel: ServiceViewModel, priceListViewModel: PriceListViewModel, navController: NavController) {
	Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Text("Service", style = MaterialTheme.typography.headlineLarge)
		if(serviceViewModel.currentService != null) {
			val priceList = priceListViewModel.priceLists.find { it.priceList.idPriceList == serviceViewModel.currentService?.idPriceList }
			Card(Modifier.fillMaxWidth().clickable { navController.navigate(Destination.SERVICE_COMMAND.route) }) {
				Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
					Column {
						Text("Service en cours", style = MaterialTheme.typography.headlineSmall)
						Text("${priceList?.priceList?.name} en ${priceList?.priceList?.currency}", style = MaterialTheme.typography.bodyLarge)
					}
					Icon(Lucide.ChevronRight, "Ouvrir", Modifier.size(32.dp))
				}
			}
		} else {
			Card(Modifier.fillMaxWidth()) {
				Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
					Text("Aucun service en cours", style = MaterialTheme.typography.headlineSmall)
					Button( { navController.navigate(Destination.SERVICE_ADD.route) } ) {
						Text("Nouveau service")
					}
				}
			}
		}
		Text("Liste des services")
		LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			serviceViewModel.services.forEach { service ->
				item {
					ServiceItem(service, priceListViewModel.priceLists.find { it.priceList.idPriceList == service.idPriceList }!!, {
						serviceViewModel.delete(service)
					}, {
						navController.navigate(Destination.SERVICE_DETAIL.routeWithArg("id" to service.idService.toString()))
					})
				}
			}
		}
	}
}