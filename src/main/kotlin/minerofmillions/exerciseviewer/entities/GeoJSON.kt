package minerofmillions.exerciseviewer.entities

import com.google.gson.*
import java.lang.reflect.Type

sealed class GeoJSON(val type: String, val bbox: List<Double>? = null) {
    object Serializer : JsonDeserializer<GeoJSON> {
        override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): GeoJSON =
            context.deserialize(
                element,
                when (val jsonType = element.asJsonObject["type"].asString) {
                    "FeatureCollection" -> FeatureCollection::class.java
                    "Feature" -> Feature::class.java
                    "Point" -> Point::class.java
                    "MultiPoint" -> MultiPoint::class.java
                    "LineString" -> LineString::class.java
                    "MultiLineString" -> MultiLineString::class.java
                    "Polygon" -> Polygon::class.java
                    "MultiPolygon" -> MultiPolygon::class.java
                    "GeometryCollection" -> GeometryCollection::class.java
                    else -> error("Invalid GeoJSON type: $jsonType")
                }
            )
    }
}

class FeatureCollection(val features: List<Feature>, bbox: List<Double>? = null) : GeoJSON("FeatureCollection", bbox)
class Feature(val properties: Map<String, Any>?, val geometry: Geometry?, bbox: List<Double>? = null) :
    GeoJSON("Feature", bbox)

fun emptyFeatureCollection() = FeatureCollection(emptyList())
fun featureCollectionOf(vararg features: Feature, bbox: List<Double>? = null) =
    FeatureCollection(features.toList(), bbox = bbox)

fun emptyLineString() = LineString(emptyList())
fun lineStringOf(vararg points: Position, bbox: List<Double>? = null) = LineString(points.toList(), bbox = bbox)
fun lineStringOf(vararg points: Coordinate, bbox: List<Double>? = null) =
    LineString(points.map { Position(it) }, bbox = bbox)

fun lineStringOf(points: Collection<Position>, bbox: List<Double>? = null) = LineString(points.toList(), bbox = bbox)

sealed class Geometry(type: String, bbox: List<Double>? = null) : GeoJSON(type, bbox) {
    object Serializer : JsonDeserializer<Geometry> {
        override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): Geometry =
            context.deserialize(
                element,
                when (val jsonType = element.asJsonObject["type"].asString) {
                    "Point" -> Point::class.java
                    "MultiPoint" -> MultiPoint::class.java
                    "LineString" -> LineString::class.java
                    "MultiLineString" -> MultiLineString::class.java
                    "Polygon" -> Polygon::class.java
                    "MultiPolygon" -> MultiPolygon::class.java
                    "GeometryCollection" -> GeometryCollection::class.java
                    else -> error("Invalid GeoJSON Geometry type: $jsonType")
                }
            )
    }
}

class Point(val coordinates: Position, bbox: List<Double>? = null) : Geometry("Point", bbox)
class MultiPoint(val coordinates: List<Point>, bbox: List<Double>? = null) : Geometry("MultiPoint", bbox)
class LineString(val coordinates: List<Position>, bbox: List<Double>? = null) : Geometry("LineString", bbox)
class MultiLineString(val coordinates: List<LineString>, bbox: List<Double>? = null) : Geometry("MultiLineString", bbox)
class Polygon(val coordinates: List<List<Position>>, bbox: List<Double>? = null) : Geometry("Polygon", bbox)
class MultiPolygon(val coordinates: List<Polygon>, bbox: List<Double>? = null) : Geometry("MultiPolygon", bbox)
class GeometryCollection(val geometries: List<Geometry>, bbox: List<Double>? = null) :
    Geometry("GeometryCollection", bbox)

class Position(val lat: Double, val lng: Double) {
    constructor(coordinate: Coordinate) : this(coordinate.lat, coordinate.lng)

    object Serializer : JsonSerializer<Position>, JsonDeserializer<Position> {
        override fun serialize(position: Position, type: Type, context: JsonSerializationContext): JsonElement =
            JsonArray().apply {
                add(position.lat)
                add(position.lng)
            }

        override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): Position {
            val arr = element.asJsonArray
            return Position(arr[0].asDouble, arr[1].asDouble)
        }
    }
}
