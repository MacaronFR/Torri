package fr.imacaron.torri.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import fr.imacaron.torri.Nearby
import fr.imacaron.torri.data.AppDataBase
import fr.imacaron.torri.data.exportDatabase
import fr.imacaron.torri.data.importDatabase
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SyncDialog(display: Boolean, nearby: Nearby, db: AppDataBase, snackbarState: SnackbarHostState, onDismiss: () -> Unit) {
	LaunchedEffect(nearby.master, nearby.connected, nearby) {
		withContext(Dispatchers.IO) {
			if (!nearby.master && nearby.connected != null) {
				val data = nearby.receiveData()?.decodeToString()
				if (data == null) {
					return@withContext
				}
				importDatabase(data, db)
				withContext(Dispatchers.Main) {
					onDismiss()
					nearby.disconnect()
					snackbarState.showSnackbar("Transfert réussi", duration = SnackbarDuration.Long)
				}
			}
		}
	}
	val scope = rememberCoroutineScope()
	if (display) {
		Dialog(onDismiss) {
			Card(Modifier.fillMaxWidth()) {
				Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
					Text("Synchronisation des appareils", style = MaterialTheme.typography.titleMedium)
					if(nearby.connected == null && nearby.connecting == null) {
						Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
							Button({
								if (nearby.advertising) {
									nearby.stopAdvertising()
								} else {
									nearby.startAdvertising()
								}
							}, Modifier.weight(1f), !nearby.discovering) {
								Text(
									if (nearby.advertising) "En attente de connexion…" else "Vers un autre appareil",
									Modifier.fillMaxWidth(),
									textAlign = TextAlign.Center
								)
							}
							Button({
								if (nearby.discovering) {
									nearby.stopDiscovery()
								} else {
									nearby.startDiscovery()
								}
							}, Modifier.weight(1f), !nearby.advertising) {
								Text(
									if (nearby.discovering) "Recherche d'appareil…" else "Depuis un autre appareil",
									Modifier.fillMaxWidth(),
									textAlign = TextAlign.Center
								)
							}
						}
					}
					if (nearby.discovering) {
						if (nearby.discoveredDevices.isEmpty()) {
							Text("Aucun appareil détecté")
						} else {
							LazyColumn(Modifier.fillMaxWidth()) {
								items(nearby.discoveredDevices) {
									Card(Modifier.clip(RoundedCornerShape(4.dp)).padding(4.dp).fillMaxWidth().clickable { nearby.connect(it) }) {
										Row(
											Modifier.padding(8.dp).fillMaxWidth(),
											horizontalArrangement = Arrangement.SpaceBetween,
											verticalAlignment = Alignment.CenterVertically
										) {
											Text("Appareil ${it.name} (${it.id})")
											IconButton({ nearby.connect(it) }) {
												Icon(Lucide.ArrowRight, "Se connecter à l'appareil")
											}
										}
									}
								}
							}
						}
					}
					nearby.connecting?.let { device ->
						Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
							Text("Connexion à ${device.name}")
							IconButton({ nearby.disconnect() }) {
								Icon(Lucide.X, "Annuler")
							}
						}
					}
					nearby.connected?.let { device ->
						ElevatedCard(Modifier.fillMaxWidth()) {
							Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
								Text(device.name, Modifier.padding(8.dp), style = MaterialTheme.typography.titleMedium)
								if(nearby.master) {
									TextButton({
										scope.launch {
											val data = exportDatabase(db)
											nearby.sendData(data.toByteArray())
										}
									}) {
										Text("Transférer les données")
									}
								}
								IconButton({ nearby.disconnect() }) {
									Icon(Lucide.LogOut, "Se déconnecter de l'appareil")
								}
							}
						}
					}
				}
			}
		}
	}
}