package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import robot.Steve

@Autonomous(name = "Auto - Otter", group = "Competition")
class Auto : LinearOpMode() {
    // Robot
    private val robot = Steve()

    // States
    private enum class State {
        DETECT,
        STOP
    }
    private var state = State.DETECT

    override fun runOpMode() {
        // Initialize
        robot.init(hardwareMap)

        // Wait for start
        waitForStart()

        // Loop
        while (opModeIsActive()) {
            telemetry.addData("State", "%s", state.toString())

            when (state) {
                State.DETECT -> {
                    // Stop
                    state = State.STOP
                }
                State.STOP -> {
                    // Stop
                    robot.halt()
                }
            }

            telemetry.update()
        }
    }
}