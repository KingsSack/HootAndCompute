package com.lasteditguild.volt.manual

import com.lasteditguild.volt.robot.Robot
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * ManualMode is an interface that defines the methods for registering motors and ticking.
 */
interface ManualMode {
    // Controller
    val controller: ManualController

    // Robot
    val robot: Robot

    /**
     * Tick the manual mode.
     *
     * @param telemetry for logging
     */
    fun tick(telemetry: Telemetry)
}