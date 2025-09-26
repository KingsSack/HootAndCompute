package dev.kingssack.volt.util

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2dDual
import com.acmerobotics.roadrunner.Time
import com.acmerobotics.roadrunner.ftc.PositionVelocityPair
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles

data class PoseMessage(
    val pose: Pose2d,
    val x: Double = pose.position.x,
    val y: Double = pose.position.y,
    val heading: Double = pose.heading.toDouble(),
    val timestamp: Long = System.nanoTime()
)

data class DriveCommandMessage(
    val poseVelocity: PoseVelocity2dDual<Time>,
    val forwardVelocity: Double = poseVelocity.linearVel.x[0],
    val forwardAcceleration: Double = poseVelocity.linearVel.x[1],
    val lateralVelocity: Double = poseVelocity.linearVel.y[0],
    val lateralAcceleration: Double = poseVelocity.linearVel.y[1],
    val angularVelocity: Double = poseVelocity.angVel[0],
    val angularAcceleration: Double = poseVelocity.angVel[1],
    val timestamp: Long = System.nanoTime()
)

data class MecanumCommandMessage(
    val voltage: Double,
    val leftFrontPower: Double,
    val leftBackPower: Double,
    val rightBackPower: Double,
    val rightFrontPower: Double,
    val timestamp: Long = System.nanoTime()
)

data class MecanumLocalizerInputsMessage(
    val leftFrontPosVel: PositionVelocityPair,
    val leftBackPosVel: PositionVelocityPair,
    val rightBackPosVel: PositionVelocityPair,
    val rightFrontPosVel: PositionVelocityPair,
    val angles: YawPitchRollAngles,
    val yaw: Double = angles.getYaw(AngleUnit.RADIANS),
    val pitch: Double = angles.getPitch(AngleUnit.RADIANS),
    val roll: Double = angles.getRoll(AngleUnit.RADIANS),
    val timestamp: Long = System.nanoTime()
)