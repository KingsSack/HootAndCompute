package dev.kingssack.volt.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

/**
 * An [AutonomousMode] that can be registered as two separate op modes, one for each alliance color.
 *
 * @property color the selected alliance color
 */
abstract class DualAutonomousMode<R : Robot> : AutonomousMode<R>() {
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
                        {
                            ColorHolder.color = AllianceColor.RED
                            val red =
                                (clazz as Class<DualAutonomousMode<*>>)
                                    .getDeclaredConstructor()
                                    .newInstance()
                            red
                        },
                        OpModeMeta.Builder()
                            .setName("${annotation.name} Red")
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
                        {
                            ColorHolder.color = AllianceColor.BLUE
                            val blue =
                                (clazz as Class<DualAutonomousMode<*>>)
                                    .getDeclaredConstructor()
                                    .newInstance()
                            blue
                        },
                        OpModeMeta.Builder()
                            .setName("${annotation.name} Blue")
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

    open val color: AllianceColor = ColorHolder.color!!

    object ColorHolder {
        var color: AllianceColor? = null
    }

    /** @return [red] if red alliance is selected, [blue] if blue alliance is selected */
    fun <T> forAlliance(red: T, blue: T): T = if (color == AllianceColor.RED) red else blue

    /**
     * @return [this] if red alliance is selected, or [this] mirrored if blue alliance is selected
     */
    fun Pose.mirrorIfBlue(): Pose = if (color == AllianceColor.BLUE) this.mirror() else this

    /**
     * @return [this] if blue alliance is selected, or [this] mirrored if red alliance is selected
     */
    fun Pose.mirrorIfRed(): Pose = if (color == AllianceColor.RED) this.mirror() else this
}

enum class AllianceColor {
    RED,
    BLUE,
}
