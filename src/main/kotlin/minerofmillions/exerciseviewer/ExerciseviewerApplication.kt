package minerofmillions.exerciseviewer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@SpringBootApplication
@Controller
class ExerciseviewerApplication {
	@RequestMapping("/view")
	fun viewData(@RequestParam(name = "name", required = true) name: String, model: Model): String {
		model.addAttribute("name", name)
		return "view"
	}
}

fun main(args: Array<String>) {
	runApplication<ExerciseviewerApplication>(*args)
}
