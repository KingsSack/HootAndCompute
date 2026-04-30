package org.firstinspires.ftc.teamcode.samples;

import dev.kingssack.volt.opmode.manual.ManualMode;
import dev.kingssack.volt.util.Event.ManualEvent.Tap;
import dev.kingssack.volt.util.buttons.Button;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

// An actual opmode would not be abstract
@SuppressWarnings("unused")
abstract public class SampleTeleOp extends ManualMode<SampleRobot> {
    @NotNull SampleRobot robot = new SampleRobot(getHardwareMap());

    @Override
    protected @NotNull SampleRobot getRobot() {
        return robot;
    }

    public SampleTeleOp() {
        super(new ManualParams());

        then(new Tap(Button.A1), (builder, param) -> {
            builder.unaryPlus(getRobot().sampleAttachment.exampleAction());
            return Unit.INSTANCE;
        });
    }
}
