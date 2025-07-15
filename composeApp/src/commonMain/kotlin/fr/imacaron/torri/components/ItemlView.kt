package fr.imacaron.torri.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CircleMinus
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.data.ItemEntity
import org.jetbrains.compose.resources.painterResource
import torri.composeapp.generated.resources.Res
import torri.composeapp.generated.resources.allDrawableResources

@Composable
fun ItemView(item: ItemEntity, price: Double, currency: String, add: () -> Unit, remove: () -> Unit) {
	Card(Modifier.clickable { add() }) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
				IconButton(onClick = { remove() }) {
					Icon(Lucide.CircleMinus, contentDescription = "Retirer un article")
				}
				Spacer(Modifier.weight(1f))
				Text("$price $currency", style = MaterialTheme.typography.headlineMedium)
			}
			Image(
				painterResource(Res.allDrawableResources.getValue(item.image)),
				contentDescription = "Image de ${item.name}",
				Modifier.size(160.dp).padding(bottom = 8.dp)
			)
			Text(item.name, style = MaterialTheme.typography.titleLarge)
		}
	}
}