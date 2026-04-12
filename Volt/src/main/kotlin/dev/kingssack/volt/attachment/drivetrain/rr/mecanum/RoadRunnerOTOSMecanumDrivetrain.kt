package dev.kingssack.volt.attachment.drivetrain.rr.mecanum

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.integrations.rr.localizer.OTOSLocalizer

/**
 * A [RoadRunnerMecanumDrivetrain] with an [OTOSLocalizer].
 *
 * @param hardwareMap the hardware map
 * @param pose the robot's initial pose
 * @param params the drive parameters
 * @param localizerParams the localizer parameters
 */
class RoadRunnerOTOSMecanumDrivetrain(
    hardwareMap: HardwareMap,
    pose: Pose2d = Pose2d(Vector2d(0.0, 0.0), 0.0),
    params: DriveParams = DriveParams(),
    localizerParams: OTOSLocalizer.LocalizerParams = OTOSLocalizer.LocalizerParams(),
) : RoadRunnerMecanumDrivetrain(hardwareMap, params) {
    override val localizer = OTOSLocalizer(hardwareMap, pose, localizerParams)
}
