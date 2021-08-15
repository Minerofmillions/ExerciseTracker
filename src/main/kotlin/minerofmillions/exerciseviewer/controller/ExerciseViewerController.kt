package minerofmillions.exerciseviewer.controller

import minerofmillions.exerciseviewer.METERS_PER_MILE
import minerofmillions.exerciseviewer.database.ExerciseDataDB
import minerofmillions.exerciseviewer.database.RouteResponseDB
import minerofmillions.exerciseviewer.entities.ExerciseData
import minerofmillions.exerciseviewer.entities.Person
import minerofmillions.exerciseviewer.peopleInOrderOfDistance
import minerofmillions.exerciseviewer.service.ExerciseViewerService
import minerofmillions.exerciseviewer.util.round
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Controller
class ExerciseViewerController(val service: ExerciseViewerService) {
    @GetMapping("nameChosen")
    fun nameChosen(
        @RequestParam(name = "name", required = true) name: String,
        response: HttpServletResponse
    ): RedirectView {
        response.addCookie(Cookie("name", name))
        return RedirectView("/view")
    }

    @PostMapping("/view")
    fun launchData(
        @RequestParam(name = "name", required = true) name: String,
        response: HttpServletResponse
    ): RedirectView {
        response.addCookie(Cookie("name", name))
        return RedirectView("/view")
    }

    @GetMapping("/view")
    fun viewData(@CookieValue(name = "name") name: String, model: Model): String {
        val person = Person.valueOf(name)
        model.addAttribute("name", person.realName)
        model.addAttribute(
            "data",
            ExerciseDataDB.getExerciseDataOf(person).sortedByDescending { it.date })
        model.addAttribute("totalIndividualDistance", ExerciseDataDB.getIndividualDistanceMiles(person))
        model.addAttribute("totalFamilyDistance", ExerciseDataDB.getFamilyDistanceMiles())
        model.addAttribute("individualRouteDistanceMeters", RouteResponseDB.individualRouteDistance)
        model.addAttribute("individualRouteDistanceMiles", round(RouteResponseDB.individualRouteDistance / METERS_PER_MILE, 2))
        model.addAttribute("person", name)
        model.addAttribute("exerciseData", ExerciseData(person))
        model.addAttribute("exerciseTypes", ExerciseData.ExerciseType.values())
        return "view"
    }

    @PostMapping("/add")
    fun submitData(
        @ModelAttribute(name = "exerciseData") data: ExerciseData,
        @RequestParam(name = "durationHour", defaultValue = "0") durationHour: Int,
        @RequestParam(name = "durationMinute", defaultValue = "0") durationMin: Int
    ): RedirectView {
        data.duration = durationHour * 60 + durationMin
        ExerciseDataDB.save(data)
        return RedirectView("/view")
    }

    @PostMapping("/reset")
    fun resetData(
        @RequestParam(name = "resetPassword") resetPassword: String
    ): RedirectView {
        if (resetPassword == "reset") ExerciseDataDB.deleteAll()
        return RedirectView("/")
    }

    @GetMapping("/delete/{id}")
    fun deleteData(@PathVariable id: Int): RedirectView {
        ExerciseDataDB.deleteById(id)
        return RedirectView("/view")
    }

    @GetMapping("/scoreboard")
    fun scoreboard(@CookieValue(name = "name", defaultValue = "") name: String, model: Model): String {
        model.addAttribute(
            "peopleStats",
            peopleInOrderOfDistance.map { service.getInformationOf(it, name) })
        model.addAttribute("personSelected", name != "")
        return "scoreboard"
    }
}
