package org.firstinspires.ftc.teamcode.opmode

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.lasteditguild.volt.autonomous.AutonomousMode
import org.firstinspires.ftc.teamcode.opmode.autonomous.Elephant

@Config
@Autonomous(name = "Elephant - Right", group = "Competition", preselectTeleOp = "Dolphin")
class ElephantR : LinearOpMode() {
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