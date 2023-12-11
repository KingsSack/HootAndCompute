package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;

@TeleOp(name = "Test Movement", group = "Testing")
public class TestOpMode extends OpMode {
    // Connected Devices
    private Blinker debug_light;
    private HardwareDevice robot;
    private IMU imu;

    // Motors
    DcMotor motorRF;
    DcMotor motorLF;
    DcMotor motorRR;
    DcMotor motorLR;

    // Variables
    enum Quadrant {
        CENTER,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }
    Quadrant getQuadrant(double x, double y) {
        if (y > 0) {
            return Quadrant.BOTTOM;
        } else if (y < 0) {
            return Quadrant.TOP;
        } else if (x > 0) {
            return Quadrant.RIGHT;
        } else if (x < 0) {
            return Quadrant.LEFT;
        } else {
            return Quadrant.CENTER;
        }
    }
    Quadrant quadrant = Quadrant.CENTER;

    // Initialize
    @Override
    public void init() {
        motorRF = hardwareMap.get(DcMotor.class, "MotorRF");
        motorLF = hardwareMap.get(DcMotor.class, "MotorLF");
        motorRR = hardwareMap.get(DcMotor.class, "MotorRR");
        motorLR = hardwareMap.get(DcMotor.class, "MotorLR");

        motorLF.setDirection(DcMotorSimple.Direction.REVERSE);
        motorLR.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    // Robot Loop
    @Override
    public void loop() {
        quadrant = getQuadrant(gamepad1.left_stick_x, gamepad1.left_stick_y);

        switch (quadrant) {
            case CENTER:
                move(0, 0);
                break;
            case LEFT:
                move(-1, 0);
                break;
            case RIGHT:
                move(1, 0);
                break;
            case TOP:
                move(0, 1);
                break;
            case BOTTOM:
                move(0, -1);
                break;
        }
    }

    void move(double leftPower, double rightPower) {
        telemetry.addData(">", "Moving, Left: %5.2f, Right: %5.2f", leftPower, rightPower);

        motorLF.setPower(leftPower);
        motorRF.setPower(rightPower);
        motorLR.setPower(leftPower);
        motorRR.setPower(rightPower);
    }
}
