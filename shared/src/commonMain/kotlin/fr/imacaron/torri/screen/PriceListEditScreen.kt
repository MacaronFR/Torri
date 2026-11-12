package fr.imacaron.torri.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash
import fr.imacaron.torri.components.AddEditItemInPriceListDialog
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.formatPrice
import fr.imacaron.torri.resources.Res
import fr.imacaron.torri.resources.allDrawableResources
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.SavedItemViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.collections.get

@Composable
fun PriceListEditScreen(priceListViewModel: PriceListViewModel, savedItems: SavedItemViewModel, navController: NavController, id: Long, serviceViewModel: ServiceViewModel, snackBarState: SnackbarHostState, commandViewModel: CommandViewModel) {
	val priceList = priceListViewModel.priceLists.find { it.priceList.idPriceList == id }
	var name by remember { mutableStateOf(priceList?.priceList?.name ?: "") }
	var currency by remember { mutableStateOf(priceList?.priceList?.currency ?: "") }
	val items = remember { mutableStateListOf<PriceListItemEntity>() }
	val itemsId = remember { mutableStateListOf<Long>() }
	val scope = rememberCoroutineScope()
	LaunchedEffect(priceList) {
		if(priceList != null) {
			items.clear()
			items.addAll(priceListViewModel.getPriceListItems(priceList.priceList))
			itemsId.addAll(items.map { it.idItem })
		}
	}
	var addItemDialog by remember { mutableStateOf(false) }
	var editItem by remember { mutableStateOf<PriceListItemEntity?>(null) }
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Card(Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp).fillMaxWidth()) {
			Row(Modifier.padding(horizontal = 4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
				Text("Modifier une carte", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
				IconButton( {
					priceList?.let {
						if(serviceViewModel.currentService?.idPriceList == it.priceList.idPriceList || serviceViewModel.services.any { service -> service.idPriceList == it.priceList.idPriceList}) {
							scope.launch {
								snackBarState.showSnackbar("Impossible de supprimer cette carte. Elle est actuellement utilisé dans le service actuel ou un service terminé")
							}
						} else {
							priceListViewModel.delete(priceList.priceList)
							navController.popBackStack()
						}
					}
				}) {
					Icon(Lucide.Trash, contentDescription = "Supprimer cette carte")
				}
			}
			Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
				OutlinedTextField(name, { name = it }, label = { Text("Nom") })
				OutlinedTextField(currency, { currency = it }, label = { Text("Devise") })
			}
			Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
				Button({
					if(priceList == null) {
						return@Button
					}
					items.forEachIndexed { index, entity -> entity.index = index }
					priceListViewModel.update(priceList.priceList.idPriceList, name, currency, items.toList())
					commandViewModel.loadService()
					navController.popBackStack()
				}, enabled = name.isNotEmpty() && currency.isNotEmpty()) { Text("Sauvegarder") }
			}
		}
		Card(Modifier.padding(horizontal = 4.dp).fillMaxWidth()) {
			LazyColumn(Modifier.padding(16.dp).fillMaxWidth()) {
				item {
					Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
						Text("Produits", style = MaterialTheme.typography.headlineSmall)
						IconButton({ addItemDialog = true }) {
							Icon(Lucide.Plus, contentDescription = "Ajouter un produit")
						}
					}
				}
				if(items.isEmpty()) {
					item {
						Text("Aucun produit", Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
					}
				}
				itemsIndexed(items, { _, item -> item.idPriceListItem }) { index, item ->
					var y by remember { mutableStateOf(0f) }
					var h by remember { mutableStateOf(0) }
					val state = rememberDraggableState {
						if(index == 0 && it < 0) {
							return@rememberDraggableState
						}
						if(index == items.size - 1 && it > 0) {
							return@rememberDraggableState
						}
						y += it
						if(y > h) {
							val nextItem = items[index + 1]
							items.removeAt(index + 1)
							items.add(index, nextItem)
							y = 0f
						} else if(y < -h) {
							val prevItem = items[index - 1]
							items.removeAt(index - 1)
							items.add(index, prevItem)
							y = 0f
						}
					}
					val itemEntity = savedItems.items.find { it.idItem == item.idItem }
					Row(Modifier.offset { IntOffset(0, y.toInt()) }.onGloballyPositioned { h = it.size.height }, verticalAlignment = Alignment.CenterVertically) {
						Icon(Lucide.GripVertical, "Déplacer", Modifier.draggable(state, Orientation.Vertical, onDragStopped = { y = 0f }))
						Image(painterResource(Res.allDrawableResources[itemEntity?.image]!!), "Image de ${itemEntity?.name}", Modifier.padding(end = 8.dp).size(32.dp))
						Text("${itemEntity?.name}: ${item.price.formatPrice()} $currency")
						Spacer(Modifier.weight(1f))
						IconButton({ editItem = item }) {
							Icon(Lucide.Pencil, contentDescription = "Modifier un produit")
						}
						IconButton({ items.remove(item); itemsId.remove(item.idItem) }) {
							Icon(Lucide.Minus, contentDescription = "Retirer un produit")
						}
					}
				}
			}
		}
	}
	if(editItem != null) {
		AddEditItemInPriceListDialog( { editItem = null }, savedItems, itemsId, editItem?.price?.toString() ?: "", editItem?.let { ei -> savedItems.items.find { it.idItem == ei.idItem }}) { price, selectedItem ->
			if(priceList != null) {
				items.find { it.idPriceListItem == editItem?.idPriceListItem }?.let {
					itemsId.remove(it.idItem)
					itemsId.add(selectedItem.idItem)
					it.price = price
					it.idItem = selectedItem.idItem
				}
				editItem = null
			}
		}
	}
	if(addItemDialog) {
		var price by remember { mutableStateOf("") }
		var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }
		val onDone = {
			if(price.toDoubleOrNull() != null && price.isNotEmpty() && selectedItem != null) {
				if(priceList != null) {
					val index = (items.maxByOrNull { it.index }?.index ?: 0) + 1
					items.add(PriceListItemEntity(idItem = selectedItem!!.idItem, idPriceList = priceList.priceList.idPriceList, price = price.toDouble(), index = index))
					itemsId.add(selectedItem!!.idItem)
					price = ""
					addItemDialog = false
					selectedItem = null
				}
			}
		}
		Dialog(
			{ addItemDialog = false }
		) {
			Card(Modifier.fillMaxWidth()) {
				OutlinedTextField(price, { price = it.replace(',', '.') }, label = { Text("Prix en $currency") }, modifier = Modifier.padding(16.dp).fillMaxWidth(), keyboardActions = KeyboardActions(onDone = { onDone() }), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, autoCorrectEnabled = false, imeAction = ImeAction.Done))
				LazyColumn {
					if(savedItems.items.count() == itemsId.count()) {
						item {
							Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
								Text("Aucun produit disponible", Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
							}
						}
					}
					items(savedItems.items.filter { it.idItem !in itemsId }) { item ->
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
							Image(painterResource(Res.allDrawableResources[item.image]!!), "Image de ${item.name}", Modifier.padding(end = 8.dp).size(32.dp))
							Text(item.name)
						}
					}
				}
				Row(Modifier.padding(16.dp, 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
					TextButton(onDone) {
						Text("Ajouter")
					}
				}
			}
		}
	}
}