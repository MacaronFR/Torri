package fr.imacaron.torri.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.Destination
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel

@Composable
fun ServiceAddScreen(priceListViewModel: PriceListViewModel, serviceViewModel: ServiceViewModel, navController: NavController) {
	Column(Modifier.padding(8.dp)) {
		Text("Nouveau service", style = MaterialTheme.typography.headlineSmall)
		LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			items(priceListViewModel.priceLists) {
				Card {
					Row(
						Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable {
							serviceViewModel.create(it.priceList.idPriceList)
							navController.navigate(Destination.SERVICE_COMMAND.route) {
								popUpTo(Destination.SERVICE.route) {
									inclusive = false
								}
							}
						}.padding(16.dp, 8.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							"${it.priceList.name} (${it.priceList.currency})",
							style = MaterialTheme.typography.titleLarge
						)
						Icon(Lucide.ChevronRight, "Sélectionner")
					}
				}
			}
		}
	}
}