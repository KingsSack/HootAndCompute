package org.firstinspires.ftc.teamcode.util

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection
import com.qualcomm.robotcore.hardware.DcMotorSimple

@Config
object DriveParams {
    // Motor names
    @JvmField
    var leftFrontName: String = "lf"
    @JvmField
    var leftBackName: String = "lr"
    @JvmField
    var rightFrontName: String = "rf"
    @JvmField
    var rightBackName: String = "org/firstinspires/ftc/teamcode/rrg/firstinspires/ftc/teamcode/rr"

    // Motor directions
    @JvmField
    var leftFrontDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
    @JvmField
    var leftBackDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
    @JvmField
    var rightFrontDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.REVERSE
    @JvmField
    var rightBackDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.REVERSE

    // IMU orientation
    @JvmField
    var logoFacingDirection: LogoFacingDirection = LogoFacingDirection.LEFT
    @JvmField
    var usbFacingDirection: UsbFacingDirection = UsbFacingDirection.FORWARD

    // drive model parameters
    @JvmField
    var inPerTick: Double = 0.0227
    @JvmField
    var lateralInPerTick: Double = 0.0238
    @JvmField
    var trackWidthTicks: Double = 1297.32

    // feedforward parameters (in tick units)
    @JvmField
    var kS: Double = 0.9134 // 1.06 // Represents the static force
    @JvmField
    var kV: Double = 0.0043 // Represents the linear velocity
    @JvmField
    var kA: Double = 0.001 // Represents the acceleration

    // path profile parameters (in inches)
    @JvmField
    var maxWheelVel: Double = 30.0
    @JvmField
    var minProfileAccel: Double = -30.0
    @JvmField
    var maxProfileAccel: Double = 40.0

    // turn profile parameters (in radians)
    @JvmField
    var maxAngVel: Double = Math.PI // shared with path
    @JvmField
    var maxAngAccel: Double = Math.PI

    // path controller gains
    @JvmField
    var axialGain: Double = 5.0
    @JvmField
    var lateralGain: Double = 4.0
    @JvmField
    var headingGain: Double = 1.0 // shared with turn

    @JvmField
    var axialVelGain: Double = 0.0
    @JvmField
    var lateralVelGain: Double = 0.0
    @JvmField
    var headingVelGain: Double = 0.0 // shared with turn
}