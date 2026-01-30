package org.firstinspires.ftc.teamcode.samples;

import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import dev.kingssack.volt.attachment.DcMotorAttachment;
import dev.kingssack.volt.core.VoltActionBuilder;
import dev.kingssack.volt.core.VoltActionBuilderKt;
import dev.kingssack.volt.robot.Robot;

public class SampleRobot extends Robot {
    public DcMotor attachmentMotor;
    public DcMotorAttachment motor;

    public SampleRobot(HardwareMap hardwareMap) {
        super(hardwareMap);
        attachmentMotor = hardwareMap.get(DcMotor.class, "motor");
        motor = new DcMotorAttachment("motor", attachmentMotor, 1.0, 100, 0, DcMotorSimple.Direction.FORWARD);
    }

    public Action score() {
        return VoltActionBuilderKt.voltAction(this, (VoltActionBuilder<? super Robot> builder) -> {
            builder.unaryPlus(motor.goTo(0.5, 100));
            builder.unaryPlus(motor.goTo(0.5, 0));
            return kotlin.Unit.INSTANCE;
        });
    }
}
