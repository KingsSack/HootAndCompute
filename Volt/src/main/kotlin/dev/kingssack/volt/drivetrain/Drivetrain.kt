package dev.kingssack.volt.drivetrain

import org.firstinspires.ftc.robotcore.external.Telemetry

abstract class Drivetrain {
    context(telemetry: Telemetry)
    abstract fun update()
}
