package fr.imacaron.torri

import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import kotlinx.coroutines.channels.Channel

expect object SumUp {
	fun init()
	fun login()
	fun logout()
	fun pay(amount: Double, command: Map<Long, Int>, items: PriceListWithItem, prices: List<PriceListItemEntity>)
	val isLogged: Boolean
	val onPayCompleted: (data: CardTransactionInfo) -> Unit
	val transactionInfo: Channel<CardTransactionInfo>
	fun cardReaderPage()
}