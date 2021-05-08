package minerofmillions.exerciseviewer

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class ExerciseData(
    val name: String,
    val type: ExerciseType,
    val date: Date,
    val distance: Double,
    val time: Long,
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long
) {
    enum class ExerciseType {
        BIKING,
        WALKING,
        RUNNING
    }
}