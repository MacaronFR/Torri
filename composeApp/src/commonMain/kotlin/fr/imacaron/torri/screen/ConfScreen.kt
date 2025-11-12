package fr.imacaron.torri.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import com.composables.icons.lucide.LogIn
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.SumUp
import fr.imacaron.torri.activated
import fr.imacaron.torri.clientKey
import fr.imacaron.torri.sumupAccessToken
import fr.imacaron.torri.sumupExpire
import fr.imacaron.torri.sumupRefreshToken
import kotlinx.coroutines.launch

@Composable
fun ConfScreen(dataStore: DataStore<Preferences>, snackBarState: SnackbarHostState, reloadConfScreen: Boolean, doReload: () -> Unit) {
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
	Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Card(Modifier.fillMaxWidth()) {
			Column(Modifier.padding(8.dp)) {
				Text("Compte", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
				if(username == null) {
					Text("Chargement...")
				}else {
					Text("Nom d'utilisateur: $username")
				}
			}
		}
		Card(Modifier.fillMaxWidth()) {
			Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
				Text(if(SumUp.isLogged) "SumUp ✔" else "SumUp", style = MaterialTheme.typography.headlineSmall)
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
						Text("Se déconnecter")
						Icon(Lucide.LogOut, "Se déconnecter")
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
						Text("Se connecter")
						Icon(Lucide.LogIn, "Se connecter")
					}
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
		Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
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
	}
}