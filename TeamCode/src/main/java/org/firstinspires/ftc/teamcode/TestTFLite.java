package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

import java.util.List;

@TeleOp(name = "Object Detection", group = "Test")
public class TestTFLite extends LinearOpMode {
    private TfodProcessor tfod;
    private String TFOD_MODEL_NAME = "red_prop.tflite";
    private String[] LABELS = {
            "prop"
    };

    private VisionPortal visionPortal;

    // Motors
    DcMotor leftFrontDrive;
    DcMotor rightFrontDrive;
    DcMotor leftRearDrive;
    DcMotor rightRearDrive;

    @Override
    public void runOpMode() {
        initTfod();

        rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
        rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
        leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");

        // Wait for the DS start button to be touched.
        telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
        telemetry.addData(">", "Touch Play to start OpMode");
        telemetry.update();
        waitForStart();

        if (opModeIsActive()) {
            while (opModeIsActive()) {
                // Telemetry data
                telemetryTfod();
                telemetry.update();

                // Share the CPU.
                sleep(20);

                // Movement
                double rightTurn = gamepad1.left_stick_y-gamepad1.right_stick_x;
                double leftTurn = -gamepad1.left_stick_y-gamepad1.right_stick_x;

                double rightFrontPower = rightTurn+gamepad1.left_stick_x;
                double leftFrontPower = leftTurn+gamepad1.left_stick_x;
                double rightRearPower = rightTurn-gamepad1.left_stick_x;
                double leftRearPower = leftTurn-gamepad1.left_stick_x;

                rightFrontDrive.setPower(rightFrontPower);
                leftFrontDrive.setPower(leftFrontPower);
                rightRearDrive.setPower(rightRearPower);
                leftRearDrive.setPower(leftRearPower);
            }
        }
    }

    private void initTfod() {
        // Create the TensorFlow processor the easy way.
        // tfod = TfodProcessor.easyCreateWithDefaults();
        tfod = new TfodProcessor.Builder()
                .setModelFileName(TFOD_MODEL_NAME)
                .setModelLabels(LABELS)
                .setIsModelQuantized(true)
                .setIsModelTensorFlow2(true)
                .setModelInputSize(300)
                .setModelAspectRatio(16.0 / 9.0)
                .build();
        tfod.setMinResultConfidence(0.7f);

        // Create the vision portal the easy way.
        visionPortal = VisionPortal.easyCreateWithDefaults(
                hardwareMap.get(WebcamName.class, "Webcam 1"), tfod);
    }

    private void telemetryTfod() {
        List<Recognition> currentRecognitions = tfod.getRecognitions();
        telemetry.addData("# Objects Detected", currentRecognitions.size());

        // Step through the list of recognitions and display info for each one.
        for (Recognition recognition : currentRecognitions) {
            double x = (recognition.getLeft() + recognition.getRight()) / 2;
            double y = (recognition.getTop()  + recognition.getBottom()) / 2;

            telemetry.addData(""," ");
            telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(), recognition.getConfidence() * 100);
            telemetry.addData("- Position", "%.0f / %.0f", x, y);
            telemetry.addData("- Size", "%.0f x %.0f", recognition.getWidth(), recognition.getHeight());
        }
    }
}
