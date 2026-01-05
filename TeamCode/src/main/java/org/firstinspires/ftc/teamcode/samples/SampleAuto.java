package org.firstinspires.ftc.teamcode.samples;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.SequentialAction;
import com.qualcomm.robotcore.hardware.HardwareMap;
import dev.kingssack.volt.opmode.autonomous.AutonomousMode;
import org.jetbrains.annotations.NotNull;

// an actual opmode would not have abstract
abstract public class SampleAuto extends AutonomousMode<SampleRobot> {
    Action sampleAction() {
        return new SequentialAction(
                super.getRobot().motor.goTo(0.5, 50),
                super.getRobot().motor.goTo(0.5, 0),
                super.getRobot().motor.goTo(0.5, 100)
        );
    }

    @Override
    public void sequence() {
        execute(builder -> {
            builder.unaryPlus(sampleAction());
            return kotlin.Unit.INSTANCE;
        });
    }

    @Override
    public String getName() {
        return "sample opmode name";
    }

    @Override
    public SampleRobot getRobot(HardwareMap hardwareMap) {
        return new SampleRobot(hardwareMap);
    }
}
