package minerofmillions.exerciseviewer.entities

import java.awt.Color
import kotlin.math.max


data class ExerciseData(
    val person: Person,
    var type: ExerciseType = ExerciseType.BIKING,
    var date: String = "",
    var distance: Double = 0.0,
    var duration: Int = 0
) {
    val weightedDistance: Double
        get() = max(distance, duration / 5.0)

    var id = -1
    val formattedDate
        get() = "${date.substring(5, 7)}/${date.substring(8, 10)}/${date.substring(0, 4)}"

    val formattedTime
        get() = "%2d:%02d".format(duration / 60, duration % 60)

    enum class ExerciseType(val readableName: String) {
        BIKING("Biking"),
        WALKING("Walking"),
        RUNNING("Running"),
        CARDIO("Cardio"),
        WEIGHTLIFTING("Weightlifting"),
    }
}

enum class Person(val realName: String, val color: Color) {
    TONY("Tony", Color(0x3F3F8B)),
    LAVERNE("Laverne", Color(0xFFC0CB)),
    VICKIE("Vickie", Color(0x00FA9A)),
    PAM("Pam", Color(0x7851A9)),
    HAYDEN("Hayden", Color(0xFF7F50)),
    KEVIN("Kevin", Color(0xDC143C)),
    JEN("Jen", Color(0x9932CC)),
    RANDY("Randy", Color(0x7C7C7C)),
    AIMEE("Aimee", Color(0x008000)),
    CINDY("Cindy", Color(0xFFDF00)),
    MARK("Mark", Color(0x4B0082)),
    JASON("Jason", Color(0x00FF00)),
    ALEX("Alex", Color(0x800020));

    val colorAsHex = "#%02X%02X%02X".format(color.red, color.green, color.blue)
}

data class PersonData(
    val person: Person,
    val distance: Double,
    val percentage: Double,
    val cssClass: String
)
