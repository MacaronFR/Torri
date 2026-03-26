package fr.imacaron.torri.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.savedstate.read
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.BookText
import com.composables.icons.lucide.CloudOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Save
import com.composables.icons.lucide.Send
import com.composables.icons.lucide.ServerOff
import fr.imacaron.torri.Destination
import fr.imacaron.torri.saveToFile
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel
import fr.imacaron.torri.viewmodel.SlaveCommandViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(navController: NavController, serviceViewModel: ServiceViewModel, commandViewModel: CommandViewModel, slaveCommandViewModel: SlaveCommandViewModel) {
	var currentRoute by remember { mutableStateOf("") }
	navController.addOnDestinationChangedListener { _, destination, _ ->
		destination.route?.let { currentRoute = it }
	}
	TopAppBar(
		{
			Row {
				Text("T", color = MaterialTheme.colorScheme.primary)
				Text("orri", color = MaterialTheme.colorScheme.onBackground)
			}
		},
		actions = {
			when(currentRoute) {
				Destination.SERVICE_COMMAND.route -> {
					var doneDialog by remember { mutableStateOf(false) }
					IconButton({ navController.navigate(Destination.SERVICE_COMMAND_DETAIL.route) }) {
						Icon(Lucide.BookText, "Détails des commandes", tint = MaterialTheme.colorScheme.primary)
					}
					IconButton( { doneDialog = true} ) {
						Icon(Lucide.Send, "Terminé et sauvegarder le service", tint = MaterialTheme.colorScheme.primary)
					}
					if(doneDialog) {
						AlertDialog(
							{ doneDialog = false },
							{ TextButton({
								serviceViewModel.setCurrentServiceDone()
								doneDialog = false
								navController.navigate(Destination.SERVICE.route)
							}) { Text("Confirmer")} },
							title = { Text("Clore et sauvegarder le service") },
							text = { Text("Un fois clos, ce service ne pourra plus être modifié") }
						)
					}
				}
				Destination.SERVICE_DETAIL.route -> {
					val serviceId = navController.currentBackStackEntry?.arguments?.read { this.getStringOrNull("id")?.toLongOrNull() }
					IconButton({
						serviceViewModel.viewModelScope.launch {
							serviceId?.let { sID ->
								serviceViewModel.services.find { it.idService == sID }?.let { service ->
									saveToFile("TORRI_SERVICE_$sID.csv", serviceViewModel.exportToCSV(service))
								}
							}
						}
					}) {
						Icon(Lucide.Save, "Exporter le service en CSV", tint = MaterialTheme.colorScheme.primary)
					}
				}
			}
			if(commandViewModel.isOnline) {
				IconButton({
					commandViewModel.disconnectAll()
				}) {
					Icon(Lucide.ServerOff, "Fermer la connexion")
				}
			} else if(slaveCommandViewModel.isOnline) {
				IconButton({
					slaveCommandViewModel.disconnect()
				}) {
					Icon(Lucide.CloudOff, "Fermer la connexion")
				}
			} else if(false) {
				IconButton({

				}) {
					Icon(Lucide.CloudOff, "Fermer la connexion")
				}
			}
		},
		navigationIcon = {
			if('/' in currentRoute) {
				IconButton({ navController.popBackStack() }) {
					Icon(
						Lucide.ArrowLeft,
						contentDescription = "Retour",
						tint = MaterialTheme.colorScheme.primary
					)
				}
			}
		}
	)
}