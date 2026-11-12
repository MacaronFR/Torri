package fr.imacaron.torri.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface PriceListDao {
	@Insert
	suspend fun insert(priceList: PriceListEntity): Long

	@Transaction
	@Query("SELECT * FROM PriceListEntity")
	suspend fun getAll(): List<PriceListWithItem>

	@Delete
	suspend fun delete(priceList: PriceListEntity): Int

	@Update
	suspend fun updatePriceList(priceList: PriceListEntity): Int
}

@Entity
data class PriceListEntity(
	@PrimaryKey(autoGenerate = true) val idPriceList: Long = 0L,
	val name: String,
	val currency: String
)

data class PriceListWithItem(
	@Embedded val priceList: PriceListEntity,
	@Relation(
		parentColumn = "idPriceList",
		entityColumn = "idItem",
		associateBy = Junction(PriceListItemEntity::class)
	)
	val items: List<ItemEntity>
)