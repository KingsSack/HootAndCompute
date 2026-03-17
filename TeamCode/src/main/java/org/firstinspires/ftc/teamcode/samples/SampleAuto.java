package org.firstinspires.ftc.teamcode.samples;

import dev.kingssack.volt.opmode.VoltOpModeMeta;
import dev.kingssack.volt.opmode.autonomous.AutonomousMode;
import kotlin.Unit;
import dev.kingssack.volt.util.Event.AutonomousEvent.Start;
import org.jetbrains.annotations.NotNull;

// an actual opmode would not have abstract
@VoltOpModeMeta(name = "sample opmode name")
@SuppressWarnings("unused")
abstract public class SampleAuto extends AutonomousMode<SampleRobot> {
    public SampleAuto() {
        super();
        then(Start.INSTANCE, builder -> {
            builder.unaryPlus(getRobot().score());
            return Unit.INSTANCE;
        });
    }
    @NotNull SampleRobot robot = new SampleRobot(getHardwareMap());
    @Override
    protected @NotNull SampleRobot getRobot() {
        return robot;
    }
}
