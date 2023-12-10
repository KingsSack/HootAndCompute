package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;

@TeleOp(name = "El Manual", group = "Competition")
public class ElManual extends OpMode {

    // Connected Devices
    private Blinker debug_light;
    private HardwareDevice robot;
    private IMU imu;

    // Motors
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;
    DcMotor hookLift;
    DcMotor hook;

    // Constants
    private double deadzone = 0.1; // Deadzone for joystick input
    private double scaleFactor = 2.0; // Exponential scaling factor

    // Booleans
    boolean slowDown = false;
    boolean intakeOn = true;

    // Servos
    CRServo intake;
    CRServo launcher;
    CRServo conveyor;

    // Initialize
    @Override
    public void init() {
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");

        leftFrontDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        leftRearDrive.setDirection(DcMotorSimple.Direction.REVERSE);

        intake = hardwareMap.get(CRServo.class, "Sweeper");
        launcher = hardwareMap.get(CRServo.class, "Launcher");

        hookLift = hardwareMap.get(DcMotor.class, "HookLift");
        hook = hardwareMap.get(DcMotor.class, "Hook");

        conveyor = hardwareMap.get(CRServo.class, "Conveyor");
        conveyor.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    // Robot Loop
    @Override
    public void loop() {
        // Get joystick inputs
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

        if (gamepad2.left_bumper) {
            launcher.setPower(.7);
        } else {
            launcher.setPower(0);
        }

        if (gamepad2.x) {
            intakeOn = !intakeOn;
        }

        if (gamepad2.b) {
            if (gamepad2.right_bumper) {
                if (hookLift.getCurrentPosition() < 600) {
                    hookLift.setPower(.6);
                } else {
                    hookLift.setPower(0);
                }
            } else {
                if (hookLift.getCurrentPosition() < 200) {
                    hookLift.setPower(.75);
                } else {
                    hookLift.setPower(0);
                }
            }
        }

        if (gamepad2.y) {
            hook.setPower(.75);
        } else if (gamepad2.a) {
            hook.setPower(-1);
        } else {
            hook.setPower(0);
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

        intake.setPower(intakeOn ? -.7 : 0);
        conveyor.setPower(intakeOn ? -.7 : 0);

        telemetry.addData("Movement", "RF %5.2f, LF %5.2f, RR %5.2f, LR %5.2f",
                rightFrontPower,
                leftFrontPower,
                rightRearPower,
                leftRearPower);
        telemetry.addData("Intake", "On: %b, Power: %5.2f", intakeOn, intake.getPower());
        telemetry.addData("Conveyor", "On: %b, Power: %5.2f", intakeOn, conveyor.getPower());
        telemetry.addData("Hook", "Position: %s, Power: %5.2f", hookLift.getCurrentPosition(), hookLift.getPower());
        telemetry.update();
    }
}
