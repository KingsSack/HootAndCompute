package autonomous

import org.firstinspires.ftc.robotcore.external.Telemetry
import robot.Robot

abstract class Autonomous(robot: Robot) {
    abstract fun run(telemetry: Telemetry)
}