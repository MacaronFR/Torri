package fr.imacaron.torri.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.viewmodel.SavedItemViewModel

@Composable
fun AddEditItemInPriceListDialog(onDismiss: () -> Unit, savedItems: SavedItemViewModel, basePrice: String = "", baseItem: ItemEntity? = null, onDone: (Double, ItemEntity) -> Unit) {
	var price by remember { mutableStateOf(basePrice) }
	var selectedItem by remember { mutableStateOf(baseItem) }
	Dialog(onDismiss) {
		Card(Modifier.fillMaxWidth()) {
			OutlinedTextField(price,
				{ price = it.replace(',', '.') },
				label = { Text("Prix") },
				modifier = Modifier.padding(16.dp).fillMaxWidth(),
				keyboardActions = KeyboardActions(onDone = {
					price.toDoubleOrNull()?.let { p ->
						selectedItem?.let { onDone(p, it) }
					}
				}),
				singleLine = true,
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, autoCorrectEnabled = false, imeAction = ImeAction.Done)
			)
			LazyColumn {
				items(savedItems.items) { item ->
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
						Text(item.name)
					}
				}
			}
			Row(Modifier.padding(16.dp, 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
				TextButton({
					price.toDoubleOrNull()?.let { p ->
						selectedItem?.let { onDone(p, it) }
					}
				} ) {
					Text(if(baseItem == null) "Ajouter" else "Modifier")
				}
			}
		}
	}
}