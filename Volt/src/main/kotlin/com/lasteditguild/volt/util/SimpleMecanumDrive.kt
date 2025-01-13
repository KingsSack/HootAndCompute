package com.lasteditguild.volt.util

import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.ftc.*
import com.acmerobotics.roadrunner.ftc.FlightRecorder
import com.lasteditguild.volt.messages.DriveCommandMessage
import com.lasteditguild.volt.messages.MecanumCommandMessage
import com.lasteditguild.volt.messages.MecanumLocalizerInputsMessage
import com.lasteditguild.volt.messages.PoseMessage
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import java.util.LinkedList
import kotlin.math.ceil
import kotlin.math.max

@Config
open class SimpleMecanumDrive(hardwareMap: HardwareMap, var pose: Pose2d) {
    companion object DriveParams {
        // IMU orientation
        var logoFacingDirection: LogoFacingDirection = LogoFacingDirection.LEFT
        var usbFacingDirection: UsbFacingDirection = UsbFacingDirection.FORWARD

        // drive model parameters
        @JvmField
        var inPerTick: Double = 0.0227
        @JvmField
        var lateralInPerTick: Double = 0.02
        @JvmField
        var trackWidthTicks: Double = 1297.32

        // feedforward parameters (in tick units)
        @JvmField
        var kS: Double = 0.9134 // Represents the static force
        @JvmField
        var kV: Double = 0.0043 // Represents the linear velocity
        @JvmField
        var kA: Double = 0.001 // Represents the acceleration

        // path profile parameters (in inches)
        @JvmField
        var maxWheelVel: Double = 60.0
        @JvmField
        var minProfileAccel: Double = -30.0
        @JvmField
        var maxProfileAccel: Double = 60.0

        // turn profile parameters (in radians)
        @JvmField
        var maxAngVel: Double = Math.PI // shared with path
        @JvmField
        var maxAngAccel: Double = Math.PI

        // path controller gains
        @JvmField
        var axialGain: Double = 5.0
        @JvmField
        var lateralGain: Double = 4.0
        @JvmField
        var headingGain: Double = 1.0 // shared with turn

        @JvmField
        var axialVelGain: Double = 0.0
        @JvmField
        var lateralVelGain: Double = 0.0
        @JvmField
        var headingVelGain: Double = 0.0 // shared with turn
    }

    val kinematics: MecanumKinematics = MecanumKinematics(
        inPerTick * trackWidthTicks, inPerTick / lateralInPerTick
    )

    private val defaultTurnConstraints: TurnConstraints = TurnConstraints(
        maxAngVel, -maxAngAccel, maxAngAccel
    )
    private val defaultVelConstraint: VelConstraint = MinVelConstraint(
        listOf(
            kinematics.WheelVelConstraint(maxWheelVel),
            AngularVelConstraint(maxAngVel)
        )
    )
    private val defaultAccelConstraint: AccelConstraint = ProfileAccelConstraint(minProfileAccel, maxProfileAccel)

    val leftFront: DcMotorEx
    val leftBack: DcMotorEx
    val rightBack: DcMotorEx
    val rightFront: DcMotorEx

    val voltageSensor: VoltageSensor

    val lazyImu: LazyImu

    val localizer: DriveLocalizer

    private val poseHistory = LinkedList<Pose2d>()

    private val estimatedPoseWriter = DownsampledWriter("ESTIMATED_POSE", 50000000)
    private val targetPoseWriter = DownsampledWriter("TARGET_POSE", 50000000)
    private val driveCommandWriter = DownsampledWriter("DRIVE_COMMAND", 50000000)
    private val mecanumCommandWriter = DownsampledWriter("MECANUM_COMMAND", 50000000)

    inner class DriveLocalizer : Localizer {
        val leftFront: Encoder = OverflowEncoder(RawEncoder(this@SimpleMecanumDrive.leftFront))
        val leftBack: Encoder = OverflowEncoder(RawEncoder(this@SimpleMecanumDrive.leftBack))
        val rightBack: Encoder = OverflowEncoder(RawEncoder(this@SimpleMecanumDrive.rightBack))
        val rightFront: Encoder = OverflowEncoder(RawEncoder(this@SimpleMecanumDrive.rightFront))
        private val imu: IMU = lazyImu.get()

        private var lastLeftFrontPos = 0
        private var lastLeftBackPos = 0
        private var lastRightBackPos = 0
        private var lastRightFrontPos = 0
        private var lastHeading: Rotation2d? = null
        private var initialized = false

        init {
            // Set motor directions
            leftFront.direction = DcMotorSimple.Direction.FORWARD
            leftBack.direction = DcMotorSimple.Direction.FORWARD
            rightBack.direction = DcMotorSimple.Direction.REVERSE
            rightFront.direction = DcMotorSimple.Direction.REVERSE
        }

        override fun update(): Twist2dDual<Time> {
            val leftFrontPosVel = leftFront.getPositionAndVelocity()
            val leftBackPosVel = leftBack.getPositionAndVelocity()
            val rightBackPosVel = rightBack.getPositionAndVelocity()
            val rightFrontPosVel = rightFront.getPositionAndVelocity()

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
                    ).times(inPerTick),
                    DualNum<Time>(
                        doubleArrayOf(
                            (leftBackPosVel.position - lastLeftBackPos).toDouble(),
                            leftBackPosVel.velocity.toDouble(),
                        )
                    ).times(inPerTick),
                    DualNum<Time>(
                        doubleArrayOf(
                            (rightBackPosVel.position - lastRightBackPos).toDouble(),
                            rightBackPosVel.velocity.toDouble(),
                        )
                    ).times(inPerTick),
                    DualNum<Time>(
                        doubleArrayOf(
                            (rightFrontPosVel.position - lastRightFrontPos).toDouble(),
                            rightFrontPosVel.velocity.toDouble(),
                        )
                    ).times(inPerTick)
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

        leftFront = hardwareMap.get(DcMotorEx::class.java, "lf")
        leftBack = hardwareMap.get(DcMotorEx::class.java, "lr")
        rightBack = hardwareMap.get(DcMotorEx::class.java, "rr")
        rightFront = hardwareMap.get(DcMotorEx::class.java, "rf")

        leftFront.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        leftBack.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rightBack.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rightFront.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set motor directions
        leftFront.direction = DcMotorSimple.Direction.FORWARD
        leftBack.direction = DcMotorSimple.Direction.FORWARD
        rightBack.direction = DcMotorSimple.Direction.REVERSE
        rightFront.direction = DcMotorSimple.Direction.REVERSE

        lazyImu = LazyImu(
            hardwareMap, "imu", RevHubOrientationOnRobot(
                logoFacingDirection, usbFacingDirection
            )
        )

        voltageSensor = hardwareMap.voltageSensor.iterator().next()

        localizer = DriveLocalizer()

        // FlightRecorder.write("MECANUM_PARAMS", DriveParams)
    }

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
                axialGain, lateralGain, headingGain,
                axialVelGain, lateralVelGain, headingVelGain
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
                kS,
                kV / inPerTick,
                kA / inPerTick
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
                axialGain, lateralGain, headingGain,
                axialVelGain, lateralVelGain, headingVelGain
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
                kS,
                kV / inPerTick,
                kA / inPerTick
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

    fun updatePoseEstimate(): PoseVelocity2d {
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

    fun actionBuilder(beginPose: Pose2d): TrajectoryActionBuilder {
        return TrajectoryActionBuilder(
            { turn: TimeTurn -> TurnAction(turn) },
            { t: TimeTrajectory -> FollowTrajectoryAction(t) },
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
}