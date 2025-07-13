package fr.imacaron.torri

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
	PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

internal const val DATA_STORE_FILE_NAME = "torri.preferences_pb"