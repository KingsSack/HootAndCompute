package dev.kingssack.volt.attachment.drivetrain

import com.acmerobotics.roadrunner.PoseVelocity2d

/** A drivetrain implementation for robots with mecanum wheels. */
abstract class MecanumDrivetrain : Drivetrain() {
    abstract fun setDrivePowers(powers: PoseVelocity2d)
}
