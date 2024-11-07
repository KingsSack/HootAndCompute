package attachment

import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap

class Claw(hardwareMap: HardwareMap) : Attachment(hardwareMap) {
    // Constants
    public val maxPower = 0.69


    // Motors
    private lateinit var clawServo : CRServo

    override fun init(hardwareMap: HardwareMap) {
        // Initialize claw
        clawServo = hardwareMap.get(CRServo::class.java, "claw")
    }

    public fun setPower(power: Double) {
        // Set servo power
        clawServo.power = power
    }
}