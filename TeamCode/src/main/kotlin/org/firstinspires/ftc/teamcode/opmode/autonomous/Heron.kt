package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.autonomous.DualAutonomousMode
import org.firstinspires.ftc.teamcode.attachment.Classifier.ReleaseType
import org.firstinspires.ftc.teamcode.robot.Jones
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.toRadians
@Suppress("Unused")
@VoltOpModeMeta("Heron", "Competition", "Seahorse")
class Heron :
    DualAutonomousMode<Jones<MecanumDriveWithPP>>() {
    private val initialPose = sw(Pose(63.875, 8.0, 90.0.toRadians()))
    override val robot: Jones<MecanumDriveWithPP> = JonesPP(hardwareMap, initialPose)
    private val launchPose: Pose = sw(Pose(60.0, 12.0, 115.0.toRadians()))
    private val finalPose: Pose = sw(Pose(60.0, 30.0, 115.0.toRadians()))

    private val patterns =
        mapOf(
            21 to listOf(ReleaseType.GREEN, ReleaseType.PURPLE, ReleaseType.PURPLE),
            22 to listOf(ReleaseType.PURPLE, ReleaseType.GREEN, ReleaseType.PURPLE),
            23 to listOf(ReleaseType.PURPLE, ReleaseType.PURPLE, ReleaseType.GREEN),
        )

    private val defaultPattern = listOf(ReleaseType.NEXT, ReleaseType.NEXT, ReleaseType.NEXT)

    private var patternId: Int? = null

    init {
        blackboard["allianceColor"] = color

        while (opModeInInit()) {
            val tags = context(telemetry) { robot.getDetectedAprilTags() }
            patternId = tags.firstOrNull { it.id in patterns.keys }?.id
            telemetry.addData("Pattern ID", patternId ?: "None detected")
            telemetry.update()
        }

        robot.visionPortal.stopStreaming()
    }

    /**
     * Drives to the launch zone, fires artifacts according to the detected pattern, drives to, and
     * saves final pose
     */
    override fun sequence() = execute {
        with(robot) {
            parallel {
                +drivetrain.path { lineTo(launchPose) }
                +launcher.enable()
            }

            for (artifact in patterns[patternId] ?: defaultPattern) {
                +classifier.releaseArtifact(artifact)
                wait(1.5)
            }

            parallel {
                +launcher.disable()
                +drivetrain.path { lineTo(finalPose) }
            }

            instant { blackboard["endPose"] = drivetrain.pose }
        }
    }
}
