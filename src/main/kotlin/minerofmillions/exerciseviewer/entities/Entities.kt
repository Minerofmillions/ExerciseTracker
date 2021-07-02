package minerofmillions.exerciseviewer.entities

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

enum class Person(val realName: String) {
    TONY("Tony"),
    LAVERNE("Laverne"),
    VICKIE("Vickie"),
    PAM("Pam"),
    HAYDEN("Hayden"),
    KEVIN("Kevin"),
    JEN("Jen"),
    RANDY("Randy"),
    AIMEE("Aimee"),
    CINDY("Cindy"),
    MARK("Mark"),
    JASON("Jason"),
    ALEX("Alex")
}

data class PersonData(
    val name: String,
    val distance: Double,
    val percentage: Double,
    val cssClass: String
)
