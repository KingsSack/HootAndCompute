package org.firstinspires.ftc.teamcode.opmode.manual

import dev.kingssack.volt.attachment.drivetrain.rr.mecanum.DriveEncoderMecanumRoadRunnerDrivetrain
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.manual.DrivetrainManualMode
import dev.kingssack.volt.util.Event.ManualEvent.Tap
import dev.kingssack.volt.util.buttons.Button
import org.firstinspires.ftc.teamcode.attachment.Classifier
import org.firstinspires.ftc.teamcode.robot.JonesRR

@VoltOpModeMeta("Olive", "Showcase")
class Olive : DrivetrainManualMode<DriveEncoderMecanumRoadRunnerDrivetrain, JonesRR>() {
    override val robot = JonesRR(hardwareMap)

    private fun defineControls() {
        // Toggle launcher
        Tap(Button.RIGHT_BUMPER1) then {
            if (robot.launcher.isStopped) +robot.launcher.enable(1000.0)
            else +robot.launcher.disable()
        }

        Tap(Button.A1) then { +robot.classifier.releaseArtifact(Classifier.ReleaseType.NEXT) }
    }

    init {
        defineControls()
    }
}
