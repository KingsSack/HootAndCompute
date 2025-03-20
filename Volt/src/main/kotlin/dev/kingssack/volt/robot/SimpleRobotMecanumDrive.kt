package dev.kingssack.volt.robot

import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.ftc.*
import dev.kingssack.volt.util.Drawing
import dev.kingssack.volt.util.Localizer
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.*
import dev.kingssack.volt.messages.DriveCommandMessage
import dev.kingssack.volt.messages.MecanumCommandMessage
import dev.kingssack.volt.messages.MecanumLocalizerInputsMessage
import dev.kingssack.volt.messages.PoseMessage
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

/**
 * Represents a robot with attachments and a mecanum drivetrain.
 *
 * @param hardwareMap the hardware map
 * @param pose the initial pose of the robot
 */
open class SimpleRobotWithMecanumDrive(
    hardwareMap: HardwareMap,
    var pose: Pose2d,
    private val params: DriveParams = DriveParams()
) : Robot() {
    /**
     * Parameters for the robot's mecanum drive.
     *
     * @property logoFacingDirection the direction the Control Hub's logo is facing
     * @property usbFacingDirection the direction the Control Hub's USB port is facing
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
        val headingVelGain: Double = 0.0
    )

    private val kinematics: MecanumKinematics = MecanumKinematics(
        params.inPerTick * params.trackWidthTicks, params.inPerTick / params.lateralInPerTick
    )

    private val defaultTurnConstraints: TurnConstraints = TurnConstraints(
        params.maxAngVel, -params.maxAngAccel, params.maxAngAccel
    )
    private val defaultVelConstraint: VelConstraint = MinVelConstraint(
        listOf(
            kinematics.WheelVelConstraint(params.maxWheelVel),
            AngularVelConstraint(params.maxAngVel)
        )
    )
    private val defaultAccelConstraint: AccelConstraint = ProfileAccelConstraint(params.minProfileAccel, params.maxProfileAccel)

    val leftFront: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "lf")
    val leftBack: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "lr")
    val rightBack: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "rr")
    val rightFront: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "rf")
    private val driveMotors = listOf(leftFront, leftBack, rightBack, rightFront)

    val voltageSensor: VoltageSensor = hardwareMap.voltageSensor.iterator().next()

    val lazyImu = LazyImu(
        hardwareMap, "imu", RevHubOrientationOnRobot(
            params.logoFacingDirection, params.usbFacingDirection
        )
    )

    val localizer = DriveLocalizer()

    private val poseHistory = LinkedList<Pose2d>()

    private val estimatedPoseWriter = DownsampledWriter("ESTIMATED_POSE", 50000000)
    private val targetPoseWriter = DownsampledWriter("TARGET_POSE", 50000000)
    private val driveCommandWriter = DownsampledWriter("DRIVE_COMMAND", 50000000)
    private val mecanumCommandWriter = DownsampledWriter("MECANUM_COMMAND", 50000000)

    inner class DriveLocalizer : Localizer {
        val leftFrontEncoder: Encoder = OverflowEncoder(RawEncoder(leftFront))
        val leftBackEncoder: Encoder = OverflowEncoder(RawEncoder(leftBack))
        val rightBackEncoder: Encoder = OverflowEncoder(RawEncoder(rightBack))
        val rightFrontEncoder: Encoder = OverflowEncoder(RawEncoder(rightFront))
        private val imu: IMU = lazyImu.get()

        private var lastLeftFrontPos = 0
        private var lastLeftBackPos = 0
        private var lastRightBackPos = 0
        private var lastRightFrontPos = 0
        private var lastHeading: Rotation2d? = null
        private var initialized = false

        init {
            // Set motor directions
            leftFrontEncoder.direction = DcMotorSimple.Direction.FORWARD
            leftBackEncoder.direction = DcMotorSimple.Direction.FORWARD
            rightBackEncoder.direction = DcMotorSimple.Direction.REVERSE
            rightFrontEncoder.direction = DcMotorSimple.Direction.REVERSE
        }

        override fun update(): Twist2dDual<Time> {
            val leftFrontPosVel = leftFrontEncoder.getPositionAndVelocity()
            val leftBackPosVel = leftBackEncoder.getPositionAndVelocity()
            val rightBackPosVel = rightBackEncoder.getPositionAndVelocity()
            val rightFrontPosVel = rightFrontEncoder.getPositionAndVelocity()

            val angles = imu.robotYawPitchRollAngles

            FlightRecorder.write(
                "MECANUM_LOCALIZER_INPUTS",
                MecanumLocalizerInputsMessage(
                    leftFrontPosVel, leftBackPosVel, rightBackPosVel, rightFrontPosVel, angles
                )
            )

            val heading = Rotation2d.exp(angles.getYaw(AngleUnit.RADIANS))

            if (!initialized) {
                initialized = true

                lastLeftFrontPos = leftFrontPosVel.position
                lastLeftBackPos = leftBackPosVel.position
                lastRightBackPos = rightBackPosVel.position
                lastRightFrontPos = rightFrontPosVel.position

                lastHeading = heading

                return Twist2dDual(
                    Vector2dDual.constant(Vector2d(0.0, 0.0), 2),
                    DualNum.constant(0.0, 2)
                )
            }

            val headingDelta = heading.minus(lastHeading!!)
            val twist = kinematics.forward(
                MecanumKinematics.WheelIncrements(
                    DualNum<Time>(
                        doubleArrayOf(
                            (leftFrontPosVel.position - lastLeftFrontPos).toDouble(),
                            leftFrontPosVel.velocity.toDouble(),
                        )
                    ).times(params.inPerTick),
                    DualNum<Time>(
                        doubleArrayOf(
                            (leftBackPosVel.position - lastLeftBackPos).toDouble(),
                            leftBackPosVel.velocity.toDouble(),
                        )
                    ).times(params.inPerTick),
                    DualNum<Time>(
                        doubleArrayOf(
                            (rightBackPosVel.position - lastRightBackPos).toDouble(),
                            rightBackPosVel.velocity.toDouble(),
                        )
                    ).times(params.inPerTick),
                    DualNum<Time>(
                        doubleArrayOf(
                            (rightFrontPosVel.position - lastRightFrontPos).toDouble(),
                            rightFrontPosVel.velocity.toDouble(),
                        )
                    ).times(params.inPerTick)
                )
            )

            lastLeftFrontPos = leftFrontPosVel.position
            lastLeftBackPos = leftBackPosVel.position
            lastRightBackPos = rightBackPosVel.position
            lastRightFrontPos = rightFrontPosVel.position

            lastHeading = heading

            return Twist2dDual(
                twist.line,
                DualNum.cons(headingDelta, twist.angle.drop(1))
            )
        }
    }

    init {
        throwIfModulesAreOutdated(hardwareMap)

        for (module in hardwareMap.getAll(LynxModule::class.java)) {
            module.bulkCachingMode = LynxModule.BulkCachingMode.AUTO
        }

        driveMotors.forEach {
            it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        }

        // Set motor directions
        leftFront.direction = DcMotorSimple.Direction.FORWARD
        leftBack.direction = DcMotorSimple.Direction.FORWARD
        rightBack.direction = DcMotorSimple.Direction.REVERSE
        rightFront.direction = DcMotorSimple.Direction.REVERSE
    }

    /**
     * Set the drive powers.
     *
     * @param powers the drive powers
     *
     * @see PoseVelocity2d
     */
    fun setDrivePowers(powers: PoseVelocity2d) {
        val wheelVels = MecanumKinematics(1.0).inverse(
            PoseVelocity2dDual.constant<Time>(powers, 1)
        )

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
            val disps = range(
                0.0, timeTrajectory.path.length(),
                max(2.0, ceil(timeTrajectory.path.length() / 2).toInt().toDouble()).toInt()
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
            targetPoseWriter.write(PoseMessage(txWorldTarget.value()))

            val robotVelRobot = updatePoseEstimate()

            val command = HolonomicController(
                params.axialGain, params.lateralGain, params.headingGain,
                params.axialVelGain, params.lateralVelGain, params.headingVelGain
            )
                .compute(txWorldTarget, pose, robotVelRobot)
            driveCommandWriter.write(
                DriveCommandMessage(
                    command
                )
            )

            val wheelVels = kinematics.inverse(command)
            val voltage = voltageSensor.voltage

            val feedforward = MotorFeedforward(
                params.kS,
                params.kV / params.inPerTick,
                params.kA / params.inPerTick
            )
            val leftFrontPower = feedforward.compute(wheelVels.leftFront) / voltage
            val leftBackPower = feedforward.compute(wheelVels.leftBack) / voltage
            val rightBackPower = feedforward.compute(wheelVels.rightBack) / voltage
            val rightFrontPower = feedforward.compute(wheelVels.rightFront) / voltage
            mecanumCommandWriter.write(
                MecanumCommandMessage(
                    voltage, leftFrontPower, leftBackPower, rightBackPower, rightFrontPower
                )
            )

            leftFront.power = leftFrontPower
            leftBack.power = leftBackPower
            rightBack.power = rightBackPower
            rightFront.power = rightFrontPower

            p.put("x", pose.position.x)
            p.put("y", pose.position.y)
            p.put("heading (deg)", Math.toDegrees(pose.heading.toDouble()))

            val error = txWorldTarget.value().minusExp(pose)
            p.put("xError", error.position.x)
            p.put("yError", error.position.y)
            p.put("headingError (deg)", Math.toDegrees(error.heading.toDouble()))

            // only draw when active; only one drive action should be active at a time
            val c = p.fieldOverlay()
            drawPoseHistory(c)

            c.setStroke("#4CAF50")
            Drawing.drawRobot(c, txWorldTarget.value())

            c.setStroke("#3F51B5")
            Drawing.drawRobot(c, pose)

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
            targetPoseWriter.write(PoseMessage(txWorldTarget.value()))

            val robotVelRobot = updatePoseEstimate()

            val command = HolonomicController(
                params.axialGain, params.lateralGain, params.headingGain,
                params.axialVelGain, params.lateralVelGain, params.headingVelGain
            ).compute(txWorldTarget, pose, robotVelRobot)
            driveCommandWriter.write(DriveCommandMessage(command))

            val wheelVels = kinematics.inverse(command)
            val voltage = voltageSensor.voltage
            val feedforward = MotorFeedforward(
                params.kS,
                params.kV / params.inPerTick,
                params.kA / params.inPerTick
            )
            val leftFrontPower = feedforward.compute(wheelVels.leftFront) / voltage
            val leftBackPower = feedforward.compute(wheelVels.leftBack) / voltage
            val rightBackPower = feedforward.compute(wheelVels.rightBack) / voltage
            val rightFrontPower = feedforward.compute(wheelVels.rightFront) / voltage
            mecanumCommandWriter.write(
                MecanumCommandMessage(
                    voltage, leftFrontPower, leftBackPower, rightBackPower, rightFrontPower
                )
            )

            leftFront.power = feedforward.compute(wheelVels.leftFront) / voltage
            leftBack.power = feedforward.compute(wheelVels.leftBack) / voltage
            rightBack.power = feedforward.compute(wheelVels.rightBack) / voltage
            rightFront.power = feedforward.compute(wheelVels.rightFront) / voltage

            val c = p.fieldOverlay()
            drawPoseHistory(c)

            c.setStroke("#4CAF50")
            Drawing.drawRobot(c, txWorldTarget.value())

            c.setStroke("#3F51B5")
            Drawing.drawRobot(c, pose)

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
        val twist: Twist2dDual<Time> = localizer.update()
        pose = pose.plus(twist.value())

        poseHistory.add(pose)
        while (poseHistory.size > 100) {
            poseHistory.removeFirst()
        }

        estimatedPoseWriter.write(PoseMessage(pose))

        return twist.velocity().value()
    }

    private fun drawPoseHistory(c: Canvas) {
        val xPoints = DoubleArray(poseHistory.size)
        val yPoints = DoubleArray(poseHistory.size)

        var i = 0
        for ((position) in poseHistory) {
            xPoints[i] = position.x
            yPoints[i] = position.y

            i++
        }

        c.setStrokeWidth(1)
        c.setStroke("#3F51B5")
        c.strokePolyline(xPoints, yPoints)
    }

    /**
     * Create a new trajectory action builder.
     *
     * @param beginPose the current pose of the robot
     * @return the action builder
     */
    fun driveActionBuilder(beginPose: Pose2d): TrajectoryActionBuilder {
        return TrajectoryActionBuilder(
            ::TurnAction,
            ::FollowTrajectoryAction,
            TrajectoryBuilderParams(
                1e-6,
                ProfileParams(
                    0.25, 0.1, 1e-2
                )
            ),
            beginPose, 0.0,
            defaultTurnConstraints,
            defaultVelConstraint, defaultAccelConstraint
        )
    }

    /**
     * Strafe to a [target] vector.
     * @return the action
     */
    fun strafeTo(target: Vector2d): Action {
        return driveActionBuilder(pose).strafeTo(target).build()
    }

    /**
     * Strafe to a [target] vector with a linear [heading].
     * @return the action
     */
    fun strafeToLinearHeading(target: Vector2d, heading: Double): Action {
        return driveActionBuilder(pose).strafeToLinearHeading(target, heading).build()
    }

    /**
     * Turn to a [target] angle in radians.
     * @return the action
     */
    fun turnTo(target: Double): Action {
        return driveActionBuilder(pose).turnTo(target).build()
    }

    /**
     * Turn a certain number of [radians].
     * @return the action
     */
    fun turn(radians: Double): Action {
        return driveActionBuilder(pose).turn(radians).build()
    }

    /**
     * Wait for a certain number of [seconds].
     * @return the action
     */
    fun wait(seconds: Double): Action {
        return driveActionBuilder(pose).waitSeconds(seconds).build()
    }

    override fun update(telemetry: Telemetry) {
        updatePoseEstimate()
        super.update(telemetry)
    }
}