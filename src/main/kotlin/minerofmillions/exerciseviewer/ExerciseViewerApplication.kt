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

internal val gson: Gson = GsonBuilder()
    .registerTypeAdapter(GeoJSON::class.java, GeoJSON.Serializer)
    .registerTypeAdapter(Geometry::class.java, Geometry.Serializer)
    .registerTypeAdapter(Position::class.java, Position.Serializer)
    .create()

internal const val METERS_PER_MILE = 1609.344

internal val peopleInOrderOfDistance
    get() = Person.values().sortedByDescending(ExerciseDataDB::getIndividualDistanceMiles)

@SpringBootApplication
class ExerciseViewerApplication

fun main(args: Array<String>) {
    runApplication<ExerciseViewerApplication>(*args)
}
