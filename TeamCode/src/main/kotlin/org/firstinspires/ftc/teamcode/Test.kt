package org.firstinspires.ftc.teamcode

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import dev.kingssack.volt.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.opmode.autonomous.*

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
            auto.execute()
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
            auto.execute()
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
            auto.execute()
    }
}

@Config
@Autonomous(name = "Capybara - Test", group = "Test", preselectTeleOp = "Dolphin")
class CapybaraTest : LinearOpMode() {
    private lateinit var auto: AutonomousMode

    override fun runOpMode() {
        auto = Capybara(hardwareMap, telemetry)
        waitForStart()
        if (opModeIsActive())
            auto.execute()
    }
}

@Config
@Autonomous(name = "Human - Test", group = "Test", preselectTeleOp = "Whale")
class HumanTest : LinearOpMode() {
    private lateinit var auto: AutonomousMode

    override fun runOpMode() {
        auto = Human(hardwareMap, telemetry)
        waitForStart()
        if (opModeIsActive())
            auto.execute()
    }
}