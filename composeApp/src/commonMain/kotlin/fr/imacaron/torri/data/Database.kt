package fr.imacaron.torri.data

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.Transactor
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.exp
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

	suspend fun cleanDb() {
		serviceDao().getAllDone().forEach { service ->
			serviceDao().delete(service)
		}
		serviceDao().getAllNotDone().forEach { service ->
			serviceDao().delete(service)
		}
		priceListDao().getAll().forEach { priceList ->
			priceListItemDao().getAlByPriceList(priceList.priceList.idPriceList).forEach { priceListItem ->
				priceListItemDao().delete(priceListItem)
			}
			priceListDao().delete(priceList.priceList)
		}
		itemDao().getAll().forEach { item ->
			itemDao().delete(item)
		}
	}
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

@Serializable
data class Export(
	val items: List<ItemExport>,
	val priceLists: List<PriceListExport>,
)

@Serializable
data class ItemExport(
	val id: Long,
	val name: String,
	val image: String
)

@Serializable
data class PriceListExport(
	val id: Long,
	val name: String,
	val currency: String,
	val prices: List<PricesExport>
)

@Serializable
data class PricesExport(
	val id: Long,
	val item: Long,
	val price: Double
)

suspend fun exportDatabase(db: AppDataBase): String {
	val items = db.itemDao().getAll().map { item ->
		ItemExport(item.idItem, item.name, item.image)
	}
	val priceLists = db.priceListDao().getAll().map { priceList ->
		val prices = db.priceListItemDao().getAlByPriceList(priceList.priceList.idPriceList).map { priceListItem ->
			PricesExport(priceListItem.idPriceListItem, priceListItem.idItem, priceListItem.price)
		}
		PriceListExport(priceList.priceList.idPriceList, priceList.priceList.name, priceList.priceList.currency, prices)
	}
	val export = Export(items, priceLists)
	return Json.encodeToString(export)
}

expect suspend fun exportDatabaseToFile(data: String)

suspend fun importDatabase(data: String, db: AppDataBase) {
	val export = Json.decodeFromString<Export>(data)
	db.useConnection(false) {
		it.withTransaction(Transactor.SQLiteTransactionType.EXCLUSIVE) {
			try {
				db.cleanDb()
				export.items.forEach { item ->
					db.itemDao().insert(ItemEntity(idItem = item.id, name = item.name, image = item.image))
				}
				export.priceLists.forEach { priceList ->
					val priceListEntity = PriceListEntity(idPriceList = priceList.id, name = priceList.name, currency = priceList.currency)
					val priceListId = db.priceListDao().insert(priceListEntity)
					priceList.prices.forEach { price ->
						db.priceListItemDao().insert(PriceListItemEntity(idPriceListItem = price.id, idItem = price.item, price = price.price, idPriceList = priceListId))
					}
				}
			} catch (t: Throwable) {
				t.printStackTrace()
				rollback(Unit)
			}
		}
	}
}

expect suspend fun importDatabaseFromFile(): Result<String>