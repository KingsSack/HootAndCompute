package dev.kingssack.volt.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.lang.reflect.ParameterizedType

abstract class MultiDualAutonomousMode<R : Robot, E: Enum<*>>(val enumClass: Class<E>) : DualAutonomousMode<R>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(registrationHelper: VoltRegistrationHelper, clazz: Class<VoltOpMode<*>>) {
            if (clazz.isAnnotationPresent(VoltOpModeMeta::class.java)) {
                val annotation = clazz.getAnnotation(VoltOpModeMeta::class.java)
                if (annotation != null) {
                    // If this is not the direct superclass, this might not work, but I don't know a better way.
                    val enumClass = ((clazz.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<Enum<*>>)
                    enumClass.enumConstants!!.forEach { value ->
                        registrationHelper.register({
                            instantiateOpMode(value as Enum<*>, clazz, AllianceColor.BLUE)
                        }, OpModeMeta.Builder().setName("${annotation.name} $value Blue").setGroup(annotation.group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(if (annotation.autoTransition == "") null else annotation.autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build())
                        registrationHelper.register({
                            instantiateOpMode(value as Enum<*>, clazz, AllianceColor.RED)
                        }, OpModeMeta.Builder().setName("${annotation.name} $value Red").setGroup(annotation.group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(if (annotation.autoTransition == "") null else annotation.autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build())
                    }
                }
            }
        }
        fun <R : Robot, E: Enum<*>> instantiateOpMode(value: E, clazz: Class<VoltOpMode<*>>, color: AllianceColor) : MultiDualAutonomousMode<R, E> {
            InfoHolder.color = color
            InfoHolder.type = value
            return (clazz as Class<MultiDualAutonomousMode<R, E>>).getDeclaredConstructor().newInstance()
        }
    }
    private object InfoHolder {
        var color: AllianceColor? = null
        var type: Enum<*>? = null
    }
    override val color: AllianceColor = InfoHolder.color!!

    val type: E = InfoHolder.type as E
}
