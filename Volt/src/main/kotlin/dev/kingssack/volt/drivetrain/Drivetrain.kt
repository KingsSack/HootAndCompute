package dev.kingssack.volt.drivetrain

import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry

abstract class Drivetrain : Attachment("Drivetrain") {
    context(telemetry: Telemetry)
    abstract override fun update()
}
