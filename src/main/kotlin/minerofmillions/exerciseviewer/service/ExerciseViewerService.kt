package minerofmillions.exerciseviewer.service

import minerofmillions.exerciseviewer.database.ExerciseDataDB
import minerofmillions.exerciseviewer.entities.Person
import minerofmillions.exerciseviewer.entities.PersonData
import org.springframework.stereotype.Service

@Service
class ExerciseViewerService {
    fun getInformationOf(person: Person, current: String = ""): PersonData {
        return PersonData(
            person.realName,
            ExerciseDataDB.getIndividualDistanceMiles(person),
            ExerciseDataDB.getIndividualDistanceMiles(person) * 100 / ExerciseDataDB.getFamilyDistanceMiles(),
            if (person.name == current) "table-active" else "",
            person.name
        )
    }
}
