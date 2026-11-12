package fr.imacaron.torri.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction

@Dao
interface CommandPriceListItemDao {
	@Insert
	suspend fun insert(commandPriceListItem: CommandPriceListItemEntity): Long

	@Insert
	suspend fun insertAll(commandPriceListItems: List<CommandPriceListItemEntity>): List<Long>

	@Transaction
	@Query("SELECT * FROM CommandPriceListItemEntity WHERE idCommand = :idCommand")
	suspend fun getByCommand(idCommand: Long): List<CommandPriceListItemsWithPriceListItem>
}

@Entity
data class CommandPriceListItemEntity(
	@PrimaryKey(autoGenerate = true) val idCommandPriceListItem: Long = 0L,
	val idCommand: Long,
	val idPriceListItem: Long,
	var quantity: Int
)

data class CommandPriceListItemsWithPriceListItem(
	@Embedded val commandPriceListItem: CommandPriceListItemEntity,
	@Relation(
		parentColumn = "idPriceListItem",
		entityColumn = "idPriceListItem"
	) val priceListItem: PriceListItemEntity?
)