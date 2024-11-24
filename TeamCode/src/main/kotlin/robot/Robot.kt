package robot

import com.qualcomm.robotcore.hardware.HardwareMap
import util.Controller

interface Robot {
    // Control
    val control: Controller

    // Initialize
    fun init(hardwareMap: HardwareMap)

    // Register
    fun registerSensors(hardwareMap: HardwareMap)
    fun registerAttachments(hardwareMap: HardwareMap)

    // Tick
    fun tick()
}