package dev.kingssack.volt.attachment.drivetrain.rr

import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.ftc.Encoder
import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.IMU
import dev.kingssack.volt.util.Localizer
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit

/**
 * A mecanum [dev.kingssack.volt.attachment.drivetrain.Drivetrain] integrated with the RoadRunner library with drive encoder localization.
 *
 * @param hardwareMap the hardware map
 * @param pose the robot's initial pose
 * @param params the drive parameters
 */
class RoadRunnerDriveEncoderMecanumDrivetrain(
    hardwareMap: HardwareMap,
    pose: Pose2d = Pose2d(Vector2d(0.0, 0.0), 0.0),
    params: DriveParams = DriveParams(),
) : RoadRunnerMecanumDrivetrain(hardwareMap, params) {
    override val localizer = DriveLocalizer(pose)

    inner class DriveLocalizer(override var pose: Pose2d) : Localizer {
        val leftFrontEncoder: Encoder = OverflowEncoder(RawEncoder(leftFront))
        val leftBackEncoder: Encoder = OverflowEncoder(RawEncoder(leftBack))
        val rightBackEncoder: Encoder = OverflowEncoder(RawEncoder(rightBack))
        val rightFrontEncoder: Encoder = OverflowEncoder(RawEncoder(rightFront))
        private val imu: IMU = lazyImu.get()

        private var lastLeftFrontPos = 0.0
        private var lastLeftBackPos = 0.0
        private var lastRightBackPos = 0.0
        private var lastRightFrontPos = 0.0
        private var lastHeading: Rotation2d? = null
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

            val headingDelta = heading.minus(lastHeading!!)
            val twist: Twist2dDual<Time> =
                kinematics.forward(
                    MecanumKinematics.WheelIncrements(
                        DualNum<Time>(
                                doubleArrayOf(
                                    leftFrontPosVel.position - lastLeftFrontPos,
                                    leftFrontPosVel.velocity!!.toDouble(),
                                )
                            )
                            .times(params.inPerTick),
                        DualNum<Time>(
                                doubleArrayOf(
                                    leftBackPosVel.position - lastLeftBackPos,
                                    leftBackPosVel.velocity!!.toDouble(),
                                )
                            )
                            .times(params.inPerTick),
                        DualNum<Time>(
                                doubleArrayOf(
                                    rightBackPosVel.position - lastRightBackPos,
                                    rightBackPosVel.velocity!!.toDouble(),
                                )
                            )
                            .times(params.inPerTick),
                        DualNum<Time>(
                                doubleArrayOf(
                                    rightFrontPosVel.position - lastRightFrontPos,
                                    rightFrontPosVel.velocity!!.toDouble(),
                                )
                            )
                            .times(params.inPerTick),
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

        init {
            leftBack.direction = DcMotorSimple.Direction.REVERSE
            rightBack.direction = DcMotorSimple.Direction.REVERSE
        }
    }
}
