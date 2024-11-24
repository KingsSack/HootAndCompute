package org.firstinspires.ftc.teamcode.manual

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

interface Manual {
    // Initialize
    fun init(hardwareMap: HardwareMap)

    // Register
    fun registerMotors(hardwareMap: HardwareMap)

    // Tick
    fun tick(telemetry: Telemetry)
}