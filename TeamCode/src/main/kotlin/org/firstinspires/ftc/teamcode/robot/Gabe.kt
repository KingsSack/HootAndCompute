package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.hardware.*
import dev.kingssack.volt.attachment.drivetrain.MecanumDrivetrain
import dev.kingssack.volt.core.voltAction
import dev.kingssack.volt.robot.RobotWithMecanumDrivetrain
import kotlin.math.abs
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Launcher
import org.firstinspires.ftc.teamcode.attachment.Storage
import dev.kingssack.volt.opmode.autonomous.AllianceColor

/**
 * Gabe is a robot for the 2025-2026 DECODE FTC Season.
 *
 * @param hardwareMap for initializing hardware components
 */
@Config
abstract class Gabe<T : MecanumDrivetrain>(
    hardwareMap: HardwareMap,
    drivetrain: T,
    allianceColor: AllianceColor = AllianceColor.BLUE,
) : RobotWithMecanumDrivetrain<T>(hardwareMap, drivetrain) {
    companion object {
        private const val LAUNCHER_LEFT_P = 54.7
        private const val LAUNCHER_LEFT_I = 0.21
        private const val LAUNCHER_LEFT_D = 0.0
        private const val LAUNCHER_LEFT_F = 13.37
        private const val LAUNCHER_RIGHT_P = 54.8
        private const val LAUNCHER_RIGHT_I = 0.23
        private const val LAUNCHER_RIGHT_D = 0.0
        private const val LAUNCHER_RIGHT_F = 13.52
        private const val LAUNCHER_MAX_VELOCITY = 2800.0
        private const val LAUNCHER_TARGET_VELOCITY = 1500.0
    }

    fun setRGBPatternToAllianceColor(allianceColor: AllianceColor) {
        if (allianceColor == AllianceColor.BLUE)
            rgb.setPattern(RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_BLUE)
        else rgb.setPattern(RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED)
    }

    var allianceColor: AllianceColor = allianceColor
        set(value) {
            field = value
            setRGBPatternToAllianceColor(value)
        }

    // Hardware
    val rgb by ledDriver("rgb")

    private val huskyLens by huskyLens("lens")
    private val distanceSensor by distanceSensor("l")

    private val leftLauncherMotor by motorEx("fll")
    private val rightLauncherMotor by motorEx("flr")

    private val storageServo by servo("ss")

    // Attachments
    val launcher = attachment {
        Launcher(
            leftLauncherMotor,
            rightLauncherMotor,
            distanceSensor,
            PIDFCoefficients(LAUNCHER_LEFT_P, LAUNCHER_LEFT_I, LAUNCHER_LEFT_D, LAUNCHER_LEFT_F),
            PIDFCoefficients(
                LAUNCHER_RIGHT_P,
                LAUNCHER_RIGHT_I,
                LAUNCHER_RIGHT_D,
                LAUNCHER_RIGHT_F,
            ),
            LAUNCHER_MAX_VELOCITY,
            LAUNCHER_TARGET_VELOCITY,
        )
    }
    val storage = attachment { Storage(storageServo) }

    init {
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION)
    }

    /**
     * Fire a specified number of artifacts.
     *
     * @param amount the number of artifacts to fire
     */
    fun fire(amount: Int) = voltAction {
        repeat(amount) {
            +launcher.enable()
            +storage.release()
            wait(0.6)
            +storage.close()
            wait(0.4)
        }
        +launcher.disable()
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
    fun pointTowardsAprilTag() = Action {
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

    context(telemetry: Telemetry)
    override fun update(): Unit =
        with(telemetry) {
            super.update()
            addData("Alliance Color", allianceColor)
        }
}
