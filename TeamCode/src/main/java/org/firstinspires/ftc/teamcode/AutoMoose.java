package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Autonomous(name = "Auto Moose", group = "Autonomous")
public class AutoMoose extends LinearOpMode {
    // Connected Devices
    private Blinker debug_light;
    private HardwareDevice robot;
    private IMU imu;

    // Variables
    final double DESIRED_DISTANCE = 12.0;
    final double SPEED_GAIN = 0.05;
    final double TURN_GAIN = 0.025;

    // Motors
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;

    // Desired Tag
    private static final int DESIRED_TAG_ID = 1;
    private VisionPortal visionPortal;
    private AprilTagProcessor aprilTag;
    private AprilTagDetection desiredTag = null;

    // Initialize
    @Override
    public void runOpMode() {
        // Target
        boolean targetFound;
        double leftPower;
        double rightPower;

        // Vision Portal
        initAprilTag();

        // Motors
        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");

        // Webcam
        setManualExposure(6, 250);

        // Ready
        telemetry.addData("", "Press play to start");
        telemetry.update();
        waitForStart();

        // Loop
        while (opModeIsActive())
        {
            targetFound = false;
            desiredTag = null;

            // Step through the list of detected tags and look for a matching tag
            List<AprilTagDetection> currentDetections = aprilTag.getDetections();
            for (AprilTagDetection detection : currentDetections) {
                if ((detection.metadata != null) && ((DESIRED_TAG_ID < 0) || (detection.id == DESIRED_TAG_ID))) {
                    targetFound = true;
                    desiredTag = detection;
                    break;  // don't look any further.
                } else {
                    telemetry.addData("Unknown Target", "Tag ID %d is not in TagLibrary\n", detection.id);
                }
            }

            // Tell the driver what we see, and what to do.
            if (targetFound) {
                telemetry.addData(">","HOLD Left-Bumper to Drive to Target\n");
                telemetry.addData("Target", "ID %d (%s)", desiredTag.id, desiredTag.metadata.name);
                telemetry.addData("Range",  "%5.1f inches (%5.1f feet)", desiredTag.ftcPose.range, desiredTag.ftcPose.range / 12);
                telemetry.addData("Bearing","%3.0f degrees", desiredTag.ftcPose.bearing);
            } else {
                telemetry.addData(">","Drive using joysticks to find valid target\n");
            }

            // If Left Bumper is being pressed, AND we have found the desired target, Drive to target Automatically .
            if (gamepad1.left_bumper && targetFound) {
                // Determine heading and range error, so we can use them to control the robot automatically.
                double range = (desiredTag.ftcPose.range - DESIRED_DISTANCE);
                double bearing = desiredTag.ftcPose.bearing;

                rightPower = 0;
                leftPower = 0;

                if (bearing >= -3 && bearing <= 3) {
                    double defaultPower = range * SPEED_GAIN;
                    rightPower = -defaultPower;
                    leftPower = defaultPower;
                }
                else {
                    if (bearing > 0) {
                        rightPower = bearing * TURN_GAIN;
                    }
                    else {
                        leftPower = bearing * TURN_GAIN;
                    }
                }

                telemetry.addData("Auto","Right %5.2f, Left %5.2f", rightPower, leftPower);
            } else {
                // Drive using manual POV Joystick mode.
                rightPower = gamepad1.left_stick_y-gamepad1.left_stick_x;
                leftPower = -gamepad1.left_stick_y-gamepad1.left_stick_x;

                telemetry.addData("Manual","Right %5.2f, Left %5.2f", rightPower, leftPower);
            }
            telemetry.update();

            // Apply desired axes motions to the drivetrain.
            moveRobot(rightPower, leftPower);
            sleep(10);
        }
    }

    public void moveRobot(double rightPower, double leftPower) {
        // Normalize wheel powers to be less than 1.0
        double max = Math.max(Math.abs(rightPower), Math.abs(leftPower));
        if (max > 1.0) {
            rightPower /= max;
            leftPower /= max;
        }

        // Send powers to the wheels.
        rightFrontDrive.setPower(rightPower);
        leftFrontDrive.setPower(leftPower);
        rightRearDrive.setPower(rightPower);
        leftRearDrive.setPower(leftPower);
    }

    private void initAprilTag() {
        // Create the AprilTag processor by using a builder.
        aprilTag = new AprilTagProcessor.Builder().build();

        // Create the vision portal by using a builder.
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTag)
                .build();
    }

    private void setManualExposure(int exposureMS, int gain) {
        if (visionPortal == null) {
            return;
        }

        // Make sure camera is streaming before we try to set the exposure controls
        if (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            telemetry.addData("Camera", "Waiting");
            telemetry.update();
            while (!isStopRequested() && (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING)) {
                sleep(20);
            }
            telemetry.addData("Camera", "Ready");
            telemetry.update();
        }

        // Set camera controls unless we are stopping.
        if (!isStopRequested())
        {
            ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
            if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
                exposureControl.setMode(ExposureControl.Mode.Manual);
                sleep(50);
            }
            exposureControl.setExposure((long)exposureMS, TimeUnit.MILLISECONDS);
            sleep(20);
            GainControl gainControl = visionPortal.getCameraControl(GainControl.class);
            gainControl.setGain(gain);
            sleep(20);
            telemetry.addData("Camera", "Ready");
            telemetry.update();
        }
    }
}
