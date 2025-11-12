package org.firstinspires.ftc.teamcode.pp;

import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.Encoder;
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Constants {
    public static FollowerConstants followerConstants = new FollowerConstants()
            .mass(5.45)
            .forwardZeroPowerAcceleration(-81.3341057)
            .lateralZeroPowerAcceleration(-140.475441)
            .translationalPIDFCoefficients(new PIDFCoefficients(0, 0, 0, 0));

    public static DriveEncoderConstants localizerConstants =
            new DriveEncoderConstants()
                    .rightFrontMotorName("rf")
                    .rightRearMotorName("rr")
                    .leftRearMotorName("lr")
                    .leftFrontMotorName("lf")
                    .leftFrontEncoderDirection(Encoder.REVERSE)
                    .leftRearEncoderDirection(Encoder.REVERSE)
                    .rightFrontEncoderDirection(Encoder.FORWARD)
                    .rightRearEncoderDirection(Encoder.FORWARD)
                    .robotWidth(13)
                    .robotLength(9)
                    .forwardTicksToInches(0.0057725)
                    .strafeTicksToInches(0.0062025)
                    .turnTicksToInches(0.0092964);

    public static PathConstraints pathConstraints = new PathConstraints(
            0.99,
            100,
            1,
            1
    );

    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
            .rightFrontMotorName("rf")
            .rightRearMotorName("rr")
            .leftRearMotorName("lr")
            .leftFrontMotorName("lf")
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .xVelocity(63.072811)
            .yVelocity(63.879606);

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .driveEncoderLocalizer(localizerConstants)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .build();
    }
}
