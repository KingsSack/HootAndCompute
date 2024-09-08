package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;

@TeleOp(name = "El Manual Blank", group = "Competition")
public class TestOpMode extends OpMode {

    // Motors
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;

    // Booleans
    boolean slowDown = false;

    // Initialize
    @Override
    public void init() {
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftRearDrive.setDirection(DcMotor.Direction.REVERSE);
    }

    // Robot Loop
    @Override
    public void loop() {
        double x = gamepad1.left_stick_x; // Drive
        double y = -gamepad1.left_stick_y; // Strafe
        double rx = gamepad1.right_stick_x; // Spin

        double rightFrontPower = y - x - rx;
        double leftFrontPower = y + x + rx;
        double rightRearPower = y + x - rx;
        double leftRearPower = y - x + rx;

        if (gamepad1.left_bumper) {
            slowDown = true;
        } else {
            slowDown = false;
        }

        if (slowDown) {
            rightFrontPower /= 3;
            leftFrontPower /= 3;
            rightRearPower /= 3;
            leftRearPower /= 3;
        }

        rightFrontDrive.setPower(rightFrontPower);
        leftFrontDrive.setPower(leftFrontPower);
        rightRearDrive.setPower(rightRearPower);
        leftRearDrive.setPower(leftRearPower);
    }

}
