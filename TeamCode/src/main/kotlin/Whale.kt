package org.firstinspires.ftc.teamcode

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.manual.Manual
import org.firstinspires.ftc.teamcode.manual.ManualMode

@Config
@TeleOp(name = "Whale", group = "Competition")
class Whale : OpMode() {
    // Manual script
    private lateinit var manual: ManualMode

    override fun init() {
        // Initialize
        manual = Manual(hardwareMap, telemetry, whaleParams, gamepad1, gamepad2)
    }

    override fun loop() {
        // Tick
        manual.tick(telemetry)
    }

    companion object {
        // Config
        @JvmStatic
        var whaleParams = Manual.ManualParams()
    }
}