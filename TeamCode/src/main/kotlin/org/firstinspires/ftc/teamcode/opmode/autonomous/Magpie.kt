package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.attachment.Classifier.ReleaseType
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.toRadians

@Config
@Autonomous(name = "Magpie", group = "Competition", preselectTeleOp = "Seahorse")
class Magpie :
    AutonomousMode<JonesPP>({
        JonesPP(it, Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians()))
    }) {
    companion object {
        @JvmField var INITIAL_X: Double = 56.0
        @JvmField var INITIAL_Y: Double = 8.0
        @JvmField var INITIAL_HEADING: Double = 90.0
    }

    private val patterns =
        mapOf(
            21 to listOf(ReleaseType.GREEN, ReleaseType.PURPLE, ReleaseType.PURPLE),
            22 to listOf(ReleaseType.PURPLE, ReleaseType.GREEN, ReleaseType.PURPLE),
            23 to listOf(ReleaseType.PURPLE, ReleaseType.PURPLE, ReleaseType.GREEN),
        )

    private val defaultPattern = listOf(ReleaseType.NEXT, ReleaseType.NEXT, ReleaseType.NEXT)

    override fun sequence() = execute {
        with(robot) {
            val tags = context(telemetry) { getDetectedAprilTags() }
            val patternId = tags.firstOrNull { it.id in patterns.keys }?.id

            +drivetrain.path { lineTo(Pose(62.0, 24.0, 115.0.toRadians())) }

            +launcher.enable()

            telemetry.addData("Pattern ID", patternId ?: "None detected")

            for (artifact in patterns[patternId] ?: defaultPattern) {
                +classifier.releaseArtifact(artifact)
                wait(0.5)
            }

            +launcher.disable()
        }
    }
}
