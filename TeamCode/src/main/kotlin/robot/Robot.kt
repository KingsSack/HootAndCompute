package robot

import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap

abstract class Robot {
    // Initialize
    abstract fun init(hardwareMap: HardwareMap)

    // Drive
    abstract fun driveWithGamepad(gamepad: Gamepad)

    // Stop
    abstract fun halt()
}