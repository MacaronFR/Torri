package fr.imacaron.torri

import androidx.compose.runtime.mutableStateMapOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import fr.imacaron.data
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class ItemsViewModel(private val dataStore: DataStore<Preferences>): ViewModel() {
	val items = data
	val itemsTotal = mutableStateMapOf<String, Int>()

	init {
		GlobalScope.launch {
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

	suspend fun save() {
		dataStore.updateData {
			it.toMutablePreferences().apply {
				items.forEach { item ->
					this[intPreferencesKey(item.name)] = itemsTotal[item.name]!!
				}
			}
		}
	}

	suspend fun reset() {
		itemsTotal.forEach { (key, _) -> itemsTotal[key] = 0 }
		save()
	}

	fun toCSV(): String {
		return "Article;Total\n" + itemsTotal.map { (key, value) -> "$key;$value" }.joinToString("\n")
	}
}