package fr.imacaron.torri

import androidx.room.Room
import androidx.room.RoomDatabase
import fr.imacaron.torri.data.AppDataBase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDataBase> {
    val dbFilePath = documentDirectory() + "/torri_room.db"
    return Room.databaseBuilder<AppDataBase>(dbFilePath)
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