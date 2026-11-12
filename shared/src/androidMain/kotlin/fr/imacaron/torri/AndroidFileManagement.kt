package fr.imacaron.torri

lateinit var main: Main

actual fun saveToFile(file: String, text: String) {
	main.saveToFile(file, text)
}