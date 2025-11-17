package fr.imacaron.torri.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.imacaron.torri.data.AppDataBase
import fr.imacaron.torri.data.PriceListEntity
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import kotlinx.coroutines.launch

class PriceListViewModel(private val db: AppDataBase): ViewModel() {
	val priceLists = mutableStateListOf<PriceListWithItem>()

	init {
		loadPriceLists()
	}

	fun loadPriceLists() {
		priceLists.clear()
		viewModelScope.launch {
			priceLists.addAll(db.priceListDao().getAll())
		}
	}

	fun create(name: String, currency: String, items: List<PriceListItemEntity>): Boolean {
		if(name.isEmpty() || currency.isEmpty()) {
			return false
		}
		viewModelScope.launch {
			val priceListId = db.priceListDao().insert(PriceListEntity(name = name, currency = currency))
			items.apply { this.forEach { it.idPriceList = priceListId } }.forEach {
				db.priceListItemDao().insert(it)
			}
			priceLists.clear()
			priceLists.addAll(db.priceListDao().getAll())
		}
		return true
	}

	suspend fun getPriceListItems(priceList: PriceListEntity): List<PriceListItemEntity> {
		return db.priceListItemDao().getAlByPriceList(priceList.idPriceList)
	}

	fun update(id: Long, name: String, currency: String, items: List<PriceListItemEntity>) {
		priceLists.find { it.priceList.idPriceList == id }?.let { priceList ->
			viewModelScope.launch {
				val newPriceList = PriceListEntity(idPriceList = id, name = name, currency = currency)
				db.priceListDao().updatePriceList(newPriceList)
				priceLists.clear()
				priceLists.addAll(db.priceListDao().getAll())
				val oldItems = db.priceListItemDao().getAlByPriceList(id)
				val oldItemsId = oldItems.map { it.idPriceListItem }
				val newItemsId = items.map { it.idPriceListItem }
				items.forEach { newItem ->
					if(newItem.idPriceListItem !in oldItemsId) {
						db.priceListItemDao().insert(newItem)
					} else {
						val updatedItem = PriceListItemEntity(newItem.idPriceListItem, newItem.idItem, newItem.price, id)
						db.priceListItemDao().update(updatedItem)
					}
				}
				oldItems.forEach { oldItem ->
					if(oldItem.idPriceListItem !in newItemsId) {
						db.priceListItemDao().delete(oldItem)
					}
				}
//				priceLists.find { it.priceList.idPriceList == id }?.let { updatedPriceList ->
//
//					val itemsId = updatedPriceList.items.map { it.idItem }
//					items.forEach { item ->
//						if(item.idItem !in itemsId) {
//							db.priceListItemDao().insert(item)
//						}
//					}
//					val newItemsId = items.map { it.idItem }
//					val priceListItems = getPriceListItems(updatedPriceList.priceList)
//					itemsId.forEach { oldItemId ->
//						if(oldItemId !in newItemsId) {
//							db.priceListItemDao().delete(priceListItems.find { it.idItem == oldItemId }!!)
//						}
//					}
//				}
			}
		}
		loadPriceLists()
	}

	fun delete(priceList: PriceListEntity) {
		viewModelScope.launch {
			getPriceListItems(priceList).forEach {
				db.priceListItemDao().delete(it)
			}
			db.priceListDao().delete(priceList)
			priceLists.clear()
			priceLists.addAll(db.priceListDao().getAll())
		}
	}
}