package fr.imacaron.torri.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.imacaron.torri.Nearby

@Composable
fun CommandSlaveScreen(nearby: Nearby) {
	if(nearby.connected == null) {
		LazyColumn {
			items(nearby.discoveredDevices) {
				Card(Modifier.fillMaxWidth().clickable { nearby.connect(it) }) {
					it.name
				}
			}
		}
	} else {
		Text("Command Slave Ready")
	}
}