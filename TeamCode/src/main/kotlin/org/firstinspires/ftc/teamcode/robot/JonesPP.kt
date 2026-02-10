package org.firstinspires.ftc.teamcode.robot

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

/**
 * [Jones] with [MecanumDriveWithPP] drivetrain.
 *
 * @param hardwareMap The FTC hardware map
 * @param initialPose The initial pose of the robot
 */
class JonesPP(hardwareMap: HardwareMap, initialPose: Pose = Pose()) :
    Jones<MecanumDriveWithPP>(
        hardwareMap,
        MecanumDriveWithPP(
            hardwareMap,
            FollowerConstants()
                .mass(9.8)
                .forwardZeroPowerAcceleration(-55.0)
                .lateralZeroPowerAcceleration(-110.0)
                .translationalPIDFCoefficients(PIDFCoefficients(0.02, 0.0, 0.0, 0.07))
                .headingPIDFCoefficients(PIDFCoefficients(0.04, 0.0, 0.0, 0.02))
                .drivePIDFCoefficients(FilteredPIDFCoefficients(0.1, 0.0, 0.0, 0.0, 0.3))
                .centripetalScaling(0.0003),
            DriveEncoderConstants()
                .rightFrontMotorName("rf")
                .rightRearMotorName("rr")
                .leftRearMotorName("lr")
                .leftFrontMotorName("lf")
                .leftFrontEncoderDirection(Encoder.FORWARD)
                .leftRearEncoderDirection(Encoder.FORWARD)
                .rightFrontEncoderDirection(Encoder.REVERSE)
                .rightRearEncoderDirection(Encoder.REVERSE)
                .robotWidth(14.0)
                .robotLength(9.0)
                .forwardTicksToInches(0.0057725)
                .strafeTicksToInches(0.0069642)
                .turnTicksToInches(0.009961),
            PathConstraints(0.99, 100.0, 1.0, 1.0),
            MecanumConstants()
                .maxPower(0.8)
                .rightFrontMotorName("rf")
                .rightRearMotorName("rr")
                .leftRearMotorName("lr")
                .leftFrontMotorName("lf")
                .leftFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
                .leftRearMotorDirection(DcMotorSimple.Direction.FORWARD)
                .rightFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
                .rightRearMotorDirection(DcMotorSimple.Direction.REVERSE)
                .xVelocity(50.0)
                .yVelocity(55.0),
            initialPose,
        ),
    )
