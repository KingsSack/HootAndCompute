package org.firstinspires.ftc.teamcode.samples;

import com.qualcomm.robotcore.hardware.HardwareMap;
import dev.kingssack.volt.opmode.autonomous.AutonomousMode;
import org.jetbrains.annotations.NotNull;

public class SampleAuto extends AutonomousMode<SampleRobot> {
    @Override
    @NotNull
    public SampleRobot createRobot(@NotNull HardwareMap hardwareMap) {
        return new SampleRobot(hardwareMap);
    }

    public SampleAuto() {
        super();

        super.getActionSequence().add(() -> super.getRobot().motor.goTo(0.5, 50));
    }
}
