package fr.imacaron.torri.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey

@Dao
interface CommandPriceListItemDao {
	@Insert
	suspend fun insert(commandPriceListItem: CommandPriceListItemEntity): Long

	@Insert
	suspend fun insertAll(commandPriceListItems: List<CommandPriceListItemEntity>): List<Long>
}

@Entity
data class CommandPriceListItemEntity(
	@PrimaryKey(autoGenerate = true) val idCommandPriceListItem: Long = 0L,
	val idCommand: Long,
	val idPriceListItem: Long,
	var quantity: Int
)