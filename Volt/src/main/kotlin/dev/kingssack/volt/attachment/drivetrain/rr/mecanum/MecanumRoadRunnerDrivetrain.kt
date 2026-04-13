package dev.kingssack.volt.attachment.drivetrain.rr.mecanum

import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.*
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.rr.RoadRunnerDrivetrain
import dev.kingssack.volt.integrations.rr.Drawing
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

/**
 * A mecanum drivetrain integrated with the RoadRunner library.
 *
 * @param hardwareMap the hardware map
 * @param params the drive parameters
 * @property leftFront the left front motor
 * @property leftBack the left back motor
 * @property rightBack the right back motor
 * @property rightFront the right front motor
 * @property voltageSensor the voltage sensor
 * @property lazyImu the lazy IMU
 * @property localizer the localizer
 */
abstract class MecanumRoadRunnerDrivetrain(
    hardwareMap: HardwareMap,
    protected val params: DriveParams = DriveParams(),
) :
    RoadRunnerDrivetrain<MecanumKinematics>(
        hardwareMap,
        RevHubOrientationOnRobot(params.logoFacingDirection, params.usbFacingDirection),
    ) {
    /**
     * Parameters for [MecanumRoadRunnerDrivetrain].
     *
     * @property logoFacingDirection the direction the Control Hub's logo is facing
     * @property usbFacingDirection the direction the Control Hub's USB port is facing
     * @property leftFrontName the name of the left front motor
     * @property leftFrontDirection the direction of the left front motor
     * @property leftBackName the name of the left back motor
     * @property leftBackDirection the direction of the left back motor
     * @property rightBackName the name of the right back motor
     * @property rightBackDirection the direction of the right back motor
     * @property rightFrontName the name of the right front motor
     * @property rightFrontDirection the direction of the right front motor
     * @property inPerTick the inches per tick
     * @property lateralInPerTick the lateral inches per tick
     * @property trackWidthTicks the track width in ticks
     * @property kS the static gain
     * @property kV the velocity gain
     * @property kA the acceleration gain
     * @property maxWheelVel the maximum wheel velocity
     * @property minProfileAccel the minimum profile acceleration
     * @property maxProfileAccel the maximum profile acceleration
     * @property maxAngVel the maximum angular velocity
     * @property maxAngAccel the maximum angular acceleration
     * @property axialGain the axial gain
     * @property lateralGain the lateral gain
     * @property headingGain the heading gain
     * @property axialVelGain the axial velocity gain
     * @property lateralVelGain the lateral velocity gain
     * @property headingVelGain the heading velocity gain
     */
    class DriveParams(
        val logoFacingDirection: LogoFacingDirection = LogoFacingDirection.UP,
        val usbFacingDirection: UsbFacingDirection = UsbFacingDirection.FORWARD,
        val leftFrontName: String = "lf",
        val leftFrontDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val leftBackName: String = "lr",
        val leftBackDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val rightBackName: String = "rr",
        val rightBackDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val rightFrontName: String = "rf",
        val rightFrontDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val inPerTick: Double = 1.0,
        val lateralInPerTick: Double = inPerTick,
        val trackWidthTicks: Double = 0.0,
        val kS: Double = 0.0,
        val kV: Double = 0.0,
        val kA: Double = 0.0,
        val maxWheelVel: Double = 50.0,
        val minProfileAccel: Double = -30.0,
        val maxProfileAccel: Double = 50.0,
        val maxAngVel: Double = Math.PI,
        val maxAngAccel: Double = Math.PI,
        val axialGain: Double = 0.0,
        val lateralGain: Double = 0.0,
        val headingGain: Double = 0.0,
        val axialVelGain: Double = 0.0,
        val lateralVelGain: Double = 0.0,
        val headingVelGain: Double = 0.0,
    )

    override val kinematics =
        MecanumKinematics(
            params.inPerTick * params.trackWidthTicks,
            params.inPerTick / params.lateralInPerTick,
        )

    override val defaultTurnConstraints =
        TurnConstraints(params.maxAngVel, -params.maxAngAccel, params.maxAngAccel)
    override val defaultVelConstraint =
        MinVelConstraint(
            listOf(
                kinematics.WheelVelConstraint(params.maxWheelVel),
                AngularVelConstraint(params.maxAngVel),
            )
        )
    override val defaultAccelConstraint =
        ProfileAccelConstraint(params.minProfileAccel, params.maxProfileAccel)

    val leftFront: DcMotorEx =
        hardwareMap.get(DcMotorEx::class.java, params.leftFrontName).apply {
            direction = params.leftFrontDirection
        }
    val leftBack: DcMotorEx =
        hardwareMap.get(DcMotorEx::class.java, params.leftBackName).apply {
            direction = params.leftBackDirection
        }
    val rightBack: DcMotorEx =
        hardwareMap.get(DcMotorEx::class.java, params.rightBackName).apply {
            direction = params.rightBackDirection
        }
    val rightFront: DcMotorEx =
        hardwareMap.get(DcMotorEx::class.java, params.rightFrontName).apply {
            direction = params.rightFrontDirection
        }
    private val driveMotors = listOf(leftFront, leftBack, rightBack, rightFront)

    private val poseHistory = LinkedList<Pose2d>()

    init {
        driveMotors.forEach { it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE }
    }

    override fun setDrivePowers(powers: PoseVelocity2d) {
        val wheelVels = MecanumKinematics(1.0).inverse(PoseVelocity2dDual.constant<Time>(powers, 1))

        var maxPowerMag = 1.0
        for (power in wheelVels.all()) {
            maxPowerMag = max(maxPowerMag, power.value())
        }

        leftFront.power = wheelVels.leftFront[0] / maxPowerMag
        leftBack.power = wheelVels.leftBack[0] / maxPowerMag
        rightBack.power = wheelVels.rightBack[0] / maxPowerMag
        rightFront.power = wheelVels.rightFront[0] / maxPowerMag
    }

    inner class FollowTrajectoryAction(private val timeTrajectory: TimeTrajectory) : Action {
        private var beginTs = -1.0

        private val xPoints: DoubleArray
        private val yPoints: DoubleArray

        init {
            val disps =
                range(
                    0.0,
                    timeTrajectory.path.length(),
                    max(2.0, ceil(timeTrajectory.path.length() / 2).toInt().toDouble()).toInt(),
                )
            xPoints = DoubleArray(disps.size)
            yPoints = DoubleArray(disps.size)
            for (i in disps.indices) {
                val p = timeTrajectory.path[disps[i], 1].value()
                xPoints[i] = p.position.x
                yPoints[i] = p.position.y
            }
        }

        override fun run(p: TelemetryPacket): Boolean {
            val t: Double
            if (beginTs < 0) {
                beginTs = now()
                t = 0.0
            } else {
                t = now() - beginTs
            }

            if (t >= timeTrajectory.duration) {
                leftFront.power = 0.0
                leftBack.power = 0.0
                rightBack.power = 0.0
                rightFront.power = 0.0

                return false
            }

            val txWorldTarget = timeTrajectory[t]
            //            targetPoseWriter.write(PoseMessage(txWorldTarget.value()))

            val robotVelRobot = updatePoseEstimate()

            val command =
                HolonomicController(
                        params.axialGain,
                        params.lateralGain,
                        params.headingGain,
                        params.axialVelGain,
                        params.lateralVelGain,
                        params.headingVelGain,
                    )
                    .compute(txWorldTarget, localizer.pose, robotVelRobot)
            //            driveCommandWriter.write(DriveCommandMessage(command))

            val wheelVels = kinematics.inverse(command)
            val voltage = voltageSensor.voltage

            val feedforward =
                MotorFeedforward(
                    params.kS,
                    params.kV / params.inPerTick,
                    params.kA / params.inPerTick,
                )
            val leftFrontPower = feedforward.compute(wheelVels.leftFront) / voltage
            val leftBackPower = feedforward.compute(wheelVels.leftBack) / voltage
            val rightBackPower = feedforward.compute(wheelVels.rightBack) / voltage
            val rightFrontPower = feedforward.compute(wheelVels.rightFront) / voltage
            //            mecanumCommandWriter.write(
            //                MecanumCommandMessage(
            //                    voltage,
            //                    leftFrontPower,
            //                    leftBackPower,
            //                    rightBackPower,
            //                    rightFrontPower,
            //                )
            //            )

            leftFront.power = leftFrontPower
            leftBack.power = leftBackPower
            rightBack.power = rightBackPower
            rightFront.power = rightFrontPower

            p.put("x", localizer.pose.position.x)
            p.put("y", localizer.pose.position.y)
            p.put("heading (deg)", Math.toDegrees(localizer.pose.heading.toDouble()))

            val error = txWorldTarget.value().minusExp(localizer.pose)
            p.put("xError", error.position.x)
            p.put("yError", error.position.y)
            p.put("headingError (deg)", Math.toDegrees(error.heading.toDouble()))

            val c = p.fieldOverlay()
            drawPoseHistory(c)

            c.setStroke("#4CAF50")
            Drawing.drawRobot(c, txWorldTarget.value())

            c.setStroke("#3F51B5")
            Drawing.drawRobot(c, localizer.pose)

            c.setStroke("#4CAF50FF")
            c.setStrokeWidth(1)
            c.strokePolyline(xPoints, yPoints)

            return true
        }

        override fun preview(fieldOverlay: Canvas) {
            fieldOverlay.setStroke("#4CAF507A")
            fieldOverlay.setStrokeWidth(1)
            fieldOverlay.strokePolyline(xPoints, yPoints)
        }
    }

    inner class TurnAction(private val turn: TimeTurn) : Action {
        private var beginTs = -1.0

        override fun run(p: TelemetryPacket): Boolean {
            val t: Double
            if (beginTs < 0) {
                beginTs = now()
                t = 0.0
            } else {
                t = now() - beginTs
            }

            if (t >= turn.duration) {
                leftFront.power = 0.0
                leftBack.power = 0.0
                rightBack.power = 0.0
                rightFront.power = 0.0

                return false
            }

            val txWorldTarget = turn[t]
            //            targetPoseWriter.write(PoseMessage(txWorldTarget.value()))

            val robotVelRobot = updatePoseEstimate()

            val command =
                HolonomicController(
                        params.axialGain,
                        params.lateralGain,
                        params.headingGain,
                        params.axialVelGain,
                        params.lateralVelGain,
                        params.headingVelGain,
                    )
                    .compute(txWorldTarget, localizer.pose, robotVelRobot)
            //            driveCommandWriter.write(DriveCommandMessage(command))

            val wheelVels = kinematics.inverse(command)
            val voltage = voltageSensor.voltage
            val feedforward =
                MotorFeedforward(
                    params.kS,
                    params.kV / params.inPerTick,
                    params.kA / params.inPerTick,
                )
            val leftFrontPower = feedforward.compute(wheelVels.leftFront) / voltage
            val leftBackPower = feedforward.compute(wheelVels.leftBack) / voltage
            val rightBackPower = feedforward.compute(wheelVels.rightBack) / voltage
            val rightFrontPower = feedforward.compute(wheelVels.rightFront) / voltage
            //            mecanumCommandWriter.write(
            //                MecanumCommandMessage(
            //                    voltage,
            //                    leftFrontPower,
            //                    leftBackPower,
            //                    rightBackPower,
            //                    rightFrontPower,
            //                )
            //            )

            leftFront.power = feedforward.compute(wheelVels.leftFront) / voltage
            leftBack.power = feedforward.compute(wheelVels.leftBack) / voltage
            rightBack.power = feedforward.compute(wheelVels.rightBack) / voltage
            rightFront.power = feedforward.compute(wheelVels.rightFront) / voltage

            val c = p.fieldOverlay()
            drawPoseHistory(c)

            c.setStroke("#4CAF50")
            Drawing.drawRobot(c, txWorldTarget.value())

            c.setStroke("#3F51B5")
            Drawing.drawRobot(c, localizer.pose)

            c.setStroke("#7C4DFFFF")
            c.fillCircle(turn.beginPose.position.x, turn.beginPose.position.y, 2.0)

            return true
        }

        override fun preview(fieldOverlay: Canvas) {
            fieldOverlay.setStroke("#7C4DFF7A")
            fieldOverlay.fillCircle(turn.beginPose.position.x, turn.beginPose.position.y, 2.0)
        }
    }

    private fun updatePoseEstimate(): PoseVelocity2d {
        val vel = localizer.update()
        poseHistory.add(localizer.pose)

        while (poseHistory.size > 100) {
            poseHistory.removeFirst()
        }

        //        estimatedPoseWriter.write(PoseMessage(localizer.pose))

        return vel
    }

    private fun drawPoseHistory(c: Canvas) {
        val xPoints = DoubleArray(poseHistory.size)
        val yPoints = DoubleArray(poseHistory.size)

        for ((i, t) in poseHistory.withIndex()) {
            xPoints[i] = t.position.x
            yPoints[i] = t.position.y
        }

        c.setStrokeWidth(1)
        c.setStroke("#3F51B5")
        c.strokePolyline(xPoints, yPoints)
    }

    override fun driveActionBuilder(beginPose: Pose2d): TrajectoryActionBuilder =
        TrajectoryActionBuilder(
            ::TurnAction,
            ::FollowTrajectoryAction,
            TrajectoryBuilderParams(1e-6, ProfileParams(0.25, 0.1, 1e-2)),
            beginPose,
            0.0,
            defaultTurnConstraints,
            defaultVelConstraint,
            defaultAccelConstraint,
        )
}
