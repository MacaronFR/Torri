package fr.imacaron.torri.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Dao
interface ItemDao {
	@Insert
	suspend fun insert(item: ItemEntity): Long

	@Query("SELECT * FROM ItemEntity")
	suspend fun getAll(): List<ItemEntity>

	@Delete
	suspend fun delete(item: ItemEntity): Int
}

@Entity
data class ItemEntity(
	@PrimaryKey(autoGenerate = true) val idItem: Long = 0L,
	val name: String,
	val image: String
)