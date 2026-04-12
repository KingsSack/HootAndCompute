package dev.kingssack.volt.attachment.drivetrain.rr.tank

import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.Vector2dDual.Companion.constant
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.rr.RoadRunnerDrivetrain
import dev.kingssack.volt.integrations.rr.Drawing.drawRobot
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

/**
 * A tank drivetrain integrated with the RoadRunner library.
 *
 * @param hardwareMap the hardware map
 * @param params the drive parameters
 * @property leftMotors the left motors
 * @property rightMotors the right motors
 */
abstract class RoadRunnerTankDrivetrain(
    hardwareMap: HardwareMap,
    protected val params: DriveParams = DriveParams(),
) :
    RoadRunnerDrivetrain<TankKinematics>(
        hardwareMap,
        RevHubOrientationOnRobot(params.logoFacingDirection, params.usbFacingDirection),
    ) {
    /**
     * Parameters for [RoadRunnerTankDrivetrain].
     *
     * @property logoFacingDirection the direction the Control Hub's logo is facing
     * @property usbFacingDirection the direction the Control Hub's USB port is facing
     * @property leftMotorNames the names of the left motors in the hardware map
     * @property leftMotorDirections the directions of the left motors
     * @property rightMotorNames the names of the right motors in the hardware map
     * @property rightMotorDirections the directions of the right motors
     * @property inPerTick the inches per tick
     * @property trackWidthTicks the track width in ticks
     * @property kS the static gain
     * @property kV the velocity gain
     * @property kA the acceleration gain
     * @property maxWheelVel the maximum wheel velocity
     * @property minProfileAccel the minimum profile acceleration
     * @property maxProfileAccel the maximum profile acceleration
     * @property maxAngVel the maximum angular velocity
     * @property maxAngAccel the maximum angular acceleration
     * @property ramseteZeta the
     */
    class DriveParams(
        val logoFacingDirection: LogoFacingDirection = LogoFacingDirection.UP,
        val usbFacingDirection: UsbFacingDirection = UsbFacingDirection.FORWARD,
        val leftMotorNames: List<String> = listOf("left"),
        val leftMotorDirections: List<DcMotorSimple.Direction> =
            listOf(DcMotorSimple.Direction.FORWARD),
        val rightMotorNames: List<String> = listOf("right"),
        val rightMotorDirections: List<DcMotorSimple.Direction> =
            listOf(DcMotorSimple.Direction.FORWARD),
        val inPerTick: Double = 1.0,
        val trackWidthTicks: Double = 0.0,
        val kS: Double = 0.0,
        val kV: Double = 0.0,
        val kA: Double = 0.0,
        val maxWheelVel: Double = 50.0,
        val minProfileAccel: Double = -30.0,
        val maxProfileAccel: Double = 50.0,
        val maxAngVel: Double = Math.PI,
        val maxAngAccel: Double = Math.PI,
        val ramseteZeta: Double = 0.7,
        val ramseteBBar: Double = 2.0,
        val turnGain: Double = 0.0,
        val turnVelGain: Double = 0.0,
    )

    override val kinematics = TankKinematics(params.inPerTick * params.trackWidthTicks)

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

    val leftMotors: List<DcMotorEx> =
        params.leftMotorNames.zip(params.leftMotorDirections).map { (name, direction) ->
            hardwareMap.get(DcMotorEx::class.java, name).apply { this.direction = direction }
        }
    val rightMotors: List<DcMotorEx> =
        params.rightMotorNames.zip(params.rightMotorDirections).map { (name, direction) ->
            hardwareMap.get(DcMotorEx::class.java, name).apply { this.direction = direction }
        }

    private val poseHistory = LinkedList<Pose2d>()

    //    private val estimatedPoseWriter = DownsampledWriter("ESTIMATED_POSE", 50000000)
    //    private val targetPoseWriter = DownsampledWriter("TARGET_POSE", 50000000)
    //    private val driveCommandWriter = DownsampledWriter("DRIVE_COMMAND", 50000000)
    //    private val tankCommandWriter = DownsampledWriter("TANK_COMMAND", 50000000)

    init {
        leftMotors.forEach { it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE }
        rightMotors.forEach { it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE }
    }

    override fun setDrivePowers(powers: PoseVelocity2d) {
        val wheelVels = TankKinematics(2.0).inverse(PoseVelocity2dDual.constant<Time>(powers, 1))

        var maxPowerMag = 1.0
        for (power in wheelVels.all()) {
            maxPowerMag = max(maxPowerMag, power.value())
        }

        leftMotors.forEach { it.power = wheelVels.left[0] / maxPowerMag }
        rightMotors.forEach { it.power = wheelVels.right[0] / maxPowerMag }
    }

    inner class FollowTrajectoryAction(val timeTrajectory: TimeTrajectory) : Action {
        private var beginTs = -1.0

        private val xPoints: DoubleArray
        private val yPoints: DoubleArray

        init {
            val disps =
                range(
                    0.0,
                    timeTrajectory.path.length(),
                    max(2, ceil(timeTrajectory.path.length() / 2).toInt()),
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
                leftMotors.forEach { it.power = 0.0 }
                rightMotors.forEach { it.power = 0.0 }

                return false
            }

            val x = timeTrajectory.profile[t]

            val txWorldTarget = timeTrajectory.path[x.value(), 3]
            //            targetPoseWriter.write(PoseMessage(txWorldTarget.value()))

            updatePoseEstimate()

            val command =
                RamseteController(kinematics.trackWidth, params.ramseteZeta, params.ramseteBBar)
                    .compute(x, txWorldTarget, localizer.pose)
            //            driveCommandWriter.write(DriveCommandMessage(command))

            val wheelVels = kinematics.inverse(command)
            val voltage: Double = voltageSensor.voltage
            val feedforward =
                MotorFeedforward(
                    params.kS,
                    params.kV / params.inPerTick,
                    params.kA / params.inPerTick,
                )
            val leftPower = feedforward.compute(wheelVels.left) / voltage
            val rightPower = feedforward.compute(wheelVels.right) / voltage
            //            tankCommandWriter.write(TankCommandMessage(voltage, leftPower,
            // rightPower))

            leftMotors.forEach { it.power = leftPower }
            rightMotors.forEach { it.power = rightPower }

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
            drawRobot(c, txWorldTarget.value())

            c.setStroke("#3F51B5")
            drawRobot(c, localizer.pose)

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
                leftMotors.forEach { it.power = 0.0 }
                rightMotors.forEach { it.power = 0.0 }

                return false
            }

            val txWorldTarget = turn[t]
            //            targetPoseWriter.write(PoseMessage(txWorldTarget.value()))

            val robotVelRobot = updatePoseEstimate()

            val command =
                PoseVelocity2dDual(
                    constant(Vector2d(0.0, 0.0), 3),
                    txWorldTarget.heading
                        .velocity()
                        .plus(
                            params.turnGain *
                                localizer.pose.heading.minus(txWorldTarget.heading.value()) +
                                params.turnVelGain *
                                    (robotVelRobot.angVel -
                                        txWorldTarget.heading.velocity().value())
                        ),
                )
            //            driveCommandWriter.write(DriveCommandMessage(command))

            val wheelVels = kinematics.inverse(command)
            val voltage = voltageSensor.voltage
            val feedforward =
                MotorFeedforward(
                    params.kS,
                    params.kV / params.inPerTick,
                    params.kA / params.inPerTick,
                )
            val leftPower = feedforward.compute(wheelVels.left) / voltage
            val rightPower = feedforward.compute(wheelVels.right) / voltage
            //            tankCommandWriter.write(TankCommandMessage(voltage, leftPower,
            // rightPower))

            leftMotors.forEach { it.power = leftPower }
            rightMotors.forEach { it.power = rightPower }

            val c = p.fieldOverlay()
            drawPoseHistory(c)

            c.setStroke("#4CAF50")
            drawRobot(c, txWorldTarget.value())

            c.setStroke("#3F51B5")
            drawRobot(c, localizer.pose)

            c.setStroke("#7C4DFFFF")
            c.fillCircle(turn.beginPose.position.x, turn.beginPose.position.y, 2.0)

            return true
        }

        override fun preview(fieldOverlay: Canvas) {
            fieldOverlay.setStroke("#7C4DFF7A")
            fieldOverlay.fillCircle(turn.beginPose.position.x, turn.beginPose.position.y, 2.0)
        }
    }

    fun updatePoseEstimate(): PoseVelocity2d {
        val vel = localizer.update()
        poseHistory.add(localizer.pose)

        while (poseHistory.size > 100) {
            poseHistory.removeFirst()
        }

        //        estimatedPoseWriter.write(PoseMessage(localizer.getPose()))

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
