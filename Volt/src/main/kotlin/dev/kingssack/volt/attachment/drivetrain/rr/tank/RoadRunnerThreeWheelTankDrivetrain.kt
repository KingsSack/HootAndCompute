package dev.kingssack.volt.attachment.drivetrain.rr.tank

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.integrations.rr.localizer.ThreeDeadWheelLocalizer

/**
 * A [RoadRunnerTankDrivetrain] with a [ThreeDeadWheelLocalizer].
 *
 * @param hardwareMap the hardware map
 * @param pose the robot's initial pose
 * @param params the drive parameters
 * @param localizerParams the localizer parameters
 */
class RoadRunnerThreeWheelTankDrivetrain(
    hardwareMap: HardwareMap,
    pose: Pose2d = Pose2d(Vector2d(0.0, 0.0), 0.0),
    params: DriveParams = DriveParams(),
    localizerParams: ThreeDeadWheelLocalizer.LocalizerParams =
        ThreeDeadWheelLocalizer.LocalizerParams(),
) : RoadRunnerTankDrivetrain(hardwareMap, params) {
    override val localizer =
        ThreeDeadWheelLocalizer(hardwareMap, params.inPerTick, pose, localizerParams)
}
