package dev.kingssack.volt.opmode.manual

import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event
import dev.kingssack.volt.util.buttons.AnalogHandler
import dev.kingssack.volt.util.buttons.AnalogInput
import dev.kingssack.volt.util.buttons.Button
import dev.kingssack.volt.util.buttons.ButtonHandler
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

/**
 * ManualMode is an abstract class that defines the methods for running a manual mode.
 *
 * @param R the robot type
 * @property params the configuration object for manual control
 */
abstract class ManualMode<R : Robot>(private val params: ManualParams = ManualParams()) :
    VoltOpMode<R>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(
            registrationHelper: VoltRegistrationHelper,
            clazz: Class<out VoltOpMode<*>>,
        ) {
            if (clazz.isAnnotationPresent(VoltOpModeMeta::class.java)) {
                val annotation = clazz.getAnnotation(VoltOpModeMeta::class.java)
                if (annotation != null) {
                    registrationHelper.register(
                        clazz.getDeclaredConstructor(),
                        OpModeMeta.Builder()
                            .setName(annotation.name)
                            .setGroup(annotation.group)
                            .setFlavor(OpModeMeta.Flavor.TELEOP)
                            .setSource(OpModeMeta.Source.EXTERNAL_LIBRARY)
                            .build(),
                    )
                }
            }
        }
    }

    /**
     * Configuration object for manual control.
     *
     * @property deadzone the minimum joystick input to register
     * @property inputExp the input exponential for fine control
     */
    data class ManualParams(val deadzone: Float = 0.05f, val inputExp: Float = 2.0f)

    /** Maps a [VoltActionBuilder] [block] to a [ManualEvent]. */
    //    protected infix fun Event.ManualEvent.then(block: VoltActionBuilder.() -> Unit) {
    //        eventHandler.register(this, block)
    //    }

    /**
     * Maps a [VoltActionBuilder] [block] to an [AnalogEvent] and provides the processed analog
     * value.
     */
    //    protected infix fun Event.ManualEvent.AnalogEvent.then(block: VoltActionBuilder.(Float) ->
    // Unit) {
    //        eventHandler.register(this, block)
    //    }

    /** Create a combo event with [buttons] */
    protected fun combo(vararg buttons: Button) = Event.ManualEvent.Combo(buttons.toSet())

    init {
        Button.entries.forEach { it.handler = ButtonHandler() }
        AnalogInput.entries.forEach { it.handler = AnalogHandler(params.deadzone, params.inputExp) }
    }

    /** Tick the manual mode. */
    override fun tick() {
        updateInputState()
        super.tick()
    }

    private fun updateInputState() {
        Button.entries.forEach { button ->
            val state = button.getter(gamepad1, gamepad2)
            button.handler.update(state)
        }

        AnalogInput.entries.forEach { analog ->
            val state = analog.getter(gamepad1, gamepad2)
            analog.handler.update(state)
        }
    }
}
