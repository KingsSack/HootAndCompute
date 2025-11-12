package org.firstinspires.ftc.teamcode.samples;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.SequentialAction;
import dev.kingssack.volt.opmode.autonomous.AutonomousMode;

public class SampleAuto extends AutonomousMode<SampleRobot> {
    public SampleAuto() {
        super(SampleRobot::new);
    }

    Action sampleAction() {
        return new SequentialAction(
                super.getRobot().motor.goTo(0.5, 50),
                super.getRobot().motor.goTo(0.5, 0),
                super.getRobot().motor.goTo(0.5, 100)
        );
    }

    @Override
    protected void sequence() {
        execute(builder -> {
            builder.unaryPlus(sampleAction());
            return kotlin.Unit.INSTANCE;
        });
    }
}
