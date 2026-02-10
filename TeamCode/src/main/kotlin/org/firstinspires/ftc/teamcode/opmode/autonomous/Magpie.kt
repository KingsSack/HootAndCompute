package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.autonomous.DualAutonomousMode
import org.firstinspires.ftc.teamcode.attachment.Classifier.ReleaseType
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.toRadians

@Suppress("unused")
@VoltOpModeMeta("Magpie", "Competition", "Seahorse")
class Magpie :
    DualAutonomousMode<JonesPP>() {
    override val robot: JonesPP = JonesPP(hardwareMap, Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians()).mirror())
    private val finalPose: Pose = sw(Pose(56.0, 36.0, 115.0.toRadians()))
    companion object {
        @JvmField var INITIAL_X: Double = 56.0
        @JvmField var INITIAL_Y: Double = 8.0
        @JvmField var INITIAL_HEADING: Double = 90.0
    }
    private val initialPose: Pose = sw(Pose(56.0, 8.0, 115.0.toRadians()))

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

    /** Fires artifacts according to the detected pattern, drives to, and saves final pose */
    override fun sequence() = execute {
        with(robot) {
            +launcher.enable()

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
