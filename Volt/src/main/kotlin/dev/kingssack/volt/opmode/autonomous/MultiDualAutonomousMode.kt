package dev.kingssack.volt.opmode.autonomous

import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import java.lang.reflect.ParameterizedType
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

/**
 * A [DualAutonomousMode] that can be registered as multiple separate opmodes, one for each value of
 * the enum [E].
 *
 * @param E the enum to register separate opmodes for
 * @property type the selected value of [E]
 */
abstract class MultiDualAutonomousMode<R : Robot, E : Enum<*>> : DualAutonomousMode<R>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(
            registrationHelper: VoltRegistrationHelper,
            clazz: Class<VoltOpMode<*>>,
        ) {
            if (clazz.isAnnotationPresent(VoltOpModeMeta::class.java)) {
                val annotation = clazz.getAnnotation(VoltOpModeMeta::class.java)
                if (annotation != null) {
                    // If this is not the direct superclass, this might not work, but I don't know a
                    // better way.
                    val enumClass =
                        (clazz.genericSuperclass as ParameterizedType).actualTypeArguments[1]
                            as Class<Enum<*>>
                    enumClass.enumConstants!!.forEach { value ->
                        registrationHelper.register(
                            { instantiateOpMode(value as Enum<*>, clazz, AllianceColor.BLUE) },
                            OpModeMeta.Builder()
                                .setName("${annotation.name} $value Blue")
                                .setGroup(annotation.group)
                                .setFlavor(OpModeMeta.Flavor.AUTONOMOUS)
                                .setTransitionTarget(
                                    if (annotation.autoTransition == "") null
                                    else annotation.autoTransition
                                )
                                .setSource(OpModeMeta.Source.EXTERNAL_LIBRARY)
                                .build(),
                        )
                        registrationHelper.register(
                            { instantiateOpMode(value as Enum<*>, clazz, AllianceColor.RED) },
                            OpModeMeta.Builder()
                                .setName("${annotation.name} $value Red")
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

        fun <R : Robot, E : Enum<*>> instantiateOpMode(
            value: E,
            clazz: Class<VoltOpMode<*>>,
            color: AllianceColor,
        ): MultiDualAutonomousMode<R, E> {
            ColorHolder.color = color
            InfoHolder.type = value
            return (clazz as Class<MultiDualAutonomousMode<R, E>>)
                .getDeclaredConstructor()
                .newInstance()
        }
    }

    private object InfoHolder {
        var type: Enum<*>? = null
    }

    val type: E = InfoHolder.type as E
}
