package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.robotcore.hardware.*
import dev.kingssack.volt.attachment.drivetrain.MecanumDrivetrain
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.core.VoltBuilderDsl
import dev.kingssack.volt.robot.RobotWithMecanumDrivetrain
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Launcher
import org.firstinspires.ftc.teamcode.attachment.Storage
import org.firstinspires.ftc.teamcode.util.AllianceColor
import kotlin.math.abs

/**
 * Gabe is a robot for the 2025-2026 DECODE FTC Season.
 *
 * @param hardwareMap for initializing hardware components
 */
@Config
abstract class Gabe<T : MecanumDrivetrain>(hardwareMap: HardwareMap, drivetrain: T) :
    RobotWithMecanumDrivetrain<T>(hardwareMap, drivetrain) {
    companion object {
        private const val LAUNCHER_LEFT_P = 40.0
        private const val LAUNCHER_LEFT_I = 0.0
        private const val LAUNCHER_LEFT_D = 0.0
        private const val LAUNCHER_LEFT_F = 13.29
        private const val LAUNCHER_RIGHT_P = 40.0
        private const val LAUNCHER_RIGHT_I = 0.0
        private const val LAUNCHER_RIGHT_D = 0.0
        private const val LAUNCHER_RIGHT_F = 12.11
        private const val LAUNCHER_MAX_VELOCITY = 6000.0
        private const val LAUNCHER_TARGET_VELOCITY = 1500.0
    }

    // Hardware
    private val huskyLens by huskyLens("lens")
    private val distanceSensor by distanceSensor("l")

    private val leftLauncherMotor by motorEx("fll")
    private val rightLauncherMotor by motorEx("flr")

    private val storageServo by servo("ss")

    // Attachments
    val launcher by attachment {
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
    val storage by attachment { Storage(storageServo) }

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

    var detectedAprilTag : Boolean = false

    context(telemetry: Telemetry)
    fun pointTowardsAprilTag(allianceColor: AllianceColor): Action {
        val tagId : Int = if (allianceColor == AllianceColor.RED) {24} else {20}
        val driveVel = 1.0
        return Action {
            val detectedTag: HuskyLens.Block? = getDetectedAprilTags(tagId).firstOrNull()
            val shouldStop = abs((detectedTag?.x ?: 160) - 160) < 5
            if (shouldStop) {
                drivetrain.setDrivePowers(PoseVelocity2d(Vector2d(0.0, 0.0), 0.0))
                detectedAprilTag = detectedTag == null
            } else {
                if (detectedTag!!.x < 160) {
                    drivetrain.setDrivePowers(PoseVelocity2d(Vector2d(0.0, 0.0), driveVel))
                } else {
                    drivetrain.setDrivePowers(PoseVelocity2d(Vector2d(0.0, 0.0), -driveVel))
                }
            }
            shouldStop
        }
    }
}

@VoltBuilderDsl
inline fun <D : MecanumDrivetrain, T : Gabe<D>> VoltActionBuilder<T>.launcher(
    block: Launcher.() -> Unit
) {
    block(robot.launcher)
}

@VoltBuilderDsl
inline fun <D : MecanumDrivetrain, T : Gabe<D>> VoltActionBuilder<T>.storage(
    block: Storage.() -> Unit
) {
    block(robot.storage)
}
