package fr.imacaron.torri

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.lifecycleScope
import com.sumup.merchant.reader.api.SumUpAPI
import com.sumup.merchant.reader.models.TransactionInfo
import fr.imacaron.torri.data.getDatabaseBuilder
import fr.imacaron.torri.data.getRoomDataBase
import fr.imacaron.torri.data.mainActivity
import fr.imacaron.torri.shared.R
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.network.tls.TLSConfigBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.registerForActivityResultSuspend
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@SuppressLint("CustomX509TrustManager")
class CustomTrustManager(config: TLSConfigBuilder): X509TrustManager {
	private val delegate = config.build().trustManager

	private val defaultTrustManager: X509TrustManager

	init {
		val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())!!
		factory.init(null as KeyStore?)
		val manager = factory.trustManagers!!

		defaultTrustManager = manager.filterIsInstance<X509TrustManager>().first()
	}

	override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
		defaultTrustManager.checkClientTrusted(chain, authType)
	}

	override fun checkServerTrusted(chain: Array<out X509Certificate?>?, authType: String?) {
		when(chain?.first()?.subjectDN?.name) {
			"CN=api.sumup.com" -> return
			"CN=licence.imacaron.fr" -> return
			else -> defaultTrustManager.checkServerTrusted(chain, authType)
		}
	}

	override fun getAcceptedIssuers(): Array<out X509Certificate?>? = delegate.acceptedIssuers
}

object DataStoreHolder {
	var dataStore: DataStore<Preferences>? = null
}

class Main(val activity: ComponentActivity) {
	val client = HttpClient(CIO) {
		engine {
			https {
				trustManager = CustomTrustManager(this)
			}
		}
		install(ContentNegotiation) {
			json()
		}
	}

	private val nearby = NearbyAndroid(this)

	@SuppressLint("SourceLockedOrientationActivity")
	fun onCreate(savedInstanceState: Bundle?) {
		mainActivity = this
		main = this
		(SumUp.codeAuthFlowFactory as AndroidCodeAuthFlowFactory).registerActivity(activity)
		activity.enableEdgeToEdge()
		if(activity.resources.getBoolean(R.bool.force_portrait)) {
			activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		}
		val dataStore = DataStoreHolder.dataStore ?: run {
			DataStoreHolder.dataStore = createDataStore { activity.applicationContext.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
			DataStoreHolder.dataStore!!
		}
		SumUp.activity = this
		SumUp.httpClient = client
		activity.setContent {
			App(getRoomDataBase(getDatabaseBuilder(activity)), dataStore, client = client, nearby = nearby)
		}
	}

	private var text = ""

	fun saveToFile(file: String, text: String) {
		val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
			addCategory(Intent.CATEGORY_OPENABLE)
			type = "text/csv"
			putExtra(Intent.EXTRA_TITLE, file)
			this@Main.text = text
		}
		resultLauncher.launch(intent)
	}

	val resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if(result.resultCode == RESULT_OK) {
			if(result.data != null) {
				var uri = result.data!!.data!!
				val stream = activity.contentResolver.openOutputStream(uri)!!
				stream.write(text.toByteArray())
				stream.close()
			}
		}
	}

	lateinit var sumUpOnResult: (data: CardTransactionInfo) -> Unit

	lateinit var sumupOnLogin: suspend (logged: Boolean) -> Unit

	fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if(requestCode == 158 && data != null) {
			val resultCode = data.extras?.getInt(SumUpAPI.Response.RESULT_CODE)
			activity.lifecycleScope.launch {
				sumupOnLogin(resultCode != SumUpAPI.Response.ResultCode.ERROR_INVALID_TOKEN)
			}
		}
		if(requestCode == 2 && data != null) {
			val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				data.extras?.getParcelable(SumUpAPI.Response.TX_INFO, TransactionInfo::class.java)
			} else {
				@Suppress("DEPRECATION")
				data.extras?.getParcelable(SumUpAPI.Response.TX_INFO)
			}!!
			sumUpOnResult(CardTransactionInfo(
				data.amount,
				data.status
			))
		}
	}

	private val permissionFlow = MutableStateFlow<Map<String, Boolean>?>(null)
	val requestPermissionLauncherSuspend = activity.registerForActivityResultSuspend(permissionFlow, ActivityResultContracts.RequestMultiplePermissions())

	val openDocumentsFlow = MutableStateFlow<Uri?>(null)
	val requestDocumentAccessLauncher = activity.registerForActivityResultSuspend(openDocumentsFlow, ActivityResultContracts.OpenDocument())

	val createDocumentFlow = MutableStateFlow<Uri?>(null)
	val requestCreateFileLauncher = activity.registerForActivityResultSuspend(createDocumentFlow, ActivityResultContracts.CreateDocument("application/json"))

	private suspend fun requestPermission(permissions: List<String>): Boolean {
		val notGranted = permissions.filter { ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED }
		val toAsk = notGranted.filter { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
		val toRequest = notGranted.filter { !ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
		requestPermissionLauncherSuspend.launch((toAsk + toRequest).toTypedArray())
		val map = permissionFlow.first { it != null }
		return map?.all { it.value } ?: false
	}

	suspend fun checkPermission(block: (granted: Boolean) -> Unit) {
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
			block(requestPermission(listOf(
				Manifest.permission.ACCESS_WIFI_STATE,
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.ACCESS_FINE_LOCATION
			)))
		} else {
			block(requestPermission(listOf(
				Manifest.permission.ACCESS_WIFI_STATE,
				Manifest.permission.NEARBY_WIFI_DEVICES,
				Manifest.permission.BLUETOOTH_ADVERTISE,
				Manifest.permission.BLUETOOTH_SCAN,
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.BLUETOOTH_CONNECT,
			)))
		}
	}
}