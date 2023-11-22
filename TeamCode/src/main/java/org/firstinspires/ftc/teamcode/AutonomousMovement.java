package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.IMU;

@Autonomous(name = "Test Movement", group = "Autonomous")
public class AutonomousMovement extends LinearOpMode{
    // Connected Devices
    private Blinker debug_light;
    private HardwareDevice robot;
    private IMU imu;

    // Constants
    final double SPEED = .75;

    // Motors
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;

    // Initialize
    @Override
    public void runOpMode() {
        // Square
        boolean turn = false;
        double distance;

        // Motors
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");

        // Ready
        telemetry.addData("", "Press play to start");
        telemetry.update();
        waitForStart();

        // Loop
        while (opModeIsActive())
        {
            if (!turn) {
                moveRobot(-SPEED, SPEED);
            }
        }
    }

    public void moveRobot(double rightPower, double leftPower) {
        // Send powers to the wheels.
        rightFrontDrive.setPower(rightPower);
        leftFrontDrive.setPower(leftPower);
        rightRearDrive.setPower(rightPower);
        leftRearDrive.setPower(leftPower);
    }
}
