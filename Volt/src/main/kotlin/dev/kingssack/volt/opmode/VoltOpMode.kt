package dev.kingssack.volt.opmode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.sinister.Scanner.Companion.INDEPENDENT
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.frozenmilk.sinister.targeting.NarrowSearch
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.VoltLogs
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.lang.reflect.Modifier
import java.lang.reflect.Constructor
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.reflect

abstract class VoltOpMode<R : Robot>() {
    protected abstract val robot: R
    val hardwareMap: HardwareMap = OpModeInfoHolder.hardwareMap!!
    val telemetry : Telemetry = OpModeInfoHolder.telemetry!!
    val gamepad1 : Gamepad =  OpModeInfoHolder.gamepad1!!
    val gamepad2 : Gamepad = OpModeInfoHolder.gamepad2!!
    val blackboard : java.util.HashMap<String, Any> = OpModeInfoHolder.blackboard!!
    fun opModeInInit() : Boolean {
        return OpModeInfoHolder.opModeInInit!!()
    }

    /** code to run when the op mode begins. */
    abstract fun begin()
    fun opModeIsActive(): Boolean {
        return OpModeInfoHolder.isActiveFunction!!()
    }
    private object OpModeInfoHolder {
        var isActiveFunction: (() -> Boolean)? = null
        var hardwareMap: HardwareMap? = null
        var telemetry: Telemetry? = null
        var gamepad1 : Gamepad? = null
        var gamepad2 : Gamepad? = null
        var blackboard : java.util.HashMap<String, Any>? = null
        var opModeInInit : (() -> Boolean)? = null
    }
    // runtime reflection to call register functions in opmodes
    class VoltRegistrationHelper(val h: RegistrationHelper) {
        private class InternalOpMode <R: Robot>(val opModeBuilder: () -> VoltOpMode<R>) : LinearOpMode() {
            override fun runOpMode() {
                telemetry.update()
                VoltOpModeWrapper.initializeOpMode()
                OpModeInfoHolder.blackboard = blackboard
                OpModeInfoHolder.telemetry = telemetry
                OpModeInfoHolder.hardwareMap = hardwareMap
                OpModeInfoHolder.isActiveFunction = { this.opModeIsActive() }
                OpModeInfoHolder.gamepad1 = gamepad1
                OpModeInfoHolder.gamepad2 = gamepad2
                OpModeInfoHolder.opModeInInit = { this.opModeInInit() }
                val opMode = opModeBuilder()
                VoltOpModeWrapper.postInitializeOpMode(opMode, opMode.robot, opMode.javaClass)
                waitForStart()
                opMode.begin()

            }
        }
        fun register(c: Constructor<VoltOpMode<*>>, meta: OpModeMeta) {
            h.register(meta, InternalOpMode {
                // works
                try {
                    c.newInstance()
                } catch (e: InvocationTargetException) {

                    // always catch InvocationTargetExceptions. they will crash the robot otherwise
                    throw e.cause!!
                }} )
        }
        fun <R: Robot> register(c: () -> VoltOpMode<R>, meta: OpModeMeta) {
            h.register(meta, InternalOpMode({
                try {
                    c()
                } catch (e: InvocationTargetException) {
                    // always catch InvocationTargetExceptions. they will crash the robot otherwise
                    throw e.cause!!
                }
            }))
        }
    }
    abstract class Registrar {
        abstract fun register(registrationHelper: VoltRegistrationHelper, clazz: Class<VoltOpMode<*>>)
    }
    @Suppress("unused")
    private object Scan : OpModeScanner() {
        override val loadAdjacencyRule = INDEPENDENT
        override val unloadAdjacencyRule = INDEPENDENT
        override val targets = NarrowSearch()
        override fun scan(loader: ClassLoader, cls: Class<*>, registrationHelper: RegistrationHelper) {
            try {
                val registrationHelper = VoltRegistrationHelper(registrationHelper)

                if (VoltOpMode::class.java.isAssignableFrom(cls) && !Modifier.isAbstract(cls.modifiers)) {
                    var c = cls
                    while (c !== VoltOpMode::class.java) {
                        val registrar = (c.declaredClasses.firstOrNull { Registrar::class.java.isAssignableFrom(it) && it.fields.any { it.name==="INSTANCE" } }?.getDeclaredField("INSTANCE")?.get(null)) as? Registrar?
                        if (registrar !== null) {
                            registrar.register(registrationHelper, cls as Class<VoltOpMode<*>>)
                            return
                        }
                        c = c.superclass as Class<*>
                    }
                }
            } catch (e : Throwable) {
                VoltLogs.log("error registering opmodes: ${e.message.toString()}")
                registrationHelper.register(OpModeMeta.Builder().setFlavor(OpModeMeta.Flavor.AUTONOMOUS).setName("error: $e").build(), object : OpMode() {
                    override fun init() {}
                    override fun loop() {}
                })
                // create an opmode with the error in its name
            }
        }
    }
}
