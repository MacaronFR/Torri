package fr.imacaron.torri

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Save

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(itemsViewModel: ItemsViewModel, commandViewModel: CommandViewModel) {
	TopAppBar(
		{ Text("Torri") },
		actions = {
			IconButton({
				itemsViewModel.reset()
				commandViewModel.reset()
			}) {
				Icon(Lucide.RefreshCw, contentDescription = "Réinitialiser", tint = MaterialTheme.colorScheme.primary)
			}
			IconButton({
				saveToFile("vente.csv", itemsViewModel.toCSV())
			}) {
				Icon(Lucide.Save, contentDescription = "Sauvegarder", tint = MaterialTheme.colorScheme.primary)
			}
		}
	)
}