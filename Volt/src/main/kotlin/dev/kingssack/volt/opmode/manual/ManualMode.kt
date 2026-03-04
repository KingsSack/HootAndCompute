package dev.kingssack.volt.opmode.manual

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event
import dev.kingssack.volt.util.buttons.AnalogHandler
import dev.kingssack.volt.util.buttons.ButtonHandler
import dev.kingssack.volt.util.buttons.GamepadAnalogInput
import dev.kingssack.volt.util.buttons.GamepadButton
import dev.kingssack.volt.util.telemetry.ActionTracer
import java.util.*

/**
 * ManualMode is an abstract class that defines the methods for running a manual mode.
 *
 * @param R the robot type
 * @property params the configuration object for manual control
 * @property robot the robot instance
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
    data class ManualParams(val deadzone: Float = 0.05f, val inputExp: Float = 2.0f)

    private val buttonEventsByButton =
        EnumMap<
            GamepadButton,
            MutableList<Pair<Event.ManualEvent.ButtonEvent, VoltActionBuilder<R>.() -> Unit>>,
        >(
            GamepadButton::class.java
        )
    private val analogEventsByInput =
        EnumMap<
            GamepadAnalogInput,
            MutableList<Pair<Event.ManualEvent.AnalogEvent, R.(Float) -> Unit>>,
        >(
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

    /** Define actions to be triggered by events */
    abstract fun defineEvents()

    /** Registers an event handler for when the button is tapped. */
    protected fun GamepadButton.onTap(block: VoltActionBuilder<R>.() -> Unit) {
        registerButtonEvent(this, Event.ManualEvent.ButtonEvent.Tap, block)
    }

    /** Registers an event handler for when the button is released. */
    protected fun GamepadButton.onRelease(block: VoltActionBuilder<R>.() -> Unit) {
        registerButtonEvent(this, Event.ManualEvent.ButtonEvent.Release, block)
    }

    /**
     * Registers an event handler for when the button is held for a certain duration.
     *
     * @param durationMs The duration in milliseconds for which the button must be held to trigger
     *   the event.
     */
    protected fun GamepadButton.onHold(
        durationMs: Double = 200.0,
        block: VoltActionBuilder<R>.() -> Unit,
    ) {
        registerButtonEvent(this, Event.ManualEvent.ButtonEvent.Hold(durationMs), block)
    }

    /** Registers an event handler for when the button is double-tapped. */
    protected fun GamepadButton.onDoubleTap(block: VoltActionBuilder<R>.() -> Unit) {
        registerButtonEvent(this, Event.ManualEvent.ButtonEvent.DoubleTap, block)
    }

    /**
     * Registers an event handler for when the button is pressed, regardless of how long it's held.
     */
    protected fun GamepadButton.whilePressed(block: R.() -> Unit) {
        registerInstantButton(this, block)
    }

    /** Registers an event handler for when the analog input changes. */
    protected fun GamepadAnalogInput.onChange(block: R.(Float) -> Unit) {
        registerAnalogEvent(this, Event.ManualEvent.AnalogEvent.Change, block)
    }

    /** Registers an event handler for when the analog input goes above a certain threshold. */
    protected fun GamepadAnalogInput.whenAbove(threshold: Float = 0.3f, block: R.(Float) -> Unit) {
        registerAnalogEvent(this, Event.ManualEvent.AnalogEvent.Threshold(threshold), block)
    }

    /** Registers an event handler for when a combination of buttons is pressed together. */
    protected fun combo(vararg buttons: GamepadButton) = ButtonComboBuilder(buttons.toSet())

    inner class ButtonComboBuilder(private val buttons: Set<GamepadButton>) {
        /**
         * Registers an event handler for when the specified combination of buttons is pressed
         * together.
         */
        infix fun then(block: VoltActionBuilder<R>.() -> Unit) {
            registerButtonCombo(buttons, block)
        }
    }

    private fun registerButtonEvent(
        button: GamepadButton,
        event: Event.ManualEvent.ButtonEvent,
        action: VoltActionBuilder<R>.() -> Unit,
    ) {
        buttonEventsByButton.getOrPut(button) { mutableListOf() }.add(event to action)
    }

    private fun registerAnalogEvent(
        input: GamepadAnalogInput,
        event: Event.ManualEvent.AnalogEvent,
        action: R.(Float) -> Unit,
    ) {
        analogEventsByInput.getOrPut(input) { mutableListOf() }.add(event to action)
    }

    private fun registerInstantButton(button: GamepadButton, action: R.() -> Unit) {
        instantButtons[button] = action
    }

    private fun registerButtonCombo(
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

    /** Initializes inputs and defines manual events */
    override fun initialize() {
        super.initialize()
        initializeInputMappings()
        defineEvents()
    }

    override fun begin() {
        while (opModeIsActive()) tick()
    }

    /** Tick the manual mode. */
    open fun tick() =
        context(telemetry) {
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
                if (event.triggered(buttonHandler)) triggerAction(action)
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

            handlers.forEach { (event, action) -> if (event.triggered(value)) robot.action(value) }
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
