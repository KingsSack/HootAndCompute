package org.firstinspires.ftc.teamcode.opmode.manual

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.attachment.drivetrain.pp.mecanum.DriveEncoderMecanumPedroPathingDrivetrain
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.manual.DrivetrainControlsManualModeNoSpeedmodes
import dev.kingssack.volt.util.Event.ManualEvent.Tap
import dev.kingssack.volt.util.buttons.Button
import org.firstinspires.ftc.teamcode.attachment.Classifier
import org.firstinspires.ftc.teamcode.robot.JonesPP

@VoltOpModeMeta("Olive")
class Olive : DrivetrainControlsManualModeNoSpeedmodes<DriveEncoderMecanumPedroPathingDrivetrain, JonesPP>() {
    override val robot = JonesPP(hardwareMap, Pose(0.0, 0.0, 0.0))

    init {
        // Toggle launcher
        Tap(Button.RIGHT_BUMPER1) then {
            if (robot.launcher.isStopped) { +robot.launcher.enable(1000.0) }
            else { +robot.launcher.disable() }
        }

        Tap(Button.A1) then {
            +robot.classifier.releaseArtifact(Classifier.ReleaseType.NEXT)
        }
    }
}
