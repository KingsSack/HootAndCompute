package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.robots.Goose

@Autonomous(name = "Auto/Helo", group = "Zandra")
class ZHello : LinearOpMode() {
    private val robot = Goose()

    override fun runOpMode() {
        waitForStart()
        telemetry.addData("status","Hello World")
        telemetry.update()
        while (opModeIsActive()) {}

    }
}