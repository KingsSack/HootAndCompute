package org.firstinspires.ftc.teamcode.samples;

import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.HardwareMap;
import dev.kingssack.volt.opmode.autonomous.AutonomousMode;
import dev.kingssack.volt.robot.Robot;
import org.jetbrains.annotations.NotNull;

public class SampleAuto extends AutonomousMode<SampleRobot> {
    @Override
    @NotNull
    public SampleRobot createRobot(@NotNull HardwareMap hardwareMap) {
        return new SampleRobot(hardwareMap);
    }

    public SampleAuto() {
        super();

        super.getActionSequence().add(this::sampleAction);
    }

    Action sampleAction() {
        Robot.SequenceBuilder sequenceBuilder = new Robot.SequenceBuilder();
        sequenceBuilder.then(super.getRobot().motor.goTo(0.5, 50));
        sequenceBuilder.then(super.getRobot().motor.goTo(0.5, 0));
        sequenceBuilder.then(super.getRobot().motor.goTo(0.5, 100));
        return sequenceBuilder.build();
    }
}
