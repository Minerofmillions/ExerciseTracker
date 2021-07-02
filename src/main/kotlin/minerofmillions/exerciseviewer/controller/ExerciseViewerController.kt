package minerofmillions.exerciseviewer.controller

import minerofmillions.exerciseviewer.entities.ExerciseData
import minerofmillions.exerciseviewer.entities.Person
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
            service.exerciseDataByPerson[person]?.sortedByDescending { it.date } ?: emptyList<ExerciseData>())
        model.addAttribute("totalIndividualDistance", service.getDistanceOf(person))
        model.addAttribute("totalFamilyDistance", service.getFamilyDistance())
        model.addAttribute("individualRouteDistanceMeters", service.individualRouteDistance)
        model.addAttribute("individualRouteDistanceMiles", round(service.individualRouteDistance / 1609.34, 2))
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
        service.add(data)
        return RedirectView("/view")
    }

    @PostMapping("/reset")
    fun resetData(
        @RequestParam(name = "resetPassword") resetPassword: String
    ): RedirectView {
        if (resetPassword == "reset") service.resetData()
        return RedirectView("/")
    }

    @GetMapping("/delete/{id}")
    fun deleteData(@PathVariable id: Int): RedirectView {
        service.delete(id)
        return RedirectView("/view")
    }

    @GetMapping("/scoreboard")
    fun scoreboard(@CookieValue(name = "name", defaultValue = "") name: String, model: Model): String {
        model.addAttribute(
            "peopleStats",
            Person.values().map { service.getInformationOf(it, name) }.sortedByDescending { it.distance })
        model.addAttribute("personSelected", name != "")
        return "scoreboard"
    }
}
