package dev.kingssack.volt.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

abstract class DualAutonomousMode<R : Robot> : AutonomousMode<R>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(registrationHelper: VoltRegistrationHelper, clazz: Class<VoltOpMode<*>>) {
            if (clazz.isAnnotationPresent(VoltOpModeMeta::class.java)) {
                val annotation = clazz.getAnnotation(VoltOpModeMeta::class.java)
                if (annotation != null) {
                    registrationHelper.register({
                        ColorHolder.color = AllianceColor.RED
                        val red = (clazz as Class<DualAutonomousMode<*>>).getDeclaredConstructor().newInstance()
                        red
                    }, OpModeMeta.Builder().setName("${annotation.name} Red").setGroup(annotation.group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(if (annotation.autoTransition == "") null else annotation.autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build())
                    registrationHelper.register({
                        ColorHolder.color = AllianceColor.BLUE
                        val blue = (clazz as Class<DualAutonomousMode<*>>).getDeclaredConstructor().newInstance()
                        blue
                    }, OpModeMeta.Builder().setName("${annotation.name} Blue").setGroup(annotation.group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(if (annotation.autoTransition == "") null else annotation.autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build())
                }
            }

        }
    }
    open val color: AllianceColor = ColorHolder.color!!
    private object ColorHolder {
        var color: AllianceColor? = null
    }
    /**
     * returns one of its 2 parameters depending on what alliance you're on
     * @param red what to return if you are on the red alliance
     * @param blue what to return if you are on the blue alliance
     * */
    @Suppress("unused")
    fun <T> sw(red: T, blue: T) : T {
        return if (color == AllianceColor.RED) red else blue
    }
    /**
     * returns its pose if red or pose mirrored if blue
     * */
    @Suppress("unused")
    fun  sw(pose: Pose) : Pose {
        return if (color == AllianceColor.RED) pose else pose.mirror()
    }
}

enum class AllianceColor {
    RED,
    BLUE,
}
