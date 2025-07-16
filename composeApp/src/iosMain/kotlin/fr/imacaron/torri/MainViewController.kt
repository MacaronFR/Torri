package fr.imacaron.torri

import androidx.compose.ui.window.ComposeUIViewController
import fr.imacaron.torri.data.getRoomDataBase
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
val dataStore = createDataStore {
    val documentDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    requireNotNull(documentDir).path + "/$DATA_STORE_FILE_NAME"
}

val client = HttpClient(Darwin) {
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
    install(ContentNegotiation) {
        json()
    }
}

@Suppress("unused", "FunctionName")
fun MainViewController() = ComposeUIViewController { App(getRoomDataBase(getDatabaseBuilder()), dataStore, client = client) }