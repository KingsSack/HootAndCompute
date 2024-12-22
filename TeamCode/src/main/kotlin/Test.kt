package org.firstinspires.ftc.teamcode

import com.acmerobotics.dashboard.config.Config
import org.firstinspires.ftc.teamcode.autonomous.Otter
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.autonomous.Elephant
import org.firstinspires.ftc.teamcode.autonomous.Rhinoceros

@Config
@Autonomous(name = "Otter - Test", group = "Test", preselectTeleOp = "Dolphin")
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
@Autonomous(name = "Elephant - Test", group = "Test", preselectTeleOp = "Dolphin")
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

@Config
@Autonomous(name = "Rhino - Test", group = "Test", preselectTeleOp = "Dolphin")
class RhinoTest : LinearOpMode() {
    companion object Config {
        @JvmField
        var params: Rhinoceros.RhinocerosParams = Rhinoceros.RhinocerosParams()
    }

    // Autonomous script
    private lateinit var auto: AutonomousMode

    override fun runOpMode() {
        // Initialize
        auto = Rhinoceros(hardwareMap, telemetry, params)

        // Wait for start
        waitForStart()

        // Execute
        if (opModeIsActive())
            auto.run()
    }
}