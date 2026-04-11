package dev.kingssack.volt.attachment.drivetrain.pp.mecanum

import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.TwoWheelConstants
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.pp.PedroPathingDrivetrain

/**
 * A mecanum [PedroPathingDrivetrain] with a two-wheel localizer.
 *
 * @param hardwareMap The FTC hardware map
 * @param followerConstants constants for the path follower
 * @param localizerConstants constants for the two-wheel localizer
 * @param pathConstraints constraints for path following
 * @param driveConstants constants specific to the mecanum drivetrain
 * @param initialPose the robot's initial pose
 */
class PedroPathingTwoWheelMecanumDrivetrain(
    hardwareMap: HardwareMap,
    followerConstants: FollowerConstants,
    localizerConstants: TwoWheelConstants,
    pathConstraints: PathConstraints,
    driveConstants: MecanumConstants,
    initialPose: Pose = Pose(),
) :
    PedroPathingDrivetrain(
        FollowerBuilder(followerConstants, hardwareMap)
            .twoWheelLocalizer(localizerConstants)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .build(),
        initialPose,
    )
