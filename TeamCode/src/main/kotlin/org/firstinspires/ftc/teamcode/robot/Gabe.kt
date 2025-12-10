package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.*
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.robotcore.hardware.*
import dev.kingssack.volt.attachment.drivetrain.MecanumDrivetrain
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.core.VoltBuilderDsl
import dev.kingssack.volt.core.voltAction
import dev.kingssack.volt.robot.RobotWithMecanumDrivetrain
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Launcher
import org.firstinspires.ftc.teamcode.attachment.Storage

/**
 * Gabe is a robot for the 2025-2026 DECODE FTC Season.
 *
 * @param hardwareMap for initializing hardware components
 */
@Config
abstract class Gabe<T : MecanumDrivetrain>(hardwareMap: HardwareMap, drivetrain: T) :
    RobotWithMecanumDrivetrain<T>(hardwareMap, drivetrain) {
    // Hardware
    private val huskyLens by huskyLens("lens")

    private val leftLauncherMotor by motor("fll")
    private val rightLauncherMotor by motor("flr")

    private val storageServo by servo("ss")

    // Attachments
    val launcher by attachment { Launcher(leftLauncherMotor, rightLauncherMotor) }
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
