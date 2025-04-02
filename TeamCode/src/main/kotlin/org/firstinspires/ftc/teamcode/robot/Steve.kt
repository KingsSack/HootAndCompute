package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.*
import dev.kingssack.volt.robot.SimpleRobotWithMecanumDrive
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.attachment.*

/**
 * Steve is a robot for the 2024-2025 INTOTHEDEEP FTC Season.
 *
 * Steve can collect samples, deposit samples, detect objects, and detect distances.
 * Steve has a lift, claw, shoulder, wrist, intake, two distance sensors, and HuskyLens.
 *
 * @param hardwareMap for initializing hardware components
 * @param initialPose for setting the initial pose
 *
 * @property lift for lifting the claw
 * @property intake for collecting samples
 * @property claw for grabbing objects
 * @property shoulder for extending the claw
 * @property elbow for extending the claw
 * @property wrist for rotating the claw
 */
@Config
class Steve(hardwareMap: HardwareMap, initialPose: Pose2d) : SimpleRobotWithMecanumDrive(
    hardwareMap, initialPose, DriveParams(
        logoFacingDirection = logoFacingDirection,
        usbFacingDirection = usbFacingDirection,
        inPerTick = inPerTick,
        lateralInPerTick = lateralInPerTick,
        trackWidthTicks = trackWidthTicks,
        kS = kS,
        kV = kV,
        kA = kA,
        maxWheelVel = maxWheelVel,
        minProfileAccel = minProfileAccel,
        maxProfileAccel = maxProfileAccel,
        maxAngVel = maxAngVel,
        maxAngAccel = maxAngAccel,
        axialGain = axialGain,
        lateralGain = lateralGain,
        headingGain = headingGain
    )
) {
    /**
     * Params is a companion object that holds the configuration for the robot Steve.
     *
     * @property lidarLeftName the name of the left distance sensor
     * @property lidarRightName the name of the right distance sensor
     * @property huskyLensName the name of the HuskyLens
     * @property potentiometerName the name of the potentiometer
     * @property liftRightName the name of the right lift motor
     * @property liftLeftName the name of the left lift motor
     * @property tailName the name of the tail servo
     * @property intakeRightName the name of the right intake servo
     * @property intakeLeftName the name of the left intake servo
     * @property clawName the name of the claw motor
     * @property shoulderName the name of the shoulder motor
     * @property elbowName the name of the elbow motor
     * @property wristName the name of the wrist motor
     */
    companion object Params {
        @JvmField
        var lidarLeftName: String = "lidarl"
        @JvmField
        var lidarRightName: String = "lidarr"
        @JvmField
        var huskyLensName: String = "lens"
        @JvmField
        var potentiometerName: String = "pt"
        @JvmField
        var liftRightName: String = "liftr"
        @JvmField
        var liftLeftName: String = "liftl"
        @JvmField
        var tailName: String = "tl"
        @JvmField
        var intakeRightName: String = "inr"
        @JvmField
        var intakeLeftName: String = "inl"
        @JvmField
        var clawName: String = "claw"
        @JvmField
        var shoulderName: String = "sh"
        @JvmField
        var elbowName: String = "el"
        @JvmField
        var wristName: String = "wr"

        @JvmField
        var logoFacingDirection: LogoFacingDirection = LogoFacingDirection.LEFT
        @JvmField
        var usbFacingDirection: UsbFacingDirection = UsbFacingDirection.FORWARD

        @JvmField
        var inPerTick: Double = 0.0227
        @JvmField
        var lateralInPerTick: Double = 0.02
        @JvmField
        var trackWidthTicks: Double = 1297.32

        @JvmField
        var kS: Double = 0.9134
        @JvmField
        var kV: Double = 0.0043
        @JvmField
        var kA: Double = 0.001

        @JvmField
        var maxWheelVel: Double = 69.0
        @JvmField
        var minProfileAccel: Double = -30.0
        @JvmField
        var maxProfileAccel: Double = 69.0

        @JvmField
        var maxAngVel: Double = Math.PI
        @JvmField
        var maxAngAccel: Double = Math.PI

        @JvmField
        var axialGain: Double = 5.0
        @JvmField
        var lateralGain: Double = 4.0
        @JvmField
        var headingGain: Double = 3.0
    }

    // Sensors
    private val lidarLeft: DistanceSensor = hardwareMap.get(DistanceSensor::class.java, lidarLeftName)
    private val lidarRight: DistanceSensor = hardwareMap.get(DistanceSensor::class.java, lidarRightName)
    private val huskyLens: HuskyLens = hardwareMap.get(HuskyLens::class.java, huskyLensName)
    private val potentiometer: AnalogInput = hardwareMap.get(AnalogInput::class.java, potentiometerName)

    // Attachments
    val lift: Lift = Lift(hardwareMap, liftRightName, liftLeftName)
    val tail: Tail = Tail(hardwareMap, tailName)
    val intake: Intake = Intake(hardwareMap, intakeLeftName, intakeRightName)
    val claw: Claw = Claw(hardwareMap, clawName)
    val shoulder: Shoulder = Shoulder(hardwareMap, shoulderName)
    val elbow: Elbow = Elbow(hardwareMap, elbowName, potentiometer)
    val wrist: Wrist = Wrist(hardwareMap, wristName)

    init {
        attachments = listOf(lift, tail, intake, claw, shoulder, elbow, wrist)

        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION)
    }

    /**
     * Extend the arm by extending the elbow and then shoulder.
     *
     * @return action to extend the arm
     */
    fun extendArm(): Action {
        return ParallelAction(
            shoulder.extend(),
            elbow.extend(),
            wrist.goTo(Wrist.centerPosition)
        )
    }

    /**
     * Extend the arm to the submersible or basket by extending the shoulder and then elbow.
     *
     * @param elbowVoltage voltage to set the elbow to
     *
     * @return action to extend the arm to the submersible
     */
    fun extendArmTo(elbowVoltage: Double): Action {
        return ParallelAction(
            shoulder.extend(),
            elbow.goTo(1.0, elbowVoltage),
            wrist.goTo(Wrist.centerPosition)
        )
    }

    /**
     * Retract the arm by retracting the shoulder and then elbow.
     *
     * @return action to retract the arm
     */
    fun retractArm(): Action {
        return SequentialAction(
            shoulder.retract(),
            elbow.retract()
        )
    }

    /**
     * Collect a sample by extending the arm, closing the claw,
     * and retracting the arm.
     *
     * @return action to collect a sample
     *
     * @see Shoulder
     * @see Claw
     * @see Wrist
     */
    fun collectSample(): Action {
        return SequentialAction(
            claw.open(),
            wrist.goTo(Wrist.centerPosition),
            extendArm(),
            claw.close(),
            retractArm()
        )
    }

    /**
     * Deposit a sample by lifting the lift, extending the shoulder,
     * opening the claw, retracting the arm, and dropping the lift.
     *
     * @param basketHeight height of the basket
     * @return action to deposit a sample
     *
     * @see Lift
     * @see Shoulder
     * @see Claw
     * @see Wrist
     */
    fun depositSample(basketHeight: Int): Action {
        return SequentialAction(
            lift.goTo(basketHeight + 72),
            wrist.goTo(Wrist.centerPosition),
            extendArmTo(3.3),
            claw.open(),
            ParallelAction(
                retractArm(),
                lift.drop()
            )
        )
    }

    /**
     * Retrieve a specimen
     */
    fun retrieveSpecimen(): Action {
        return SequentialAction(
            extendArm(),
            claw.close(),
            retractArm()
        )
    }

    /**
     * Deposit a specimen by extending the arm, raising the lift,
     * extending the arm, opening the claw, and retracting the arm.
     *
     * @param barHeight height of the submersible bar
     * @return action to deposit a specimen
     *
     * @see Shoulder
     * @see Elbow
     * @see Wrist
     * @see Lift
     * @see Claw
     */
    fun depositSpecimen(barHeight: Int): Action {
        return SequentialAction(
            extendArmTo(1.6),
            lift.goTo(barHeight),
            extendArmTo(1.32),
            lift.goTo(barHeight - 300),
            ParallelAction(
                claw.open(),
                lift.drop(),
                retractArm()
            )
            // InstantAction { lift.reset() }
        )
    }

    /**
     * Get detected objects from HuskyLens.
     *
     * @param telemetry for logging
     * @return array of detected objects
     *
     * @see HuskyLens
     * @see HuskyLens.Block
     */
    fun getDetectedObjects(telemetry: Telemetry): Array<out HuskyLens.Block>? {
        // Get objects
        val blocks = huskyLens.blocks()
        telemetry.addData("Block count", blocks.size)
        for (block in blocks) {
            telemetry.addData("Block", block.toString())
        }
        return blocks
    }

    /**
     * Get distance to an obstacle from distance sensor.
     *
     * @param telemetry for logging
     * @return distance to an obstacle
     *
     * @see DistanceSensor
     */
    fun getDistanceToObstacle(telemetry: Telemetry): Double {
        // Get distances
        val distanceLeft = lidarLeft.getDistance(DistanceUnit.MM)
        val distanceRight = lidarRight.getDistance(DistanceUnit.MM)
        val averageDistance = (distanceLeft + distanceRight) / 2

        telemetry.addData("Range left", "%.01f mm".format(distanceLeft))
        telemetry.addData("Range right", "%.01f mm".format(distanceRight))
        telemetry.addData("Average range", "%.01f mm".format(averageDistance))

        return averageDistance
    }
}