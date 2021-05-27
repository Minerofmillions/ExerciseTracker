package minerofmillions.exerciseviewer

import com.google.gson.annotations.Expose

class Response(
    @Expose var routes: List<Route>
)

class Route(
    @Expose var bounds: Bounds,
    @Expose var legs: List<RouteLeg>
)

class RouteLeg(
    @Expose var steps: List<RouteStep>,
    @Expose var start_location: Coordinate,
    @Expose var end_location: Coordinate
)

class RouteStep(
    @Expose var distance: Distance,
    @Expose var start_location: Coordinate,
    @Expose var end_location: Coordinate,
    @Expose var html_instructions: String
)

class Distance(
    @Expose var text: String,
    @Expose var value: Int
)

class Bounds(
    @Expose var northeast: Coordinate,
    @Expose var southwest: Coordinate
) {
    val center
        get() = Coordinate(
            (northeast.lat + southwest.lat) / 2,
            (northeast.lng + southwest.lng) / 2
        )
}

class Coordinate(
    @Expose var lat: Double,
    @Expose var lng: Double
)
