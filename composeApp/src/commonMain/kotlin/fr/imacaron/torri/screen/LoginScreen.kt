package fr.imacaron.torri.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
	Column(
		Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
		horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Text(error)
		OutlinedTextField(clientId, { clientId = it }, Modifier.padding(bottom = 8.dp), label = { Text("Identifiant Client") })
		OutlinedTextField(licence, { licence = it }, Modifier.padding(bottom = 8.dp), label = { Text("N° Licence") })
		Button({
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
						} else {
							error = "unknown"
						}
					} ?: run {
						error = "unknown"
					}
				}
			}
		}) {
			Text("Connexion")
		}
	}
}