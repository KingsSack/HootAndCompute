package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "ResetServos", group = "Competition")
public class ResetServos extends OpMode {

    Servo arm;
    Servo claw;
    Servo launcher;

    @Override
    public void init() {
        arm = hardwareMap.get(Servo.class, "Arm");
        claw = hardwareMap.get(Servo.class, "Claw");
        launcher = hardwareMap.get(Servo.class, "Launcher");
    }

    @Override
    public void loop() {
        arm.setPosition(.5);
        claw.setPosition(.5);
        launcher.setPosition(.5);
    }

}
