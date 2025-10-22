package fr.imacaron.torri.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.imacaron.torri.MainActivity
import io.ktor.util.cio.readChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

const val DB_NAME = "torri_room"

lateinit var mainActivity: MainActivity

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDataBase> {
	val appContext = ctx.applicationContext
	val dbFile = appContext.getDatabasePath("$DB_NAME.db")
	return Room.databaseBuilder<AppDataBase>(appContext, dbFile.absolutePath)
}

actual suspend fun exportDatabaseToFile(data: String) {
	mainActivity.requestCreateFileLauncher.launch("export_torri")
	var uri: Uri?
	do {
		uri = mainActivity.createDocumentFlow.first()
	} while (uri == null)
	mainActivity.contentResolver.openOutputStream(uri)?.use { output ->
		output.write(data.toByteArray())
	}
}

actual suspend fun importDatabaseFromFile(): Result<String> {
	mainActivity.requestDocumentAccessLauncher.launch(arrayOf("application/json"))
	var uri: Uri?
	do {
		uri = mainActivity.openDocumentsFlow.first()
	} while (uri == null)
	val inputStream = mainActivity.contentResolver.openInputStream(uri)
	if(inputStream == null) {
		return Result.failure(RuntimeException("Can't open file"))
	}
	val data = inputStream.readBytes().decodeToString()
	return Result.success(data)
}