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
import dev.kingssack.volt.opmode.autonomous.AllianceColor

/**
 * [Gabe] with [MecanumDriveWithPP] drivetrain.
 *
 * @param hardwareMap The FTC hardware map.
 * @param initialPose The initial pose of the robot.
 */
@Config
class GabePP(hardwareMap: HardwareMap, initialPose: Pose = Pose(), allianceColor: AllianceColor = AllianceColor.BLUE) :
    Gabe<MecanumDriveWithPP>(
        hardwareMap,
        MecanumDriveWithPP(
            hardwareMap,
            FollowerConstants()
                .mass(9.15)
                .forwardZeroPowerAcceleration(-70.0)
                .lateralZeroPowerAcceleration(-130.0)
                .translationalPIDFCoefficients(PIDFCoefficients(0.2, 0.0, 0.0, 0.01))
                .headingPIDFCoefficients(PIDFCoefficients(1.0, 0.0, 0.0, 0.02))
                .drivePIDFCoefficients(FilteredPIDFCoefficients(0.01, 0.0, 0.0, 0.0, 0.1))
                .centripetalScaling(0.00046),
            DriveEncoderConstants()
                .rightFrontMotorName("rf")
                .rightRearMotorName("rr")
                .leftRearMotorName("lr")
                .leftFrontMotorName("lf")
                .leftFrontEncoderDirection(Encoder.FORWARD)
                .leftRearEncoderDirection(Encoder.REVERSE)
                .rightFrontEncoderDirection(Encoder.FORWARD)
                .rightRearEncoderDirection(Encoder.REVERSE)
                .robotWidth(13.0)
                .robotLength(9.0)
                .forwardTicksToInches(0.00583)
                .strafeTicksToInches(0.00602)
                .turnTicksToInches(0.00928),
            PathConstraints(0.99, 100.0, 1.0, 1.0),
            MecanumConstants()
                .maxPower(1.0)
                .rightFrontMotorName("rf")
                .rightRearMotorName("rr")
                .leftRearMotorName("lr")
                .leftFrontMotorName("lf")
                .leftFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
                .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
                .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
                .rightRearMotorDirection(DcMotorSimple.Direction.REVERSE)
                .xVelocity(55.0)
                .yVelocity(52.0),
            initialPose,
        ),
        allianceColor
    ) {
    context(telemetry: Telemetry)
    override fun update() {
        drivetrain.update()
        super.update()
    }
}
