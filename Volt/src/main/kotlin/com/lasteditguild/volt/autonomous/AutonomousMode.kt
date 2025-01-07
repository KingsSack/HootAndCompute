package com.lasteditguild.volt.autonomous

import com.lasteditguild.volt.robot.Robot

/**
 * AutonomousMode is an interface that defines the methods for running an autonomous mode.
 */
interface AutonomousMode {
    // Controller
    val controller: AutonomousController

    // Robot
    val robot: Robot

    // Run
    fun run()
}