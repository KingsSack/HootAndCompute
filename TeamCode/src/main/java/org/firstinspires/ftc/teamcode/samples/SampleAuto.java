package org.firstinspires.ftc.teamcode.samples;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.SequentialAction;
import dev.kingssack.volt.opmode.VoltOpModeMeta;
import dev.kingssack.volt.opmode.autonomous.AutonomousMode;
import org.jetbrains.annotations.NotNull;

// an actual opmode would not have abstract
@VoltOpModeMeta(name = "sample opmode name")
abstract public class SampleAuto extends AutonomousMode<SampleRobot> {
    Action sampleAction() {
        return new SequentialAction(
                robot.motor.goTo(0.5, 50),
                robot.motor.goTo(0.5, 0),
                robot.motor.goTo(0.5, 100)
        );
    }

    @Override
    public void sequence() {
        execute(builder -> {
            builder.unaryPlus(sampleAction());
            return kotlin.Unit.INSTANCE;
        });
    }

    @NotNull SampleRobot robot = new SampleRobot(getHardwareMap());
    @Override
    protected @NotNull SampleRobot getRobot() {
        return robot;
    }
}
