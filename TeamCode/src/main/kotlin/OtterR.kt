package org.firstinspires.ftc.teamcode

import com.acmerobotics.dashboard.config.Config
import org.firstinspires.ftc.teamcode.autonomous.Otter
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.autonomous.AutonomousMode

@Config
@Autonomous(name = "Otter - Right", group = "Competition", preselectTeleOp = "Whale")
class OtterR : LinearOpMode() {
    companion object Config {
        @JvmStatic
        var otterRParams: Otter.OtterParams = Otter.OtterParams()
    }

    // Autonomous script
    private lateinit var auto: AutonomousMode

    override fun runOpMode() {
        // Initialize
        auto = Otter(hardwareMap, telemetry, otterRParams)

        // Wait for start
        waitForStart()

        // Execute
        if (opModeIsActive())
            auto.run()
    }
}