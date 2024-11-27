package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.manual.Man

@TeleOp(name = "Whale", group = "Competition")
class Whale : OpMode() {
    // Manual script
    private lateinit var man: Man

    override fun init() {
        // Initialize
        man = Man(hardwareMap, gamepad1, gamepad2)
    }

    override fun loop() {
        // Tick
        man.tick(telemetry)
    }
}