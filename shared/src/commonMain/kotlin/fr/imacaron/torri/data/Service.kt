package fr.imacaron.torri.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.datetime.LocalDate

@Dao
interface ServiceDao {
	@Insert
	suspend fun create(service: ServiceEntity): Long

	@Delete
	suspend fun delete(service: ServiceEntity): Int

	@Query("SELECT * FROM ServiceEntity WHERE pending = false")
	suspend fun getAllDone(): List<ServiceEntity>

	@Query("SELECT * FROM ServiceEntity WHERE pending = true")
	suspend fun getAllNotDone(): List<ServiceEntity>

	@Query("SELECT * FROM ServiceEntity WHERE idService = :id")
	suspend fun getById(id: Long): ServiceEntity?

	@Transaction
	@Query("SELECT * FROM ServiceEntity WHERE idService = :id")
	suspend fun getByIdWithPriceList(id: Long): ServiceWithPriceList?

	@Transaction
	@Query("SELECT * FROM ServiceEntity WHERE idService = :id")
	suspend fun getByIdWithCommands(id: Long): ServiceWithCommands?

	@Update
	suspend fun update(service: ServiceEntity): Int
}

@Entity
data class ServiceEntity(
	@PrimaryKey(autoGenerate = true) val idService: Long = 0L,
	val date: LocalDate,
	val idPriceList: Long,
	val pending: Boolean,
)

data class ServiceWithPriceList(
	@Embedded val service: ServiceEntity,
	@Relation(
		parentColumn = "idPriceList",
		entityColumn = "idPriceList"
	)
	val priceList: PriceListEntity
)

data class ServiceWithCommands(
	@Embedded val service: ServiceEntity,
	@Relation(
		parentColumn = "idService",
		entityColumn = "idService"
	)
	val commands: List<CommandEntity>
)