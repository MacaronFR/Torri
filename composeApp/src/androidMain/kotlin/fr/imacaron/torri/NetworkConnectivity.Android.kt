package fr.imacaron.torri

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

actual fun isNetworkAvailable(): Boolean {
	val connectivityManager = activity.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
	connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.let { capabilities ->
		if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
			return true
		}
	}
	return false
}