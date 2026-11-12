package fr.imacaron.torri

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity: ComponentActivity() {
	lateinit var main: Main
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		main = Main(this)
		main.onCreate(savedInstanceState)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		main.onActivityResult(requestCode, resultCode, data)
	}
}