package dev.kingssack.volt.opmode.manual

import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event
import dev.kingssack.volt.util.EventHandler
import dev.kingssack.volt.util.buttons.AnalogHandler
import dev.kingssack.volt.util.buttons.AnalogInput
import dev.kingssack.volt.util.buttons.Button
import dev.kingssack.volt.util.buttons.ButtonHandler
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.util.*

/**
 * ManualMode is an abstract class that defines the methods for running a manual mode.
 *
 * @param R the robot type
 * @property params the configuration object for manual control
 */
abstract class ManualMode<R : Robot>(private val params: ManualParams = ManualParams()) :
    VoltOpMode<R, Event.ManualEvent>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(
            registrationHelper: VoltRegistrationHelper,
            clazz: Class<VoltOpMode<*, *>>,
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

    private val buttonHandlers = EnumMap<Button, ButtonHandler>(Button::class.java)
    private val analogHandlers = EnumMap<AnalogInput, AnalogHandler>(AnalogInput::class.java)

    override val eventHandler = EventHandler.ManualHandler(buttonHandlers, analogHandlers)

    /** Create a combo event with [buttons] */
    protected fun combo(vararg buttons: Button) = Event.ManualEvent.Combo(buttons.toSet())

    private fun initializeInputMappings() {
        Button.entries.forEach { button -> buttonHandlers[button] = ButtonHandler() }
        AnalogInput.entries.forEach { analog ->
            analogHandlers[analog] = AnalogHandler(params.deadzone, params.inputExp)
        }
    }

    init {
        initializeInputMappings()
    }

    /** Tick the manual mode. */
    override fun tick() {
        updateInputState()
        super.tick()
    }

    private fun updateInputState() {
        Button.entries.forEach { button ->
            val state = button.get(gamepad1, gamepad2)
            buttonHandlers[button]?.update(state)
        }

        AnalogInput.entries.forEach { analog ->
            val state = analog.get(gamepad1, gamepad2)
            analogHandlers[analog]?.update(state)
        }
    }
}
