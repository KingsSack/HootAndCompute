package dev.kingssack.volt.opmode.autonomous

import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

abstract class MultiAutonomousMode<R : Robot, E: Enum<E>>(val enumClass: Class<E>) : AutonomousMode<R>() {
    override fun register(registrationHelper: RegistrationHelper) {
        enumClass.enumConstants!!.forEach { value -> {
            val instance : MultiAutonomousMode<R, E> = javaClass.getDeclaredConstructor().newInstance()
            instance.type = value
            registrationHelper.register(OpModeMeta.Builder().setName("$name "+value.name).setGroup(group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), instance)
        } }
    }

    lateinit var type: E private set
}
