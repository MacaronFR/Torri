package fr.imacaron.torri.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import fr.imacaron.torri.sumupAccessToken
import fr.imacaron.torri.sumupExpire
import fr.imacaron.torri.sumupRefreshToken
import kotlinx.coroutines.launch

@Composable
fun ConfScreen(dataStore: DataStore<Preferences>, snackBarState: SnackbarHostState, reloadConfScreen: Boolean, doReload: () -> Unit) {
	val lifecycleOwner = LocalLifecycleOwner.current
	val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
	val scope = rememberCoroutineScope()
	LaunchedEffect(lifecycleState) {
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
	}
}