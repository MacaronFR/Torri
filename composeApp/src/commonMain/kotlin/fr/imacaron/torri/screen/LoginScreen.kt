package fr.imacaron.torri.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import fr.imacaron.torri.LicenceRegistration
import fr.imacaron.torri.activated
import fr.imacaron.torri.clientKey
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(dataStore: DataStore<Preferences>, licenceRegistration: LicenceRegistration) {
	var clientId by remember { mutableStateOf("") }
	var licence by remember { mutableStateOf("") }
	var error by remember { mutableStateOf("") }
	val scope = rememberCoroutineScope()
	val licenceFocus = remember { FocusRequester() }
	fun login() {
		scope.launch {
			licenceRegistration.register(clientId, licence).onSuccess {
				dataStore.updateData {
					it.toMutablePreferences().apply {
						set(activated, true)
						set(clientKey, clientId)
					}
				}
			}.onFailure {
				it.message?.let { message ->
					if ("client" in message) {
						error = "client"
					} else if ("licence" in message) {
						error = "licence"
					} else if("network" in message){
						error = "network"
					} else if("devices" in message) {
						error = "devices"
					} else {
						error = "unknown"
					}
				} ?: run {
					error = "unknown"
				}
			}
		}
	}
	Column(
		Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
		horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		if(error == "network") {
			Text("Erreur: Aucune connexion au réseau", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
		} else if(error == "unknown") {
			Text("Erreur inconnue", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
		} else if(error == "devices") {
			Text("Erreur: Cette licence utilise déjà son nombre maximum d'appareils", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
		}
		OutlinedTextField(
			clientId,
			{ clientId = it },
			Modifier.padding(bottom = 8.dp),
			label = { Text("Identifiant Client") },
			isError = error == "client",
			singleLine = true,
			supportingText = { if(error == "client") Text("Erreur sur l'identifiant Client") },
			keyboardActions = KeyboardActions(onNext = { licenceFocus.requestFocus() }),
			keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
		)
		OutlinedTextField(licence,
			{ licence = it },
			Modifier.padding(bottom = 8.dp).focusRequester(licenceFocus),
			label = { Text("N° Licence") },
			isError = error == "licence",
			singleLine = true,
			supportingText = { if(error == "licence") Text("Erreur sur le N° de Licence") },
			keyboardActions = KeyboardActions(onDone = { login() }),
			keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
		)
		Button({ login() }) {
			Text("Connexion")
		}
	}
}