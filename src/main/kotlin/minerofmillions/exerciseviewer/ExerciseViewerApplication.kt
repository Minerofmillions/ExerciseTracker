package minerofmillions.exerciseviewer

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import minerofmillions.exerciseviewer.util.MutableTimeMap
import minerofmillions.exerciseviewer.util.mutableTimeMapOf
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File
import kotlin.math.ceil

@SpringBootApplication
class ExerciseViewerApplication

fun main(args: Array<String>) {
    runApplication<ExerciseViewerApplication>(*args)
}

