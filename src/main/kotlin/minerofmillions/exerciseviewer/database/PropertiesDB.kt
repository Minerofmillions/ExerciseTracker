package minerofmillions.exerciseviewer.database

import java.io.File
import java.util.*

object PropertiesDB {
    private val properties by lazy {
        Properties().apply {
            File(".env").reader().use { load(it) }
        }
    }

    fun getProperty(name: String): String? = System.getenv(name) ?: properties.getProperty(name)
}
