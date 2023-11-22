package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;

@TeleOp(name = "Test OpMode", group = "Testing")
public class TestOpMode extends OpMode {
    // Connected Devices
    private Blinker debug_light;
    private HardwareDevice robot;
    private IMU imu;

    // Constants
    static final double MAX_POS = 1.0;
    static final double MIN_POS =  0.0;

    // Motors
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;

    // Servos
    Servo arm;
    Servo claw;
    double arm_position = (MAX_POS - MIN_POS) / 2;
    double claw_position = (MAX_POS - MIN_POS) / 2;

    // Initialize
    @Override
    public void init() {
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");

        arm = hardwareMap.get(Servo.class, "Arm");
        claw = hardwareMap.get(Servo.class, "Claw");
    }

    // Robot Loop
    @Override
    public void loop() {
        rightFrontDrive.setPower(gamepad1.left_stick_y-gamepad1.left_stick_x);
        leftFrontDrive.setPower(-gamepad1.left_stick_y-gamepad1.left_stick_x);
        rightRearDrive.setPower(gamepad1.left_stick_y-gamepad1.left_stick_x);
        leftRearDrive.setPower(-gamepad1.left_stick_y-gamepad1.left_stick_x);

        if (gamepad1.y) {
            if (arm_position < 1) {
                arm_position += .01;
            }
        }
        if (gamepad1.a) {
            if (arm_position > 0) {
                arm_position -= .01;
            }
        }

        if (gamepad1.x) {
            if (claw_position < 1) {
                claw_position += .01;
            }
        }
        if (gamepad1.b) {
            if (claw_position > 0) {
                claw_position -= .01;
            }
        }

        telemetry.addData("Arm Servo Value", "Value: %s", arm.getPosition());
        telemetry.addData("Claw Servo Value", "Value: %s", claw.getPosition());
        telemetry.update();

        arm.setPosition(arm_position);
        claw.setPosition(claw_position);
    }
}
