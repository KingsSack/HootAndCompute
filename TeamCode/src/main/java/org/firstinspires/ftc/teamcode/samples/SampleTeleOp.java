package org.firstinspires.ftc.teamcode.samples;

import dev.kingssack.volt.opmode.manual.ManualMode;
import dev.kingssack.volt.util.Event.ManualEvent.*;
import dev.kingssack.volt.util.buttons.Button;
import kotlin.Unit;

@SuppressWarnings("unused")
public class SampleTeleOp extends ManualMode<SampleRobot> {
    public SampleTeleOp() {
        super(SampleRobot::new, new ManualParams());
    }

    @Override
    public void defineEvents() {
        then(new Tap(Button.A1), builder -> {
            builder.unaryPlus(getRobot().sampleAttachment.exampleAction());
            return Unit.INSTANCE;
        });
    }
}
