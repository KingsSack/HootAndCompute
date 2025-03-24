package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry

class Example(hardwareMap: HardwareMap, lName: String, rName: String) : Attachment() {
    val leftMotor: DcMotor = hardwareMap.dcMotor[lName]
    val rightMotor: DcMotor = hardwareMap.dcMotor[rName]

    init {
        // Set mode
        leftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        rightMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER

        // Set zero power behavior
        leftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rightMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set direction
        leftMotor.direction = DcMotorSimple.Direction.FORWARD
        rightMotor.direction = DcMotorSimple.Direction.REVERSE

        // Add to list
        motors = listOf(leftMotor, rightMotor)
    }

    inner class Control(
        private val power: Double,
        private val targetPosition: Int
    ) : ControlAction() {
        private var reversing = false

        override fun init() {
            // Determine reversing
            reversing = targetPosition < leftMotor.currentPosition

            // Set power
            leftMotor.power = if (reversing) -power else power
            rightMotor.power = if (reversing) -power else power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Get position
            val currentPosition = leftMotor.currentPosition


            if (reversing) {
                // Reverse
                if (currentPosition > targetPosition) return false
            } else {
                // Forward
                if (currentPosition < targetPosition) return false
            }
            return true
        }

        override fun handleStop() {
            // Stop the motor
            leftMotor.power = 0.0
            rightMotor.power = 0.0
        }
    }

    fun goTo(power: Double, position: Int): Action {
        // Return Control
        return Control(power, position)
    }

    fun live(power: Double): Action {
        // Return Control
        return Control(power, 2000)
    }

    override fun update(telemetry: Telemetry) {
        // Log motor position
        telemetry.addLine("==== EXAMPLE ====")
        telemetry.addData("Left Position", leftMotor.currentPosition)
        telemetry.addData("Right Position", rightMotor.currentPosition)
        telemetry.addLine()
    }
}