package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.pedropathing.control.FilteredPIDFCoefficients
import com.pedropathing.control.PIDFCoefficients
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.Encoder
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.MecanumDriveWithPP
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * [Jones] with [MecanumDriveWithPP] drivetrain.
 *
 * @param hardwareMap The FTC hardware map.
 * @param initialPose The initial pose of the robot.
 */
@Config
class JonesPP(hardwareMap: HardwareMap, initialPose: Pose = Pose()) :
    Jones<MecanumDriveWithPP>(
        hardwareMap,
        MecanumDriveWithPP(
            hardwareMap,
            FollowerConstants()
                .mass(mass)
                .forwardZeroPowerAcceleration(forwardZeroPowerAcceleration)
                .lateralZeroPowerAcceleration(lateralZeroPowerAcceleration)
                .translationalPIDFCoefficients(translationalPIDFCoefficients)
                .headingPIDFCoefficients(headingPIDFCoefficients)
                .drivePIDFCoefficients(drivePIDFCoefficients)
                .centripetalScaling(centripetalScaling),
            DriveEncoderConstants()
                .rightFrontMotorName(rightFrontMotorName)
                .rightRearMotorName(rightRearMotorName)
                .leftRearMotorName(leftRearMotorName)
                .leftFrontMotorName(leftFrontMotorName)
                .leftFrontEncoderDirection(leftFrontEncoderDirection)
                .leftRearEncoderDirection(leftRearEncoderDirection)
                .rightFrontEncoderDirection(rightFrontEncoderDirection)
                .rightRearEncoderDirection(rightRearEncoderDirection)
                .robotWidth(robotWidth)
                .robotLength(robotLength)
                .forwardTicksToInches(forwardTicksToInches)
                .strafeTicksToInches(strafeTicksToInches)
                .turnTicksToInches(turnTicksToInches),
            PathConstraints(tValueConstraint, timeoutConstraint, brakingStrength, brakingStart),
            MecanumConstants()
                .maxPower(maxPower)
                .rightFrontMotorName(rightFrontMotorName)
                .rightRearMotorName(rightRearMotorName)
                .leftRearMotorName(leftRearMotorName)
                .leftFrontMotorName(leftFrontMotorName)
                .leftFrontMotorDirection(leftFrontMotorDirection)
                .leftRearMotorDirection(leftRearMotorDirection)
                .rightFrontMotorDirection(rightFrontMotorDirection)
                .rightRearMotorDirection(rightRearMotorDirection)
                .xVelocity(xVelocity)
                .yVelocity(yVelocity),
            initialPose,
        ),
    ) {
    companion object {
        @JvmField var mass: Double = 5.45
        @JvmField var forwardZeroPowerAcceleration: Double = -70.0
        @JvmField var lateralZeroPowerAcceleration: Double = -150.0
        @JvmField
        var translationalPIDFCoefficients: PIDFCoefficients = PIDFCoefficients(0.3, 0.0, 0.0, 0.015)
        @JvmField
        var headingPIDFCoefficients: PIDFCoefficients = PIDFCoefficients(0.8, 0.01, 0.01, 0.025)
        @JvmField
        var drivePIDFCoefficients: FilteredPIDFCoefficients =
            FilteredPIDFCoefficients(0.8, 0.0, 0.02, 0.1, 0.1)
        @JvmField var centripetalScaling: Double = 0.00054

        @JvmField var rightFrontMotorName: String = "rf"
        @JvmField var leftFrontMotorName: String = "lf"
        @JvmField var rightRearMotorName: String = "rr"
        @JvmField var leftRearMotorName: String = "lr"
        @JvmField var leftFrontEncoderDirection: Double = Encoder.FORWARD
        @JvmField var leftRearEncoderDirection: Double = Encoder.FORWARD
        @JvmField var rightFrontEncoderDirection: Double = Encoder.REVERSE
        @JvmField var rightRearEncoderDirection: Double = Encoder.REVERSE
        @JvmField var robotWidth: Double = 13.0
        @JvmField var robotLength: Double = 9.0
        @JvmField var forwardTicksToInches: Double = 0.0057725
        @JvmField var strafeTicksToInches: Double = 0.004527
        @JvmField var turnTicksToInches: Double = 0.0092964

        @JvmField var tValueConstraint: Double = 0.99
        @JvmField var timeoutConstraint: Double = 100.0
        @JvmField var brakingStrength: Double = 1.0
        @JvmField var brakingStart: Double = 1.0

        @JvmField var maxPower: Double = 1.0
        @JvmField
        var leftFrontMotorDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        @JvmField
        var leftRearMotorDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        @JvmField
        var rightFrontMotorDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.REVERSE
        @JvmField
        var rightRearMotorDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.REVERSE
        @JvmField var xVelocity: Double = 50.0
        @JvmField var yVelocity: Double = 55.0
    }

    context(telemetry: Telemetry)
    override fun update() {
        drivetrain.update()
        super.update()
    }
}
