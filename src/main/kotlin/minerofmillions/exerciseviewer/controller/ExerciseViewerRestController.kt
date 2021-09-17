package minerofmillions.exerciseviewer.controller

import minerofmillions.exerciseviewer.database.RouteResponseDB
import minerofmillions.exerciseviewer.entities.GeoJSON
import minerofmillions.exerciseviewer.entities.Person
import minerofmillions.exerciseviewer.entities.emptyFeatureCollection
import minerofmillions.exerciseviewer.service.ExerciseViewerService
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@EnableCaching
@RestController
class ExerciseViewerRestController(val service: ExerciseViewerService) {

    @RequestMapping("/data/individual/route")
    @Cacheable("individualRoute")
    fun getIndividualRouteData(): GeoJSON = RouteResponseDB.totalIndividualRoute

    @RequestMapping("/data/family/route")
    @Cacheable("totalRoute")
    fun getFamilyRouteData(): GeoJSON = emptyFeatureCollection()

    @RequestMapping("/data/test/route")
    @Cacheable("testRoute")
    fun getTestRouteData(): GeoJSON = RouteResponseDB.totalFamilyRoute

    @RequestMapping("/data/distance/route")
    @Cacheable("distanceRoute")
    fun getDistanceRouteData(): GeoJSON = emptyFeatureCollection()

    @RequestMapping("/data/individual/progress")
    fun getIndividualProgress(@CookieValue name: String): GeoJSON? =
        RouteResponseDB.getIndividualProgress(Person.valueOf(name))

    @RequestMapping("/data/family/progress")
    fun getFamilyProgress(): GeoJSON? = RouteResponseDB.getFamilyProgress()

    @RequestMapping("/data/test/progress")
    fun getTestProgress(): GeoJSON? = RouteResponseDB.getFamilyProgress()

    @RequestMapping("/data/distance/progress")
    fun getDistanceProgresses() = RouteResponseDB.getDistanceProgresses()

    @RequestMapping("/data/individual/options")
    fun getIndividualOptions() = RouteResponseDB.individualMapOptions

    @RequestMapping("/data/family/options")
    fun getFamilyOptions() = RouteResponseDB.familyMapOptions

    @RequestMapping("/data/test/options")
    fun getTestOptions() = RouteResponseDB.familyMapOptions

    @RequestMapping("/data/distance/options")
    fun getDistanceOptions() = RouteResponseDB.familyMapOptions
}
