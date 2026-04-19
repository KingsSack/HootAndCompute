package dev.kingssack.volt.opmode.autonomous

import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

/**
 * AutonomousMode is an abstract class that defines the methods for running an autonomous mode.
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
}
