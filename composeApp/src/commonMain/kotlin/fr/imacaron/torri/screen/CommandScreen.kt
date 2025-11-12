package fr.imacaron.torri.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.composables.icons.lucide.CheckCheck
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.Destination
import fr.imacaron.torri.components.ItemView
import fr.imacaron.torri.components.PayementDialog
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel

@Composable
fun CommandScreen(cols: Int, displaySidePanel: Boolean, serviceViewModel: ServiceViewModel, navController: NavController, priceListViewModel: PriceListViewModel, commandViewModel: CommandViewModel, portrait: Boolean) {
	commandViewModel.loadService()
	val service = serviceViewModel.currentService
	if(service == null) {
		return
	}
	commandViewModel.service = service
	val priceList = priceListViewModel.priceLists.find { it.priceList.idPriceList == service.idPriceList }
	if(priceList == null) {
		navController.navigate(Destination.SERVICE.route)
		return
	}
	val items = priceList.items
	val prices = remember { mutableStateListOf<PriceListItemEntity>() }
	LaunchedEffect(priceListViewModel, priceList) {
		prices.addAll(priceListViewModel.getPriceListItems(priceList.priceList))
	}
	if(displaySidePanel) {
		if(portrait) {
			Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
				ItemSelection(cols, items, prices, priceList, commandViewModel, Modifier.weight(2f))
				CommandDetail(commandViewModel, items, priceList, prices, portrait, displaySidePanel, Modifier.weight(1f))
			}
		} else {
			Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				ItemSelection(cols, items, prices, priceList, commandViewModel, Modifier.weight(1f))
				CommandDetail(commandViewModel, items, priceList, prices, portrait, displaySidePanel, Modifier.weight(1f))
			}
		}
	} else {
		Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
			CommandDetail(commandViewModel, items, priceList, prices, portrait, displaySidePanel)
			ItemSelection(cols, items, prices, priceList, commandViewModel)
		}
	}
}

@Composable
fun CommandDetail(commandViewModel: CommandViewModel, items: List<ItemEntity>, priceList: PriceListWithItem, prices: List<PriceListItemEntity>, portrait: Boolean, displaySidePanel: Boolean, modifier: Modifier = Modifier) {
	var expand by remember { mutableStateOf(false) }
	Card(modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { expand = !expand }.animateContentSize().heightIn(32.dp).run { if(!portrait) this.fillMaxHeight() else this }) {
		Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
			if(commandViewModel.totalItem > 1) {
				Text("${commandViewModel.totalItem} Articles", style = MaterialTheme.typography.headlineSmall)
			} else {
				Text("${commandViewModel.totalItem} Article", style = MaterialTheme.typography.headlineSmall)
			}
			Text("Total ${commandViewModel.totalPrice}€", style = MaterialTheme.typography.headlineSmall)
		}
		if(displaySidePanel || expand) {
			Column(Modifier.weight(1f)) {
				if(commandViewModel.command.isEmpty()) {
					Text("Aucun article")
				} else {
					commandViewModel.command.forEach { (itemId, quantity) ->
						Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
							Text(items.find { it.idItem == itemId }?.name ?: "Inconnu", style = MaterialTheme.typography.titleLarge)
							Text("x$quantity", style = MaterialTheme.typography.titleLarge)
							Spacer(Modifier.weight(1f))
							Text("${commandViewModel.prices[itemId]?.times(quantity)} ${priceList.priceList.currency}", style = MaterialTheme.typography.titleMedium)
						}
					}
				}
			}
		}
		if(displaySidePanel) {
			var payementDialog by remember { mutableStateOf(false) }
			Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End) {
				Button({ payementDialog = true }) {
					Icon(Lucide.CheckCheck, "Payer")
					Text("Payer")
				}
			}
			if(payementDialog) {
				PayementDialog({ payementDialog = false }, commandViewModel, priceList, prices)
			}
		}
	}
}

@Composable
fun ItemSelection(cols: Int, items: List<ItemEntity>, prices: List<PriceListItemEntity>, priceList: PriceListWithItem, commandViewModel: CommandViewModel, modifier: Modifier = Modifier) {
	LazyVerticalGrid(
		GridCells.Fixed(cols),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier = modifier
	) {
		items(items) { item ->
			prices.find { it.idItem == item.idItem }?.let { price ->
				ItemView(
					item,
					price.price,
					priceList.priceList.currency,
					{ commandViewModel.add(price) },
					{ commandViewModel.remove(price) })
			}
		}
	}
}