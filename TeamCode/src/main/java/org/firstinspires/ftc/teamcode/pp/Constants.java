package org.firstinspires.ftc.teamcode.pp;

import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.control.PredictiveBrakingCoefficients;
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
            .mass(9.8)
            .headingPIDFCoefficients(new PIDFCoefficients(0.04, 0, 0, 0.01))
            .predictiveBrakingCoefficients(new PredictiveBrakingCoefficients(0.2, 0.027, 0.00027))
            .centripetalScaling(0);

    public static DriveEncoderConstants localizerConstants =
            new DriveEncoderConstants()
                    .rightFrontMotorName("rf")
                    .rightRearMotorName("rr")
                    .leftRearMotorName("lr")
                    .leftFrontMotorName("lf")
                    .leftFrontEncoderDirection(Encoder.FORWARD)
                    .leftRearEncoderDirection(Encoder.FORWARD)
                    .rightFrontEncoderDirection(Encoder.REVERSE)
                    .rightRearEncoderDirection(Encoder.REVERSE)
                    .robotWidth(14)
                    .robotLength(9)
                    .forwardTicksToInches(0.0057725)
                    .strafeTicksToInches(0.0069642)
                    .turnTicksToInches(0.009961);

    public static PathConstraints pathConstraints = new PathConstraints(
            0.95,
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
            .leftFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .leftRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .xVelocity(55)
            .yVelocity(55);

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .driveEncoderLocalizer(localizerConstants)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .build();
    }
}
