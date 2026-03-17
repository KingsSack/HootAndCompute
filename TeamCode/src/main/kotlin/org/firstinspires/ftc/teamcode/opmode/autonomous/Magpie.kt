package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.autonomous.DualAutonomousMode
import dev.kingssack.volt.util.Event.AutonomousEvent.Start
import org.firstinspires.ftc.teamcode.attachment.Classifier.ReleaseType
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.toRadians

@VoltOpModeMeta("Magpie", "Competition", "Seahorse")
class Magpie : DualAutonomousMode<JonesPP>() {
    companion object {
        @JvmField var INITIAL_X: Double = 56.0
        @JvmField var INITIAL_Y: Double = 8.0
        @JvmField var INITIAL_HEADING: Double = 90.0
    }

    override val robot =
        JonesPP(hardwareMap, sw(Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians())))

    private val finalPose = sw(Pose(56.0, 36.0, 115.0.toRadians()))

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
        // Fires artifacts according to the detected pattern and leaves
        Start then
                {
                    +robot.launcher.enable()

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
