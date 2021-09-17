package minerofmillions.exerciseviewer.database

import minerofmillions.exerciseviewer.METERS_PER_MILE
import minerofmillions.exerciseviewer.entities.*
import minerofmillions.exerciseviewer.gson
import minerofmillions.exerciseviewer.peopleInOrderOfDistance
import minerofmillions.exerciseviewer.util.TimeMap
import minerofmillions.exerciseviewer.util.toTimeMap
import java.io.File
import kotlin.math.*

object RouteResponseDB {
    val totalIndividualRoute: FeatureCollection
    private val individualRouteToDistance: TimeMap<Int, FeatureCollection>
    val individualRouteDistance: Int
    val individualMapOptions = mapOf(
        "lat" to 32.04526885,
        "lng" to -80.1408972,
        "zoom" to 6,
        "style" to "https://{s}.basemaps.cartocdn.com/rastertiles/light_all/{z}/{x}/{y}.png"
    )

    val totalFamilyRoute: FeatureCollection
    private val familyRouteToDistance: TimeMap<Int, FeatureCollection>
    val familyRouteDistance: Int
    val familyMapOptions
        get() = getOptionsFromRoute(getFamilyProgress())

    fun getIndividualProgress(person: Person) =
        individualRouteToDistance[ExerciseDataDB.getIndividualDistanceMeters(person)]!!

    fun getFamilyProgress() = familyRouteToDistance[ExerciseDataDB.getFamilyDistanceMeters()]!!

    fun getDistanceProgresses() = peopleInOrderOfDistance.drop(1)
        .runningFold(peopleInOrderOfDistance[0] to ExerciseDataDB.getFamilyDistanceMeters()) { (oldPerson, dist), person ->
            person to dist - ExerciseDataDB.getIndividualDistanceMeters(oldPerson)
        }.map { it.first.color to familyRouteToDistance[it.second] }

    init {
        val individualResponse = File("individualResponse.json").reader().use {
            gson.fromJson(it, Response::class.java)
        }
        totalIndividualRoute = getTotalRouteFromResponse(individualResponse)
        individualRouteToDistance = getRoutesToDistancesFromResponse(individualResponse)
        this.individualRouteDistance = getFullRouteDistanceFromResponse(individualResponse)

        val familyResponse = File("familyResponse.json").reader().use {
            gson.fromJson(it, Response::class.java)
        }
        totalFamilyRoute = getTotalRouteFromResponse(familyResponse)
        familyRouteToDistance = getRoutesToDistancesFromResponse(familyResponse)
        this.familyRouteDistance = getFullRouteDistanceFromResponse(familyResponse)
    }

    private fun getTotalRouteFromResponse(response: Response): FeatureCollection {
        val fullRoute = getFullRouteFromResponse(response)

        val bbox = getBBox(fullRoute.values)
        val lineString = lineStringOf(fullRoute.values, bbox = bbox)
        return featureCollectionOf(Feature(null, lineString, bbox = bbox), bbox = bbox)
    }

    private fun getRoutesToDistancesFromResponse(response: Response): TimeMap<Int, FeatureCollection> {
        val fullRoute = getFullRouteFromResponse(response)

        return fullRoute.keys.associateWith { distance ->
            val points = fullRoute.entries.filter { it.key in 0..distance }.map { it.value }
            val bbox = getBBox(points)
            val lineString = lineStringOf(points, bbox = bbox)
            featureCollectionOf(
                Feature(null, lineString, bbox = bbox),
                bbox = bbox
            )
        }.toTimeMap()
    }

    private fun getFullRouteFromResponse(response: Response): Map<Int, Position> {
        val route = response.routes.first()

        val positions = route.legs.flatMap { leg ->
            leg.steps.flatMap { step ->
                getPositions(step)
            }
        }

        val startLocation = Position(route.legs.first().start_location)
        var fullDistance = 0
        val fullRoute = mutableMapOf(0 to startLocation)
        positions.forEach { (pos, distance) ->
            fullDistance += distance
            fullRoute[fullDistance] = pos
        }
        return fullRoute
    }

    private fun getFullRouteDistanceFromResponse(response: Response): Int =
        response.routes.first().legs.sumOf { it.distance.value }

    private fun getBBox(points: Collection<Position>): List<Double>? {
        val minLat = points.minOfOrNull { it.lat }
        val minLng = points.minOfOrNull { it.lng }
        val maxLat = points.maxOfOrNull { it.lat }
        val maxLng = points.maxOfOrNull { it.lng }
        if (minLat == null || minLng == null || maxLat == null || maxLng == null) return null
        return listOf(minLat, minLng, maxLat, maxLng)
    }

    private fun getPositions(step: RouteStep): List<Pair<Position, Int>> {
        val dLat = step.end_location.lat - step.start_location.lat
        val dLng = step.end_location.lng - step.start_location.lng
        val numSteps = ceil(step.distance.value / (2 * METERS_PER_MILE)).toInt()
        return (1..numSteps).map {
            Position(
                step.start_location.lat + (it * dLat / numSteps),
                step.start_location.lng + (it * dLng / numSteps)
            ) to (step.distance.value.toDouble() / numSteps).roundToInt()
        }
    }

    private fun getOptionsFromRoute(route: GeoJSON): Map<String, Any> {
        val bbox = route.bbox!!
        val centerLat = (bbox[0] + bbox[2]) / 2
        val centerLng = (bbox[1] + bbox[3]) / 2

        val dLat = bbox[2] - bbox[0]
        val dLng = bbox[3] - bbox[1]

        //  lngShown = 360 / 2.0.pow(zoomLevel)
        val zoomLevel = log2(360 / max(dLat, dLng)).coerceAtMost(15.0)

        return mapOf(
            "lat" to centerLat,
            "lng" to centerLng,
            "zoom" to zoomLevel.roundToInt(),
            "style" to "https://{s}.basemaps.cartocdn.com/rastertiles/light_all/{z}/{x}/{y}.png"
        )
    }

    private fun zoom(latitude: Double, altitude: Double) =
        log2(27.3611 * 3671010 * 768 * cos(PI * latitude / 180) / (altitude * 256))
}
