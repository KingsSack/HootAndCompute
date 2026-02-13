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

abstract class Magpie(private val alliance: AllianceColor, private val initialPose: Pose) :
    AutonomousMode<Jones<MecanumDriveWithPP>>({ JonesPP(it, initialPose) }) {
    private val finalPose: Pose = Pose(56.0, 36.0, 115.0.toRadians()).maybeFlip(alliance)

    private val patterns =
        mapOf(
            21 to listOf(ReleaseType.GREEN, ReleaseType.PURPLE, ReleaseType.PURPLE),
            22 to listOf(ReleaseType.PURPLE, ReleaseType.GREEN, ReleaseType.PURPLE),
            23 to listOf(ReleaseType.PURPLE, ReleaseType.PURPLE, ReleaseType.GREEN),
        )

    private val defaultPattern = listOf(ReleaseType.NEXT, ReleaseType.NEXT, ReleaseType.NEXT)

    private var patternId: Int? = null

    // Fires artifacts according to the detected pattern and leaves
    override val sequence = sequence {
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
}

@Autonomous(name = "Magpie Blue", group = "Competition", preselectTeleOp = "Seahorse")
class MagpieBlue : Magpie(AllianceColor.BLUE, Pose(56.0, 8.0, 115.0.toRadians()))

@Autonomous(name = "Magpie Red", group = "Competition", preselectTeleOp = "Seahorse")
class MagpieRed : Magpie(AllianceColor.RED, Pose(56.0, 8.0, 115.0.toRadians()).mirror())
