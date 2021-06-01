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

    enum class ExerciseType(val readableName: String) {
        BIKING("Biking"),
        WALKING("Walking"),
        RUNNING("Running")
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