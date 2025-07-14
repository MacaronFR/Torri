package fr.imacaron.torri.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Dao
interface PriceListItemDao {
	@Insert
	suspend fun insert(priceListItem: PriceListItemEntity): Long

	@Insert
	suspend fun insertAll(priceListItems: List<PriceListItemEntity>): List<Long>

	@Query("SELECT * FROM PriceListItemEntity WHERE idPriceList = :idPriceList")
	suspend fun getAlByPriceList(idPriceList: Long): List<PriceListItemEntity>

	@Delete
	suspend fun delete(priceListItem: PriceListItemEntity): Int
}

@Entity
data class PriceListItemEntity(
	@PrimaryKey(autoGenerate = true) val idPriceListItem: Long = 0L,
	val idItem: Long,
	val price: Double,
	var idPriceList: Long
)