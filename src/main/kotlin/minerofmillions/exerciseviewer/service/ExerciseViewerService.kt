package minerofmillions.exerciseviewer.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import minerofmillions.exerciseviewer.entities.*
import minerofmillions.exerciseviewer.util.MutableTimeMap
import minerofmillions.exerciseviewer.util.mutableTimeMapOf
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.AbortableInputStream
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.util.*
import kotlin.math.*

@Service
class ExerciseViewerService {
    final val gson: Gson = GsonBuilder()
        .registerTypeAdapter(GeoJSON::class.java, GeoJSON.Serializer)
        .registerTypeAdapter(Geometry::class.java, Geometry.Serializer)
        .registerTypeAdapter(Position::class.java, Position.Serializer)
        .create()
    final val exerciseDataFile = File("data.json")
    private val exerciseData = mutableListOf<ExerciseData>()

    val exerciseDataByPerson
        get() = exerciseData.groupBy { it.person }

    final val individualRouteJSON: GeoJSON
    final val individualRouteToDistance: MutableTimeMap<Int, FeatureCollection> = mutableTimeMapOf()
    final val individualRouteDistance: Int
    private val individualResponse: Response = File("individualResponse.json").reader().use {
        gson.fromJson(it, Response::class.java)
    }
    final val individualMapOptions: Map<String, Any> = mapOf(
        "lat" to 32.04526885,
        "lng" to -80.1408972,
        "zoom" to 6,
        "style" to "http://{s}.tile.osm.org/{z}/{x}/{y}.png"
    )

    final val familyRouteJSON: GeoJSON
    final val familyRouteToDistance: MutableTimeMap<Int, FeatureCollection> = mutableTimeMapOf()
    final val familyRouteDistance: Int
    private val familyResponse: Response = File("totalResponse.json").reader().use {
        gson.fromJson(it, Response::class.java)
    }
    final val familyMapOptions: Map<String, Any>
        get() = getOptionsFromRoute(familyRouteToDistance[(getFamilyDistance() * 1609.34).roundToInt()]!!)

    final val testMapOptions: Map<String, Any>
        get() = getOptionsFromRoute(familyRouteJSON)

    private fun zoom(latitude: Double, altitude: Double) =
        log2(27.3611 * 3671010 * 768 * cos(PI * latitude / 180) / (altitude * 256))

    private fun getOptionsFromRoute(route: GeoJSON): Map<String, Any> {
        val bbox = route.bbox!!
        val avgLat = (bbox[0] + bbox[2]) / 2
        val avgLng = (bbox[1] + bbox[3]) / 2

        val dLat = bbox[2] - bbox[0]
        val dLng = bbox[3] - bbox[1]

        return mapOf(
            "lat" to avgLat,
            "lng" to avgLng,
            "zoom" to zoom(avgLat, 236262.09493654885 * dLng * 1.75).roundToInt(),
            "style" to "http://{s}.tile.osm.org/{z}/{x}/{y}.png"
        )
    }

    private val latestId: Int
        get() = exerciseData.maxOfOrNull { it.id } ?: 0

    private val s3 = S3Client.builder().region(Region.US_EAST_1).build()

    private val properties = Properties().apply {
        File(".env").reader().use { load(it) }
    }
    private val bucket = properties["BUCKETEER_BUCKET_NAME"] as String

    private val credentials = object : AwsCredentials {
        override fun accessKeyId(): String = properties["BUCKETEER_AWS_ACCESS_KEY_ID"] as String
        override fun secretAccessKey(): String = properties["BUCKETEER_AWS_SECRET_ACCESS_KEY"] as String
    }

    fun saveData() {
        saveDataToS3()
    }

    fun resetData() {
        exerciseData.clear()
        saveDataToS3()
    }

    private fun getDataFromS3() {
        try {
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key("exercisedata")
                .overrideConfiguration {
                    it.credentialsProvider { credentials }
                }
                .build()

            val responseTransformer: (GetObjectResponse, AbortableInputStream) -> List<ExerciseData> =
                { _, abortableInputStream ->
                    gson.fromJson(
                        abortableInputStream.reader(),
                        ExerciseDataListTypeToken.type
                    ) ?: emptyList()
                }

            val pastData = s3.getObject(getObjectRequest, responseTransformer)

            exerciseData.addAll(pastData)
        } catch (e: NoSuchKeyException) {
        }
    }

    private fun saveDataToS3() {
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key("exercisedata")
                .overrideConfiguration {
                    it.credentialsProvider { credentials }
                }
                .build(),
            RequestBody.fromString(gson.toJson(exerciseData))
        )
    }

    internal fun getFamilyDistance() = exerciseData.sumByDouble { it.weightedDistance }
    internal fun getDistanceOf(person: Person) =
        (exerciseDataByPerson[person] ?: emptyList()).sumByDouble { it.weightedDistance }

    init {
        individualRouteJSON = parseResponse(individualResponse, individualRouteToDistance)
        individualRouteDistance = individualResponse.routes[0].legs.sumBy { it.distance.value }

        familyRouteJSON = parseResponse(familyResponse, familyRouteToDistance)
        familyRouteDistance = familyResponse.routes[0].legs.sumBy { it.distance.value }

        getDataFromS3()
    }

    private fun parseResponse(response: Response, routeToDistance: MutableTimeMap<Int, FeatureCollection>): GeoJSON {
        val route = response.routes[0]

        val positions = route.legs.flatMap { leg ->
            leg.steps.flatMap { step ->
                getPositions(step)
            }
        }

        val startLocation = route.legs.first().start_location
        routeToDistance[0] = FeatureCollection(
            listOf(
                Feature(
                    null,
                    LineString(listOf(Position(startLocation)))
                )
            ),
            listOf(startLocation.lat, startLocation.lng, startLocation.lat, startLocation.lng)
        )
        val fullRoute = mutableListOf<Position>()
        var fullDistance = 0
        positions.forEach { (pos, distance) ->
            fullDistance += distance
            fullRoute += pos
            val minLat = fullRoute.minOf { it.lat }
            val minLng = fullRoute.minOf { it.lng }
            val maxLat = fullRoute.maxOf { it.lat }
            val maxLng = fullRoute.maxOf { it.lng }
            routeToDistance[fullDistance] = FeatureCollection(
                listOf(
                    Feature(
                        null,
                        LineString(fullRoute.toList())
                    )
                ),
                listOf(minLat, minLng, maxLat, maxLng)
            )
        }

        val bounds = route.bounds

        return FeatureCollection(
            listOf(
                Feature(
                    null,
                    LineString(fullRoute)
                )
            ),
            listOf(bounds.southwest.lat, bounds.southwest.lng, bounds.northeast.lat, bounds.northeast.lng)
        )
    }

    private object ExerciseDataListTypeToken : TypeToken<List<ExerciseData>>()

    private fun getPositions(step: RouteStep): List<Pair<Position, Int>> {
        val dLat = step.end_location.lat - step.start_location.lat
        val dLng = step.end_location.lng - step.start_location.lng
        val numSteps = ceil(step.distance.value / 3218.69).toInt()
        return (1..numSteps).map {
            Position(
                step.start_location.lat + (it * dLat / numSteps),
                step.start_location.lng + (it * dLng / numSteps)
            ) to (step.distance.value.toDouble() / numSteps).roundToInt()
        }
    }

    fun add(data: ExerciseData) {
        data.id = latestId + 1
        exerciseData.add(data)
        saveData()
    }

    fun delete(id: Int) {
        exerciseData.removeIf { it.id == id }
        saveData()
    }
}
