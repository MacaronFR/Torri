package fr.imacaron

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        activity = this
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        fun createDataStore() = createDataStore { applicationContext.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
        setContent {
            App(createDataStore())
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
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("requestCode: $requestCode, resultCode: $resultCode")
        if(resultCode == RESULT_OK && requestCode == 1) {
            if(data != null) {
                val uri = data.data!!
                val stream = contentResolver.openOutputStream(uri)!!
                stream.write(text.toByteArray())
                stream.close()
            }
        }
    }
}