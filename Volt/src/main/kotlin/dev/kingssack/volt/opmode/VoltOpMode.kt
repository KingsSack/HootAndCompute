package dev.kingssack.volt.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.sinister.Scanner.Companion.INDEPENDENT
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.frozenmilk.sinister.targeting.NarrowSearch
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
    protected abstract fun register(registrationHelper:RegistrationHelper)

    @Suppress("unused")
    private object Scan : OpModeScanner() {
        override val loadAdjacencyRule = INDEPENDENT
        override val unloadAdjacencyRule = INDEPENDENT
        override val targets = NarrowSearch()
        override fun scan(loader: ClassLoader, cls: Class<*>, registrationHelper:RegistrationHelper) {
            if (VoltOpMode::class.java.isAssignableFrom(cls) && !Modifier.isAbstract(cls.modifiers))
                cls.getDeclaredMethod("register", RegistrationHelper::class.java)(registrationHelper)
        }
    }
}
