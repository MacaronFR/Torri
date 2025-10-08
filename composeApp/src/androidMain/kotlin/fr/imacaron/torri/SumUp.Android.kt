package fr.imacaron.torri

import com.sumup.merchant.reader.api.SumUpAPI
import com.sumup.merchant.reader.api.SumUpLogin
import com.sumup.merchant.reader.api.SumUpPayment
import com.sumup.merchant.reader.api.SumUpState
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import kotlinx.coroutines.channels.Channel
import java.math.BigDecimal

actual object SumUp {
	lateinit var activity: MainActivity

	actual fun init() {
		SumUpState.init(activity)
		activity.sumUpOnResult = onPayCompleted
	}

	actual fun login() {
		val login = SumUpLogin.builder("sup_afk_XVqKMitmvgVSZ5c9iwAO1d4yW7X63bNp").build()
		SumUpAPI.openLoginActivity(activity, login, 1)
	}

	actual fun logout() {
		SumUpAPI.logout()
	}

	actual fun pay(amount: Double, command: Map<Long, Int>, items: PriceListWithItem, prices: List<PriceListItemEntity>) {
		val payment = SumUpPayment.builder()
			.total(BigDecimal(amount))
			.currency(SumUpPayment.Currency.EUR)
			.apply {
				command.forEach {(id, qty) ->
					val item = items.items.find { it.idItem == id }!!
					val price = prices.find { it.idItem == id }!!.price * qty
					addAdditionalInfo("${item.name} X $qty", "$price€")
				}
			}.build()
		SumUpAPI.checkout(activity, payment, 2)
	}

	actual val isLogged: Boolean
		get() {
			return SumUpAPI.isLoggedIn()
		}

	actual val onPayCompleted: (data: CardTransactionInfo) -> Unit = {
		transactionInfo.trySend(it)
	}

	actual val transactionInfo: Channel<CardTransactionInfo> = Channel()

	actual fun cardReaderPage() {
		SumUpAPI.openCardReaderPage(activity, 4)
	}
}