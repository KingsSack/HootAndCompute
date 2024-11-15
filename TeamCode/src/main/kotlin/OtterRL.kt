package org.firstinspires.ftc.teamcode

import autonomous.Auto
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import robot.Steve
import util.Position

@Autonomous(name = "Otter - RL", group = "Competition")
class OtterRL : LinearOpMode(){
    // Robot
    private val robot = Steve()

    // Sample positions
    private val samplePositions = listOf(
        Position(609.6, 609.6)  // First sample to collect
    )

    // Basket position
    private val basketPosition = Position(-304.8, 0.0)

    // Observation zone position
    private val observationZonePosition = Position(-609.6, -2133.6)

    // Autonomous script
    private val auto = Auto(robot, samplePositions, basketPosition, observationZonePosition)

    override fun runOpMode() {
        // Initialize
        robot.init(hardwareMap)

        // Wait for start
        waitForStart()

        // Loop
        while (opModeIsActive()) {
            auto.run(telemetry)
        }
    }
}