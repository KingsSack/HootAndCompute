package org.firstinspires.ftc.teamcode.util

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.util.Degrees
import dev.kingssack.volt.util.Inches
import dev.kingssack.volt.util.Radians

fun Double.toRadians(): Double = Math.toRadians(this)

fun Double.toDegrees(): Double = Math.toDegrees(this)

val Double.inches
    get() = Inches(this)
val Double.deg
    get() = Degrees(this)
val Double.rad
    get() = Radians(this)

/** Mirrors a [Pose] across the field's vertical axis, based on the current [alliance]. */
fun Pose.maybeFlip(alliance: AllianceColor): Pose =
    when (alliance) {
        AllianceColor.BLUE -> this
        AllianceColor.RED -> this.mirror()
    }
