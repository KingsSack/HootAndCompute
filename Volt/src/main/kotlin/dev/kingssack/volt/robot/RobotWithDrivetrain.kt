package dev.kingssack.volt.robot

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.drivetrain.Drivetrain

/**
 * A base class for robots that have a drivetrain.
 *
 * @param T The type of drivetrain the robot has.
 * @param hardwareMap The hardware map of the robot.
 * @property drivetrain The drivetrain of the robot.
 */
abstract class RobotWithDrivetrain<T : Drivetrain>(
    hardwareMap: HardwareMap,
    open val drivetrain: T,
) : Robot(hardwareMap)
