package fr.imacaron.torri.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.composables.icons.lucide.Banknote
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.SumUp
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import fr.imacaron.torri.viewmodel.CommandViewModel
import kotlinx.coroutines.launch

@Composable
fun PayementDialog(onDismiss: () -> Unit, commandViewModel: CommandViewModel, items: PriceListWithItem, prices: List<PriceListItemEntity>) {
	var payementMethod by remember { mutableStateOf("") }
	val coroutineScope = rememberCoroutineScope()
	Dialog(onDismiss ) {
		Card {
			Text("Méthode de paiement", Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
			Column {
				Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).clip(RoundedCornerShape(16.dp)).clickable { payementMethod = "ESP" }, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
					RadioButton(payementMethod == "ESP", { payementMethod = "ESP" })
					Icon(Lucide.Banknote, contentDescription = "Paiement espèce")
					Text("Espèce")
				}
				if(!SumUp.isLogged) {
					Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).clip(RoundedCornerShape(16.dp)).clickable { payementMethod = "CB" }, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
						RadioButton(payementMethod == "CB", { payementMethod = "CB" })
						Icon(Lucide.CreditCard, contentDescription = "Paiement carte bancaire")
						Text("Carte Bancaire")
					}
				}
				if(SumUp.isLogged) {
					Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).clip(RoundedCornerShape(16.dp)).clickable { payementMethod = "SUMUP" }, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
						RadioButton(payementMethod == "SUMUP", { payementMethod = "SUMUP" })
						Icon(Lucide.CreditCard, contentDescription = "Paiement SumUp")
						Text("SumUp")
					}
				}
			}
			Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
				TextButton({
					if(payementMethod.isNotEmpty()) {
						if(payementMethod == "SUMUP" && SumUp.isLogged) {
							SumUp.pay(commandViewModel.totalPrice, commandViewModel.command.toMap(), items, prices)
							coroutineScope.launch {
								val info = SumUp.transactionInfo.receive()
								if(info.status != "FAILED") {
									commandViewModel.pay(payementMethod)
									onDismiss()
									payementMethod = ""
								} else {
									// TODO: Gestion erreur
								}
							}
						} else {
							commandViewModel.pay(payementMethod)
							onDismiss()
							payementMethod = ""
						}
					}
				}) {
					Text("Valider")
				}
			}
		}
	}
}