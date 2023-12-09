package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;

@TeleOp(name = "El Manual", group = "Competition")
public class ElManual extends OpMode {

   // Connected Devices
   private Blinker debug_light;
   private HardwareDevice robot;
   private IMU imu;

   // Motors
   DcMotor leftFrontDrive;
   DcMotor rightFrontDrive;
   DcMotor leftRearDrive;
   DcMotor rightRearDrive;
   DcMotor hookLift;
   DcMotor hook;

   // Booleans
   boolean slowDown = false;
   boolean intakeOn = true;

   // Servos
   CRServo intake;
   CRServo launcher;
   CRServo conveyor;

   // Initialize
   @Override
   public void init() {
       rightFrontDrive = hardwareMap.get(DcMotor.class, "MotorRF");
       leftFrontDrive = hardwareMap.get(DcMotor.class, "MotorLF");
       rightRearDrive = hardwareMap.get(DcMotor.class, "MotorRR");
       leftRearDrive = hardwareMap.get(DcMotor.class, "MotorLR");

       intake = hardwareMap.get(CRServo.class, "Sweeper");
       launcher = hardwareMap.get(CRServo.class, "Launcher");

       hookLift = hardwareMap.get(DcMotor.class, "HookLift");
       hook = hardwareMap.get(DcMotor.class, "Hook");

       conveyor = hardwareMap.get(CRServo.class, "Conveyor");
   }

   // Robot Loop
   @Override
   public void loop() {
       double rightTurn = -gamepad1.left_stick_y-gamepad1.right_stick_x;
       double leftTurn = gamepad1.left_stick_y-gamepad1.right_stick_x;

       double rightFrontPower = rightTurn-gamepad1.left_stick_x;
       double leftFrontPower = leftTurn-gamepad1.left_stick_x;
       double rightRearPower = rightTurn+gamepad1.left_stick_x;
       double leftRearPower = leftTurn+gamepad1.left_stick_x;

       if (gamepad1.left_bumper) {
           slowDown = true;
       } else {
           slowDown = false;
       }

       if (gamepad2.left_bumper) {
           launcher.setPower(.7);
       }
       else {
           launcher.setPower(0);
       }

       if (gamepad2.dpad_down) {
           intakeOn = !intakeOn;
       }

       if (gamepad2.b) {
           int pos = hookLift.getCurrentPosition();

           if (gamepad2.right_bumper) {
               if (hookLift.getCurrentPosition() < 640) {
                   hookLift.setPower(.6);
               }
               else {
                   hookLift.setPower(0);
               }
           }
           else {
               if (hookLift.getCurrentPosition() < 180) {
                   hookLift.setPower(.75);
               }
               else {
                   hookLift.setPower(0);
               }
           }
           // hookLift.setTargetPosition(hookLift.getCurrentPosition() + 40);
       }

       if (gamepad2.y) {
           hook.setPower(.75);
       }
       else if (gamepad2.a) {
           hook.setPower(-1);
       }
       else {
           hook.setPower(0);
       }

       if (slowDown) {
           rightFrontPower /= 3;
           leftFrontPower /= 3;
           rightRearPower /= 3;
           leftRearPower /= 3;
       }

       rightFrontDrive.setPower(rightFrontPower);
       leftFrontDrive.setPower(leftFrontPower);
       rightRearDrive.setPower(rightRearPower);
       leftRearDrive.setPower(leftRearPower);

       intake.setPower(intakeOn ? -.7 : 0);
       conveyor.setPower(intakeOn ? -.5 : 0);

       telemetry.addData("Movement", "RF %5.2f, LF %5.2f, RR %5.2f, LR %5.2f",
               rightFrontPower,
               leftFrontPower,
               rightRearPower,
               leftRearPower);
       telemetry.addData("Intake", "On: %b, Power: %5.2f", intakeOn, intake.getPower());
       telemetry.addData("Hook", "Position: %s, Power: %5.2f", hookLift.getCurrentPosition(), hookLift.getPower());
       telemetry.update();
   }
}
