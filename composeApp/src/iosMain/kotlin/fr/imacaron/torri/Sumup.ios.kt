package fr.imacaron.torri

import androidx.compose.material3.SnackbarHostState
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import fr.imacaron.torri.ios.SMPCheckoutRequest
import fr.imacaron.torri.ios.SMPPaymentMethodCardReader
import fr.imacaron.torri.ios.SMPProcessAsDebit
import fr.imacaron.torri.ios.SMPSumUpSDK
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.IosCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import platform.Foundation.NSDecimalNumber
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
actual object SumUp {
    lateinit var viewController: UIViewController

    actual var snackBarState: SnackbarHostState? = null

    actual fun init() {
        SMPSumUpSDK.setupWithAPIKey("sup_afk_XVqKMitmvgVSZ5c9iwAO1d4yW7X63bNp")
    }

    actual fun login(accessToken: String) {
        SMPSumUpSDK.loginWithToken(accessToken) { isLogged, error ->
            runBlocking {
                onLogin(isLogged)
            }
        }
    }

    actual fun logout() {
        SMPSumUpSDK.logoutWithCompletionBlock(null)
    }

    actual fun pay(amount: Double, command: Map<Long, Int>, items: PriceListWithItem, prices: List<PriceListItemEntity>) {
        val payment = SMPCheckoutRequest.requestWithTotal(NSDecimalNumber(amount), null, "EUR", SMPPaymentMethodCardReader).apply {
            setProcessAs(SMPProcessAsDebit)
        }
        SMPSumUpSDK.checkoutWithRequest(payment, viewController) { result, error ->
            result?.let {
                onPayCompleted(CardTransactionInfo(
                    amount,
                    if(it.success) "SUCESS" else "FAILED"
                ))
            }
        }
    }

    actual val isLogged: Boolean
        get() {
            return SMPSumUpSDK.isLoggedIn()
        }

    actual val onPayCompleted: (data: CardTransactionInfo) -> Unit = {
        transactionInfo.trySend(it)
    }
    actual var onLogin: suspend (isLogged: Boolean) -> Unit = {}

    actual val transactionInfo: Channel<CardTransactionInfo> = Channel()

    @OptIn(DelicateCoroutinesApi::class)
    actual fun cardReaderPage() {
        GlobalScope.launch {
            snackBarState?.showSnackbar("Fonctionnalité non disponible sur iOs")
        }
    }

    actual val codeAuthFlowFactory: CodeAuthFlowFactory = IosCodeAuthFlowFactory(false)

    actual val openIDClient: OpenIdConnectClient by lazy {
        OpenIdConnectClient {
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
        }
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