package dev.kingssack.volt.opmode

import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.Scanner.Companion.INDEPENDENT
import dev.frozenmilk.sinister.targeting.TeamCodeSearch
import dev.kingssack.volt.robot.Robot
import java.lang.reflect.Modifier

abstract class VoltOpMode<R : Robot>(robotFactory: (HardwareMap) -> R) : LinearOpMode() {
    protected val robot: R by lazy { robotFactory(hardwareMap) }

    /** Optional initialization code. */
    open fun initialize() {
        // Default implementation does nothing
    }

    /** Optional code to run when the op mode begins. */
    abstract fun begin()

    override fun runOpMode() {
        initialize()
        waitForStart()
        begin()
    }



    // runtime reflection to call register functions in opmodes
    protected abstract fun register(opModeManager: AnnotatedOpModeManager)
    @Suppress("unused")
    private object Scan : Scanner {

        override val loadAdjacencyRule = INDEPENDENT
        override val unloadAdjacencyRule = INDEPENDENT

        override val targets = TeamCodeSearch()
        var opmodes: MutableList<Class<*>> = mutableListOf()
        // It extends multiOpMode
        override fun scan(loader: ClassLoader, cls: Class<*>) {

            if (VoltOpMode::class.java.isAssignableFrom(cls) && !Modifier.isAbstract(cls.modifiers))
                opmodes.add(cls)
        }

        override fun unload(loader: ClassLoader, cls: Class<*>) {
            opmodes = mutableListOf()
        }
        @OpModeRegistrar
        fun registerOpModes(opModeManager: AnnotatedOpModeManager) {
            opmodes.forEach { it.getDeclaredMethod("register", AnnotatedOpModeManager::class.java)(opModeManager) }
        }
    }
}
