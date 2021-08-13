package minerofmillions.exerciseviewer.util

import java.awt.Color
import kotlin.math.pow

fun round(x: Double, places: Int): Double = (x * 10.0.pow(places)).toInt() / 10.0.pow(places)

fun toHex(c: Color) = "#%02X%02X%02X".format(c.red, c.green, c.blue)
