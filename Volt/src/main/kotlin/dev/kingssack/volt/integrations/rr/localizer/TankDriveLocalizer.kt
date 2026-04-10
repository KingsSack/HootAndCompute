package dev.kingssack.volt.integrations.rr.localizer

import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.ftc.Encoder
import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.robotcore.hardware.DcMotorEx

/**
 * A [RoadRunnerLocalizer] that uses the drive encoders to localize the robot.
 *
 * @param leftMotors the left motors
 * @param rightMotors the right motors
 * @param kinematics the mecanum kinematics
 * @param inPerTick the number of inches per encoder tick
 * @param pose the initial pose
 * @property leftEncoders the left encoders
 * @property rightEncoders the right encoders
 */
class TankDriveLocalizer(
    leftMotors: List<DcMotorEx>,
    rightMotors: List<DcMotorEx>,
    private val kinematics: TankKinematics,
    private val inPerTick: Double,
    override var pose: Pose2d,
) : RoadRunnerLocalizer {
    val leftEncoders: List<Encoder> = leftMotors.map { OverflowEncoder(RawEncoder(it)) }
    val rightEncoders: List<Encoder> = rightMotors.map { OverflowEncoder(RawEncoder(it)) }

    private var lastLeftPos = 0.0
    private var lastRightPos = 0.0
    private var initialized = false

    override fun update(): PoseVelocity2d {
        var meanLeftPos = 0.0
        var meanLeftVel = 0.0
        meanLeftPos /= leftEncoders.size.toDouble()
        meanLeftVel /= leftEncoders.size.toDouble()

        var meanRightPos = 0.0
        var meanRightVel = 0.0
        meanRightPos /= rightEncoders.size.toDouble()
        meanRightVel /= rightEncoders.size.toDouble()

        if (!initialized) {
            initialized = true

            lastLeftPos = meanLeftPos
            lastRightPos = meanRightPos

            return PoseVelocity2d(Vector2d(0.0, 0.0), 0.0)
        }

        val twist =
            kinematics.forward(
                TankKinematics.WheelIncrements(
                    DualNum<Time>(doubleArrayOf(meanLeftPos - lastLeftPos, meanLeftVel))
                        .times(inPerTick),
                    DualNum<Time>(doubleArrayOf(meanRightPos - lastRightPos, meanRightVel))
                        .times(inPerTick),
                )
            )

        lastLeftPos = meanLeftPos
        lastRightPos = meanRightPos

        pose = pose.plus(twist.value())

        return twist.velocity().value()
    }
}
