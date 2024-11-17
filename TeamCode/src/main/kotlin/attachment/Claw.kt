package attachment

import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime

class Claw(hardwareMap: HardwareMap, name: String) : Attachment(hardwareMap) {
    // Constants
    val maxPower = 0.72
    private val openCloseTime = 0.6

    // Initialize claw
    private var clawServo: CRServo = hardwareMap.get(CRServo::class.java, name)

    // Runtime
    private val runtime: ElapsedTime = ElapsedTime()

    // Booleans
    private var moving = false
    private var open = true

    fun openClose() : Boolean {
        if (!moving) {
            runtime.reset()
            moving = true
            if (open) setPower(-maxPower) else setPower(maxPower)  // Open or close claw
        }

        // Check if the claw has finished moving
        if (runtime.seconds() >= openCloseTime) {
            moving = false
            setPower(0.0)
            open = !open
            return true
        }
        return false
    }

    fun setPower(power: Double) {
        // Set servo power
        clawServo.power = power
    }
}