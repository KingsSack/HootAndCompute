package attachment

import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.HardwareMap

class Extender(hardwareMap: HardwareMap, name: String) : Attachment(hardwareMap) {
    // Initialize extender
    private val extenderServo : Servo = hardwareMap.get(Servo::class.java, name)

    private val servoMinPosition = 0.0
    private val servoMaxPosition = 1.0

    var currentPosition = extenderServo.position

    fun extend() {
        // Extend the servo to the center
        currentPosition = servoMaxPosition
        extenderServo.position = currentPosition
    }

    fun retract() {
        // Retract the servo to the left
        currentPosition = servoMinPosition
        extenderServo.position = currentPosition
    }
}