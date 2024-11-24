package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.manual.Man
import robot.Steve

@TeleOp(name = "Whale", group = "Competition")
class Whale : OpMode() {
    // Robot
    private val robot = Steve()

    // Manual script
    private val man = Man(robot, gamepad1, gamepad2)

    override fun init() {
        // Initialize
        robot.init(hardwareMap)
        man.init(hardwareMap)
    }

    override fun loop() {
        // Tick
        man.tick(telemetry)
    }
}