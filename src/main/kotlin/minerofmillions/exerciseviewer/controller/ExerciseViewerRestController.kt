package minerofmillions.exerciseviewer.controller

import minerofmillions.exerciseviewer.entities.GeoJSON
import minerofmillions.exerciseviewer.entities.Person
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
    fun getIndividualRouteData(): GeoJSON = service.routeJSON

    @RequestMapping("/data/total/route")
    @Cacheable("totalRoute")
    fun getTotalRouteData(): GeoJSON = service.routeJSON

    @RequestMapping("/data/individual/progress")
    fun getIndividualProgress(@CookieValue name: String): GeoJSON? =
        service.routeToDistance[(service.getDistanceOf(Person.valueOf(name)) * 1609.34).roundToInt()]

    @RequestMapping("/data/total/progress")
    fun getTotalProgress(): GeoJSON? =
        service.routeToDistance[(service.getTotalDistance() * 1609.34).roundToInt()]

}