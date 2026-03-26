package fr.imacaron.torri.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.composables.icons.lucide.CheckCheck
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.Destination
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import fr.imacaron.torri.viewmodel.BaseCommandViewModel
import fr.imacaron.torri.viewmodel.SlaveCommandViewModel

@Composable
fun FAB(navController: NavController, commandViewModel: BaseCommandViewModel, items: PriceListWithItem?, prices: List<PriceListItemEntity>) {
	var currentRoute by remember { mutableStateOf("") }
	navController.addOnDestinationChangedListener { _, destination, _ ->
		destination.route?.let { currentRoute = it }
	}
	if(currentRoute == Destination.SERVICE_COMMAND.route || commandViewModel is SlaveCommandViewModel) {
		var payementDialog by remember { mutableStateOf(false) }
		FloatingActionButton(onClick = { payementDialog = true }) {
			Icon(Lucide.CheckCheck, contentDescription = "Terminer la commande")
		}
		if(payementDialog) {
			if(commandViewModel is SlaveCommandViewModel) {
				commandViewModel.priceList?.let {
					PayementDialog({ payementDialog = false }, commandViewModel, it, commandViewModel.pricesScreen)
				}
			} else if(items != null){
				PayementDialog({ payementDialog = false }, commandViewModel, items, prices)
			}
		}
	}
}