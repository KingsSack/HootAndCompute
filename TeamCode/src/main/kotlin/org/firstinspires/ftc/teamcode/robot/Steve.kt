package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.SequentialAction
import dev.kingssack.volt.robot.SimpleRobotWithMecanumDrive
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.attachment.*

/**
 * Steve is a robot for the 2024-2025 INTO THE DEEP FTC Season.
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
        var maxWheelVel: Double = 50.0
        @JvmField
        var minProfileAccel: Double = -30.0
        @JvmField
        var maxProfileAccel: Double = 50.0

        @JvmField
        var maxAngVel: Double = Math.PI
        @JvmField
        var maxAngAccel: Double = Math.PI

        @JvmField
        var axialGain: Double = 5.0
        @JvmField
        var lateralGain: Double = 4.0
        @JvmField
        var headingGain: Double = 1.0
    }

    // Sensors
    private val lidarLeft: DistanceSensor = hardwareMap.get(DistanceSensor::class.java, lidarLeftName)
    private val lidarRight: DistanceSensor = hardwareMap.get(DistanceSensor::class.java, lidarRightName)
    private val huskyLens: HuskyLens = hardwareMap.get(HuskyLens::class.java, huskyLensName)
    private val potentiometer: AnalogInput = hardwareMap.get(AnalogInput::class.java, potentiometerName)

    // Attachments
    val lift: Lift = Lift(hardwareMap, liftRightName, liftLeftName)
    val intake: Intake = Intake(hardwareMap, intakeLeftName, intakeRightName)
    val claw: Claw = Claw(hardwareMap, clawName)
    val shoulder: Shoulder = Shoulder(hardwareMap, shoulderName)
    val elbow: Elbow = Elbow(hardwareMap, elbowName, potentiometer)
    val wrist: Wrist = Wrist(hardwareMap, wristName)

    init {
        attachments = listOf(lift, intake, claw, shoulder, elbow, wrist)

        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION)
    }

    /**
     * Extend the arm by extending the elbow and then shoulder.
     *
     * @return action to extend the arm
     */
    fun extendArm(): Action {
        return SequentialAction(
            shoulder.extend(),
            elbow.extend()
        )
    }

    /**
     * Extend the arm to the submersible by extending the shoulder and then elbow.
     *
     * @return action to extend the arm to the submersible
     */
    fun extendArmToSubmersible(): Action {
        return SequentialAction(
            shoulder.extend(),
            elbow.goTo(1.0, 0.68)
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
            extendArm(),
            wrist.goTo(Wrist.centerPosition),
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
            lift.goTo(basketHeight + 50),
            shoulder.extend(),
            wrist.goTo(Wrist.centerPosition),
            claw.open(),
            retractArm(),
            lift.drop()
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