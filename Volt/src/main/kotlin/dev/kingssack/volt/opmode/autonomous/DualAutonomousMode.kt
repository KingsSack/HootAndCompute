package dev.kingssack.volt.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import kotlin.jvm.javaClass

abstract class DualAutonomousMode<R : Robot> : AutonomousMode<R>() {
    override fun register(registrationHelper: RegistrationHelper) {
        val red : DualAutonomousMode<R> = javaClass.getDeclaredConstructor().newInstance()
        val blue : DualAutonomousMode<R> = javaClass.getDeclaredConstructor().newInstance()
        red.color = AllianceColor.RED
        blue.color = AllianceColor.BLUE
        registrationHelper.register(OpModeMeta.Builder().setName("$name Red").setGroup(group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), red)
        registrationHelper.register(OpModeMeta.Builder().setName("$name Blue").setGroup(group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), blue)
    }

    lateinit var color: AllianceColor private set

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
