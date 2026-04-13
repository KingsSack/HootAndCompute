package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.rr.mecanum.DriveEncoderMecanumRoadRunnerDrivetrain
import dev.kingssack.volt.attachment.drivetrain.rr.mecanum.MecanumRoadRunnerDrivetrain

/**
 * [Jones] with [DriveEncoderMecanumRoadRunnerDrivetrain] drivetrain.
 *
 * @param hardwareMap The FTC hardware map.
 * @param initialPose The initial pose of the robot.
 */
@Config
class JonesRR(hardwareMap: HardwareMap, initialPose: Pose2d = Pose2d(Vector2d(0.0, 0.0), 0.0)) :
    Jones<DriveEncoderMecanumRoadRunnerDrivetrain>(
        hardwareMap,
        DriveEncoderMecanumRoadRunnerDrivetrain(
            hardwareMap,
            initialPose,
            MecanumRoadRunnerDrivetrain.DriveParams(
                logoFacingDirection = logoFacingDirection,
                usbFacingDirection = usbFacingDirection,
                leftFrontName = leftFrontName,
                leftFrontDirection = leftFrontDirection,
                leftBackName = leftBackName,
                leftBackDirection = leftBackDirection,
                rightBackName = rightBackName,
                rightBackDirection = rightBackDirection,
                rightFrontName = rightFrontName,
                rightFrontDirection = rightFrontDirection,
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
                headingGain = headingGain,
            ),
        ),
    ) {
    companion object {
        @JvmField var logoFacingDirection: LogoFacingDirection = LogoFacingDirection.UP
        @JvmField var usbFacingDirection: UsbFacingDirection = UsbFacingDirection.RIGHT

        @JvmField var leftFrontName: String = "lf"
        @JvmField var leftFrontDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        @JvmField var leftBackName: String = "lr"
        @JvmField var leftBackDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        @JvmField var rightBackName: String = "rr"
        @JvmField var rightBackDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.REVERSE
        @JvmField var rightFrontName: String = "rf"
        @JvmField var rightFrontDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.REVERSE

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
}
