package fr.imacaron.torri

import androidx.compose.runtime.mutableStateMapOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.data
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class CommandViewModel(private val dataStore: DataStore<Preferences>): ViewModel() {
	val items = data
	val command = mutableStateMapOf<Item, Int>()

	init {
		GlobalScope.launch {
			dataStore.data.collect { pref ->
				items.forEach { item ->
					command[item] = pref[intPreferencesKey(item.name + "_command")] ?: 0
				}
			}
		}
	}

	fun add(item: Item) {
		command[item] = command[item]!! + 1
	}

	fun remove(item: Item) {
		command[item]?.let {
			if(it > 0) {
				command[item] = it -1
			}
		}
	}

	fun save() {
		viewModelScope.launch {
			dataStore.updateData {
				it.toMutablePreferences().apply {
					items.forEach { item ->
						this[intPreferencesKey(item.name + "_command")] = command[item]!!
					}
				}
			}
		}
	}

	fun reset() {
		command.forEach { (key, _) -> command[key] = 0 }
		save()
	}
}