package org.firstinspires.ftc.teamcode.util

import dev.kingssack.volt.util.Degrees
import dev.kingssack.volt.util.Inches
import dev.kingssack.volt.util.Radians

fun Double.toRadians(): Double = Math.toRadians(this)
fun Double.toDegrees(): Double = Math.toDegrees(this)

val Double.inches get() = Inches(this)
val Double.deg get() = Degrees(this)
val Double.rad get() = Radians(this)
