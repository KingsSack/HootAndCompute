package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.PtzControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

import java.util.List;

@Autonomous(name = "El Auto Red", group = "Competition")
public class ElAutoRed extends LinearOpMode {
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;
    DcMotor hookLift;
    TfodProcessor tfod;
    AprilTagProcessor aprilTag;
    VisionPortal visionPortal;
    IMU imu;

    // Constants
    private String TFOD_MODEL_NAME = "red_prop.tflite";
    private String[] LABELS = {
            "prop"
    };

    // Robot Variables
    private ElapsedTime runtime = new ElapsedTime();
    static final double COUNTS_PER_MOTOR_REV = 1440; // eg: TETRIX Motor Encoder
    static final double DRIVE_GEAR_REDUCTION = 1.0; // No external gearing
    static final double WHEEL_DIAMETER_INCHES = 4.0; // For circumference
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double DRIVE_SPEED = .8;
    public enum State
    {
        DETECT,
        NO_DETECT,
        PROP_LEFT,
        PROP_RIGHT,
        PROP_CENTER,
        MOVE_TO_BACKSTAGE,
        STOP
    }
    public State state = State.DETECT;

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

        tfod = new TfodProcessor.Builder()
                .setModelFileName(TFOD_MODEL_NAME)
                .setModelLabels(LABELS)
                .setIsModelQuantized(true)
                .setIsModelTensorFlow2(true)
                .setModelInputSize(300)
                .setModelAspectRatio(16.0 / 9.0)
                .build();
        tfod.setMinResultConfidence(0.8f);

        aprilTag = AprilTagProcessor.easyCreateWithDefaults();

        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessors(tfod, aprilTag)
                .build();

        /* PtzControl ptzControl = visionPortal.getCameraControl(PtzControl.class);
        ptzControl.setZoom(100); */

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
            while (opModeIsActive()) {
                telemetry.addData("Status", "Running");
                telemetry.addData("State", "%s", state.toString());

                Recognition prop = detectProp();

                // State machine
                switch (state) {
                    case DETECT:
                        if (prop == null) {
                            // If not detected, go to NO_DETECT state
                            state = State.NO_DETECT;
                            break;
                        }
                        else {
                            // If detected, find where the prop is
                            double x = (prop.getLeft() + prop.getRight()) / 2;
                            telemetry.addData("Prop X", "%5.2f", x);
                            if (x < 320) {
                                // If prop is on the left, set state to PROP_CENTER
                                state = State.PROP_CENTER;
                            }
                            else {
                                // If prop is on the right, set state to PROP_RIGHT
                                state = State.PROP_RIGHT;
                            }
                            break;
                        }
                    case NO_DETECT:
                        // Move forward a tiny bit at a slow speed
                        move(.05, 4);
                        // Check if it sees prop
                        if (prop != null) {
                            // If detected, set state to DETECT
                            state = State.DETECT;
                            break;
                        }
                        else {
                            // Else, set state to MOVE_TO_BACKSTAGE
                            state = State.MOVE_TO_BACKSTAGE;
                            break;
                        }
                    case PROP_RIGHT:
                        // Strafe right a specific number of rotations
                        strafe(DRIVE_SPEED, -8);
                        // Move forward a specific number of rotations
                        move(DRIVE_SPEED, 26);
                        // Move backward a specific number of rotations
                        move(DRIVE_SPEED, -26);
                        // When back at starting position, set state to MOVE_TO_BACKSTAGE
                        state = State.MOVE_TO_BACKSTAGE;
                        break;
                    case PROP_LEFT:
                        // Move forward a specific number of rotations
                        move(DRIVE_SPEED, 26);
                        // Turn 90 degrees
                        turn(90);
                        // Move forward a specific number of rotations
                        move(DRIVE_SPEED, 12);
                        // Turn back -90 degrees
                        turn(-90);
                        // Set state to MOVE_TO_BACKSTAGE
                        state = State.MOVE_TO_BACKSTAGE;
                        break;
                    case PROP_CENTER:
                        // Move forward a specific number of rotations
                        move(DRIVE_SPEED, 31);
                        // Move backward a specific number of rotations
                        move(DRIVE_SPEED, -31);
                        // When back at starting position, set state to MOVE_TO_BACKSTAGE
                        state = State.MOVE_TO_BACKSTAGE;
                        break;
                    case MOVE_TO_BACKSTAGE:
                        // Strafe towards backstage
                        strafe(DRIVE_SPEED, -45);
                        // Lift the arm to release the yellow pixel
                        lift();
                        // Strafe a bit further
                        strafe(DRIVE_SPEED, -5);
                        // Set state to STOP
                        state = State.STOP;
                        break;
                    case STOP:
                        // Stop the robot
                        halt();
                        break;
                }

                // Telemetry data
                telemetry.update();
            }
        }

        // Stop
        telemetry.addData("Status", "Stopped");
        telemetry.update();
    }

    void move(double power, double distance) {
        // Use encoder drive to move forward a specified distance in inches
        encoderDrive(power, distance, distance, distance, distance);
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

        while (leftFrontDrive.isBusy() && rightFrontDrive.isBusy() && leftRearDrive.isBusy() && rightRearDrive.isBusy()) {
            // Keep the camera alive
            Recognition prop = detectProp();
            // Display info for the drivers
            telemetry.addData("State", "%s", state.toString());
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

    void turn(double degrees) {
        double currentOrientation = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        double targetOrientation = currentOrientation + degrees;
        double threshold = 0.5; // You can adjust this value as needed

        while (Math.abs(currentOrientation - targetOrientation) > threshold) {
            currentOrientation = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double difference = currentOrientation - targetOrientation;

            if (difference > 0) {
                // Turn right
                leftFrontDrive.setPower(0.5);
                rightFrontDrive.setPower(-0.5);
                leftRearDrive.setPower(0.5);
                rightRearDrive.setPower(-0.5);
            } else {
                // Turn left
                leftFrontDrive.setPower(-0.5);
                rightFrontDrive.setPower(0.5);
                leftRearDrive.setPower(-0.5);
                rightRearDrive.setPower(0.5);
            }
        }

        // Stop the robot
        halt();
    }

    void halt() {
        // Stop all motors
        leftFrontDrive.setPower(0);
        rightFrontDrive.setPower(0);
        leftRearDrive.setPower(0);
        rightRearDrive.setPower(0);
    }

    void lift() {
        // Get initial arm position
        double initialPos = hookLift.getCurrentPosition();

        // Raise the arm
        while(hookLift.getCurrentPosition() <= initialPos + 260) {
            hookLift.setPower(.8);
        }
        hookLift.setPower(0);
    }

    private Recognition detectProp() {
        // Create a list of current recognitions
        List<Recognition> currentRecognitions = tfod.getRecognitions();
        telemetry.addData("# Objects Detected", currentRecognitions.size());
        for (Recognition recognition : currentRecognitions) {
            if (recognition.getLabel().equals("prop")) {
                // If it detects the prop, return it
                return recognition;
            }
        }
        return null;
    }
}
