package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

@Config
public final class Configuration {
    public static class ManualParams {
        // control parameters
        public double deadzone = 0.05; // Minimum joystick input to register
        public double minPower = 0.05; // Minimum power to move motors
        public double turnScale = 0.8; // Reduce turn sensitivity
        public double inputExp = 2.0;  // Input exponential for fine control

        // speeds
        public double turbo = 1.0;
        public double normal = 0.75;
        public double precise = 0.5;
    }
    public static ManualParams whaleParams = new ManualParams();

    public static class DriveParams {
        // IMU orientation
        public RevHubOrientationOnRobot.LogoFacingDirection logoFacingDirection = RevHubOrientationOnRobot.LogoFacingDirection.LEFT;
        public RevHubOrientationOnRobot.UsbFacingDirection usbFacingDirection = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

        // drive model parameters
        public double inPerTick = 0.0243902439;
        public double lateralInPerTick = inPerTick;
        public double trackWidthTicks = 1290.0;

        // feedforward parameters (in tick units)
        public double kS = 0.86;
        public double kV = 0.0043;
        public double kA = 0.0000001;

        // path profile parameters (in inches)
        public double maxWheelVel = 50;
        public double minProfileAccel = -30;
        public double maxProfileAccel = 50;

        // turn profile parameters (in radians)
        public double maxAngVel = Math.PI; // shared with path
        public double maxAngAccel = Math.PI;

        // path controller gains
        public double axialGain = 0.0;
        public double lateralGain = 0.0;
        public double headingGain = 0.0; // shared with turn

        public double axialVelGain = 0.0;
        public double lateralVelGain = 0.0;
        public double headingVelGain = 0.0; // shared with turn
    }
    public static DriveParams driveParams = new DriveParams();

    public static class FieldParams {
        // basket
        public double basketX = 60.0;
        public double basketY = 60.0;
        public int lowerBasketHeight = 500;
        public int upperBasketHeight = 500;

        // observation zone
        public double observationX = -60.0;
        public double observationY = 60.0;

        // sample positions
        public double[] samplePositionsX = new double[]{0.0, 0.0, 0.0};
        public double[] samplePositionsY = new double[]{0.0, 0.0, 0.0};
    }
    public static FieldParams fieldParams = new FieldParams();

    public static class OtterParams {
        // inital
        public double initialX = 24.0;
        public double initialY = 60.0;
        public double initialHeading = -90.0;

        // preloaded
        public boolean isPreloaded = false;

        // number of samples to collect
        public int numSamples = 0;
    }
    public static OtterParams otterLParams = new OtterParams();
    public static OtterParams otterRParams = new OtterParams();
    public static OtterParams otterTestParams = new OtterParams();

    public static class TestParams {
        // inital
        public double initialX = 24.0;
        public double initialY = 60.0;
        public double initialHeading = -90.0;
    }
    public static TestParams testParams = new TestParams();

    public static class LiftParams {
        // constants
        public int maxPosition = 1900;
        public double maxPower = 0.8;
    }
    public static LiftParams liftParams = new LiftParams();

    public static class ExtenderParams {
        // constants
        public double minPosition = 0.0;
        public double maxPosition = 1.0;
    }
    public static ExtenderParams extenderParams = new ExtenderParams();

    public static class ClawParams {
        // constants
        public double maxPower = 0.72;
        public double openCloseTime = 0.6;
    }
    public static ClawParams clawParams = new ClawParams();
}
