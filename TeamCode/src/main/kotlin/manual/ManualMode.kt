package org.firstinspires.ftc.teamcode.manual

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.Robot

/**
 * ManualMode is an interface that defines the methods for registering motors and ticking.
 */
interface ManualMode {
    // Controller
    val controller: ManualController

    // Robot
    val robot: Robot

    /**
     * Register motors for manual movement.
     *
     * @param hardwareMap the hardware map
     */
    fun registerMotors(hardwareMap: HardwareMap)

    /**
     * Tick the manual mode.
     *
     * @param telemetry for logging
     */
    fun tick(telemetry: Telemetry)
}