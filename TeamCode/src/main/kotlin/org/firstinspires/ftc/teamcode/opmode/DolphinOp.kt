package org.firstinspires.ftc.teamcode.opmode

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.lasteditguild.volt.manual.ManualMode
import org.firstinspires.ftc.teamcode.opmode.manual.Dolphin

@Config
@TeleOp(name = "Dolphin", group = "Competition")
class DolphinOp : OpMode() {
    companion object Config {
        @JvmField
        var params: Dolphin.DolphinParams = Dolphin.DolphinParams()
    }

    // Manual script
    private lateinit var manual: ManualMode

    override fun init() {
        // Initialize
        manual = Dolphin(hardwareMap, telemetry, params, gamepad1, gamepad2)
    }

    override fun loop() {
        // Tick
        manual.tick(telemetry)
    }
}