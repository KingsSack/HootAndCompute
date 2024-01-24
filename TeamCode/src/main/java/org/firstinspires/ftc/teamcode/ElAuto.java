package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

@Autonomous(name = "El Auto", group = "Competition")
public class ElAuto extends LinearOpMode {
    IMU imu;
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;
    DcMotor hookLift;

    private ElapsedTime runtime = new ElapsedTime();
    static final double COUNTS_PER_MOTOR_REV = 1440; // eg: TETRIX Motor Encoder
    static final double DRIVE_GEAR_REDUCTION = 1.0; // No external gearing
    static final double WHEEL_DIAMETER_INCHES = 4.0; // For circumference
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double DRIVE_SPEED = .8;

    void initialize() {
        // Initialize
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        imu = hardwareMap.get(IMU.class, "imu");

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftRearDrive.setDirection(DcMotor.Direction.REVERSE);

        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftRearDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightRearDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftRearDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightRearDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addData("Starting at", "%7d, %7d, %7d, %7d",
                leftFrontDrive.getCurrentPosition(), rightFrontDrive.getCurrentPosition(),
                leftRearDrive.getCurrentPosition(), rightRearDrive.getCurrentPosition()
        );

        imu.resetYaw();

        hookLift = hardwareMap.get(DcMotor.class, "HookLift");

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    @Override
    public void runOpMode() {
        initialize();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // Run
        if (opModeIsActive()) {
            strafe(DRIVE_SPEED, -48);
        }
    }

    void strafe(double power, double distance) {
        // Use encoder drive to strafe a specified distance in inches
        encoderDrive(power, -distance, distance, distance, -distance);
    }

    public void encoderDrive(double speed, double inchesLF, double inchesRF, double inchesLR, double inchesRR) {
        // Determine new target position, and pass to motor controller
        int newLFTarget = leftFrontDrive.getCurrentPosition() + (int)(inchesLF * COUNTS_PER_INCH);
        int newRFTarget = rightFrontDrive.getCurrentPosition() + (int)(inchesRF * COUNTS_PER_INCH);
        int newLRTarget = leftRearDrive.getCurrentPosition() + (int)(inchesLR * COUNTS_PER_INCH);
        int newRRTarget = rightRearDrive.getCurrentPosition() + (int)(inchesRR * COUNTS_PER_INCH);
        leftFrontDrive.setTargetPosition(newLFTarget);
        rightFrontDrive.setTargetPosition(newRFTarget);
        leftRearDrive.setTargetPosition(newLRTarget);
        rightRearDrive.setTargetPosition(newRRTarget);

        // Turn On RUN_TO_POSITION
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftRearDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightRearDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // Reset the timeout time and start motion
        runtime.reset();
        leftFrontDrive.setPower(Math.abs(speed));
        rightFrontDrive.setPower(Math.abs(speed));
        leftRearDrive.setPower(Math.abs(speed));
        rightRearDrive.setPower(Math.abs(speed));

        while (leftFrontDrive.isBusy()) {
            telemetry.addData("Running to",  " %7d, %7d, %7d, %7d",
                    newRFTarget,  newLFTarget,
                    newRRTarget, newLRTarget
            );
            telemetry.addData("Currently at",  " at %7d, %7d, %7d, %7d",
                    leftFrontDrive.getCurrentPosition(), rightFrontDrive.getCurrentPosition(),
                    leftRearDrive.getCurrentPosition(), rightRearDrive.getCurrentPosition()
            );
            telemetry.update();
        }

        // Stop all motion;
        halt();

        // Turn off RUN_TO_POSITION
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftRearDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightRearDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    void halt() {
        // Stop all motors
        leftFrontDrive.setPower(0);
        rightFrontDrive.setPower(0);
        leftRearDrive.setPower(0);
        rightRearDrive.setPower(0);
    }

    void lift() {
        while(hookLift.getCurrentPosition() < 260) {
            hookLift.setPower(.8);
        }
        hookLift.setPower(0);
    }
}
