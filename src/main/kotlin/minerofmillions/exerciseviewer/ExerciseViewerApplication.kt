package minerofmillions.exerciseviewer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import minerofmillions.exerciseviewer.database.ExerciseDataDB
import minerofmillions.exerciseviewer.entities.GeoJSON
import minerofmillions.exerciseviewer.entities.Geometry
import minerofmillions.exerciseviewer.entities.Person
import minerofmillions.exerciseviewer.entities.Position
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

internal val gson: Gson = GsonBuilder()
    .registerTypeAdapter(GeoJSON::class.java, GeoJSON.Serializer)
    .registerTypeAdapter(Geometry::class.java, Geometry.Serializer)
    .registerTypeAdapter(Position::class.java, Position.Serializer)
    .create()

internal const val METERS_PER_MILE = 1609.344

internal val peopleInOrderOfDistance
    get() = Person.values().sortedByDescending(ExerciseDataDB::getIndividualDistanceMiles)

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")

internal val trackStartDate = LocalDate.of(2021, 6, 1)

fun healthImport() {
    val healthInfo = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("apple_health_export/export.xml")
    val workouts = healthInfo.getElementsByTagName("Workout")
    (0 until workouts.length).map { workouts.item(it) }.forEach { workout ->
        val startDate = LocalDate.parse(workout.attributes.getNamedItem("startDate").nodeValue, dateTimeFormatter)
        if (startDate >= trackStartDate) {
            println("$startDate - ${workout.attributes.getNamedItem("workoutActivityType").nodeValue}:")
            println("\t${workout.attributes.getNamedItem("totalDistance").nodeValue} ${workout.attributes.getNamedItem("totalDistanceUnit").nodeValue}")
            println("\t${workout.attributes.getNamedItem("duration").nodeValue} ${workout.attributes.getNamedItem("durationUnit").nodeValue}")
        }
    }
}

@SpringBootApplication
class ExerciseViewerApplication

fun main(args: Array<String>) {
    healthImport()
    runApplication<ExerciseViewerApplication>(*args)
}
