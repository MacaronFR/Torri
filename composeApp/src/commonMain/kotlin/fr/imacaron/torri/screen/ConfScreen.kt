package fr.imacaron.torri.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.LogIn
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.Nearby
import fr.imacaron.torri.SumUp
import fr.imacaron.torri.activated
import fr.imacaron.torri.clientKey
import fr.imacaron.torri.sumupAccessToken
import fr.imacaron.torri.sumupExpire
import fr.imacaron.torri.sumupRefreshToken
import kotlinx.coroutines.launch

@Composable
fun ConfScreen(dataStore: DataStore<Preferences>, snackBarState: SnackbarHostState, reloadConfScreen: Boolean, doReload: () -> Unit, nearby: Nearby) {
	val lifecycleOwner = LocalLifecycleOwner.current
	val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
	val scope = rememberCoroutineScope()
	var username by remember { mutableStateOf<String?>(null) }
	LaunchedEffect(lifecycleState) {
		dataStore.data.collect { pref ->
			username = pref[clientKey]
		}
		when(lifecycleState) {
			Lifecycle.State.RESUMED -> {
				SumUp.isLogged
			}
			else -> {}
		}
	}
	LaunchedEffect(reloadConfScreen) {
		SumUp.isLogged
	}
	Column(Modifier.padding(8.dp)) {
		Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
			Text(if(SumUp.isLogged) "SumUp ✔" else "SumUp")
			if(SumUp.isLogged) {
				Button({
					SumUp.logout()
					scope.launch {
						dataStore.updateData {
							it.toMutablePreferences().apply {
								remove(sumupAccessToken)
								remove(sumupRefreshToken)
								remove(sumupExpire)
							}
						}
					}
					doReload()
				}) {
					Text("Se déconnecter de SumUp")
					Icon(Lucide.LogOut, "Se déconnecter de SumUp")
				}
			} else {
				Button({
					scope.launch {
						val newTokens = SumUp.fetchToken()
						if(newTokens == null) {
							snackBarState.showSnackbar("Impossible de se connecter")
							return@launch
						}
						dataStore.updateData {
							it.toMutablePreferences().apply {
								set(sumupAccessToken, newTokens.access_token)
								newTokens.refresh_token?.let { refreshToken -> set(sumupRefreshToken, refreshToken)}
								newTokens.expires_in?.let { expiresIn -> set(sumupExpire, newTokens.received_at + expiresIn) }
							}
						}
						SumUp.login(newTokens.access_token)
					}
				}) {
					Text("Se connecter à SumUp")
					Icon(Lucide.LogIn, "Se connecter à SumUp")
				}
			}
		}
		if(SumUp.isLogged) {
			Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
				Button({ SumUp.cardReaderPage() }) {
					Text("Configuration du terminal")
				}
			}
		}
		Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
			Column {
				Text("Compte")
				if(username == null) {
					Text("Chargement...")
				}else {
					Text("Nom d'utilisateur: $username")
				}
			}
			Button({
				scope.launch {
					dataStore.updateData {
						it.toMutablePreferences().apply {
							remove(activated)
							remove(clientKey)
						}
					}
				}
			}, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)) {
				Text("Se déconnecter")
				Icon(Lucide.LogOut, "Se déconnecter")
			}
		}
		Row {
			Button(
				{
					if(nearby.discovering) {
						nearby.stopDiscovery()
					}else {
						nearby.startDiscovery()
					}
				},
				enabled = !nearby.advertising && nearby.connecting == null && nearby.connected == null
			) {
				if(nearby.discovering) {
					Text("Discovering")
				} else {
					Text("Discover")
				}
			}
			Button(
				{
					if(nearby.advertising) {
						nearby.stopAdvertising()
					} else {
						nearby.startAdvertising()
					}
				},
				enabled = !nearby.discovering && nearby.connecting == null && nearby.connected == null
			) {
				if(nearby.advertising) {
					Text("Advertising")
				} else {
					Text("Advertise")
				}
			}
		}
		if(nearby.discovering) {
			if(nearby.discoveredDevices.isEmpty()) {
				Text("Aucun appareil détecté")
			} else {
				LazyColumn {
					items(nearby.discoveredDevices) {
						Card(Modifier.padding(8.dp).fillMaxWidth().clickable { nearby.connect(it) }) {
							Row(Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
		nearby.connecting?.let { connecting ->
			Text("Connexion à ${connecting.name}…")
		}
		nearby.connected?.let { connected ->
			Card(Modifier.padding(8.dp).fillMaxWidth()) {
				Row(Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
					Text("Connecté à ${connected.name}")
					IconButton({ nearby.disconnect() }) {
						Icon(Lucide.LogOut, "Se déconnecter de l'appareil")
					}
				}
			}
		}
	}
}