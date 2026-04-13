package dev.kingssack.volt.attachment.drivetrain.rr.mecanum

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.integrations.rr.localizer.MecanumDriveLocalizer

/**
 * A [MecanumRoadRunnerDrivetrain] with a [MecanumDriveLocalizer].
 *
 * @param hardwareMap the hardware map
 * @param pose the robot's initial pose
 * @param params the drive parameters
 */
class DriveEncoderMecanumRoadRunnerDrivetrain(
    hardwareMap: HardwareMap,
    pose: Pose2d = Pose2d(Vector2d(0.0, 0.0), 0.0),
    params: DriveParams = DriveParams(),
) : MecanumRoadRunnerDrivetrain(hardwareMap, params) {
    override val localizer =
        MecanumDriveLocalizer(
            leftFront,
            leftBack,
            rightBack,
            rightFront,
            kinematics,
            lazyImu.get(),
            params.inPerTick,
            pose,
        )
}
