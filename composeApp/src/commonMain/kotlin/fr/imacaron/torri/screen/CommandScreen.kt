package fr.imacaron.torri.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.rememberScrollState
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
import fr.imacaron.torri.Nearby
import fr.imacaron.torri.components.CommandDetail
import fr.imacaron.torri.components.ItemSelection
import fr.imacaron.torri.components.ItemView
import fr.imacaron.torri.components.PayementDialog
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import fr.imacaron.torri.formatPrice
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel

@Composable
fun CommandScreen(cols: Int, displaySidePanel: Boolean, serviceViewModel: ServiceViewModel, navController: NavController, priceListViewModel: PriceListViewModel, commandViewModel: CommandViewModel, portrait: Boolean) {
	commandViewModel.loadService()
	val service = serviceViewModel.currentService ?: return
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
				ItemSelection(cols, items, prices, priceList, commandViewModel, Modifier.weight(3f))
				CommandDetail(commandViewModel, items, priceList, prices, portrait, displaySidePanel, Modifier.weight(1f))
			}
		} else {
			Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				ItemSelection(cols, items, prices, priceList, commandViewModel, Modifier.weight(3f))
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