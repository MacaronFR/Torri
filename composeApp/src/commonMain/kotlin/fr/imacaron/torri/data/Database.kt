package fr.imacaron.torri.data

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.imacaron.torri.data.migration.Migration1to2Spec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
	entities = [ItemEntity::class, PriceListEntity::class, PriceListItemEntity::class],
	version = 2,
	autoMigrations = [
		AutoMigration(from = 1, to = 2, spec = Migration1to2Spec::class)
	]
)
@ConstructedBy(AppDataBaseConstructor::class)
abstract class AppDataBase: RoomDatabase() {
	abstract fun itemDao(): ItemDao
	abstract fun priceListDao(): PriceListDao
	abstract fun priceListItemDao(): PriceListItemDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDataBaseConstructor: RoomDatabaseConstructor<AppDataBase> {
	override fun initialize(): AppDataBase
}

fun getRoomDataBase(
	builder: RoomDatabase.Builder<AppDataBase>
): AppDataBase {
	return builder
		.fallbackToDestructiveMigrationOnDowngrade(true)
		.setDriver(BundledSQLiteDriver())
		.setQueryCoroutineContext(Dispatchers.IO)
		.build()
}