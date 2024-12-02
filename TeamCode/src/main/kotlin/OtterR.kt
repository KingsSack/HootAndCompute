package org.firstinspires.ftc.teamcode

import org.firstinspires.ftc.teamcode.autonomous.Otter
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.autonomous.AutonomousMode

@Autonomous(name = "Otter - Right", group = "Competition", preselectTeleOp = "Whale")
class OtterR : LinearOpMode() {
    // Autonomous script
    private lateinit var auto: AutonomousMode

    override fun runOpMode() {
        // Initialize
        auto = Otter(hardwareMap, telemetry, Configuration.otterRParams)

        // Wait for start
        waitForStart()

        // Execute
        if (opModeIsActive())
            auto.run()
    }
}