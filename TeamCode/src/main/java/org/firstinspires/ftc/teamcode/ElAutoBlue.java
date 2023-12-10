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

@Autonomous(name = "El Auto Blue", group = "Competition")
public class ElAutoBlue extends LinearOpMode {
    IMU imu;
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;
    TfodProcessor tfod;
    AprilTagProcessor aprilTag;
    VisionPortal visionPortal;
    CRServo intake;
    CRServo launcher;

    // Constants
    private String TFOD_MODEL_NAME = "red_prop.tflite";
    private String[] LABELS = {
            "prop"
    };

    // Robot Variables
    private double MAX_SPEED = .75;
    private double TURN_GAIN = .02;
    public enum states
    {
        DETECT,
        NO_DETECT,
        DROP_PIXEL,
        TURN_TOWARDS_BACKSTAGE,
        MOVE_TO_BACKSTAGE,
        STOP
    }
    public states state = states.DETECT;

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

                Recognition prop = propDetected();

                // State machine
                switch (state) {
                    case DETECT:
                        if (prop == null) {
                            // If not detected, go to NO_DETECT state
                            resetPower();
                            state = states.NO_DETECT;
                            break;
                        }
                        else {
                            // If detected, move towards the prop
                            double x = (prop.getLeft() + prop.getRight()) / 2;
                            move(MAX_SPEED, MAX_SPEED);
                        }
                        if (prop.getHeight() > 288) {
                            // When close to the prop, go to DROP_PIXEL state
                            resetPower();
                            state = states.DROP_PIXEL;
                            break;
                        }
                        break;
                    case NO_DETECT:
                        // Move forward a bit
                        move(MAX_SPEED, MAX_SPEED);
                        sleep(500);
                        if (prop != null) {
                            // If detected, go to DETECT state
                            resetPower();
                            state = states.DETECT;
                            break;
                        }
                        else {
                            // Else, go to backstage
                            resetPower();
                            state = states.TURN_TOWARDS_BACKSTAGE;
                            break;
                        }
                    case DROP_PIXEL:
                        // Move backwards
                        move(-MAX_SPEED, -MAX_SPEED);
                        sleep(900);
                        resetPower();
                        state = states.TURN_TOWARDS_BACKSTAGE;
                        break;
                    case TURN_TOWARDS_BACKSTAGE:
                        double yaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
                        if (yaw > 80) {
                            // Set state to MOVE_TO_BACKSTAGE
                            resetPower();
                            state = states.MOVE_TO_BACKSTAGE;
                            break;
                        }
                        else {
                            // Spin towards backstage
                            spin(90);
                        }
                        break;
                    case MOVE_TO_BACKSTAGE:
                        // Move towards backstage
                        move(MAX_SPEED / 3, MAX_SPEED / 3);
                        sleep(1000);
                        /* AngularVelocity angularVelocity = imu.getRobotAngularVelocity(AngleUnit.DEGREES);
                        if (angularVelocity.xRotationRate + angularVelocity.yRotationRate < 2) {
                            resetPower();
                            state = states.STOP;
                            break;
                        } */
                        break;
                    case STOP:
                        resetPower();
                        break;
                }

                // Telemetry data
                telemetry.addData("", "");
                telemetry.addData(">", "Movement: %5.2f, %5.2f", rightFrontDrive.getPower(), leftFrontDrive.getPower());
                telemetry.addData(">", "%s", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
                if (prop != null) {
                    telemetry.addData(">", "%s", prop.toString());
                }
                telemetry.update();

                // Share the CPU
                sleep(20);
            }
        }

        // Stop
        telemetry.addData("Status", "Stopped");
        telemetry.update();
    }

    void spin(double expectedHeading) {
        // Spin
        double heading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        double error = expectedHeading - heading;

        move(-error * TURN_GAIN, error * TURN_GAIN);
    }

    void move(double leftPower, double rightPower) {
        double max = Math.max(Math.abs(rightPower), Math.abs(leftPower));
        if (max > MAX_SPEED) {
            rightPower /= max;
            leftPower /= max;
        }

        telemetry.addData(">", "Moving, Left: %5.2f, Right: %5.2f", leftPower, rightPower);

        leftFrontDrive.setPower(-leftPower);
        rightFrontDrive.setPower(rightPower);
        leftRearDrive.setPower(-leftPower);
        rightRearDrive.setPower(rightPower);
    }

    void resetPower() {
        leftFrontDrive.setPower(0);
        rightFrontDrive.setPower(0);
        leftRearDrive.setPower(0);
        rightRearDrive.setPower(0);
    }

    private Recognition propDetected() {
        List<Recognition> currentRecognitions = tfod.getRecognitions();
        telemetry.addData("# Objects Detected", currentRecognitions.size());
        for (Recognition recognition : currentRecognitions) {
            if (recognition.getLabel().equals("prop")) {
                return recognition;
            }
        }
        return null;
    }

    @SuppressLint("DefaultLocale")
    private void detectAprilTag() {
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("# AprilTags Detected", currentDetections.size());

        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw));
                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
            } else {
                telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
                telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
            }
        }

        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
        telemetry.addLine("RBE = Range, Bearing & Elevation");
    }
}
