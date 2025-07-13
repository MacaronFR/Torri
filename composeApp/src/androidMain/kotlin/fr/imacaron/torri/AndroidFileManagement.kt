package fr.imacaron.torri

lateinit var activity: MainActivity

actual fun saveToFile(file: String, text: String) {
	activity.saveToFile(file, text)
}