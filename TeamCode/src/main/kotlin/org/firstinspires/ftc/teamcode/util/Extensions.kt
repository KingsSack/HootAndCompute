package org.firstinspires.ftc.teamcode.util

fun Double.toRadians(): Double = Math.toRadians(this)

val Double.inches get() = Inches(this)
val Double.deg get() = Degrees(this)
