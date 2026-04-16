package dev.kingssack.volt.attachment.drivetrain.pp.mecanum

import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.PinpointConstants
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.pp.PedroPathingDrivetrain

/**
 * A mecanum [PedroPathingDrivetrain] with a Pinpoint localizer.
 *
 * @param hardwareMap The FTC hardware map
 * @param followerConstants constants for the path follower
 * @param localizerConstants constants for the pinpoint localizer
 * @param pathConstraints constraints for path following
 * @param driveConstants constants specific to the mecanum drivetrain
 * @param initialPose the robot's initial pose
 */
class PinpointMecanumPedroPathingDrivetrain(
    hardwareMap: HardwareMap,
    followerConstants: FollowerConstants = FollowerConstants(),
    localizerConstants: PinpointConstants = PinpointConstants(),
    pathConstraints: PathConstraints = PathConstraints(0.99, 100.0, 1.0, 1.0),
    driveConstants: MecanumConstants = MecanumConstants(),
    initialPose: Pose = Pose(),
) :
    PedroPathingDrivetrain(
        FollowerBuilder(followerConstants, hardwareMap)
            .pinpointLocalizer(localizerConstants)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .build(),
        initialPose,
    )
