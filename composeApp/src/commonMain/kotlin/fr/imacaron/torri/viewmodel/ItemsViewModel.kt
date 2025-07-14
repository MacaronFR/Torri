package fr.imacaron.torri.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.torri.data
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class ItemsViewModel(private val dataStore: DataStore<Preferences>): ViewModel() {
	val items = data
	val itemsTotal = mutableStateMapOf<String, Int>()

	init {
		viewModelScope.launch {
			dataStore.data.collect { pref ->
				items.forEach { item ->
					itemsTotal[item.name] = pref[intPreferencesKey(item.name)] ?: 0
				}
			}
		}
	}

	fun add(name: String): () -> Unit {
		return {
			itemsTotal[name] = itemsTotal[name]!! + 1
		}
	}

	fun remove(name: String): () -> Unit {
		return {
			if(itemsTotal[name]!! > 0) {
				itemsTotal[name] = itemsTotal[name]!! - 1
			}
		}
	}

	fun save() {
		viewModelScope.launch {
			dataStore.updateData {
				it.toMutablePreferences().apply {
					items.forEach { item ->
						this[intPreferencesKey(item.name)] = itemsTotal[item.name]!!
					}
				}
			}
		}
	}

	fun reset() {
		itemsTotal.forEach { (key, _) -> itemsTotal[key] = 0 }
		save()
	}

	fun toCSV(): String {
		return "Article;Total\n" + itemsTotal.map { (key, value) -> "$key;$value" }.joinToString("\n")
	}
}