package dev.kingssack.volt.attachment.drivetrain.pp.mecanum

import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.ThreeWheelIMUConstants
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathConstraints
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.pp.PedroPathingDrivetrain

/**
 * A mecanum [PedroPathingDrivetrain] with a three-wheel plus IMU localizer.
 *
 * @param hardwareMap The FTC hardware map
 * @param followerConstants constants for the path follower
 * @param localizerConstants constants for the three-wheel plus IMU localizer
 * @param pathConstraints constraints for path following
 * @param driveConstants constants specific to the mecanum drivetrain
 * @param initialPose the robot's initial pose
 */
class ThreeWheelIMUMecanumPedroPathingDrivetrain(
    hardwareMap: HardwareMap,
    followerConstants: FollowerConstants = FollowerConstants(),
    localizerConstants: ThreeWheelIMUConstants = ThreeWheelIMUConstants(),
    pathConstraints: PathConstraints = PathConstraints(0.99, 100.0, 1.0, 1.0),
    driveConstants: MecanumConstants = MecanumConstants(),
    initialPose: Pose = Pose(),
) :
    PedroPathingDrivetrain(
        FollowerBuilder(followerConstants, hardwareMap)
            .threeWheelIMULocalizer(localizerConstants)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .build(),
        initialPose,
    )
