package fr.imacaron.torri

import fr.imacaron.torri.data.ItemEntity
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.SavedItemViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel
import kotlinx.coroutines.delay
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
val lock = AtomicBoolean(false)

@OptIn(ExperimentalAtomicApi::class)
suspend fun createDataForDemo(savedItems: SavedItemViewModel, priceListViewModel: PriceListViewModel, serviceViewModel: ServiceViewModel) {
	if(lock.load()) return
	lock.store(true)
	createItems(savedItems)
	while(savedItems.items.isEmpty()) {
		delay(100)
	}
	savedItems.reload()
	while(savedItems.items.isEmpty()) {
		delay(100)
	}
	createPriceList(priceListViewModel, savedItems.items)
	while(priceListViewModel.priceLists.isEmpty()) {
		delay(100)
	}
	priceListViewModel.priceLists.firstOrNull()?.let { priceList ->
		serviceViewModel.create(priceList.priceList.idPriceList)
	}
	serviceViewModel.reload()
	lock.store(false)
}

private fun createItems(savedItems: SavedItemViewModel) {
	savedItems.create("Produit 1", "ananas")
	savedItems.create("Produit 2", "avocat")
	savedItems.create("Produit 3", "bonbons")
	savedItems.create("Produit 4", "banane")
}

private fun createPriceList(priceListViewModel: PriceListViewModel, items: List<ItemEntity>) {
	val prices = items.map { item ->
		PriceListItemEntity(idItem = item.idItem, price = item.idItem * 1.5, idPriceList = -1L)
	}
	priceListViewModel.create("Carte 1", "€", prices)
}