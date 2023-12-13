package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;

import static java.lang.Math.abs;


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
        if (abs(gamepad1.left_stick_x) > abs(gamepad1.left_stick_y)) {
            move(gamepad1.left_stick_x,-gamepad1.left_stick_x );
        }
        else {
            move(gamepad1.left_stick_y, gamepad1.left_stick_y);
        }
    }

    void move(double frontPower, double backPower) {
        telemetry.addData(">", "Moving, Front: %5.2f, Back: %5.2f", frontPower, backPower);

        motorLF.setPower(frontPower);
        motorRF.setPower(frontPower);
        motorLR.setPower(backPower);
        motorRR.setPower(backPower);
    }
}
