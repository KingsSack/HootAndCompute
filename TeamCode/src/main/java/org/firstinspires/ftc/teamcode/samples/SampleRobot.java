package org.firstinspires.ftc.teamcode.samples;

import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import dev.kingssack.volt.attachment.DcMotorAttachment;
import dev.kingssack.volt.core.VoltActionBuilderKt;
import dev.kingssack.volt.robot.Robot;
import dev.kingssack.volt.util.telemetry.ActionTracer;

public class SampleRobot extends Robot {
    private final Servo sampleAttachmentServo;
    private final DcMotor attachmentMotor;

    public final SampleAttachment sampleAttachment;
    public final DcMotorAttachment motor;

    public SampleRobot(HardwareMap hardwareMap) {
        super(hardwareMap, ActionTracer.INSTANCE);
        sampleAttachmentServo = hardwareMap.get(Servo.class, "servo");
        attachmentMotor = hardwareMap.get(DcMotor.class, "motor");

        sampleAttachment = attachment(() -> new SampleAttachment(sampleAttachmentServo));
        motor = attachment(() -> new DcMotorAttachment(
                "motor",
                attachmentMotor,
                1.0,
                100,
                0,
                DcMotorSimple.Direction.FORWARD
        ));
    }

    public Action score() {
        return VoltActionBuilderKt.voltAction(
                this,
                builder -> {
                    builder.unaryPlus(motor.goTo(0.5, 100));
                    builder.unaryPlus(motor.goTo(0.5, 0));
                    builder.unaryPlus(sampleAttachment.exampleAction());
                    return kotlin.Unit.INSTANCE;
                }
        );
    }
}
