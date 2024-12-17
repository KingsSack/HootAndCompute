package org.firstinspires.ftc.teamcode

import com.acmerobotics.dashboard.config.Config
import org.firstinspires.ftc.teamcode.autonomous.Otter
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.autonomous.Elephant

@Config
@Autonomous(name = "Otter - Test", group = "Test", preselectTeleOp = "Whale")
class OtterTest : LinearOpMode() {
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
            auto.run()
    }
}

@Config
@Autonomous(name = "Elephant - Test", group = "Test", preselectTeleOp = "Whale")
class ElephantTest : LinearOpMode() {
    companion object Config {
        @JvmField
        var params: Elephant.ElephantParams = Elephant.ElephantParams()
    }

    // Autonomous script
    private lateinit var auto: AutonomousMode

    override fun runOpMode() {
        // Initialize
        auto = Elephant(hardwareMap, telemetry, params)

        // Wait for start
        waitForStart()

        // Execute
        if (opModeIsActive())
            auto.run()
    }
}