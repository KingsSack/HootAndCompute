package dev.kingssack.volt.opmode.autonomous

import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event.AutonomousEvent
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

/**
 * A [VoltOpMode] for autonomously controlling a [robot].
 *
 * @param R the type of robot
 */
abstract class AutonomousMode<R : Robot> : VoltOpMode<R>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(
            registrationHelper: VoltRegistrationHelper,
            clazz: Class<out VoltOpMode<*>>,
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

    /** Bind an [AutonomousEvent] to a [block]. */
    protected infix fun <P> AutonomousEvent<P>.then(block: VoltActionBuilder.(P) -> Unit) {
        eventHandler.bind(this, block)
    }
}
