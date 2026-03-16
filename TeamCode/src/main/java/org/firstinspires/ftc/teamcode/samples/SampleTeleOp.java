package org.firstinspires.ftc.teamcode.samples;

import dev.kingssack.volt.opmode.VoltOpModeMeta;
import dev.kingssack.volt.opmode.manual.ManualMode;
import dev.kingssack.volt.util.Event.ManualEvent.*;
import dev.kingssack.volt.util.buttons.Button;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

// an actual opmode would not have abstract
@VoltOpModeMeta(name = "sample opmode name")
@SuppressWarnings("unused")
abstract public class SampleTeleOp extends ManualMode<SampleRobot> {
    public SampleTeleOp() {
        super(new ManualParams());
    }

    @Override
    public void defineEvents() {
        then(new Tap(Button.A1), builder -> {
            builder.unaryPlus(getRobot().sampleAttachment.exampleAction());
            return Unit.INSTANCE;
        });
    }
    SampleRobot robot = new SampleRobot(getHardwareMap());
    @NotNull
    @Override
    protected SampleRobot getRobot() {
        return robot;
    }
}
