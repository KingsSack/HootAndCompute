package dev.kingssack.volt.opmode.autonomous

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event
import dev.kingssack.volt.util.telemetry.ActionTracer
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

/**
 * AutonomousMode is an abstract class that defines the methods for running an autonomous mode.
 *
 * @param R the type of robot
 */
abstract class AutonomousMode<R : Robot> : VoltOpMode<R>() {
    private val dash: FtcDashboard? = FtcDashboard.getInstance()
    private val canvas = Canvas()

    @Suppress("unused")
    object Register : Registrar() {
        override fun register(
            registrationHelper: VoltRegistrationHelper,
            clazz: Class<VoltOpMode<*>>,
        ) {
            if (clazz.isAnnotationPresent(VoltOpModeMeta::class.java)) {
                val annotation = clazz.getAnnotation(VoltOpModeMeta::class.java)
                if (annotation != null) {
                    registrationHelper.register(
                        clazz.getDeclaredConstructor(),
                        OpModeMeta.Builder()
                            .setName(annotation.name)
                            .setGroup(annotation.group)
                            .setFlavor(OpModeMeta.Flavor.AUTONOMOUS)
                            .setTransitionTarget(
                                if (annotation.autoTransition == "") null
                                else annotation.autoTransition
                            )
                            .setSource(OpModeMeta.Source.EXTERNAL_LIBRARY)
                            .build(),
                    )
                }
            }
        }
    }

    private val events =
        mutableListOf<Pair<Event.AutonomousEvent, VoltActionBuilder<R>.() -> Unit>>()

    /** Maps an action to an event */
    protected infix fun Event.AutonomousEvent.then(block: VoltActionBuilder<R>.() -> Unit) {
        events.add(this to block)
    }

    /** A place to define actions to be triggered by events */
    abstract fun defineEvents()

    /** Defines autonomous events */
    init {
        defineEvents()
    }

    override fun begin() {
        events.forEach { (event, action) ->
            when (event) {
                Event.AutonomousEvent.Start ->
                    runAction(VoltActionBuilder(robot).apply(action).build())
            }
        }
    }

    private fun runAction(action: Action) {
        action.preview(canvas)

        var running = true
        while (running && opModeIsActive() && !Thread.currentThread().isInterrupted) {
            val packet = TelemetryPacket()
            packet.fieldOverlay().operations.addAll(canvas.operations)

            running = action.run(packet)

            context(telemetry) { robot.update() }
            context(packet) { ActionTracer.writePacket() }
            dash?.sendTelemetryPacket(packet)
        }
    }
}
