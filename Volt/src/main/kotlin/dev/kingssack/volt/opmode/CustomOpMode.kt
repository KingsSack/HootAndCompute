package dev.kingssack.volt.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import dev.kingssack.volt.opmode.manual.ManualMode
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Flavor

abstract class AutonomousOpMode : LinearOpMode() {
    protected lateinit var auto: AutonomousMode

    open override fun runOpMode() {
        waitForStart()
        if (opModeIsActive()) auto.execute()
    }
}

abstract class ManualOpMode : OpMode() {
    protected lateinit var manual: ManualMode

    override fun loop() {
        manual.tick(telemetry)
    }
}

private const val GROUP = "Volt"

fun metaForClass(cls: Class<out OpMode?>, flavor: Flavor): OpModeMeta {
    return OpModeMeta.Builder()
        .setName(cls.simpleName)
        .setGroup(GROUP)
        .setFlavor(flavor)
        .build()
}

object CustomOpModes {
    @JvmStatic
    val autos = mutableListOf<AutonomousOpMode>()

    @JvmStatic
    val teleops = mutableListOf<ManualOpMode>()

    @JvmStatic
    @OpModeRegistrar
    fun register(manager: OpModeManager) {
        autos.forEach {
            manager.register(metaForClass(it::class.java, Flavor.AUTONOMOUS), it)
        }
        teleops.forEach {
            manager.register(metaForClass(it::class.java, Flavor.TELEOP), it)
        }
    }
}