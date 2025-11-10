package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.*
import com.pedropathing.geometry.Pose
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.robotcore.hardware.*
import dev.kingssack.volt.drivetrain.SimpleMecanumDriveWithPP
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.attachment.Launcher
import org.firstinspires.ftc.teamcode.pp.Constants

/**
 * Jones is a robot for the 2025-2026 DECODE FTC Season.
 *
 * @param hardwareMap for initializing hardware components
 * @param initialPose for setting the initial pose
 */
@Config
class Jones(hardwareMap: HardwareMap, initialPose: Pose = Pose()) : Robot(hardwareMap) {
    companion object {
        @JvmField var lidarLeftName: String = "lidarl"
        @JvmField var lidarRightName: String = "lidarr"
        @JvmField var huskyLensName: String = "lens"

        @JvmField var leftLauncherMotorName: String = "ll"
        @JvmField var rightLauncherMotorName: String = "rl"
    }

    // Drivetrain
    val drivetrain =
        SimpleMecanumDriveWithPP(
            hardwareMap,
            Constants.followerConstants,
            Constants.localizerConstants,
            Constants.pathConstraints,
            Constants.driveConstants,
            initialPose,
        )

    // Hardware
    private val lidarLeft by distanceSensor(lidarLeftName)
    private val lidarRight by distanceSensor(lidarRightName)
    private val huskyLens by huskyLens(huskyLensName)

    private val leftLauncherMotor by motor(leftLauncherMotorName)
    private val rightLauncherMotor by motor(rightLauncherMotorName)

    // Attachments
    val launcher = Launcher(leftLauncherMotor, rightLauncherMotor)

    init {
        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION)
    }

    /**
     * Get detected AprilTags from HuskyLens.
     *
     * @return array of detected AprilTags
     * @see HuskyLens
     * @see HuskyLens.Block
     */
    context(telemetry: Telemetry)
    fun getDetectedAprilTags(id: Int? = null): Array<out HuskyLens.Block> {
        // Get AprilTags
        val blocks = huskyLens.blocks()
        telemetry.addData("Block count", blocks.size)

        // If an id is provided, filter to matching blocks; otherwise return all blocks.
        val result: Array<out HuskyLens.Block> =
            if (id == null) {
                blocks
            } else {
                blocks.filter { it.id == id }.toTypedArray()
            }

        // Log each block in the result
        for (block in result) {
            telemetry.addData("Block", block.toString())
        }

        // Also log the filtered count (useful when id was provided)
        telemetry.addData("Filtered count", result.size)

        return result
    }

    /**
     * Get distance to an obstacle from distance sensor.
     *
     * @return distance to an obstacle
     * @see DistanceSensor
     */
    context(telemetry: Telemetry)
    fun getDistanceToObstacle(): Double {
        // Get distances
        val distanceLeft = lidarLeft.getDistance(DistanceUnit.MM)
        val distanceRight = lidarRight.getDistance(DistanceUnit.MM)
        val averageDistance = (distanceLeft + distanceRight) / 2

        telemetry.addData("Range left", "%.01f mm".format(distanceLeft))
        telemetry.addData("Range right", "%.01f mm".format(distanceRight))
        telemetry.addData("Average range", "%.01f mm".format(averageDistance))

        return averageDistance
    }

    context(telemetry: Telemetry)
    override fun update() {
        drivetrain.update()
        super.update()
    }
}
