package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous (name = "Drive By Encoder", group = "Testing")
public class DriveByEncoder extends LinearOpMode {

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
    private ElapsedTime runtime = new ElapsedTime();
    static final double COUNTS_PER_MOTOR_REV = 1440; // eg: TETRIX Motor Encoder
    static final double DRIVE_GEAR_REDUCTION = 1.0; // No external gearing
    static final double WHEEL_DIAMETER_INCHES = 4.0; // For circumference
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double DRIVE_SPEED = 1;
    static final double TURN_SPEED = 0.5;

    // Initialize
    void initialize() {
        motorRF = hardwareMap.get(DcMotor.class, "MotorRF");
        motorLF = hardwareMap.get(DcMotor.class, "MotorLF");
        motorRR = hardwareMap.get(DcMotor.class, "MotorRR");
        motorLR = hardwareMap.get(DcMotor.class, "MotorLR");

        motorLF.setDirection(DcMotor.Direction.REVERSE);
        motorLR.setDirection(DcMotor.Direction.REVERSE);

        motorRF.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorLF.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorRR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorLR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motorRF.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorLF.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorRR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorLR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addData("Starting at", "%7d, %7d, %7d, %7d",
                motorRF.getCurrentPosition(), motorLF.getCurrentPosition(),
                motorRR.getCurrentPosition(), motorLR.getCurrentPosition()
        );
        telemetry.update();
    }

    @Override
    public void runOpMode() {
        initialize();

        waitForStart();

        // Step through each leg of the path,
        // Note: Reverse movement is obtained by setting a negative distance (not speed)
        encoderDrive(DRIVE_SPEED,  24,  48, 5.0);  // S1: Forward 47 Inches with 5 Sec timeout

        sleep(1000);  // pause to dis play final telemetry message.
    }

    // Encoder Drive
    public void encoderDrive(double speed, double leftInches, double rightInches, double timeoutS) {
        int newRFTarget;
        int newLFTarget;
        int newRRTarget;
        int newLRTarget;

        // Ensure that the OpMode is still active
        if (opModeIsActive()) {
            // Determine new target position, and pass to motor controller
            newRFTarget = motorRF.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);
            newLFTarget = motorLF.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH);
            newRRTarget = motorRR.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);
            newLRTarget = motorLR.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH);
            motorRF.setTargetPosition(newRFTarget);
            motorLF.setTargetPosition(newLFTarget);
            motorRR.setTargetPosition(newRRTarget);
            motorLR.setTargetPosition(newLRTarget);

            // Turn On RUN_TO_POSITION
            motorRF.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorLF.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorRR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorLR.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // Reset the timeout time and start motion
            runtime.reset();
            motorRF.setPower(Math.abs(speed));
            motorLF.setPower(Math.abs(speed));
            motorRR.setPower(Math.abs(speed));
            motorLR.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() && (runtime.seconds() < timeoutS) && (motorRF.isBusy() && motorLF.isBusy() && motorRR.isBusy() && motorLR.isBusy())) {
                // Display it for the driver.
                telemetry.addData("Running to",  " %7d, %7d, %7d, %7d",
                        newRFTarget,  newLFTarget,
                        newRRTarget, newLRTarget
                );
                telemetry.addData("Currently at",  " at %7d, %7d, %7d, %7d",
                        motorRF.getCurrentPosition(), motorLF.getCurrentPosition(),
                        motorRR.getCurrentPosition(), motorLR.getCurrentPosition()
                );
                telemetry.update();
            }

            // Stop all motion;
            motorRF.setPower(0);
            motorLF.setPower(0);
            motorRR.setPower(0);
            motorLR.setPower(0);

            // Turn off RUN_TO_POSITION
            motorRF.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorLF.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorRR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorLR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

}
