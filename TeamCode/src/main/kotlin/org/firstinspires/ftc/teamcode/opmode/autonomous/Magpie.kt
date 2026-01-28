package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.attachment.Classifier.ReleaseType
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.toRadians

@Config
@Autonomous(name = "Magpie", group = "Competition", preselectTeleOp = "Seahorse")
class Magpie :
    AutonomousMode<JonesPP>({ hardwareMap ->
        JonesPP(hardwareMap, Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians()).mirror())
    }) {
    companion object {
        @JvmField var INITIAL_X: Double = 56.0
        @JvmField var INITIAL_Y: Double = 8.0
        @JvmField var INITIAL_HEADING: Double = 90.0
    }

    private val paths by lazy { PathConstants(robot.drivetrain.follower, AllianceColor.BLUE) }

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
            val pattern = tags.firstOrNull { it.id in patterns.keys }?.id

            +drivetrain.pathTo(paths.pathToLaunchZoneFromWall)

            +launcher.enable()

            if (pattern == null) {
                telemetry.addData("Pattern ID", "None detected")
            } else {
                telemetry.addData("Pattern ID", pattern)
            }

            for (artifact in patterns[pattern] ?: defaultPattern) {
                +classifier.releaseArtifact(artifact)
                wait(0.5)
            }

            +launcher.disable()
        }
    }
}
