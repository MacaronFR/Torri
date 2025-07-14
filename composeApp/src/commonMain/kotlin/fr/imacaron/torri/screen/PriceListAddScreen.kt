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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Plus
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.SavedItemViewModel
import org.jetbrains.compose.resources.painterResource
import torri.composeapp.generated.resources.Res
import torri.composeapp.generated.resources.allDrawableResources

@Composable
fun PriceListAddScreen(priceList: PriceListViewModel, savedItems: SavedItemViewModel, navController: NavController) {
	var name by remember { mutableStateOf("") }
	var currency by remember { mutableStateOf("") }
	val items = remember { mutableStateListOf<PriceListItemEntity>() }
	var addItemDialog by remember { mutableStateOf(false) }
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Card(Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp).fillMaxWidth()) {
			Text("Ajouter un tarif", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
			Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
				OutlinedTextField(name, { name = it }, label = { Text("Nom") })
				OutlinedTextField(currency, { currency = it }, label = { Text("Devise") })
			}
			Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
				TextButton({
					priceList.create(name, currency, items.toList())
					navController.popBackStack()
					name = ""
					currency = ""
					items.clear()
					addItemDialog = false
				}) { Text("Sauvegarder") }
			}
		}
		Card(Modifier.padding(horizontal = 4.dp).fillMaxWidth()) {
			Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
				Text("Produits", style = MaterialTheme.typography.headlineSmall)
				IconButton({ addItemDialog = true }) {
					Icon(Lucide.Plus, contentDescription = "Ajouter un produit")
				}
			}
			Column(Modifier.padding(16.dp).fillMaxWidth()) {
				items.forEach { item ->
					val itemEntity = savedItems.items.find { it.idItem == item.idItem }
					Row {
						Image(painterResource(Res.allDrawableResources[itemEntity?.image]!!), "Image de ${itemEntity?.name}", Modifier.padding(end = 8.dp).size(32.dp))
						Text("${itemEntity?.name}: ${item.price} $currency")
						Spacer(Modifier.weight(1f))
						IconButton({ items.remove(item) }) {
							Icon(Lucide.Minus, contentDescription = "Retirer un produit")
						}
					}
				}
			}
		}
	}
	if(addItemDialog) {
		var price by remember { mutableStateOf("") }
		var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }
		Dialog(
			{ addItemDialog = false }
		) {
			Card(Modifier.fillMaxWidth()) {
				OutlinedTextField(price, { price = it }, label = { Text("Prix en $currency") }, modifier = Modifier.padding(16.dp).fillMaxWidth())
				LazyColumn {
					items(savedItems.items) { item ->
						Row(
							Modifier
								.fillMaxWidth()
								.padding(horizontal = 16.dp)
								.clickable {
									selectedItem = item
								}
								.clip(RoundedCornerShape(24.dp)),
							horizontalArrangement = Arrangement.Start,
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(selectedItem?.idItem == item.idItem, { selectedItem = item })
							Text(item.name)
						}
					}
				}
				Row(Modifier.padding(16.dp, 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
					TextButton({
						if(price.toDoubleOrNull() != null && price.isNotEmpty() && selectedItem != null) {
							items.add(PriceListItemEntity(idItem = selectedItem!!.idItem, idPriceList = -1, price = price.toDouble()))
							price = ""
							addItemDialog = false
							selectedItem = null
						}
					}) {
						Text("Ajouter")
					}
				}
			}
		}
	}
}