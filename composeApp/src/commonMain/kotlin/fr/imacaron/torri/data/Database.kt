package fr.imacaron.torri.data

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.imacaron.torri.data.migration.Migration1to2Spec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Database(
	entities = [ItemEntity::class, PriceListEntity::class, PriceListItemEntity::class, CommandEntity::class, CommandPriceListItemEntity::class, ServiceEntity::class],
	version = 3,
	autoMigrations = [
		AutoMigration(from = 1, to = 2, spec = Migration1to2Spec::class),
		AutoMigration(from = 2, to = 3)
	]
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDataBaseConstructor::class)
abstract class AppDataBase: RoomDatabase() {
	abstract fun itemDao(): ItemDao
	abstract fun priceListDao(): PriceListDao
	abstract fun priceListItemDao(): PriceListItemDao
	abstract fun commandDao(): CommandDao
	abstract fun serviceDao(): ServiceDao
	abstract fun commandPriceListItemDao(): CommandPriceListItemDao
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

class Converters {
	@OptIn(ExperimentalTime::class)
	@TypeConverter
	fun fromTimestamp(value: Long?): LocalDate? {
		return value?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date }
	}

	@OptIn(ExperimentalTime::class)
	@TypeConverter
	fun dateToTimestamp(date: LocalDate?): Long? {
		return date?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds()
	}
}