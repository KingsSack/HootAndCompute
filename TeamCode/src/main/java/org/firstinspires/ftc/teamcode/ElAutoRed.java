package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
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
    static final double DRIVE_SPEED = 1;
    public enum State
    {
        DETECT,
        NO_DETECT,
        MOVE_FORWARD,
        PROP_LEFT,
        PROP_RIGHT,
        PROP_CENTER,
        LEAVE_PIXEL,
        MOVE_TO_BACKSTAGE,
        STOP
    }
    public State state = State.DETECT;
    public enum Locations
    {
        RIGHT,
        LEFT,
        CENTER
    }
    public Locations propLocation = Locations.CENTER;

    void initialize() {
        // Initialize
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        imu = hardwareMap.get(IMU.class, "imu");

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);

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
                            if (x > 400) {
                                // If prop is on the right, set state to PROP_RIGHT
                                propLocation = Locations.RIGHT;
                            }
                            else if (x < 240) {
                                // If prop is on the left, set state to PROP_LEFT
                                propLocation = Locations.LEFT;
                            }
                            else {
                                // If prop is in the center, set state to PROP_CENTER
                                propLocation = Locations.CENTER;
                            }
                            state = State.MOVE_FORWARD;
                            break;
                        }
                    case NO_DETECT:
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
                    case MOVE_FORWARD:
                        move(DRIVE_SPEED, 20);
                        switch (propLocation) {
                            case RIGHT:
                                state = State.PROP_RIGHT;
                                break;
                            case LEFT:
                                state = State.PROP_LEFT;
                                break;
                            case CENTER:
                                state = State.PROP_CENTER;
                                break;
                        }
                        break;
                    case PROP_RIGHT:
                        // Strafe right a specific number of rotations
                        // Move forward a specific number of rotations
                        // Set state to LEAVE_PIXEL
                        state = State.LEAVE_PIXEL;
                        break;
                    case PROP_LEFT:
                        // Rotate left towards prop
                        // Move forward a specific number of rotations
                        // Set state to LEAVE_PIXEL
                        state = State.LEAVE_PIXEL;
                        break;
                    case PROP_CENTER:
                        // Move forward a specific number of rotations
                        move(DRIVE_SPEED, 4);
                        // Set state to LEAVE_PIXEL
                        state = State.LEAVE_PIXEL;
                        break;
                    case LEAVE_PIXEL:
                        // Move backwards
                        move(DRIVE_SPEED, -24);
                        // When back at starting position, set state to MOVE_TO_BACKSTAGE
                        state = State.MOVE_TO_BACKSTAGE;
                        break;
                    case MOVE_TO_BACKSTAGE:
                        // Strafe towards backstage
                        strafe(DRIVE_SPEED / 3, 24);
                        // If close to backstage, leave second pixel
                        strafe(DRIVE_SPEED, -4);
                        // Set state to STOP
                        state = State.STOP;
                        break;
                    case STOP:
                        // Stop the robot
                        halt();
                        break;
                }

                // Telemetry data
                telemetry.addData(">", "Movement: %5.2f, %5.2f", rightFrontDrive.getPower(), leftFrontDrive.getPower());
                telemetry.addData(">", "Angular Velocity: %s", imu.getRobotAngularVelocity(AngleUnit.DEGREES).xRotationRate + imu.getRobotAngularVelocity(AngleUnit.DEGREES).yRotationRate);
                telemetry.update();
            }
        }

        // Stop
        telemetry.addData("Status", "Stopped");
        telemetry.update();
    }

    void move(double power, double distance) {
        encoderDrive(power, distance, distance, 5.0, 1);
    }

    void strafe(double power, double distance) {
        encoderDrive(power, -distance, distance, 5.0, (float) Math.sqrt(2));
    }

    public void encoderDrive(double speed, double leftInches, double rightInches, double timeoutS, float multiplier) {
        int newRFTarget;
        int newLFTarget;
        int newRRTarget;
        int newLRTarget;

        // Determine new target position, and pass to motor controller
        newLFTarget =  leftFrontDrive.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH / multiplier);
        newRFTarget = rightFrontDrive.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH / multiplier);
        newLRTarget = leftRearDrive.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH / multiplier);
        newRRTarget = rightRearDrive.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH / multiplier);
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

        while (runtime.seconds() < timeoutS && leftFrontDrive.isBusy() && rightFrontDrive.isBusy() && leftRearDrive.isBusy() && rightRearDrive.isBusy()) {
            // Display it for the driver.
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
        leftFrontDrive.setPower(0);
        rightFrontDrive.setPower(0);
        leftRearDrive.setPower(0);
        rightRearDrive.setPower(0);

        // Turn off RUN_TO_POSITION
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftRearDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightRearDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    void halt() {
        leftFrontDrive.setPower(0);
        rightFrontDrive.setPower(0);
        leftRearDrive.setPower(0);
        rightRearDrive.setPower(0);
    }

    private Recognition detectProp() {
        List<Recognition> currentRecognitions = tfod.getRecognitions();
        telemetry.addData("# Objects Detected", currentRecognitions.size());
        for (Recognition recognition : currentRecognitions) {
            if (recognition.getLabel().equals("prop")) {
                return recognition;
            }
        }
        return null;
    }
}
