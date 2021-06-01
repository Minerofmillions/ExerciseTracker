package minerofmillions.exerciseviewer.service

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import minerofmillions.exerciseviewer.entities.*
import minerofmillions.exerciseviewer.util.MutableTimeMap
import minerofmillions.exerciseviewer.util.mutableTimeMapOf
import org.springframework.stereotype.Service
import java.io.File
import kotlin.math.ceil

@Service
class ExerciseViewerService {
    final val gson = GsonBuilder()
        .registerTypeAdapter(GeoJSON::class.java, GeoJSON.Serializer)
        .registerTypeAdapter(Geometry::class.java, Geometry.Serializer)
        .registerTypeAdapter(Position::class.java, Position.Serializer)
        .setPrettyPrinting()
        .create()
    final val exerciseDataFile = File("data.json")
    final val exerciseData = mutableListOf<ExerciseData>()

    val exerciseDataByPerson
        get() = exerciseData.groupBy { it.person }

    val routeJSON: GeoJSON
    val routeToDistance: MutableTimeMap<Int, GeoJSON> = mutableTimeMapOf()

    internal fun getTotalDistance() = exerciseData.sumByDouble { it.weightedDistance }
    internal fun getDistanceOf(person: Person) =
        (exerciseDataByPerson[person] ?: emptyList()).sumByDouble { it.weightedDistance }

    init {
        val response = File("response.json").reader().use {
            gson.fromJson(it, Response::class.java)
        }

        val route = response.routes[0]

        val positions = route.legs.flatMap { leg ->
            leg.steps.flatMap { step ->
                getPositions(step)
            }
        }

        routeToDistance[0] = FeatureCollection(
            listOf(
                Feature(
                    null,
                    LineString(listOf(Position(route.legs.first().start_location)))
                )
            )
        )
        val fullRoute = mutableListOf<Position>()
        var fullDistance = 0
        positions.forEach { (pos, distance) ->
            fullDistance += distance
            fullRoute += pos
            routeToDistance[fullDistance] = FeatureCollection(
                listOf(
                    Feature(
                        null,
                        LineString(fullRoute.toList())
                    )
                )
            )
        }

        routeJSON = FeatureCollection(
            listOf(
                Feature(
                    null,
                    LineString(fullRoute)
                )
            )
        )

        if (!exerciseDataFile.exists()) {
            exerciseDataFile.createNewFile()
            exerciseDataFile.writeText("[]")
        }
        exerciseData.addAll(
            gson.fromJson(
                exerciseDataFile.reader(),
                ExerciseDataListTypeToken.type
            ) ?: emptyList()
        )
    }

    private object ExerciseDataListTypeToken : TypeToken<List<ExerciseData>>()

    private fun getFeature(route: Route) = Feature(
        null,
        LineString(mutableListOf(Position(route.legs[0].start_location)).apply {
            addAll(route.legs.flatMap { leg ->
                leg.steps.flatMap {
                    val positions = getPositions(it)
                    positions.map { pos -> pos.first }
                }
            })
        })
    )

    private fun getPositions(step: RouteStep): List<Pair<Position, Int>> {
        val dLat = step.end_location.lat - step.start_location.lat
        val dLng = step.end_location.lng - step.start_location.lng
        val numSteps = ceil(step.distance.value / 3218.69).toInt()
        return (0 until numSteps).map {
            Position(
                step.start_location.lat + (it + 1) * dLat / numSteps,
                step.start_location.lng + (it + 1) * dLng / numSteps
            ) to step.distance.value
        }
    }
}


