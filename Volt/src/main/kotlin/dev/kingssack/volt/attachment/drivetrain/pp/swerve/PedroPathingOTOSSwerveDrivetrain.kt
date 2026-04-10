package dev.kingssack.volt.attachment.drivetrain.pp.swerve

import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.CoaxialPod
import com.pedropathing.ftc.drivetrains.SwerveConstants
import com.pedropathing.ftc.localization.constants.OTOSConstants
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.pp.PedroPathingDrivetrain

/**
 * A mecanum [dev.kingssack.volt.attachment.drivetrain.Drivetrain] integrated with the PedroPathing
 * library.
 *
 * @param hardwareMap The FTC hardware map
 * @param followerConstants constants for the path follower
 * @param localizerConstants constants for the OTOS localizer
 * @param pathConstraints constraints for path following
 * @param driveConstants constants specific to the swerve drivetrain
 * @param initialPose the robot's initial pose
 */
class PedroPathingOTOSSwerveDrivetrain(
    hardwareMap: HardwareMap,
    followerConstants: FollowerConstants,
    localizerConstants: OTOSConstants,
    pathConstraints: PathConstraints,
    driveConstants: SwerveConstants,
    vararg pods: CoaxialPod,
    initialPose: Pose = Pose(),
) :
    PedroPathingDrivetrain(
        FollowerBuilder(followerConstants, hardwareMap)
            .OTOSLocalizer(localizerConstants)
            .pathConstraints(pathConstraints)
            .swerveDrivetrain(driveConstants, *pods)
            .build(),
        initialPose,
    )
