package minerofmillions.exerciseviewer.controller

import minerofmillions.exerciseviewer.entities.ExerciseData
import minerofmillions.exerciseviewer.entities.Person
import minerofmillions.exerciseviewer.service.ExerciseViewerService
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
        model.addAttribute("data", service.exerciseDataByPerson[person] ?: emptyList<ExerciseData>())
        model.addAttribute("totalIndividualDistance", service.getDistanceOf(person))
        model.addAttribute("totalFamilyDistance", service.getTotalDistance())
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
        service.exerciseData.add(data)
        service.exerciseDataFile.writer().use {
            service.gson.toJson(
                service.exerciseData, it
            )
        }
        return RedirectView("/view")
    }
}