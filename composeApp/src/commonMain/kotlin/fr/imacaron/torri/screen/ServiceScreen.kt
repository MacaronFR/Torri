package fr.imacaron.torri.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import fr.imacaron.torri.Destination
import fr.imacaron.torri.viewmodel.ServiceViewModel

@Composable
fun ServiceScreen(serviceViewModel: ServiceViewModel, navController: NavController) {
	Column {
		Text("Service")
		if(serviceViewModel.currentService != null) {
			Card(Modifier.clickable { navController.navigate(Destination.SERVICE_COMMAND.route) }) {
				Text("Service en cours")
			}
		} else {
			Button( { navController.navigate(Destination.SERVICE_ADD.route) } ) {
				Text("Nouveau service")
			}
		}
		Text("Liste des services")
		LazyColumn {
			serviceViewModel.services.forEach { service ->
				item {
					Text("${service.date} ${service.idService}")
				}
			}
		}
	}
}