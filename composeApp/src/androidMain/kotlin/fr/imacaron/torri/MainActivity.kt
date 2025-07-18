package fr.imacaron.torri

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import fr.imacaron.torri.data.getRoomDataBase
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.network.tls.TLSConfigBuilder
import io.ktor.serialization.kotlinx.json.json
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
        if(chain?.first()?.subjectDN?.name == "CN=licence.imacaron.fr") {
            return
        } else {
            defaultTrustManager.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<out X509Certificate?>? = delegate.acceptedIssuers
}

class MainActivity : ComponentActivity() {
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

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        activity = this
        enableEdgeToEdge()
        if(resources.getBoolean(R.bool.force_portrait)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        super.onCreate(savedInstanceState)
        fun createDataStore() = createDataStore { applicationContext.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
        setContent {
            App(getRoomDataBase(getDatabaseBuilder(this)), createDataStore(), client = client)
        }
    }

    private var text = ""

    fun saveToFile(file: String, text: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, file)
            this@MainActivity.text = text
        }
        resultLauncher.launch(intent)
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            if(result.data != null) {
                var uri = result.data!!.data!!
                val stream = contentResolver.openOutputStream(uri)!!
                stream.write(text.toByteArray())
                stream.close()
            }
        }
    }
}