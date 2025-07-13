package fr.imacaron.torri

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleMinus
import com.composables.icons.lucide.CirclePlus
import com.composables.icons.lucide.Lucide
import org.jetbrains.compose.resources.painterResource
import torri.composeapp.generated.resources.Res
import torri.composeapp.generated.resources.allDrawableResources

@Composable
fun ItemView(item: Item, total: Int, add: () -> Unit, remove: () -> Unit) {
	Card(Modifier.clickable { add() }) {
		Box(Modifier.padding(4.dp)) {
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Image(
					painterResource(Res.allDrawableResources.getValue(item.image)),
					contentDescription = "Image de ${item.name}"
				)
				Text(item.name)
				Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
					Button(onClick = { if(total > 0) remove() }) {
						Icon(Lucide.CircleMinus, contentDescription = "Retirer un article", tint = MaterialTheme.colorScheme.onPrimary)
					}
					Button(onClick = add) {
						Icon(Lucide.CirclePlus, contentDescription = "Ajouter un article", tint = MaterialTheme.colorScheme.onPrimary)
					}
				}
			}
			Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
				Text("$total")
			}
		}
	}
}