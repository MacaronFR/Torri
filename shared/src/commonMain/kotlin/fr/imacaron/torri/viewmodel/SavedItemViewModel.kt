package fr.imacaron.torri.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.torri.data.AppDataBase
import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.resources.Res
import fr.imacaron.torri.resources.allDrawableResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SavedItemViewModel(
	val db: AppDataBase
): ViewModel() {
	val items = mutableStateListOf<ItemEntity>()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			items.clear()
			items.addAll(db.itemDao().getAll())
			println("loaded")
		}
	}

	fun reload(): Job {
		return viewModelScope.launch(Dispatchers.IO) {
			items.clear()
			items.addAll(db.itemDao().getAll())
			println("reloaded")
		}
	}

	fun create(name: String, image: String): Boolean {
		if(name.isEmpty()) {
			return false
		}
		if(image !in Res.allDrawableResources) {
			return false
		}
		viewModelScope.launch {
			db.itemDao().insert(ItemEntity(name = name, image = image))
			items.clear()
			items.addAll(db.itemDao().getAll())
			println("Created")
		}
		return true
	}

	fun remove(item: ItemEntity): Boolean {
		if(item in items) {
			viewModelScope.launch {
				if(db.itemDao().delete(item) != 1) {
					//ERROR
				}
			}
			items.remove(item)
			return true
		}
		return false
	}

	fun update(item: ItemEntity) {
		if(item.idItem in items.map { it.idItem }) {
			viewModelScope.launch {
				db.itemDao().update(item)
			}
			val i = items.indexOfFirst { it.idItem == item.idItem }
			items[i] = item
		}
	}
}