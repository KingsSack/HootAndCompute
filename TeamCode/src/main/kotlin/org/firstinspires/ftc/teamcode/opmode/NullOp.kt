package org.firstinspires.ftc.teamcode.opmode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime

@TeleOp(name = "Manual - NullOp", group = "Competition")
class NullOp : OpMode() {
    private val runtime = ElapsedTime()

    override fun init() {
        telemetry.addData("Status", "Initialized")
    }

    override fun loop() {
        telemetry.addData("Status", "Run Time: $runtime")
        telemetry.update()
    }

    override fun start() {
        runtime.reset()
    }
}