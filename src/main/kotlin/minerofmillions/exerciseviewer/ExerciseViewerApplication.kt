package minerofmillions.exerciseviewer

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import minerofmillions.util.MutableTimeMap
import minerofmillions.util.mutableTimeMapOf
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import kotlin.math.ceil
import kotlin.math.roundToInt

@SpringBootApplication
@Controller
class ExerciseViewerApplication {
    @PostMapping("/view")
    fun launchData(
        @RequestParam(name = "name", required = true) name: String,
        model: Model,
        response: HttpServletResponse
    ): String {
        response.addCookie(Cookie("name", name))
        val person = Person.valueOf(name)
        model.addAttribute("name", person.realName)
        model.addAttribute("data", exerciseDataByPerson[person] ?: emptyList<ExerciseData>())
        return "view"
    }

    @GetMapping("/view")
    fun viewData(@CookieValue(name = "name") name: String, model: Model): String {
        val person = Person.valueOf(name)
        model.addAttribute("name", person.realName)
        model.addAttribute("data", exerciseDataByPerson[person] ?: emptyList<ExerciseData>())
        return "view"
    }

    @GetMapping("/add")
    fun addData(@CookieValue(name = "name") name: String, model: Model): String {
        val person = Person.valueOf(name)
        model.addAttribute("name", person.realName)
        model.addAttribute("person", name)
        model.addAttribute("exerciseData", ExerciseData(person))
        model.addAttribute("exerciseTypes", ExerciseData.ExerciseType.values())
        return "add_data"
    }

    @PostMapping("/add")
    fun submitData(
        @ModelAttribute(name = "exerciseData") data: ExerciseData,
        @RequestParam(name = "durationHour") durationHour: Int,
        @RequestParam(name = "durationMinute") durationMin: Int
    ): RedirectView {
        data.duration = durationHour * 60 + durationMin
        exerciseData.add(data)
        exerciseDataFile.writer().use { gson.toJson(exerciseData, it) }
        return RedirectView("/view")
    }

    @RestController
    @EnableCaching
    class ExerciseViewerRestController {
        @RequestMapping("/data/individual/route")
        @Cacheable("individualRoute")
        fun getIndividualRouteData(): GeoJSON = routeJSON

        @RequestMapping("/data/total/route")
        @Cacheable("totalRoute")
        fun getTotalRouteData(): GeoJSON = routeJSON

        @RequestMapping("/data/individual/progress")
        fun getIndividualProgress(@CookieValue name: String): GeoJSON? =
            routeToDistance[(getDistanceOf(Person.valueOf(name)) * 1609.34).roundToInt()]

        @RequestMapping("/data/total/progress")
        fun getTotalProgress(): GeoJSON? = routeToDistance[(getTotalDistance() * 1609.34).roundToInt()]
    }

    companion object {
        private val gson = GsonBuilder()
            .registerTypeAdapter(GeoJSON::class.java, GeoJSON.Serializer)
            .registerTypeAdapter(Geometry::class.java, Geometry.Serializer)
            .registerTypeAdapter(Position::class.java, Position.Serializer)
            .setPrettyPrinting()
            .create()
        private val exerciseDataFile = File("data.json")
        private val exerciseData = mutableListOf<ExerciseData>()

        private val exerciseDataByPerson
            get() = exerciseData.groupBy { it.person }

        private val routeJSON: GeoJSON
        private val routeToDistance: MutableTimeMap<Int, GeoJSON> = mutableTimeMapOf()

        internal fun getTotalDistance() = exerciseData.sumByDouble { it.distance }
        internal fun getDistanceOf(person: Person) =
            (exerciseDataByPerson[person] ?: emptyList()).sumByDouble { it.distance }

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
    }

    private object ExerciseDataListTypeToken : TypeToken<List<ExerciseData>>()
}

fun main(args: Array<String>) {
    runApplication<ExerciseViewerApplication>(*args)
}

fun downloadRoute() {
    val apiKey = "AIzaSyBjGjOEvEB_i1qTbXbgdCBvfGumlIto58k"
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder(
        URI(
            "https://maps.googleapis.com/maps/api/directions/json?" +
                    "key=$apiKey&" +
                    "origin=5524+Wedgegate+Dr+27616&" +
                    "destination=146+Hollingshead+Loop+33896&" +
                    "mode=bicycling&" +
                    "waypoints=613+Trader+Mill+Rd+29223%7C116+Kennington+Dr+29445%7C1563+Castlewick+Ave+29455%7C515+Stonecreek+Dr+29414"
        )
    )
        .GET()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
    File("response.json").writer().use { writer ->
        response.body().reader().use { reader ->
            writer.write(reader.readText())
        }
    }
}

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
