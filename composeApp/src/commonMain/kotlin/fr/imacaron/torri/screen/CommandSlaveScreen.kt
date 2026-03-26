package fr.imacaron.torri.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.LogIn
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.components.CommandDetail
import fr.imacaron.torri.components.ItemSelection
import fr.imacaron.torri.viewmodel.SlaveCommandViewModel
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun CommandSlaveScreen(slaveCommandViewModel: SlaveCommandViewModel, cols: Int, displaySidePanel: Boolean, portrait: Boolean) {
	if(!slaveCommandViewModel.connected) {
		LazyColumn {
			items(slaveCommandViewModel.detectedDevices) {
				Card(Modifier.fillMaxWidth().padding(8.dp).clickable { slaveCommandViewModel.connect(it) }) {
					Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
						Text(it.name)
						Icon(Lucide.LogIn, "Se connecter à l'appareil")
					}
				}
			}
		}
	} else if(slaveCommandViewModel.priceList == null) {
		Text("Chargement des prix...")
	} else {
		if (displaySidePanel) {
			if (portrait) {
				Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
					ItemSelection(cols, slaveCommandViewModel.items, slaveCommandViewModel.pricesScreen, slaveCommandViewModel.priceList!!, slaveCommandViewModel, Modifier.weight(3f))
					CommandDetail(slaveCommandViewModel, slaveCommandViewModel.items, slaveCommandViewModel.priceList!!, slaveCommandViewModel.pricesScreen, portrait, displaySidePanel, Modifier.weight(1f))
				}
			} else {
				Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					ItemSelection(cols, slaveCommandViewModel.items, slaveCommandViewModel.pricesScreen, slaveCommandViewModel.priceList!!, slaveCommandViewModel, Modifier.weight(3f))
					CommandDetail(slaveCommandViewModel, slaveCommandViewModel.items, slaveCommandViewModel.priceList!!, slaveCommandViewModel.pricesScreen, portrait, displaySidePanel, Modifier.weight(1f))
				}
			}
		} else {
			Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
				CommandDetail(slaveCommandViewModel, slaveCommandViewModel.items, slaveCommandViewModel.priceList!!, slaveCommandViewModel.pricesScreen, portrait, displaySidePanel)
				ItemSelection(cols, slaveCommandViewModel.items, slaveCommandViewModel.pricesScreen, slaveCommandViewModel.priceList!!, slaveCommandViewModel)
			}
		}
	}
}