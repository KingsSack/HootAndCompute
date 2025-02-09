package org.firstinspires.ftc.teamcode.opmode

import com.acmerobotics.dashboard.config.Config
import org.firstinspires.ftc.teamcode.opmode.autonomous.Otter
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import dev.kingssack.volt.autonomous.AutonomousMode

@Config
@Autonomous(name = "Otter - Right", group = "Competition", preselectTeleOp = "Whale")
class OtterR : LinearOpMode() {
    companion object Config {
        @JvmField
        var params: Otter.OtterParams = Otter.OtterParams()
    }

    // Autonomous script
    private lateinit var auto: AutonomousMode

    override fun runOpMode() {
        // Initialize
        auto = Otter(hardwareMap, telemetry, params)

        // Wait for start
        waitForStart()

        // Execute
        if (opModeIsActive())
            auto.execute()
    }
}