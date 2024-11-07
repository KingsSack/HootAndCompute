package attachment

import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap

class Claw(hardwareMap: HardwareMap, name: String) : Attachment(hardwareMap) {
    // Constants
    val maxPower = 0.72

    // Initialize claw
    private var clawServo : CRServo = hardwareMap.get(CRServo::class.java, name)

    fun setPower(power: Double) {
        // Set servo power
        clawServo.power = power
    }
}