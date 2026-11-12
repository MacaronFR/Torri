package fr.imacaron.torri

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.DISPATCH_QUEUE_SERIAL_WITH_AUTORELEASE_POOL
import platform.darwin.dispatch_queue_create

actual fun isNetworkAvailable(): Boolean {
    return runBlocking {
        return@runBlocking networkStatusFlow.first()
    }
}

private val networkStatusFlow = watchNetworkStatus()

private fun watchNetworkStatus(): Flow<Boolean> {
    return callbackFlow {
        val monitor = nw_path_monitor_create()
        val queue = dispatch_queue_create(
            "fr.imacaron.torri.NetworkConnectivity",
            DISPATCH_QUEUE_SERIAL_WITH_AUTORELEASE_POOL
        )
        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            when (status) {
                nw_path_status_satisfied -> trySend(true)
                else -> {
                    trySend(false)
                }
            }
        }
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)

        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }
}