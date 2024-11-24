package autonomous

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

interface Autonomous {
//    // States
//    sealed class State
//
//    // Events
//    sealed class Event
//    class EmitEvent(private val auto: Autonomous, private val event: Event) : Action {
//        override fun run(p: TelemetryPacket): Boolean {
//            auto.handleEvent(event)
//            return false
//        }
//    }
//    fun emitEvent(event)

    // Initialize
    fun init(hardwareMap: HardwareMap, telemetry: Telemetry, initialPose: Pose2d)

    // Register
    fun registerDrive(hardwareMap: HardwareMap, initialPose: Pose2d)

    // Tick
    fun tick(telemetry: Telemetry)

    // Event handler
    // fun handleEvent(event: Event)
}