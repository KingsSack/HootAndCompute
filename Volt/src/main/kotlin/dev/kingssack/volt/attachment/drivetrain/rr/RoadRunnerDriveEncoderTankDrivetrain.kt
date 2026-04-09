package dev.kingssack.volt.attachment.drivetrain.rr

import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.ftc.Encoder
import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.util.Localizer

/**
 * A tank [dev.kingssack.volt.attachment.drivetrain.Drivetrain] integrated with the RoadRunner
 * library with drive encoder localization.
 *
 * @param hardwareMap the hardware map
 * @param pose the robot's initial pose
 * @param params the drive parameters
 */
class RoadRunnerDriveEncoderTankDrivetrain(
    hardwareMap: HardwareMap,
    pose: Pose2d = Pose2d(Vector2d(0.0, 0.0), 0.0),
    params: DriveParams = DriveParams(),
) : RoadRunnerTankDrivetrain(hardwareMap, params) {
    override val localizer = DriveLocalizer(pose)

    inner class DriveLocalizer(override var pose: Pose2d) : Localizer {
        val leftEncoders: List<Encoder> = leftMotors.map { OverflowEncoder(RawEncoder(it)) }
        val rightEncoders: List<Encoder> = rightMotors.map { OverflowEncoder(RawEncoder(it)) }

        private var lastLeftPos = 0.0
        private var lastRightPos = 0.0
        private var initialized = false

        override fun update(): PoseVelocity2d {
            var delta: Twist2dDual<Time?>?

            var meanLeftPos = 0.0
            var meanLeftVel = 0.0
            val leftReadings = leftEncoders.map {
                val p = it.getPositionAndVelocity()
                meanLeftPos += p.position.toDouble()
                meanLeftVel += p.velocity!!.toDouble()
                p
            }
            meanLeftPos /= leftEncoders.size.toDouble()
            meanLeftVel /= leftEncoders.size.toDouble()

            var meanRightPos = 0.0
            var meanRightVel = 0.0
            val rightReadings = rightEncoders.map {
                val p = it.getPositionAndVelocity()
                meanRightPos += p.position.toDouble()
                meanRightVel += p.velocity!!.toDouble()
                p
            }
            meanRightPos /= rightEncoders.size.toDouble()
            meanRightVel /= rightEncoders.size.toDouble()

            //            write(
            //                "TANK_LOCALIZER_INPUTS",
            //                TankLocalizerInputsMessage(leftReadings, rightReadings)
            //            )

            if (!initialized) {
                initialized = true

                lastLeftPos = meanLeftPos
                lastRightPos = meanRightPos

                return PoseVelocity2d(Vector2d(0.0, 0.0), 0.0)
            }

            val twist: Twist2dDual<Time?> =
                kinematics.forward(
                    TankKinematics.WheelIncrements(
                        DualNum<Time?>(doubleArrayOf(meanLeftPos - lastLeftPos, meanLeftVel))
                            .times(params.inPerTick),
                        DualNum<Time?>(doubleArrayOf(meanRightPos - lastRightPos, meanRightVel))
                            .times(params.inPerTick),
                    )
                )

            lastLeftPos = meanLeftPos
            lastRightPos = meanRightPos

            pose = pose.plus(twist.value())

            return twist.velocity().value()
        }
    }
}
