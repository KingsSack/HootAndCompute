package dev.kingssack.volt.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.sinister.Scanner.Companion.INDEPENDENT
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.frozenmilk.sinister.targeting.NarrowSearch
import dev.kingssack.volt.robot.Robot
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
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
        override fun scan(loader: ClassLoader, cls: Class<*>, registrationHelper: RegistrationHelper) {
            try {
                if (VoltOpMode::class.java.isAssignableFrom(cls) && !Modifier.isAbstract(cls.modifiers))
                    (cls as Class<VoltOpMode<*>>).getDeclaredConstructor().newInstance().register(registrationHelper)
                // due to type erasure, casting is necessary here
            } catch (e : Error) {
                registrationHelper.register(OpModeMeta.Builder().setName("error: $e").build(), object : OpMode() {
                    override fun init() {}
                    override fun loop() {}
                })
                // create an opmode with the error in its name
            }
        }
    }
}
