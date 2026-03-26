package fr.imacaron.torri.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import fr.imacaron.torri.viewmodel.BaseCommandViewModel

@Composable
fun ItemSelection(cols: Int, items: List<ItemEntity>, prices: List<PriceListItemEntity>, priceList: PriceListWithItem, commandViewModel: BaseCommandViewModel, modifier: Modifier = Modifier) {
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