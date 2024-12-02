package org.firstinspires.ftc.teamcode.manual

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.robot.Robot

interface ManualMode {
    // Controller
    val controller: ManualController

    // Robot
    val robot: Robot

    // Register
    fun registerMotors(hardwareMap: HardwareMap)

    // Tick
    fun tick(telemetry: Telemetry)
}