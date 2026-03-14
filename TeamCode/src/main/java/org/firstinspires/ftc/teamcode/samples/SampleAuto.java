package org.firstinspires.ftc.teamcode.samples;

import dev.kingssack.volt.opmode.autonomous.AutonomousMode;
import dev.kingssack.volt.util.Event.AutonomousEvent.Start;
import kotlin.Unit;

@SuppressWarnings("unused")
public class SampleAuto extends AutonomousMode<SampleRobot> {
    public SampleAuto() {
        super(SampleRobot::new);
    }

    @Override
    public void defineEvents() {
        then(Start.INSTANCE, builder -> {
            builder.unaryPlus(getRobot().score());
            return Unit.INSTANCE;
        });
    }
}
