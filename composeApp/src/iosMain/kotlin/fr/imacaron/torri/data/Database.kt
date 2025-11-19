package fr.imacaron.torri.data

import androidx.room.Room
import androidx.room.RoomDatabase
import fr.imacaron.torri.ios.openfile.OpenFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel
import okio.ByteString.Companion.toByteString
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDataBase> {
    val dbFilePath = documentDirectory() + "/torri_room.db"
    return Room.databaseBuilder<AppDataBase>(dbFilePath)
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun exportDatabaseToFile(data: String) {
    val channel = Channel<Boolean>(1)
    OpenFile.shared.text = data
    OpenFile.shared.setOpenFileExport(true)
    OpenFile.shared.setOnResultSave {
        channel.trySend(true)
    }
    channel.receive()
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun importDatabaseFromFile(): Result<String> {
    val channel = Channel<String?>(1)
    OpenFile.shared.setOpenFileBrowser(true)
    OpenFile.shared.setOnResult { urls ->
        urls?.firstOrNull()?.let { url ->
            if("Cloud" in (url as NSURL).toString()) {
                channel.trySend("Cloud error")
                return@setOnResult
            }
            val data = NSData.dataWithContentsOfFile(url.path!!)
            val string = data?.toByteString()?.utf8()
            channel.trySend(string)
        } ?: channel.trySend(null)
    }
    val data = channel.receive()
    return if(data != null) {
        if(data == "Cloud error") {
            Result.failure(RuntimeException("Le fichier ne peut pas être récupérer depuis ICloud"))
        } else {
            Result.success(data)
        }
    } else {
        Result.failure(RuntimeException("Impossible de lire le fichier"))
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        NSDocumentDirectory,
        NSUserDomainMask,
        null,
        false,
        null
    )
    return requireNotNull(documentDirectory?.path)
}