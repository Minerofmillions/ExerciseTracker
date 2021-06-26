package minerofmillions.exerciseviewer.controller

import minerofmillions.exerciseviewer.entities.GeoJSON
import minerofmillions.exerciseviewer.entities.Person
import minerofmillions.exerciseviewer.entities.emptyFeatureCollection
import minerofmillions.exerciseviewer.service.ExerciseViewerService
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.math.roundToInt

@EnableCaching
@RestController
class ExerciseViewerRestController(val service: ExerciseViewerService) {

    @RequestMapping("/data/individual/route")
    @Cacheable("individualRoute")
    fun getIndividualRouteData(): GeoJSON = service.individualRouteJSON

    @RequestMapping("/data/family/route")
    @Cacheable("totalRoute")
    fun getFamilyRouteData(): GeoJSON = emptyFeatureCollection()

    @RequestMapping("/data/test/route")
    @Cacheable("testRoute")
    fun getTestRouteData(): GeoJSON = service.familyRouteJSON

    @RequestMapping("/data/individual/progress")
    fun getIndividualProgress(@CookieValue name: String): GeoJSON? =
        service.individualRouteToDistance[(service.getDistanceOf(Person.valueOf(name)) * 1609.34).roundToInt()]

    @RequestMapping("/data/family/progress")
    fun getTotalProgress(): GeoJSON? =
        service.familyRouteToDistance[(service.getFamilyDistance() * 1609.34).roundToInt()]

    @RequestMapping("/data/test/progress")
    fun getTestProgress(): GeoJSON? =
        service.familyRouteToDistance[(service.getFamilyDistance() * 1609.34).roundToInt()]

    @RequestMapping("/data/individual/options")
    fun getIndividualOptions() = service.individualMapOptions

    @RequestMapping("/data/family/options")
    fun getFamilyOptions() = service.familyMapOptions

    @RequestMapping("/data/test/options")
    fun getTestOptions() = service.testMapOptions
}
