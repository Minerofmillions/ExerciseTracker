package minerofmillions.exerciseviewer.entities

import minerofmillions.exerciseviewer.database.ExerciseDataDB
import java.awt.Color
import kotlin.math.max


data class ExerciseData(
    val person: Person,
    var type: ExerciseType = person.latestExerciseType,
    var date: String = "",
    var distance: Double = 0.0,
    var duration: Int = 0
) {
    val weightedDistance: Double
        get() = type.getWeightedDistance(distance, duration)

    var id = -1
    val formattedDate
        get() = "${date.substring(5, 7)}/${date.substring(8, 10)}/${date.substring(0, 4)}"

    val formattedDuration
        get() = "%2d:%02d".format(duration / 60, duration % 60)

    enum class ExerciseType(
        val readableName: String,
        val getWeightedDistance: (distance: Double, duration: Int) -> Double
    ) {
        BIKING("Biking", { distance, _ -> distance }),
        WALKING("Walking", { distance, duration -> max(distance, duration / 5.0) }),
        RUNNING("Running", { distance, duration -> max(distance, duration / 5.0) }),
        CARDIO("Cardio", { distance, duration -> max(distance, duration / 5.0) }),
        WEIGHTLIFTING("Weightlifting", { distance, duration -> max(distance, duration / 5.0) })
    }
}

enum class Person(
    val realName: String,
    val color: Color,
    defaultExerciseType: ExerciseData.ExerciseType = ExerciseData.ExerciseType.BIKING
) {
    TONY("Tony", Color(0x746FC1), ExerciseData.ExerciseType.WALKING),
    LAVERNE("Laverne", Color(0xFFC0CB), ExerciseData.ExerciseType.WALKING),
    VICKIE("Vickie", Color(0x00FA9A), ExerciseData.ExerciseType.WALKING),
    PAM("Pam", Color(0x7851A9), ExerciseData.ExerciseType.WALKING),
    HAYDEN("Hayden", Color(0xFF7F50), ExerciseData.ExerciseType.WALKING),
    KEVIN("Kevin", Color(0xDC143C)),
    JEN("Jen", Color(0xFF00FF)),
    RANDY("Randy", Color(0x7C7C7C)),
    AIMEE("Aimee", Color(0x6ACE57)),
    CINDY("Cindy", Color(0xFFDF00)),
    MARK("Mark", Color(0x9E57D5)),
    JASON("Jason", Color(0x00FF00)),
    ALEX("Alex", Color(0xc54e54)),
    ETHAN("Ethan", Color(0x00FFFF)),
    CARTER("Carter", Color(0x007CFF));

    val colorAsHex = "#%02X%02X%02X".format(color.red, color.green, color.blue)
    val latestExerciseType = ExerciseDataDB.getExerciseDataOf(this).maxByOrNull { it.id }?.type ?: defaultExerciseType
}

data class PersonData(
    val person: Person,
    val distance: Double,
    val percentage: Double,
    val cssClass: String
)
