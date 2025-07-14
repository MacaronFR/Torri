package fr.imacaron.torri.data.migration

import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec

@RenameColumn(
	tableName = "ItemEntity",
	fromColumnName = "id",
	toColumnName = "idItem"
)
class Migration1to2Spec: AutoMigrationSpec