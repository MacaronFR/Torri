package fr.imacaron.torri.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Dao
interface PriceListItemDao {
	@Insert
	suspend fun insert(priceListItem: PriceListItemEntity): Long

	@Insert
	suspend fun insertAll(priceListItems: List<PriceListItemEntity>): List<Long>

	@Query("SELECT * FROM PriceListItemEntity WHERE idPriceList = :idPriceList")
	suspend fun getAlByPriceList(idPriceList: Long): List<PriceListItemEntity>

	@Update
	suspend fun update(priceListItem: PriceListItemEntity): Int

	@Delete
	suspend fun delete(priceListItem: PriceListItemEntity): Int
}

@Entity
data class PriceListItemEntity(
	@PrimaryKey(autoGenerate = true) val idPriceListItem: Long = 0L,
	var idItem: Long,
	var price: Double,
	var idPriceList: Long
)