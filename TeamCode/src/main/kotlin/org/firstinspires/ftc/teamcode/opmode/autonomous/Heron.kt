package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.attachment.Classifier.ReleaseType
import org.firstinspires.ftc.teamcode.robot.Jones
import org.firstinspires.ftc.teamcode.robot.JonesPP
import org.firstinspires.ftc.teamcode.util.AllianceColor
import org.firstinspires.ftc.teamcode.util.maybeFlip
import org.firstinspires.ftc.teamcode.util.toRadians

abstract class Heron(private val alliance: AllianceColor, private val initialPose: Pose) :
    AutonomousMode<Jones<MecanumDriveWithPP>>({ JonesPP(it, initialPose) }) {
    private val launchPose: Pose = Pose(60.0, 12.0, 115.0.toRadians()).maybeFlip(alliance)
    private val finalPose: Pose = Pose(60.0, 30.0, 115.0.toRadians()).maybeFlip(alliance)

    private val patterns =
        mapOf(
            21 to listOf(ReleaseType.GREEN, ReleaseType.PURPLE, ReleaseType.PURPLE),
            22 to listOf(ReleaseType.PURPLE, ReleaseType.GREEN, ReleaseType.PURPLE),
            23 to listOf(ReleaseType.PURPLE, ReleaseType.PURPLE, ReleaseType.GREEN),
        )

    private val defaultPattern = listOf(ReleaseType.NEXT, ReleaseType.NEXT, ReleaseType.NEXT)

    private var patternId: Int? = null

    override fun initialize() {
        super.initialize()
        blackboard["allianceColor"] = alliance

        while (opModeInInit()) {
            val tags = context(telemetry) { robot.getDetectedAprilTags() }
            patternId = tags.firstOrNull { it.id in patterns.keys }?.id
            telemetry.addData("Pattern ID", patternId ?: "None detected")
            telemetry.update()
        }

        robot.visionPortal.stopStreaming()
    }

    /**
     * Drives to the launch zone, fires artifacts according to the detected pattern, and saves pose
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

@Autonomous(name = "Heron Blue", group = "Competition", preselectTeleOp = "Seahorse")
class HeronBlue : Heron(AllianceColor.BLUE, Pose(63.875, 8.0, 90.0.toRadians()))

@Autonomous(name = "Heron Red", group = "Competition", preselectTeleOp = "Seahorse")
class HeronRed : Heron(AllianceColor.RED, Pose(63.875, 8.0, 90.0.toRadians()).mirror())
