package dev.kingssack.volt.robot

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.MecanumDrivetrain

/**
 * A base class for robots that have a mecanum drivetrain.
 *
 * @param T The type of mecanum drivetrain the robot has.
 * @param hardwareMap The hardware map of the robot.
 * @property drivetrain The mecanum drivetrain of the robot.
 */
abstract class RobotWithMecanumDrivetrain<T : MecanumDrivetrain>(
    hardwareMap: HardwareMap,
    drivetrain: T,
) : RobotWithDrivetrain<T>(hardwareMap, drivetrain)
