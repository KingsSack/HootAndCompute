package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry

@Config
class Intake(hardwareMap: HardwareMap, leftName: String, rightName: String) : Attachment() {
    companion object Params {
        @JvmField
        var maxPower: Double = 0.72
    }

    private val intakeLeft = hardwareMap.crservo[leftName]
    private val intakeRight = hardwareMap.crservo[rightName]

    var reversing = false

    init {
        intakeLeft.direction = DcMotorSimple.Direction.REVERSE

        crServos = listOf(intakeLeft, intakeRight)
    }

    inner class Control(
        private val power: Double
    ) : ControlAction() {
        override fun init() {
            crServos.forEach { it.power = power }
        }

        override fun update(packet: TelemetryPacket): Boolean {
            return true
        }

        override fun handleStop() {}
    }

    fun enableIntake(): Action {
        reversing = false
        return Control(maxPower)
    }
    fun disableIntake(): Action {
        reversing = false
        return Control(0.0)
    }
    fun reverseIntake(): Action {
        reversing = true
        return Control(-maxPower)
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addLine("==== INTAKE ====")
        telemetry.addData("Left Power", intakeLeft.power)
        telemetry.addData("Right Power", intakeRight.power)
        telemetry.addLine()
    }
}