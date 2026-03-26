package fr.imacaron.torri

actual fun Double.formatPrice(): String {
	return String.format("%.2f", this)
}

actual val os: String = "Android"