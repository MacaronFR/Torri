package fr.imacaron.torri.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import fr.imacaron.torri.Destination

@Composable
fun BottomBar(navController: NavController) {
	var currentRoute by remember { mutableStateOf("") }
	navController.addOnDestinationChangedListener { controller, destination, arguments ->
		currentRoute = destination.route ?: ""
	}
	NavigationBar {
		Destination.entries.filter { '/' !in it.route }.forEach { dest ->
			NavigationBarItem(
				selected = currentRoute == dest.route,
				onClick = { navController.navigate(dest.route) },
				icon = { Icon(dest.icon, contentDescription = "Icone de ${dest.label}") },
				label = { Text(dest.label) }
			)
		}
	}
}