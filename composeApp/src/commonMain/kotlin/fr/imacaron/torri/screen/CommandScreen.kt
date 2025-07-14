package fr.imacaron.torri.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.imacaron.torri.components.ItemView
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.ItemsViewModel
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun CommandScreen(cols: Int, itemsViewModel: ItemsViewModel, commandViewModel: CommandViewModel, displaySidePanel: Boolean) {
	LazyVerticalGrid(
		GridCells.Fixed(cols),
		contentPadding = PaddingValues(4.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp)
	) {
		items(itemsViewModel.items) { item ->
			ItemView(
				item,
				itemsViewModel.itemsTotal[item.name] ?: 0,
				{
					itemsViewModel.add(item.name)()
					commandViewModel.add(item)
				},
				{
					itemsViewModel.remove(item.name)()
					commandViewModel.remove(item)
				}
			)
		}
	}
	if(displaySidePanel) {
		val total = commandViewModel.command.values.sum()
		Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
			Card(Modifier.padding(4.dp).fillMaxWidth()) {
				Text("Commande en cours :", style = MaterialTheme.typography.displaySmall)
				if(total == 0) {
					Text("Aucun article")
				}
				commandViewModel.command.filter { it.value > 0 }.forEach { (item, total) ->
					Text("${item.name} : x$total")
				}
			}
			Card(Modifier.padding(4.dp).fillMaxWidth()) {
				Text("Total : $total article${if(total > 1) "s" else ""}")
				Button({ commandViewModel.reset() } ) {
					Text("Valider")
				}
			}
		}
	}
}