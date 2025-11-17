package fr.imacaron.torri.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Dao
interface CommandDao {
	@Insert
	suspend fun insert(command: CommandEntity): Long

	@Query("SELECT * FROM CommandEntity WHERE idService = :idService")
	suspend fun getByService(idService: Long): List<CommandEntity>

	@Query("DELETE FROM CommandEntity WHERE idCommand = :idCommand")
	suspend fun delete(idCommand: Long): Int
}

@Entity
data class CommandEntity(
	@PrimaryKey(autoGenerate = true) val idCommand: Long = 0L,
	val idService: Long,
	var total: Double,
	var payementMethod: String,
)