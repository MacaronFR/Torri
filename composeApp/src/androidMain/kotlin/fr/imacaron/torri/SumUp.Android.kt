package fr.imacaron.torri

import com.sumup.merchant.reader.api.SumUpAPI
import com.sumup.merchant.reader.api.SumUpLogin
import com.sumup.merchant.reader.api.SumUpPayment
import com.sumup.merchant.reader.api.SumUpState
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.channels.Channel
import org.publicvalue.multiplatform.oidc.DefaultOpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import java.math.BigDecimal

actual object SumUp {
	lateinit var activity: MainActivity

	var httpClient: HttpClient = HttpClient(CIO)

	actual fun init() {
		SumUpState.init(activity)
		activity.sumUpOnResult = onPayCompleted
		activity.sumupOnLogin = onLogin
	}

	actual fun login(accessToken: String) {
		val login = SumUpLogin.builder("sup_afk_XVqKMitmvgVSZ5c9iwAO1d4yW7X63bNp").accessToken(accessToken).build()
		SumUpAPI.openLoginActivity(activity, login, 158)
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

	actual var onLogin: suspend (isLogged: Boolean) -> Unit = {  }

	actual val transactionInfo: Channel<CardTransactionInfo> = Channel()

	actual fun cardReaderPage() {
		SumUpAPI.openCardReaderPage(activity, 4)
	}

	actual val codeAuthFlowFactory: CodeAuthFlowFactory = AndroidCodeAuthFlowFactory(useWebView = false)

	actual val openIDClient: DefaultOpenIdConnectClient by lazy {
		DefaultOpenIdConnectClient(httpClient, OpenIdConnectClientConfig().apply {
			endpoints {
				tokenEndpoint = "https://api.sumup.com/token"
				authorizationEndpoint = "https://api.sumup.com/authorize"
				userInfoEndpoint = "https://api.sumup.com/v0.1/me"
			}
			disableNonce = true
			clientId = "cc_classic_wWlsq1V9CwF8a76vjUWYPyFZFzAFN"
			clientSecret = "cc_sk_classic_RbrYRdcsvyeWU4oQzVpRrS2pVQW0BtPrrZpg0W0DBEu2xCQyCi"
			codeChallengeMethod = CodeChallengeMethod.S256
			redirectUri = "fr.imacaron.torri://torri.imacaron.fr/login/sumup"
		})
	}

	actual suspend fun fetchToken(): AccessTokenResponse? {
		try {
			return codeAuthFlowFactory.createAuthFlow(openIDClient).getAccessToken()
		} catch (t: Throwable) {
			t.printStackTrace()
			return null
		}
	}

	actual suspend fun refreshToken(refreshToken: String): AccessTokenResponse? {
		return try {
			openIDClient.refreshToken(refreshToken)
		} catch (t: Throwable) {
			t.printStackTrace()
			null
		}
	}
}