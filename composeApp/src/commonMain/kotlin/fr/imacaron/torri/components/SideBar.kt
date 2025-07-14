package fr.imacaron.torri.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Inbox
import com.composables.icons.lucide.Lucide

@Composable
fun SideBar(paddingValues: PaddingValues) {
	var selected by remember { mutableStateOf(false) }
	NavigationRail(Modifier.padding(paddingValues)) {
		NavigationRailItem(
			selected = selected,
			onClick = { selected = !selected },
			icon = { Icon(Lucide.Inbox, contentDescription = "Icone de Produit") },
			label = { Text("Produits") }
		)
	}
}

@Composable
fun SidebarItem(text: String, icon: ImageVector, selected: Boolean, badge: String = "", onClick: () -> Unit) {
	Row(
		Modifier
			.width(336.dp)
			.height(56.dp)
			.padding(horizontal = 12.dp)
			.clip(RoundedCornerShape(24.dp))
			.background(if(selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)
			.clickable(onClick = onClick),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
	) {
		Row(Modifier.padding(start = 16.dp).fillMaxHeight(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			Icon(icon, contentDescription = "Icone de $text", tint = if(selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
			Text(text, color = if(selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
		}
		Text(badge, color = if(selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp, end = 24.dp))
	}
}