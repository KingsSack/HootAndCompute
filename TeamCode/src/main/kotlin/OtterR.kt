package org.firstinspires.ftc.teamcode

import autonomous.Auto
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import robot.Steve
import util.Position

@Autonomous(name = "Otter - Right", group = "Competition")
class OtterR : LinearOpMode(){
    // Robot
    private val robot = Steve()

    // Sample positions
    private val samplePositions: List<Position> = listOf() // No samples to collect

    // Basket position
    private val basketPosition = Position(-304.8, 0.0)

    // Observation zone position
    private val observationZonePosition = Position(0.0, 609.6)

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