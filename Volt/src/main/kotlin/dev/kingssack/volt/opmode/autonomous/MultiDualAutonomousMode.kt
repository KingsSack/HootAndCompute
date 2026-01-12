package dev.kingssack.volt.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

abstract class MultiDualAutonomousMode<R : Robot, E: Enum<E>>(val enumClass: Class<E>) : AutonomousMode<R>() {
    override fun register(registrationHelper: RegistrationHelper) {
        enumClass.enumConstants!!.forEach { value -> {
            val blue : MultiDualAutonomousMode<R, E> = javaClass.getDeclaredConstructor().newInstance()
            val red : MultiDualAutonomousMode<R, E> = javaClass.getDeclaredConstructor().newInstance()
            blue.type = value
            red.type = value
            red.color = AllianceColor.RED
            blue.color = AllianceColor.BLUE
            registrationHelper.register(OpModeMeta.Builder().setName("$name "+value.name+" Blue").setGroup(group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), blue)
            registrationHelper.register(OpModeMeta.Builder().setName("$name "+value.name+" Red").setGroup(group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), red)
        } }
    }
    lateinit var color: AllianceColor private set

    /**
     * returns one of its 2 parameters depending on what alliance you're on
     * @param red what to return if you are on the red alliance
     * @param blue what to return if you are on the blue alliance
     * */
    @Suppress("unused")
    fun <T> sw(red: T, blue: T) : T {
        return if (color == AllianceColor.RED) {red} else {blue}
    }
    /**
     * returns its pose if red or pose mirrored if blue
     * */
    @Suppress("unused")
    fun  sw(pose: Pose) : Pose {
        return if (color == AllianceColor.RED) pose else pose.mirror()
    }

    lateinit var type: E private set
}
