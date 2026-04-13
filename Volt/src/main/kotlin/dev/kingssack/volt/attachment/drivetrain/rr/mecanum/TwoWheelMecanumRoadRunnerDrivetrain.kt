package dev.kingssack.volt.attachment.drivetrain.rr.mecanum

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.integrations.rr.localizer.TwoDeadWheelLocalizer

/**
 * A [MecanumRoadRunnerDrivetrain] with a [TwoDeadWheelLocalizer].
 *
 * @param hardwareMap the hardware map
 * @param pose the robot's initial pose
 * @param params the drive parameters
 * @param localizerParams the localizer parameters
 */
class TwoWheelMecanumRoadRunnerDrivetrain(
    hardwareMap: HardwareMap,
    pose: Pose2d = Pose2d(Vector2d(0.0, 0.0), 0.0),
    params: DriveParams = DriveParams(),
    localizerParams: TwoDeadWheelLocalizer.LocalizerParams = TwoDeadWheelLocalizer.LocalizerParams(),
) : MecanumRoadRunnerDrivetrain(hardwareMap, params) {
    override val localizer =
        TwoDeadWheelLocalizer(hardwareMap, lazyImu.get(), params.inPerTick, pose, localizerParams)
}
