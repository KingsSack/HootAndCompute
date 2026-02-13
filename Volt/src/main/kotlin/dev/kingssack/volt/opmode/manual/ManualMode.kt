package dev.kingssack.volt.opmode.manual

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.buttons.AnalogEvent
import dev.kingssack.volt.util.buttons.AnalogHandler
import dev.kingssack.volt.util.buttons.ButtonEvent
import dev.kingssack.volt.util.buttons.ButtonHandler
import dev.kingssack.volt.util.buttons.ControlScope
import dev.kingssack.volt.util.buttons.GamepadAnalogInput
import dev.kingssack.volt.util.buttons.GamepadButton
import dev.kingssack.volt.util.telemetry.ActionTracer
import java.util.*
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * ManualMode is an abstract class that defines the methods for running a manual mode.
 *
 * @param R the robot type
 * @property params the configuration object for manual control
 * @property robot the robot instance
 * @property controls the control mappings for the manual mode
 */
abstract class ManualMode<R : Robot>(
    robotFactory: (HardwareMap) -> R,
    private val params: ManualParams = ManualParams(),
) : VoltOpMode<R>(robotFactory) {
    /**
     * Configuration object for manual control.
     *
     * @property deadzone the minimum joystick input to register
     * @property inputExp the input exponential for fine control
     */
    data class ManualParams(val deadzone: Float = 0.1f, val inputExp: Float = 2.0f)

    private data class EventHandler<R : Robot>(
        val event: ButtonEvent,
        val action: VoltActionBuilder<R>.() -> Unit,
    )

    private val buttonEventsByButton =
        EnumMap<GamepadButton, MutableList<EventHandler<R>>>(GamepadButton::class.java)
    private val analogEventsByInput =
        EnumMap<GamepadAnalogInput, MutableList<Pair<AnalogEvent, R.(Float) -> Unit>>>(
            GamepadAnalogInput::class.java
        )
    private val instantButtons = EnumMap<GamepadButton, R.() -> Unit>(GamepadButton::class.java)
    private val buttonCombos =
        mutableListOf<Pair<Set<GamepadButton>, VoltActionBuilder<R>.() -> Unit>>()

    private val buttonHandlers = EnumMap<GamepadButton, ButtonHandler>(GamepadButton::class.java)
    private val analogHandlers =
        EnumMap<GamepadAnalogInput, AnalogHandler>(GamepadAnalogInput::class.java)

    private var runningActions = mutableListOf<Action>()
    private val dash: FtcDashboard? = FtcDashboard.getInstance()

    abstract val controls: ControlScope<R>.() -> Unit

    /** Can be used to define control mappings for the manual mode. */
    protected fun controls(block: ControlScope<R>.() -> Unit) = block

    internal fun registerButtonEvent(
        button: GamepadButton,
        event: ButtonEvent,
        action: VoltActionBuilder<R>.() -> Unit,
    ) {
        buttonEventsByButton.getOrPut(button) { mutableListOf() }.add(EventHandler(event, action))
    }

    internal fun registerAnalogEvent(
        input: GamepadAnalogInput,
        event: AnalogEvent,
        action: R.(Float) -> Unit,
    ) {
        analogEventsByInput.getOrPut(input) { mutableListOf() }.add(event to action)
    }

    internal fun registerInstantButton(button: GamepadButton, action: R.() -> Unit) {
        instantButtons[button] = action
    }

    internal fun registerButtonCombo(
        buttons: Set<GamepadButton>,
        action: VoltActionBuilder<R>.() -> Unit,
    ) {
        buttonCombos.add(buttons to action)
    }

    private fun initializeInputMappings() {
        GamepadButton.entries.forEach { button -> buttonHandlers[button] = ButtonHandler() }
        GamepadAnalogInput.entries.forEach { analog ->
            analogHandlers[analog] = AnalogHandler(params.deadzone, params.inputExp)
        }
    }

    override fun initialize() {
        super.initialize()
        initializeInputMappings()
        ControlScope(this).apply(controls)
    }

    override fun begin() {
        while (opModeIsActive()) context(telemetry) { tick() }
    }

    /** Tick the manual mode. */
    context(telemetry: Telemetry)
    open fun tick() {
        updateInputState()
        processEvents()
        runActions()
        robot.update()
    }

    private fun updateInputState() {
        GamepadButton.entries.forEach { button ->
            val state = button.get(gamepad1, gamepad2)
            buttonHandlers[button]?.update(state)
        }

        GamepadAnalogInput.entries.forEach { analog ->
            val state = analog.get(gamepad1, gamepad2)
            analogHandlers[analog]?.update(state)
        }
    }

    private fun processEvents() {
        buttonEventsByButton.forEach { (button, handlers) ->
            val buttonHandler = buttonHandlers[button] ?: return@forEach

            handlers.forEach { (event, action) ->
                val shouldTrigger =
                    when (event) {
                        ButtonEvent.Tap -> buttonHandler.justPressed()
                        ButtonEvent.Release -> buttonHandler.justReleased()
                        ButtonEvent.DoubleTap -> buttonHandler.doubleTapped()
                        is ButtonEvent.Hold -> buttonHandler.held(event.durationMs)
                        is ButtonEvent.Combo -> false
                    }

                if (shouldTrigger) triggerAction(action)
            }
        }

        buttonCombos.forEach { (buttons, action) ->
            val allPressed = buttons.all { buttonHandlers[it]?.pressed == true }
            val anyJustPressed = buttons.any { buttonHandlers[it]?.justPressed() == true }

            if (allPressed && anyJustPressed) triggerAction(action)
        }

        instantButtons.forEach { (button, block) ->
            if (buttonHandlers[button]?.pressed == true) {
                robot.block()
            }
        }

        analogEventsByInput.forEach { (input, handlers) ->
            val handler = analogHandlers[input] ?: return@forEach
            val value = handler.value

            handlers.forEach { (event, action) ->
                val shouldTrigger =
                    when (event) {
                        AnalogEvent.Change -> value != 0.0f
                        is AnalogEvent.Threshold -> value >= event.min
                    }

                if (shouldTrigger) robot.action(value)
            }
        }
    }

    private fun triggerAction(block: VoltActionBuilder<R>.() -> Unit) {
        val builder = VoltActionBuilder(robot).apply(block)
        runningActions.add(builder.build())
    }

    private fun runActions() {
        val packet = TelemetryPacket()
        runningActions.removeAll { action ->
            action.preview(packet.fieldOverlay())
            !action.run(packet)
        }
        context(packet) { ActionTracer.writePacket() }
        dash?.sendTelemetryPacket(packet)
    }
}
