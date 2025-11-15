package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.*
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.*
import dev.kingssack.volt.drivetrain.MecanumDriveWithRR
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Launcher
import org.firstinspires.ftc.teamcode.attachment.Storage

/**
 * Gabe is a robot for the 2025-2026 DECODE FTC Season.
 *
 * @param hardwareMap for initializing hardware components
 * @param initialPose for setting the initial pose
 */
@Config
class Gabe(hardwareMap: HardwareMap, initialPose: Pose2d = Pose2d(Vector2d(0.0, 0.0), 0.0)) :
    Robot(hardwareMap) {
    companion object {
        @JvmField var logoFacingDirection: LogoFacingDirection = LogoFacingDirection.LEFT
        @JvmField var usbFacingDirection: UsbFacingDirection = UsbFacingDirection.FORWARD

        @JvmField var inPerTick: Double = 0.0227
        @JvmField var lateralInPerTick: Double = 0.02
        @JvmField var trackWidthTicks: Double = 1297.32

        @JvmField var kS: Double = 0.9134
        @JvmField var kV: Double = 0.0043
        @JvmField var kA: Double = 0.001

        @JvmField var maxWheelVel: Double = 60.0
        @JvmField var minProfileAccel: Double = -30.0
        @JvmField var maxProfileAccel: Double = 60.0

        @JvmField var maxAngVel: Double = Math.PI
        @JvmField var maxAngAccel: Double = Math.PI

        @JvmField var axialGain: Double = 5.0
        @JvmField var lateralGain: Double = 4.0
        @JvmField var headingGain: Double = 3.0
    }

    // Drivetrain
    val drivetrain =
        MecanumDriveWithRR(
            hardwareMap,
            initialPose,
            MecanumDriveWithRR.DriveParams(
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
                headingGain = headingGain,
            ),
        )

    // Hardware
    private val huskyLens by huskyLens("lens")

    private val leftLauncherMotor by motor("fll")
    private val rightLauncherMotor by motor("flr")

    private val storageServo by servo("ss")

    // Attachments
    val launcher = Launcher(leftLauncherMotor, rightLauncherMotor)
    val storage = Storage(storageServo)

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

    context(telemetry: Telemetry)
    override fun update() {
        drivetrain.update()
        super.update()
    }
}
