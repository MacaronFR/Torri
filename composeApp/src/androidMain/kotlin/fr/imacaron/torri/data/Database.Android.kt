package fr.imacaron.torri.data

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.imacaron.torri.MainActivity
import kotlinx.coroutines.flow.first

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