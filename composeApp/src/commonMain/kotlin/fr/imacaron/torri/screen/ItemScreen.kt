package fr.imacaron.torri.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Plus
import fr.imacaron.torri.Destination
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.SavedItemViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import torri.composeapp.generated.resources.Res
import torri.composeapp.generated.resources.allDrawableResources

@Composable
fun ItemScreen(savedItems: SavedItemViewModel, priceListViewModel: PriceListViewModel, snackBarState: SnackbarHostState, navHost: NavHostController) {
	val scope = rememberCoroutineScope()
	var item by remember { mutableStateOf<ItemEntity?>(null) }
	Column {
		Text("Produits", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
		OutlinedCard(Modifier.padding(8.dp).fillMaxWidth().clickable(onClick = { navHost.navigate(Destination.ITEMS_ADD.route) })) {
			Row(Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
				Icon(Lucide.Plus, contentDescription = "Ajouter un article")
				Text("Ajouter un article", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 8.dp))
			}
		}
		LazyColumn {
			items(savedItems.items) {
				Card(Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth()) {
					Row(Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
						Image(painterResource(Res.allDrawableResources[it.image]!!), "Image de ${it.name}", Modifier.size(48.dp).padding(end = 8.dp))
						Text(it.name, style = MaterialTheme.typography.headlineSmall)
						Spacer(Modifier.weight(1f))
						IconButton( {
							if(priceListViewModel.priceLists.any { pl -> pl.items.any { i -> i.idItem == it.idItem } }) {
								scope.launch {
									snackBarState.showSnackbar("Ce produit est utilisé dans une carte et ne peut pas être supprimé")
								}
							} else {
								item = it
							}
						}) {
							Icon(Lucide.Minus, contentDescription = "Retirer un article")
						}
					}
				}
			}
		}
	}
	if(item != null) {
		AlertDialog(
			{ item = null },
			{ TextButton({
				savedItems.remove(item!!)
				item = null
			}) { Text("Supprimer") } },
			dismissButton = { TextButton({ item = null }) { Text("Annuler") } },
			title = { Text("Supprimer l'article") },
			text = { Text("Êtes vous sur de vouloir supprimer l'article ${item!!.name} ?") }
		)
	}
}