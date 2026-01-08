package dev.kingssack.volt.opmode.autonomous

import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

abstract class DualAutonomousMode<R : Robot> : AutonomousMode<R>() {
    private fun construct(color: AllianceColor) : DualAutonomousMode<R> {
        return try {
            javaClass.getDeclaredConstructor(AllianceColor::class.java).newInstance(color)
        } catch (e: NoSuchMethodException) {
            javaClass.getDeclaredConstructor().newInstance()
        }
    }
    override fun register(registrationHelper: RegistrationHelper) {
        val red : DualAutonomousMode<R> = construct(AllianceColor.RED)
        val blue : DualAutonomousMode<R> = construct(AllianceColor.BLUE)
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
        return if (color == AllianceColor.RED) {red} else {blue}
    }
}

enum class AllianceColor {
    RED,
    BLUE,
}
