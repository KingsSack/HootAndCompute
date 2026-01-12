package dev.kingssack.volt.opmode.autonomous

import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

abstract class MultiAutonomousMode<R : Robot, E: Enum<E>>(val enumClass: Class<E>) : AutonomousMode<R>() {
    private fun construct(enum: E) : MultiAutonomousMode<R, E> {
        return try {
            javaClass.getDeclaredConstructor(AllianceColor::class.java).newInstance(enum)
        } catch (_: NoSuchMethodException) {
            javaClass.getDeclaredConstructor().newInstance()
        }
    }
    override fun register(registrationHelper: RegistrationHelper) {
        enumClass.enumConstants!!.forEach { value -> {
            val instance : MultiAutonomousMode<R, E> = construct(value)
            instance.type = value
            registrationHelper.register(OpModeMeta.Builder().setName("$name "+value.name).setGroup(group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), instance)
        } }
    }

    lateinit var type: E private set
}
