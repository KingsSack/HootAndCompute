package dev.kingssack.volt.opmode.autonomous

import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.lang.reflect.ParameterizedType

abstract class MultiAutonomousMode<R : Robot, E: Enum<*>> : AutonomousMode<R>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(registrationHelper: VoltRegistrationHelper, clazz: Class<VoltOpMode<*>>) {
            if (clazz.isAnnotationPresent(VoltOpModeMeta::class.java)) {
                val annotation = clazz.getAnnotation(VoltOpModeMeta::class.java)
                if (annotation != null) {
                    // If this is not the direct superclass, this will not work, but I don't know a better way.
                    val enumClass = ((clazz.genericSuperclass as ParameterizedType).actualTypeArguments[1])
                    if (enumClass is Class<*> && enumClass.isEnum) {
                        enumClass.enumConstants!!.forEach { value ->
                            registrationHelper.register({
                                TypeHolder.type = value as Enum<*>?
                                instantiateOpMode(clazz)
                            }, OpModeMeta.Builder().setName("${annotation.name} $value").setGroup(annotation.group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(if (annotation.autoTransition == "") null else annotation.autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build())
                        }
                    }
                }
            }
        }
        fun <R : Robot> instantiateOpMode(clazz: Class<VoltOpMode<*>>) : VoltOpMode<R> {
            //
            return (clazz as Class<VoltOpMode<R>>).getDeclaredConstructor().newInstance()
        }
    }
    private object TypeHolder {
        var type: Enum<*>? = null
    }
    val type: E = TypeHolder.type as E
}
