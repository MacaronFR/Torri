package fr.imacaron.torri

import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import kotlinx.coroutines.channels.Channel
import org.publicvalue.multiplatform.oidc.DefaultOpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

expect object SumUp {
	fun init()
	fun login(accessToken: String)
	fun logout()
	fun pay(amount: Double, command: Map<Long, Int>, items: PriceListWithItem, prices: List<PriceListItemEntity>)
	val isLogged: Boolean
	val onPayCompleted: (data: CardTransactionInfo) -> Unit
	var onLogin: suspend (isLogged: Boolean) -> Unit
	val transactionInfo: Channel<CardTransactionInfo>
	fun cardReaderPage()

	val codeAuthFlowFactory: CodeAuthFlowFactory
	val openIDClient: DefaultOpenIdConnectClient

	suspend fun fetchToken(): AccessTokenResponse?
	suspend fun refreshToken(refreshToken: String): AccessTokenResponse?
}