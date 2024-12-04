package org.firstinspires.ftc.teamcode.robot

import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * Robot is an interface that defines the methods for registering sensors and attachments.
 */
interface Robot {
    /**
     * Register sensors to the robot.
     *
     * @param hardwareMap the hardware map
     */
    fun registerSensors(hardwareMap: HardwareMap)

    /**
     * Register attachments to the robot.
     *
     * @param hardwareMap the hardware map
     */
    fun registerAttachments(hardwareMap: HardwareMap)
}