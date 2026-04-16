package dev.kingssack.volt.integrations.rr.localizer

import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.ftc.Encoder
import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.IMU
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit

/**
 * A [RoadRunnerLocalizer] that uses the drive encoders and an IMU to localize the robot.
 *
 * @param leftFront the left front motor
 * @param leftBack the left rear motor
 * @param rightBack the right rear motor
 * @param rightFront the right front motor
 * @param kinematics the mecanum kinematics
 * @param imu the imu
 * @param inPerTick the number of inches per encoder tick
 * @param pose the initial pose
 * @property leftFrontEncoder the left front encoder
 * @property leftBackEncoder the left rear encoder
 * @property rightBackEncoder the right rear encoder
 * @property rightFrontEncoder the right front encoder
 */
class MecanumDriveLocalizer(
    leftFront: DcMotorEx,
    leftBack: DcMotorEx,
    rightBack: DcMotorEx,
    rightFront: DcMotorEx,
    private val kinematics: MecanumKinematics,
    val imu: IMU,
    private val inPerTick: Double,
    override var pose: Pose2d,
) : RoadRunnerLocalizer {
    val leftFrontEncoder: Encoder = OverflowEncoder(RawEncoder(leftFront))
    val leftBackEncoder: Encoder = OverflowEncoder(RawEncoder(leftBack))
    val rightBackEncoder: Encoder = OverflowEncoder(RawEncoder(rightBack))
    val rightFrontEncoder: Encoder = OverflowEncoder(RawEncoder(rightFront))

    private var lastLeftFrontPos = 0.0
    private var lastLeftBackPos = 0.0
    private var lastRightBackPos = 0.0
    private var lastRightFrontPos = 0.0
    private var lastHeading = Rotation2d(0.0, 0.0)

    private var initialized = false

    override fun update(): PoseVelocity2d {
        val leftFrontPosVel = leftFrontEncoder.getPositionAndVelocity()
        val leftBackPosVel = leftBackEncoder.getPositionAndVelocity()
        val rightBackPosVel = rightBackEncoder.getPositionAndVelocity()
        val rightFrontPosVel = rightFrontEncoder.getPositionAndVelocity()

        val angles = imu.robotYawPitchRollAngles

        val heading = Rotation2d.exp(angles.getYaw(AngleUnit.RADIANS))

        if (!initialized) {
            initialized = true

            lastLeftFrontPos = leftFrontPosVel.position.toDouble()
            lastLeftBackPos = leftBackPosVel.position.toDouble()
            lastRightBackPos = rightBackPosVel.position.toDouble()
            lastRightFrontPos = rightFrontPosVel.position.toDouble()

            lastHeading = heading

            return PoseVelocity2d(Vector2d(0.0, 0.0), 0.0)
        }

        val headingDelta = heading.minus(lastHeading)
        val twist =
            kinematics.forward(
                MecanumKinematics.WheelIncrements(
                    DualNum<Time>(
                            doubleArrayOf(
                                leftFrontPosVel.position - lastLeftFrontPos,
                                leftFrontPosVel.velocity!!.toDouble(),
                            )
                        )
                        .times(inPerTick),
                    DualNum<Time>(
                            doubleArrayOf(
                                leftBackPosVel.position - lastLeftBackPos,
                                leftBackPosVel.velocity!!.toDouble(),
                            )
                        )
                        .times(inPerTick),
                    DualNum<Time>(
                            doubleArrayOf(
                                rightBackPosVel.position - lastRightBackPos,
                                rightBackPosVel.velocity!!.toDouble(),
                            )
                        )
                        .times(inPerTick),
                    DualNum<Time>(
                            doubleArrayOf(
                                rightFrontPosVel.position - lastRightFrontPos,
                                rightFrontPosVel.velocity!!.toDouble(),
                            )
                        )
                        .times(inPerTick),
                )
            )

        lastLeftFrontPos = leftFrontPosVel.position.toDouble()
        lastLeftBackPos = leftBackPosVel.position.toDouble()
        lastRightBackPos = rightBackPosVel.position.toDouble()
        lastRightFrontPos = rightFrontPosVel.position.toDouble()

        lastHeading = heading

        pose = pose.plus(Twist2d(twist.line.value(), headingDelta))

        return twist.velocity().value()
    }
}
