package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.*
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.*
import dev.kingssack.volt.robot.SimpleRobotWithMecanumDrive

/**
 * Test is a test robot.
 *
 * @param hardwareMap for initializing hardware components
 * @param initialPose for setting the initial pose
 */
@Config
class Test(hardwareMap: HardwareMap, initialPose: Pose2d) :
        SimpleRobotWithMecanumDrive(
                hardwareMap,
                initialPose,
                DriveParams(
                        logoFacingDirection = logoFacingDirection,
                        usbFacingDirection = usbFacingDirection,
                        inPerTick = inPerTick,
                        lateralInPerTick = lateralInPerTick,
                        trackWidthTicks = trackWidthTicks,
                        kS = kS,
                        kV = kV,
                        kA = kA,
                        maxWheelVel = maxWheelVel,
                        minProfileAccel = minProfileAccel,
                        maxProfileAccel = maxProfileAccel,
                        maxAngVel = maxAngVel,
                        maxAngAccel = maxAngAccel,
                        axialGain = axialGain,
                        lateralGain = lateralGain,
                        headingGain = headingGain
                )
        ) {
    /** Params is a companion object that holds the configuration for the robot. */
    companion object Params {
        @JvmField var logoFacingDirection: LogoFacingDirection = LogoFacingDirection.LEFT
        @JvmField var usbFacingDirection: UsbFacingDirection = UsbFacingDirection.FORWARD

        @JvmField var inPerTick: Double = 0.0227
        @JvmField var lateralInPerTick: Double = 0.02
        @JvmField var trackWidthTicks: Double = 1297.32

        @JvmField var kS: Double = 0.9134
        @JvmField var kV: Double = 0.0043
        @JvmField var kA: Double = 0.001

        @JvmField var maxWheelVel: Double = 60.0
        @JvmField var minProfileAccel: Double = -30.0
        @JvmField var maxProfileAccel: Double = 60.0

        @JvmField var maxAngVel: Double = Math.PI
        @JvmField var maxAngAccel: Double = Math.PI

        @JvmField var axialGain: Double = 5.0
        @JvmField var lateralGain: Double = 4.0
        @JvmField var headingGain: Double = 3.0
    }

    // Sensors

    // Attachments

    init {
        attachments = listOf()
    }
}