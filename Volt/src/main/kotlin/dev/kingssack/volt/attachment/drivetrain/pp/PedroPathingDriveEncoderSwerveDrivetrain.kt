package dev.kingssack.volt.attachment.drivetrain.pp

import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.CoaxialPod
import com.pedropathing.ftc.drivetrains.SwerveConstants
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * A swerve [dev.kingssack.volt.attachment.drivetrain.Drivetrain] integrated with the PedroPathing
 * library.
 *
 * @param hardwareMap the FTC hardware map
 * @param followerConstants constants for the path follower
 * @param localizerConstants constants for the drive encoder localizer
 * @param pathConstraints constraints for path following
 * @param swerveConstants constants specific to the swerve drivetrain
 * @param pods the coaxial pods used by the swerve drivetrain
 * @param initialPose the robot's initial pose
 */
class PedroPathingDriveEncoderSwerveDrivetrain(
    hardwareMap: HardwareMap,
    followerConstants: FollowerConstants,
    localizerConstants: DriveEncoderConstants,
    pathConstraints: PathConstraints,
    swerveConstants: SwerveConstants,
    vararg pods: CoaxialPod,
    initialPose: Pose = Pose(),
) :
    PedroPathingDrivetrain(
        FollowerBuilder(followerConstants, hardwareMap)
            .driveEncoderLocalizer(localizerConstants)
            .pathConstraints(pathConstraints)
            .swerveDrivetrain(swerveConstants, *pods)
            .build(),
        initialPose,
    )
