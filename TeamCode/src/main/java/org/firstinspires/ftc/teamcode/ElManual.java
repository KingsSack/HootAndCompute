package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;

@TeleOp(name = "El Manual", group = "Competition")
public class ElManual extends OpMode {

    // Motors
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;
    DcMotor hookLift;
    DcMotor hook;

    // Booleans
    boolean slowDown = false;

    // Servos
    CRServo launcher;

    // Initialize
    @Override
    public void init() {
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftRearDrive.setDirection(DcMotor.Direction.REVERSE);

        launcher = hardwareMap.get(CRServo.class, "Launcher");

        hookLift = hardwareMap.get(DcMotor.class, "HookLift");
        hook = hardwareMap.get(DcMotor.class, "Hook");
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
            // Slow down the robot while driver one holds the left bumper
            slowDown = true;
        } else {
            slowDown = false;
        }

        if (gamepad2.left_bumper) {
            // If driver two holds the left bumper, launch the airplane
            launcher.setPower(-.72);
        } else {
            launcher.setPower(0);
        }

        if (gamepad2.b) {
            // If driver two holds b, move the arm up
            if (gamepad2.right_bumper) {
                // If they hold the right bumper as well, lift it all the way
                if (hookLift.getCurrentPosition() < 500) {
                    hookLift.setPower(.5);
                } else {
                    hookLift.setPower(0);
                }
            } else {
                // Otherwise, lift it a little bit
                if (hookLift.getCurrentPosition() < 260) {
                    hookLift.setPower(.5);
                } else {
                    hookLift.setPower(0);
                }
            }
        }
        else if (gamepad2.x) {
            // If driver two holds x, move the arm down
            if (hookLift.getCurrentPosition() > 0) {
                hookLift.setPower(-.5);
            } else {
                hookLift.setPower(0);
            }
        }
        else {
            hookLift.setPower(0);
        }

        if (gamepad2.y) {
            // If driver two holds y, move the hook up
            hook.setPower(.75);
        } else if (gamepad2.a) {
            // If driver two holds a, move the hook down
            hook.setPower(-1);
        } else {
            hook.setPower(0);
        }

        if (slowDown) {
            // If slow down is enabled, divide the power by 3
            rightFrontPower /= 3;
            leftFrontPower /= 3;
            rightRearPower /= 3;
            leftRearPower /= 3;
        }

        // Move the robot
        rightFrontDrive.setPower(rightFrontPower);
        leftFrontDrive.setPower(leftFrontPower);
        rightRearDrive.setPower(rightRearPower);
        leftRearDrive.setPower(leftRearPower);

        // Telemetry data
        telemetry.addData("Movement", "RF %5.2f, LF %5.2f, RR %5.2f, LR %5.2f",
                rightFrontPower,
                leftFrontPower,
                rightRearPower,
                leftRearPower);
        telemetry.addData("Hook", "Position: %s, Power: %5.2f", hookLift.getCurrentPosition(), hookLift.getPower());
        telemetry.update();
    }

}
