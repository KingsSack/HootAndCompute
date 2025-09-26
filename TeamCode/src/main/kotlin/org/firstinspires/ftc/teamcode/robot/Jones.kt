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

/**
 * Jones is a robot for the 2025-2026 DECODE FTC Season.
 *
 * @param hardwareMap for initializing hardware components
 * @param initialPose for setting the initial pose
 */
@Config
class Jones(hardwareMap: HardwareMap, initialPose: Pose2d) : SimpleRobotWithMecanumDrive(
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
     * Params is a companion object that holds the configuration for the robot.
     *
     * @property lidarLeftName the name of the left distance sensor
     * @property lidarRightName the name of the right distance sensor
     * @property huskyLensName the name of the HuskyLens
     */
    companion object Params {
        @JvmField
        var lidarLeftName: String = "lidarl"
        @JvmField
        var lidarRightName: String = "lidarr"
        @JvmField
        var huskyLensName: String = "lens"

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
        var maxWheelVel: Double = 60.0
        @JvmField
        var minProfileAccel: Double = -30.0
        @JvmField
        var maxProfileAccel: Double = 60.0

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

    // Attachments

    init {
        attachments = listOf()

        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION)
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