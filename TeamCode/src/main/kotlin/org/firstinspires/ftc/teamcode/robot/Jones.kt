package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.robotcore.hardware.*
import dev.kingssack.volt.attachment.drivetrain.MecanumDrivetrain
import dev.kingssack.volt.robot.RobotWithMecanumDrivetrain
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.attachment.Classifier
import org.firstinspires.ftc.teamcode.attachment.Launcher
import dev.kingssack.volt.opmode.autonomous.AllianceColor
import org.firstinspires.ftc.teamcode.attachment.Pusher
import kotlin.math.abs
/**
 * Jones is a robot for the 2025-2026 DECODE FTC Season.
 *
 * @param hardwareMap for initializing hardware components
 */
@Config
abstract class Jones<T : MecanumDrivetrain>(hardwareMap: HardwareMap, drivetrain: T) :
    RobotWithMecanumDrivetrain<T>(hardwareMap, drivetrain) {
    companion object {
        @JvmField var lidarLeftName: String = "lsl"
        @JvmField var lidarRightName: String = "lsr"
        @JvmField var huskyLensName: String = "lens"

        @JvmField var launcherLeftP: Double = 24.0
        @JvmField var launcherLeftI: Double = 0.1
        @JvmField var launcherLeftD: Double = 0.0
        @JvmField var launcherLeftF: Double = 14.3
        @JvmField var launcherRightP: Double = 24.0
        @JvmField var launcherRightI: Double = 0.1
        @JvmField var launcherRightD: Double = 0.0
        @JvmField var launcherRightF: Double = 13.4
        @JvmField var launcherMaxVelocity: Double = 2800.0
        @JvmField var launcherTargetVelocity: Double = 1500.0
        @JvmField var launcherMediumVelocity: Double = 1400.0
        @JvmField var launcherLowVelocity: Double = 1100.0
    }

    // Hardware
    private val lidarLeft by distanceSensor(lidarLeftName)
    private val lidarRight by distanceSensor(lidarRightName)
    private val huskyLens by huskyLens(huskyLensName)

    private val gateServo by servo("gs")
    private val classifierServo by servo("cs")
    private val classifierSensor1 by colorSensor("cs1")
    private val classifierSensor2 by colorSensor("cs2")
    private val classifierSensor3 by colorSensor("cs3")

    private val pusherServo by servo("ps")

    private val leftLauncherMotor by motorEx("fll")
    private val rightLauncherMotor by motorEx("flr")

    // Attachments
    val launcher = attachment {
        Launcher(
            leftLauncherMotor,
            rightLauncherMotor,
            lidarLeft,
            PIDFCoefficients(launcherLeftP, launcherLeftI, launcherLeftD, launcherLeftF),
            PIDFCoefficients(launcherRightP, launcherRightI, launcherRightD, launcherRightF),
            launcherMaxVelocity,
            launcherTargetVelocity,
        )
    }

    val classifier = attachment {
        Classifier(
            gateServo,
            classifierServo,
            classifierSensor1,
            classifierSensor2,
            classifierSensor3,
        )
    }

    val pusher = attachment { Pusher(pusherServo) }

    init {
        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION)
    }

    /**
     * Get detected AprilTags from HuskyLens.
     *
     * @param id optional ID to filter detected tags; if null, returns all detected tags
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

    var detectedAprilTag: Boolean = false

    context(telemetry: Telemetry)
    fun pointTowardsAprilTag(allianceColor: AllianceColor) = Action {
        val targetId = if (allianceColor == AllianceColor.RED) 24 else 20
        val detectedTag = getDetectedAprilTags(targetId).firstOrNull()

        if (detectedTag == null) {
            drivetrain.setDrivePowers(PoseVelocity2d(Vector2d(0.0, 0.0), 0.0))
            false
        } else {
            val error = detectedTag.x - 160
            val tolerance = 5

            if (abs(error) < tolerance) {
                drivetrain.setDrivePowers(PoseVelocity2d(Vector2d(0.0, 0.0), 0.0))
                detectedAprilTag = true
                false
            } else {
                val turnPower = (error / 160.0).coerceIn(-0.5, 0.5)
                drivetrain.setDrivePowers(PoseVelocity2d(Vector2d(0.0, 0.0), -turnPower))
                true
            }
        }
    }

    /**
     * Get distance to an obstacle from the distance sensor.
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

        with(telemetry) {
            addData("Left Range", "%.01f mm".format(distanceLeft))
            addData("Right Range", "%.01f mm".format(distanceRight))
            addData("Average Range", "%.01f mm".format(averageDistance))
        }

        return averageDistance
    }
}
