package fr.imacaron.torri

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import io.ktor.serialization.kotlinx.json.json

class LicenceRegistration(client: HttpClient? = null) {

	@Serializable
	private data class Auth(
		val licence: String,
		val deviceId: String,
		val model: String,
		val brand: String,
		val version: String,
		val platform: String
	)

	private val client = client ?: HttpClient(CIO) {
		install(ContentNegotiation) {
			json()
		}
	}

	suspend fun register(clientId: String, licence: String): Result<Unit> {
		if(!isNetworkAvailable()) {
			return Result.failure(Exception("No network"))
		}
		return withContext(Dispatchers.IO) {
			try {
				val response = client.post("https://licence.imacaron.fr/torri/$clientId/licence") {
					contentType(ContentType.Application.Json)
					setBody(Auth(
						licence,
						deviceId,
						model,
						brand,
						version,
						platform
					))
				}
				if(response.status.value == 404) {
					return@withContext Result.failure(Exception("ID client error"))
				}
				if(response.status.value == 403 && response.bodyAsText() == "Licence number mismatch") {
					return@withContext Result.failure(Exception("licence number error"))
				}
				if(response.status.value == 403 && response.bodyAsText() == "Max devices reached") {
					return@withContext Result.failure(Exception("max devices"))
				}
				if(response.status.value != 200) {
					return@withContext Result.failure(Exception("Unknown error"))
				}
				Result.success(Unit)
			} catch(_: Exception) {
				Result.failure(Exception("Error on network"))
			}
		}
	}

	suspend fun validate(clientId: String): Result<Unit> {
		if(!isNetworkAvailable()) {
			return Result.success(Unit)
		}
		return withContext(Dispatchers.IO) {
			val response = client.get("https://licence.imacaron.fr/torri/$clientId/devices/$deviceId")
			if(response.status.value != 200) {
				return@withContext Result.failure(Exception("Unknown error"))
			} else {
				return@withContext Result.success(Unit)
			}
		}
	}
}