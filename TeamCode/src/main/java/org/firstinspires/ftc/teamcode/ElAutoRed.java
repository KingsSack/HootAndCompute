package org.firstinspires.ftc.teamcode;

import android.annotation.SuppressLint;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

import java.util.List;

@Autonomous(name = "El Auto Red", group = "Competition")
public class ElAutoRed extends LinearOpMode {
    IMU imu;
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;
    TfodProcessor tfod;
    AprilTagProcessor aprilTag;
    VisionPortal visionPortal;

    // Constants
    private String TFOD_MODEL_NAME = "red_prop.tflite";
    private String[] LABELS = {
            "prop"
    };

    // Robot Variables
    private double MAX_SPEED = .75;
    private double TURN_GAIN = .02;
    public enum State
    {
        DETECT,
        NO_DETECT,
        PROP_LEFT,
        PROP_RIGHT,
        PROP_CENTER,
        LEAVE_PIXEL,
        MOVE_TO_BACKSTAGE,
        STOP
    }
    public State state = State.DETECT;

    void Init() {
        // Initialize
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");
        imu = hardwareMap.get(IMU.class, "imu");

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
        Init();

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
                                state = State.PROP_RIGHT;
                                break;
                            }
                            else if (x < 240) {
                                // If prop is on the left, set state to PROP_LEFT
                                state = State.PROP_LEFT;
                                break;
                            }
                            else {
                                // If prop is in the center, set state to PROP_CENTER
                                state = State.PROP_CENTER;
                                break;
                            }
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
                    case PROP_RIGHT:
                        // Strafe right a specific number of rotations
                        // Move forward a specific number of rotations
                        // Set state to LEAVE_PIXEL
                        break;
                    case PROP_LEFT:
                        // Rotate left towards prop
                        // Move forward a specific number of rotations
                        // Set state to LEAVE_PIXEL
                        break;
                    case PROP_CENTER:
                        // Move forward a specific number of rotations
                        // Set state to LEAVE_PIXEL
                        break;
                    case LEAVE_PIXEL:
                        // Move backwards
                        move(-MAX_SPEED);
                        halt();
                        // When back at starting position, set state to MOVE_TO_BACKSTAGE
                        break;
                    case MOVE_TO_BACKSTAGE:
                        // Strafe towards backstage
                        strafe(MAX_SPEED / 3);
                        // If close to backstage, leave second pixel
                        // Set state to STOP
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

    void move(double power) {
        leftFrontDrive.setPower(power);
        rightFrontDrive.setPower(power);
        leftRearDrive.setPower(power);
        rightRearDrive.setPower(power);
    }

    void strafe(double power) {
        leftFrontDrive.setPower(power);
        rightFrontDrive.setPower(power);
        leftRearDrive.setPower(-power);
        rightRearDrive.setPower(-power);
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
