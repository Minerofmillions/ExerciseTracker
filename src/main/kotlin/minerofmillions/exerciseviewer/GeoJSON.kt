package minerofmillions.exerciseviewer

import com.google.gson.*
import java.lang.reflect.Type

sealed class GeoJSON(val type: String) {
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

class FeatureCollection(val features: List<Feature>) : GeoJSON("FeatureCollection")
class Feature(val properties: Map<String, Any>?, val geometry: Geometry?) : GeoJSON("Feature")

sealed class Geometry(type: String) : GeoJSON(type) {
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

class Point(val coordinates: Position) : Geometry("Point")
class MultiPoint(val coordinates: List<Point>) : Geometry("MultiPoint")
class LineString(val coordinates: List<Position>) : Geometry("LineString")
class MultiLineString(val coordinates: List<LineString>) : Geometry("MultiLineString")
class Polygon(val coordinates: List<List<Position>>) : Geometry("Polygon")
class MultiPolygon(val coordinates: List<Polygon>) : Geometry("MultiPolygon")
class GeometryCollection(val geometries: List<Geometry>) : Geometry("GeometryCollection")

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