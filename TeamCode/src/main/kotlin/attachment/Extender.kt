package attachment

import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime

class Extender(hardwareMap: HardwareMap, name: String) : Attachment(hardwareMap) {
    // Initialize extender
    private val extenderServo : Servo = hardwareMap.get(Servo::class.java, name)

    // Control
    private val servoMinPosition = 0.0
    private val servoMaxPosition = 1.0

    // Position
    var currentPosition = extenderServo.position

    // Runtime
    private val runtime: ElapsedTime = ElapsedTime()
    private val cooldown = 1.0

    fun extend(): Boolean {
        // Set the servo position, servo moves automatically
        setPos(servoMaxPosition)
        return true
    }

    fun retract(): Boolean {
        // Set the servo position, servo moves automatically
        setPos(servoMinPosition)
        return true
    }

    private fun setPos(pos: Double) {
        // Set the servo position
        if (runtime.seconds() >= cooldown) {
            runtime.reset()
            currentPosition = pos
            extenderServo.position = currentPosition
        }
    }
}