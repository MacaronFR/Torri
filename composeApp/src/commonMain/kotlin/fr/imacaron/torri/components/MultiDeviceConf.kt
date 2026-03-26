package fr.imacaron.torri.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.SlaveCommandViewModel

@Composable
fun MultiDeviceConf(slaveCommandViewModel: SlaveCommandViewModel, commandViewModel: CommandViewModel) {
	Text(
		"Connecter des appareils pour prendre des commandes",
		Modifier.padding(top = 8.dp),
		style = MaterialTheme.typography.titleMedium
	)
	if(!slaveCommandViewModel.isOnline && !commandViewModel.isOnline) {
		Card(Modifier.fillMaxWidth()) {
			TextButton({ commandViewModel.startMasterCommand() }, Modifier.fillMaxWidth()) {
				Text("Démarrer en tant qu'appareil principal")
			}
			TextButton({ slaveCommandViewModel.startSlaveCommand() }, Modifier.fillMaxWidth()) {
				Text("Démarrer en tant qu'appareil de commande")
			}
			TextButton({  }, Modifier.fillMaxWidth()) {
				Text("Démarrer en tant qu'appareil de cuisine")
			}
		}
	} else if(commandViewModel.isOnline) {
		Card(Modifier.fillMaxWidth()) {
			Column(Modifier.padding(8.dp)) {
				Text("Appareil connectés: Total ${commandViewModel.connectedDevices.size}")
				LazyColumn {
					items(commandViewModel.connectedDevices) {
						Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
							Text(it.name)
							Text("Type: ${it.type}")
							IconButton({ commandViewModel.disconnectDevice(it) }) {
								Icon(Lucide.LogOut, "Déconecter l'appareil")
							}
						}
					}
				}
			}
		}
	}
}