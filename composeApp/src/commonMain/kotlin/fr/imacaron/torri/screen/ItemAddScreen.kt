package fr.imacaron.torri.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import fr.imacaron.torri.viewmodel.SavedItemViewModel
import org.jetbrains.compose.resources.painterResource
import torri.composeapp.generated.resources.Res
import torri.composeapp.generated.resources.allDrawableResources

@Composable
fun ItemAddScreen(savedItems: SavedItemViewModel, navController: NavController) {
	var name by remember { mutableStateOf("") }
	var image by remember { mutableStateOf(Res.allDrawableResources.keys.first()) }
	var imageDialog by remember { mutableStateOf(false) }
	Card(Modifier.padding(4.dp).fillMaxWidth()) {
		Text("Ajouter un article", Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
		Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			IconButton({ imageDialog = true }, Modifier.padding(4.dp)) {
				Image(painterResource(Res.allDrawableResources[image]!!), contentDescription = "Image", Modifier.size(48.dp))
			}
			OutlinedTextField(name, { name = it }, label = { Text("Nom") }, singleLine = true)
		}
		Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
			Button( {
				savedItems.create(name, image)
				navController.popBackStack()
				name = ""
				image = Res.allDrawableResources.keys.first()
			} ) {
				Text("Ajouter")
			}
		}
	}
	if(imageDialog) {
		Dialog(
			{ imageDialog = false }
		) {
			Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp)) {
				Text("Choisissez une image", Modifier.padding(24.dp, 24.dp, 24.dp, 16.dp), style = MaterialTheme.typography.headlineSmall)
				HorizontalDivider()
				LazyColumn(Modifier.padding(horizontal = 24.dp).heightIn(0.dp, 550.dp)) {
					items(Res.allDrawableResources.map { it.key to it.value }) { (imageKey, drawable) ->
						Row(
							Modifier
								.clip(RoundedCornerShape(24.dp))
								.clickable { image = imageKey }
								.fillMaxWidth(),
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(image == imageKey, {} )
							Image(painterResource(drawable), "Image $imageKey", Modifier.height(24.dp))
							Text(imageKey.capitalize(Locale.current).replace("_", " "), Modifier.padding(start = 8.dp))
						}
					}
				}
				HorizontalDivider()
				Row(Modifier.padding(24.dp, 8.dp, 24.dp, 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
					TextButton({ imageDialog = false }) { Text("Confirmer") }
				}
			}
		}
	}
}