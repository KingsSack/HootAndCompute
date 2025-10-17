package org.firstinspires.ftc.teamcode.samples;

import com.qualcomm.robotcore.hardware.HardwareMap;
import dev.kingssack.volt.attachment.SimpleAttachmentWithDcMotor;
import dev.kingssack.volt.robot.Robot;

public class SampleRobot extends Robot {
    public SimpleAttachmentWithDcMotor motor;

    public SampleRobot(HardwareMap hardwareMap) {
        motor = new SimpleAttachmentWithDcMotor(hardwareMap, "motor", 1.0, 100, 0);
        registerAttachments(motor);
    }
}
