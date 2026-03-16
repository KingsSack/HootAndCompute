package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.autonomous.DualAutonomousMode
import dev.kingssack.volt.util.Event.AutonomousEvent.Start
import org.firstinspires.ftc.teamcode.attachment.Classifier.ReleaseType
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.toRadians

@VoltOpModeMeta("Heron", "Competition", "Seahorse")
class Heron : DualAutonomousMode<JonesPP>() {
    override val robot = JonesPP(hardwareMap, sw(Pose(63.875, 8.0, 90.0.toRadians())))

    private val launchPose = sw(Pose(60.0, 12.0, 115.0.toRadians()))
    private val finalPose = sw(Pose(60.0, 30.0, 115.0.toRadians()))

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

    override fun defineEvents() {
        // Drives to the launch zone, fires artifacts according to the detected pattern, and leaves
        Start then
            {
                parallel {
                    +robot.drivetrain.path { lineTo(launchPose) }
                    +robot.launcher.enable()
                }

                for (artifact in patterns[patternId] ?: defaultPattern) {
                    +robot.classifier.releaseArtifact(artifact)
                    wait(1.5)
                }

                parallel {
                    +robot.launcher.disable()
                    +robot.drivetrain.path { lineTo(finalPose) }
                }

                instant { blackboard["endPose"] = robot.drivetrain.pose }
            }
    }
}
