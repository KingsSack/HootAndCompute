package attachment

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap

class Arm(hardwareMap: HardwareMap, private val liftMotorName: String) : Attachment(hardwareMap) {
    // Motors
    private lateinit var liftMotor : DcMotor

    override fun init(hardwareMap: HardwareMap) {
        // Initialize arm
        liftMotor = hardwareMap.get(DcMotor::class.java, liftMotorName)
    }

    fun liftArm(power: Double) {
        // Move arm
        liftMotor.power = power
    }
}