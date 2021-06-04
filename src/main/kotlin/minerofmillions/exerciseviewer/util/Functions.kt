package minerofmillions.exerciseviewer.util

import kotlin.math.pow

fun round(x: Double, places: Int): Double = (x * 10.0.pow(places)).toInt() / 10.0.pow(places)