package org.firstinspires.ftc.teamcode.samples;

import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.Servo;
import dev.kingssack.volt.attachment.Attachment;
import kotlin.Unit;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.jetbrains.annotations.NotNull;

public class SampleAttachment extends Attachment {
    private final Servo servo;

    public SampleAttachment(Servo servo) {
        super("Sample");
        this.servo = servo;
    }

    public Action exampleAction() {
        return action(builder -> {
            builder.init(() -> {
                requireReady();
                return Unit.INSTANCE;
            });

            builder.loop(packet -> {
                servo.setPosition(1.0);
                return true;
            });

            return Unit.INSTANCE;
        });
    }

    @Override
    public void update(@NotNull Telemetry telemetry) {
        super.update(telemetry);
        telemetry.addData("Position", servo.getPosition());
    }
}
