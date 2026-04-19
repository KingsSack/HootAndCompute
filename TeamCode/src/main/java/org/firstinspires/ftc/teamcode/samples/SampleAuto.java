package org.firstinspires.ftc.teamcode.samples;

import dev.kingssack.volt.opmode.autonomous.AutonomousMode;
import kotlin.Unit;
import dev.kingssack.volt.util.Event.AutonomousEvent.Start;
import org.jetbrains.annotations.NotNull;

// An actual opmode would not be abstract
@SuppressWarnings("unused")
abstract public class SampleAuto extends AutonomousMode<SampleRobot> {
    @NotNull SampleRobot robot = new SampleRobot(getHardwareMap());

    @Override
    protected @NotNull SampleRobot getRobot() {
        return robot;
    }

    public SampleAuto() {
        super();

        then(Start.INSTANCE, (builder, param) -> {
            builder.unaryPlus(getRobot().score());
            return Unit.INSTANCE;
        });
    }
}
