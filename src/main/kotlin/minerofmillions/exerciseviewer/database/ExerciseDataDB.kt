package minerofmillions.exerciseviewer.database

import com.google.gson.reflect.TypeToken
import minerofmillions.exerciseviewer.METERS_PER_MILE
import minerofmillions.exerciseviewer.entities.ExerciseData
import minerofmillions.exerciseviewer.entities.Person
import minerofmillions.exerciseviewer.gson
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.util.*
import kotlin.math.roundToInt

object ExerciseDataDB {
    private val latestId: Int
        get() = exerciseData.maxOfOrNull { it.id } ?: 0

    private val exerciseData = mutableListOf<ExerciseData>()
    private val s3 = S3Client.builder().region(Region.US_EAST_1).build()
    private val properties = Properties().apply {
        File(".env").reader().use { load(it) }
    }
    private val bucket = properties["BUCKETEER_BUCKET_NAME"] as String
    private val credentials = object : AwsCredentials {
        override fun accessKeyId(): String = properties["BUCKETEER_AWS_ACCESS_KEY_ID"] as String
        override fun secretAccessKey(): String = properties["BUCKETEER_AWS_SECRET_ACCESS_KEY"] as String
    }

    init {
        getDataFromS3()
    }

    fun save(element: ExerciseData): ExerciseData =
        element.apply { id = latestId + 1 }.also { exerciseData.add(it); saveDataToS3() }

    fun saveAll(elements: MutableIterable<ExerciseData>): MutableIterable<ExerciseData> =
        elements.onEach(this::save)

    fun findById(id: Int): Optional<ExerciseData> =
        Optional.ofNullable(exerciseData.firstOrNull { it.id == id })

    fun existsById(id: Int): Boolean = exerciseData.any { it.id == id }

    fun findAll(): Iterable<ExerciseData> = exerciseData
    fun findAllById(ids: Iterable<Int>): Iterable<ExerciseData> = exerciseData.filter { it.id in ids }

    fun count(): Long = exerciseData.size.toLong()

    fun deleteById(id: Int) {
        exerciseData.removeAt(exerciseData.indexOfFirst { it.id == id })
        saveDataToS3()
    }

    fun delete(element: ExerciseData) {
        exerciseData.remove(element)
        saveDataToS3()
    }

    fun deleteAll(elements: MutableIterable<ExerciseData>) {
        exerciseData.removeAll(elements)
        saveDataToS3()
    }

    fun deleteAll() {
        exerciseData.clear()
        saveDataToS3()
    }

    fun getExerciseDataOf(person: Person) = exerciseData.filter { it.person == person }

    fun getFamilyDistanceMiles() = exerciseData.sumByDouble { it.weightedDistance }

    internal fun getFamilyDistanceMeters() = (getFamilyDistanceMiles() * METERS_PER_MILE).roundToInt()

    internal fun getIndividualDistanceMiles(person: Person) =
        getExerciseDataOf(person).sumByDouble { it.weightedDistance }

    internal fun getIndividualDistanceMeters(person: Person) =
        (getIndividualDistanceMiles(person) * METERS_PER_MILE).roundToInt()

    private fun getDataFromS3() {
        try {
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key("exercisedata")
                .overrideConfiguration {
                    it.credentialsProvider { credentials }
                }
                .build()

            val pastData = s3.getObject<List<ExerciseData>>(getObjectRequest) { response, abortableInputStream ->
                abortableInputStream.reader().use { reader ->
                    gson.fromJson(
                        reader,
                        ExerciseDataListTypeToken.type
                    ) ?: emptyList()
                }
            }

            exerciseData.addAll(pastData)
        } catch (e: NoSuchKeyException) {
        }
    }

    private fun saveDataToS3() {
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key("exercisedata")
                .overrideConfiguration {
                    it.credentialsProvider { credentials }
                }
                .build(),
            RequestBody.fromString(gson.toJson(exerciseData))
        )
    }

    private object ExerciseDataListTypeToken : TypeToken<List<ExerciseData>>()
}
