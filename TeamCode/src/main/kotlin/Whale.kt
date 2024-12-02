package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.manual.Manual
import org.firstinspires.ftc.teamcode.manual.ManualMode

@TeleOp(name = "Whale", group = "Competition")
class Whale : OpMode() {
    // Manual script
    private lateinit var man: ManualMode

    override fun init() {
        // Initialize
        man = Manual(hardwareMap, telemetry, Configuration.whaleParams, gamepad1, gamepad2)
    }

    override fun loop() {
        // Tick
        man.tick(telemetry)
    }
}