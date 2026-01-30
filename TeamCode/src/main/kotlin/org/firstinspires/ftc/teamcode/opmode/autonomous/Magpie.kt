package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.acmerobotics.dashboard.config.Config
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.robot.JonesPP
import dev.kingssack.volt.opmode.autonomous.AllianceColor
import org.firstinspires.ftc.teamcode.util.PathConstants
import org.firstinspires.ftc.teamcode.util.toRadians

@Config
@Suppress("unused")
class Magpie :
    AutonomousMode<JonesPP>() {
    override fun getRobot(hardwareMap: HardwareMap): JonesPP {
        return JonesPP(hardwareMap, Pose(INITIAL_X, INITIAL_Y, INITIAL_HEADING.toRadians()).mirror())
    }
    override val name = "Magpie"
    override val group = "Competition"
    override val autoTransition = "Seahorse"
    companion object {
        @JvmField var INITIAL_X: Double = 56.0
        @JvmField var INITIAL_Y: Double = 8.0
        @JvmField var INITIAL_HEADING: Double = 90.0
    }

    private val paths by lazy { PathConstants(robot.drivetrain.follower, AllianceColor.BLUE) }

    enum class ArtifactColor {
        PURPLE,
        GREEN,
    }

    lateinit var artifacts: List<ArtifactColor>

    override fun sequence() = execute {
        with(robot) {
            val tags = context(telemetry) { getDetectedAprilTags() }
            +drivetrain.pathTo(paths.pathToLaunchZoneFromWall)

            when (tags[0].id) {
                21 -> {
                    telemetry.addData("Pattern", "GPP")
                    artifacts =
                        listOf(ArtifactColor.GREEN, ArtifactColor.PURPLE, ArtifactColor.PURPLE)
                }

                22 -> {
                    telemetry.addData("Pattern", "PGP")
                    artifacts =
                        listOf(ArtifactColor.PURPLE, ArtifactColor.GREEN, ArtifactColor.PURPLE)
                }

                23 -> {
                    telemetry.addData("Pattern", "PPG")
                    artifacts =
                        listOf(ArtifactColor.PURPLE, ArtifactColor.PURPLE, ArtifactColor.GREEN)
                }

                else -> {
                    telemetry.addData("Pattern", "Unknown - defaulting to PPG")
                    artifacts =
                        listOf(ArtifactColor.PURPLE, ArtifactColor.PURPLE, ArtifactColor.GREEN)
                }
            }

            +launcher.enable()

            for (artifact in artifacts) {
                when (artifact) {
                    ArtifactColor.PURPLE -> {
                        +classifier.releasePurple()
                    }
                    ArtifactColor.GREEN -> {
                        +classifier.releaseGreen()
                    }
                }
            }

            +launcher.disable()
        }
    }
}
