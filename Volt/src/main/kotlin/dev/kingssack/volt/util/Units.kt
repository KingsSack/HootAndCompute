package dev.kingssack.volt.util

import com.qualcomm.robotcore.hardware.Servo

@JvmInline
value class Power(val value: Double) {
    init {
        require(value in -1.0..1.0) { "Power must be in range [-1.0, 1.0]" }
    }
}

@JvmInline
value class Seconds(val value: Double) {
    init {
        require(value >= 0.0) { "Seconds must be non-negative" }
    }
}

@JvmInline
value class ServoPosition(val value: Double) {
    init {
        require(value in Servo.MIN_POSITION..Servo.MAX_POSITION) {
            "Position must be in range [${Servo.MIN_POSITION}, ${Servo.MAX_POSITION}]"
        }
    }
}

@JvmInline
value class Voltage(val value: Double)
