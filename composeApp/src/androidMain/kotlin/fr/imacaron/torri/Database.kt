package fr.imacaron.torri

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.imacaron.torri.data.AppDataBase

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDataBase> {
	val appContext = ctx.applicationContext
	val dbFile = appContext.getDatabasePath("torri_room.db")
	return Room.databaseBuilder<AppDataBase>(appContext, dbFile.absolutePath)
}