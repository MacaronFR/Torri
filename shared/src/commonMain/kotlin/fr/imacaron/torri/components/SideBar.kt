package fr.imacaron.torri.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import fr.imacaron.torri.Destination

@Composable
fun SideBar(navController: NavController) {
	var currentRoute by remember { mutableStateOf("") }
	navController.addOnDestinationChangedListener { controller, destination, arguments ->
		currentRoute = destination.route ?: ""
	}
	NavigationRail {
		Destination.entries.filter { '/' !in it.route }.forEach { dest ->
			NavigationRailItem(
				selected = currentRoute == dest.route,
				onClick = { navController.navigate(dest.route) { popUpTo(Destination.SERVICE.route) { inclusive = true } } },
				icon = { Icon(dest.icon, contentDescription = dest.label) },
				label = { Text(dest.label) }
			)
		}
	}
}