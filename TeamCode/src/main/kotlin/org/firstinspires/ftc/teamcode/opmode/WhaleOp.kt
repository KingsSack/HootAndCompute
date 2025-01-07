package org.firstinspires.ftc.teamcode.opmode

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.lasteditguild.volt.manual.ManualMode
import org.firstinspires.ftc.teamcode.opmode.manual.Whale

@Config
@TeleOp(name = "Whale", group = "Competition")
class WhaleOp : OpMode() {
    companion object Config {
        @JvmField
        var params: Whale.WhaleParams = Whale.WhaleParams()
    }

    // Manual script
    private lateinit var manual: ManualMode

    override fun init() {
        // Initialize
        manual = Whale(hardwareMap, telemetry, params, gamepad1, gamepad2)
    }

    override fun loop() {
        // Tick
        manual.tick(telemetry)
    }
}