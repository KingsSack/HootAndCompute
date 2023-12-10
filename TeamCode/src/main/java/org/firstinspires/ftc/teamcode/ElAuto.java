package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.*;

@Autonomous(name = "El Auto", group = "Competition")
public class ElAuto extends LinearOpMode {
    IMU imu;
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;
    private double MAX_SPEED = .75;

    void Init() {
        // Initialize
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");
    }

    @Override
    public void runOpMode() {
        Init();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // Run
        if (opModeIsActive()) {
            while (opModeIsActive()) {
                move(MAX_SPEED / 3, MAX_SPEED / 3);
            }
        }
    }

    void move(double leftPower, double rightPower) {
        double max = Math.max(Math.abs(rightPower), Math.abs(leftPower));
        if (max > MAX_SPEED) {
            rightPower /= max;
            leftPower /= max;
        }

        telemetry.addData(">", "Moving, Left: %5.2f, Right: %5.2f", leftPower, rightPower);

        leftFrontDrive.setPower(-leftPower);
        rightFrontDrive.setPower(rightPower);
        leftRearDrive.setPower(-leftPower);
        rightRearDrive.setPower(rightPower);
    }
}
