package org.firstinspires.ftc.teamcode.autonomous

import org.firstinspires.ftc.teamcode.robot.Robot
import org.firstinspires.ftc.teamcode.util.MecanumDrive

interface AutonomousMode {
    // Controller
    val controller: AutonomousController

    // Robot
    val robot: Robot

    // Drive
    val drive: MecanumDrive

    // Run
    fun run()
}