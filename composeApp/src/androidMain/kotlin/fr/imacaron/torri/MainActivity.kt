package fr.imacaron.torri

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import fr.imacaron.torri.data.getRoomDataBase

class MainActivity : ComponentActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        activity = this
        enableEdgeToEdge()
        if(resources.getBoolean(R.bool.force_portrait)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        super.onCreate(savedInstanceState)
        fun createDataStore() = createDataStore { applicationContext.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
        setContent {
            App(getRoomDataBase(getDatabaseBuilder(this)))
        }
    }

    private var text = ""

    fun saveToFile(file: String, text: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, file)
            this@MainActivity.text = text
        }
        resultLauncher.launch(intent)
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            if(result.data != null) {
                var uri = result.data!!.data!!
                val stream = contentResolver.openOutputStream(uri)!!
                stream.write(text.toByteArray())
                stream.close()
            }
        }
    }
}