package org.firstinspires.ftc.teamcode.robot

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.util.Controller

interface Robot {
    // Control
    val control: Controller

    // Register
    fun registerSensors(hardwareMap: HardwareMap)
    fun registerAttachments(hardwareMap: HardwareMap)

    // Tick
    fun tick() {
        control.run()
    }
}