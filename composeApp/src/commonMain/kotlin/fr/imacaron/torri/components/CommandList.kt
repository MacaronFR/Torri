package fr.imacaron.torri.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Banknote
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.ShieldQuestion
import fr.imacaron.torri.data.CommandEntity
import fr.imacaron.torri.data.CommandPriceListItemsWithPriceListItem
import fr.imacaron.torri.data.PriceListEntity
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.SavedItemViewModel
import org.jetbrains.compose.resources.painterResource
import torri.composeapp.generated.resources.Res
import torri.composeapp.generated.resources.allDrawableResources

@Composable
fun CommandList(
	commands: List<CommandEntity>,
	commandViewModel: CommandViewModel,
	priceList: PriceListEntity,
	savedItemsViewModel: SavedItemViewModel
) {
	if (commands.isEmpty()) {
		Text(
			"Aucun historique de commande pour ce service",
			Modifier.padding(16.dp),
			style = MaterialTheme.typography.headlineSmall
		)
	} else {
		LazyColumn {
			itemsIndexed(commands) { index, command ->
				var expand by remember { mutableStateOf(false) }
				val detail = mutableStateListOf<CommandPriceListItemsWithPriceListItem>()
				LaunchedEffect(expand, commandViewModel, command) {
					if (expand) {
						detail.addAll(commandViewModel.loadCommandDetail(command))
					}
				}
				Card(
					Modifier.padding(8.dp).animateContentSize().fillMaxWidth().heightIn(16.dp)
						.clickable { expand = !expand }) {
					Row(
						Modifier.padding(8.dp, 4.dp).fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							"Commande #${index + 1}, total ${command.total} ${priceList.currency}",
							style = MaterialTheme.typography.headlineSmall
						)
						val icon = when (command.payementMethod) {
							"ESP" -> Lucide.Banknote
							"CB" -> Lucide.CreditCard
							"SUMUP" -> Lucide.CreditCard
							else -> Lucide.ShieldQuestion
						}
						Icon(icon, "Méthode de paiement", Modifier.size(24.dp))
						IconButton({ commandViewModel.removeFromHistory(command) }) {
							Icon(Lucide.Minus, "Retirer la commande de l'historique")
						}
					}
					if (expand) {
						detail.forEach {
							Row(
								Modifier.padding(horizontal = 16.dp, 4.dp),
								horizontalArrangement = Arrangement.spacedBy(8.dp)
							) {
								if (it.priceListItem == null) {
									Text("Produit inconnu")
								} else {
									savedItemsViewModel.items.find { i -> i.idItem == it.priceListItem.idItem }
										?.let { item ->
											Image(
												painterResource(Res.allDrawableResources[item.image]!!),
												null,
												Modifier.size(24.dp)
											)
											Text(
												"${item.name} x${it.commandPriceListItem.quantity}",
												style = MaterialTheme.typography.bodyLarge
											)

										}
								}
							}
						}
					}
				}
			}
		}
	}
}