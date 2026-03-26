package fr.imacaron.torri.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CheckCheck
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.Nearby
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import fr.imacaron.torri.formatPrice
import fr.imacaron.torri.viewmodel.BaseCommandViewModel
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun CommandDetail(commandViewModel: BaseCommandViewModel, items: List<ItemEntity>, priceList: PriceListWithItem, prices: List<PriceListItemEntity>, portrait: Boolean, displaySidePanel: Boolean, modifier: Modifier = Modifier) {
	var expand by remember { mutableStateOf(false) }
	Card(modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { expand = !expand }.animateContentSize().heightIn(32.dp).run { if(!portrait) this.fillMaxHeight() else this }) {
		Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
			if(commandViewModel.totalItem > 1) {
				Text("${commandViewModel.totalItem} Articles", style = MaterialTheme.typography.headlineSmall)
			} else {
				Text("${commandViewModel.totalItem} Article", style = MaterialTheme.typography.headlineSmall)
			}
			Text("Total ${commandViewModel.totalPrice.formatPrice()}${priceList.priceList.currency}", style = MaterialTheme.typography.headlineSmall)
		}
		if(displaySidePanel || expand) {
			Column(Modifier.weight(1f).scrollable(rememberScrollState(), Orientation.Vertical)) {
				if(commandViewModel.command.isEmpty()) {
					Text("Aucun article")
				} else {
					commandViewModel.command.forEach { (priceListItemId, quantity) ->
						prices.find { it.idPriceListItem == priceListItemId }?.let { priceListItem ->
							Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
								Text("$quantity", style = MaterialTheme.typography.titleLarge)
								Text(items.find { it.idItem == priceListItem.idItem }?.name ?: "Inconnu", style = MaterialTheme.typography.titleLarge)
								Spacer(Modifier.weight(1f))
								Text("${priceListItem.price.times(quantity).formatPrice()} ${priceList.priceList.currency}", style = MaterialTheme.typography.titleMedium)
							}
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