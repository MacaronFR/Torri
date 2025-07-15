package fr.imacaron.torri.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.composables.icons.lucide.Banknote
import com.composables.icons.lucide.CheckCheck
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.Destination
import fr.imacaron.torri.viewmodel.CommandViewModel

@Composable
fun FAB(navController: NavController, commandViewModel: CommandViewModel) {
	var currentRoute by remember { mutableStateOf("") }
	navController.addOnDestinationChangedListener { controller, destination, arguments ->
		destination.route?.let { currentRoute = it }
	}
	if(currentRoute == Destination.SERVICE_COMMAND.route) {
		var payementDialog by remember { mutableStateOf(false) }
		var payementMethod by remember { mutableStateOf("") }
		FloatingActionButton(onClick = { payementDialog = true }) {
			Icon(Lucide.CheckCheck, contentDescription = "Terminer la commande")
		}
		if(payementDialog) {
			Dialog({ payementDialog = false }) {
				Card {
					Text("Méthode de paiement", Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
					Column {
						Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).clip(RoundedCornerShape(16.dp)).clickable { payementMethod = "ESP" }, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
							RadioButton(payementMethod == "ESP", { payementMethod = "ESP" })
							Icon(Lucide.Banknote, contentDescription = "Paiement espèce")
							Text("Espèce")
						}
					}
					Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
						TextButton({
							if(payementMethod.isNotEmpty()) {
								commandViewModel.pay(payementMethod)
								payementDialog = false
								payementMethod = ""
							}
						}) {
							Text("Valider")
						}
					}
				}
			}
		}
	}
}