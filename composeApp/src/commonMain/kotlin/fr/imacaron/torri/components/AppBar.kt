package fr.imacaron.torri.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Save
import fr.imacaron.torri.Destination
import fr.imacaron.torri.saveToFile
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.ItemsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(itemsViewModel: ItemsViewModel, commandViewModel: CommandViewModel, navController: NavController) {
	var currentRoute by remember { mutableStateOf("") }
	navController.addOnDestinationChangedListener { controller, destination, arguments ->
		destination.route?.let { currentRoute = it }
	}
	TopAppBar(
		{ Text("Torri") },
		actions = {
			when(currentRoute) {
				Destination.COMMAND.route -> {
					IconButton({
						itemsViewModel.reset()
						commandViewModel.reset()
					}) {
						Icon(
							Lucide.RefreshCw,
							contentDescription = "Réinitialiser",
							tint = MaterialTheme.colorScheme.primary
						)
					}
					IconButton({
						saveToFile("vente.csv", itemsViewModel.toCSV())
					}) {
						Icon(Lucide.Save, contentDescription = "Sauvegarder", tint = MaterialTheme.colorScheme.primary)
					}
				}
				Destination.ITEMS.route -> {
					IconButton( { navController.navigate(Destination.ITEMS_ADD.route) }) {
						Icon(Lucide.Plus, contentDescription = "Ajouter un produit", tint = MaterialTheme.colorScheme.primary)
					}
				}
				Destination.PRICE_LIST.route -> {
					IconButton( { navController.navigate(Destination.PRICE_LIST_ADD.route) }) {
						Icon(Lucide.Plus, contentDescription = "Ajouter un tarif", tint = MaterialTheme.colorScheme.primary)
					}
				}
			}
		},
		navigationIcon = {
			if('/' in currentRoute) {
				IconButton({ navController.popBackStack() }) {
					Icon(
						Lucide.ArrowLeft,
						contentDescription = "Retour",
						tint = MaterialTheme.colorScheme.primary
					)
				}
			}
		}
	)
}