package dev.kingssack.volt.opmode.autonomous

import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

abstract class DualAutonomousMode<R : Robot>(robotFactory: (HardwareMap) -> R) : AutonomousMode<R>(robotFactory) {

    override fun register(opModeManager: AnnotatedOpModeManager) {
        val red : DualAutonomousMode<R> = javaClass.getDeclaredConstructor().newInstance()
        val blue : DualAutonomousMode<R> = javaClass.getDeclaredConstructor().newInstance()
        red.color = AllianceColor.RED
        blue.color = AllianceColor.BLUE
        opModeManager.register(OpModeMeta.Builder().setName("$name red").setGroup(group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), red)
        opModeManager.register(OpModeMeta.Builder().setName("$name blue").setGroup(group).setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setTransitionTarget(autoTransition).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), blue)
    }
    lateinit var color: AllianceColor private set
    /**
     * returns one of its 2 parameters depending on what alliance you're on
     * @param red what to return if you are on the red alliance
     * @param blue what to return if you are on the blue alliance
     * */
    @Suppress("unused")
    fun <a> sw(red: a, blue: a) : a {
        return if (color == AllianceColor.RED) {red} else {blue}
    }
}

enum class AllianceColor {
    RED,
    BLUE,
}
