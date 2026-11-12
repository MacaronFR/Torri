package fr.imacaron.torri.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash
import fr.imacaron.torri.data.PriceListWithItem
import fr.imacaron.torri.data.ServiceEntity
import kotlinx.datetime.number

@Composable
fun ServiceItem(service: ServiceEntity, priceList: PriceListWithItem, delete: () -> Unit, onClick: () -> Unit) {
	Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
		Row(Modifier.padding(16.dp, 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
			Column {
				Text("Service du ${service.date.day}/${service.date.month.number}/${service.date.year}")
				Text("Carte ${priceList.priceList.name} en ${priceList.priceList.currency}")
			}
			IconButton(delete) {
				Icon(Lucide.Trash, "Supprimer le service")
			}
		}
	}
}