package fr.imacaron.torri.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash
import fr.imacaron.torri.Destination
import fr.imacaron.torri.viewmodel.PriceListViewModel

@Composable
fun PriceListScreen(priceList: PriceListViewModel, navController: NavController) {
	Column(Modifier.padding(8.dp)) {
		Text("Cartes", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
		LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			items(priceList.priceLists) {
				Card(Modifier.clickable { navController.navigate(Destination.PRICE_LIST_EDIT.routeWithArg("id" to it.priceList.idPriceList.toString())) }) {
					Row(Modifier.padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
						Text(it.priceList.name, style = MaterialTheme.typography.headlineSmall)
						Text(it.priceList.currency, style = MaterialTheme.typography.bodySmall)
						Spacer(Modifier.weight(1f))
						IconButton( { navController.navigate(Destination.PRICE_LIST_EDIT.routeWithArg("id" to it.priceList.idPriceList.toString())) } ) {
							Icon(Lucide.Pencil, "Éditer la carte ${it.priceList.name}")
						}
					}
				}
			}
		}
	}
}