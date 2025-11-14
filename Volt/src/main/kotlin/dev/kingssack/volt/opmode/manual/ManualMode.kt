package dev.kingssack.volt.opmode.manual

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.AnalogHandler
import dev.kingssack.volt.util.ButtonHandler
import dev.kingssack.volt.util.GamepadAnalogInput
import dev.kingssack.volt.util.GamepadButton
import java.util.*
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * ManualMode is an abstract class that defines the methods for running a manual mode.
 *
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
    data class ManualParams(val deadzone: Double = 0.1, val inputExp: Double = 2.0)

    private enum class InteractionType {
        RELEASE,
        DOUBLE_TAP,
        HOLD,
        PRESS,
    }

    private val interactionHandlers =
        mapOf(
            InteractionType.RELEASE to
                mutableMapOf<GamepadButton, VoltActionBuilder<R>.() -> Unit>(),
            InteractionType.DOUBLE_TAP to
                mutableMapOf<GamepadButton, VoltActionBuilder<R>.() -> Unit>(),
            InteractionType.HOLD to mutableMapOf<GamepadButton, VoltActionBuilder<R>.() -> Unit>(),
            InteractionType.PRESS to mutableMapOf<GamepadButton, VoltActionBuilder<R>.() -> Unit>(),
        )

    private lateinit var buttonStateGetters: EnumMap<GamepadButton, () -> Boolean>
    private lateinit var analogStateGetters: EnumMap<GamepadAnalogInput, () -> Float>

    private val buttonHandlers = EnumMap<GamepadButton, ButtonHandler>(GamepadButton::class.java)
    private val analogHandlers =
        EnumMap<GamepadAnalogInput, AnalogHandler>(GamepadAnalogInput::class.java)

    private var runningActions = mutableListOf<Action>()
    private val dash: FtcDashboard? = FtcDashboard.getInstance()

    private fun initializeInputMappings() {
        buttonStateGetters =
            EnumMap(
                mapOf(
                    GamepadButton.A1 to gamepad1::a,
                    GamepadButton.B1 to gamepad1::b,
                    GamepadButton.X1 to gamepad1::x,
                    GamepadButton.Y1 to gamepad1::y,
                    GamepadButton.LEFT_BUMPER1 to gamepad1::left_bumper,
                    GamepadButton.RIGHT_BUMPER1 to gamepad1::right_bumper,
                    GamepadButton.LEFT_STICK_BUTTON1 to gamepad1::left_stick_button,
                    GamepadButton.RIGHT_STICK_BUTTON1 to gamepad1::right_stick_button,
                    GamepadButton.DPAD_UP1 to gamepad1::dpad_up,
                    GamepadButton.DPAD_DOWN1 to gamepad1::dpad_down,
                    GamepadButton.DPAD_LEFT1 to gamepad1::dpad_left,
                    GamepadButton.DPAD_RIGHT1 to gamepad1::dpad_right,
                    GamepadButton.BACK1 to gamepad1::back,
                    GamepadButton.START1 to gamepad1::start,
                    GamepadButton.GUIDE1 to gamepad1::guide,
                    GamepadButton.A2 to gamepad2::a,
                    GamepadButton.B2 to gamepad2::b,
                    GamepadButton.X2 to gamepad2::x,
                    GamepadButton.Y2 to gamepad2::y,
                    GamepadButton.LEFT_BUMPER2 to gamepad2::left_bumper,
                    GamepadButton.RIGHT_BUMPER2 to gamepad2::right_bumper,
                    GamepadButton.LEFT_STICK_BUTTON2 to gamepad2::left_stick_button,
                    GamepadButton.RIGHT_STICK_BUTTON2 to gamepad2::right_stick_button,
                    GamepadButton.DPAD_UP2 to gamepad2::dpad_up,
                    GamepadButton.DPAD_DOWN2 to gamepad2::dpad_down,
                    GamepadButton.DPAD_LEFT2 to gamepad2::dpad_left,
                    GamepadButton.DPAD_RIGHT2 to gamepad2::dpad_right,
                    GamepadButton.BACK2 to gamepad2::back,
                    GamepadButton.START2 to gamepad2::start,
                    GamepadButton.GUIDE2 to gamepad2::guide,
                )
            )

        analogStateGetters =
            EnumMap(
                mapOf(
                    GamepadAnalogInput.LEFT_STICK_X1 to gamepad1::left_stick_x,
                    GamepadAnalogInput.LEFT_STICK_Y1 to gamepad1::left_stick_y,
                    GamepadAnalogInput.RIGHT_STICK_X1 to gamepad1::right_stick_x,
                    GamepadAnalogInput.RIGHT_STICK_Y1 to gamepad1::right_stick_y,
                    GamepadAnalogInput.LEFT_TRIGGER1 to gamepad1::left_trigger,
                    GamepadAnalogInput.RIGHT_TRIGGER1 to gamepad1::right_trigger,
                    GamepadAnalogInput.LEFT_STICK_X2 to gamepad2::left_stick_x,
                    GamepadAnalogInput.LEFT_STICK_Y2 to gamepad2::left_stick_y,
                    GamepadAnalogInput.RIGHT_STICK_X2 to gamepad2::right_stick_x,
                    GamepadAnalogInput.RIGHT_STICK_Y2 to gamepad2::right_stick_y,
                    GamepadAnalogInput.LEFT_TRIGGER2 to gamepad2::left_trigger,
                    GamepadAnalogInput.RIGHT_TRIGGER2 to gamepad2::right_trigger,
                )
            )

        // Initialize the handler maps
        GamepadButton.entries.forEach { button -> buttonHandlers[button] = ButtonHandler() }
        GamepadAnalogInput.entries.forEach { analog ->
            analogHandlers[analog] = AnalogHandler(params.deadzone, params.inputExp)
        }
    }

    override fun initialize() {
        super.initialize()
        initializeInputMappings()
    }

    override fun begin() {
        while (opModeIsActive()) context(telemetry) { tick() }
    }

    private fun updateButtonHandlers() {
        // Iterate through the enum values and update corresponding handlers
        for (button in GamepadButton.entries) {
            buttonHandlers[button]?.update(buttonStateGetters[button]!!())
        }
    }

    private fun updateAnalogHandlers() {
        // Iterate through the enum values and update corresponding handlers
        for (analogInput in GamepadAnalogInput.entries) {
            analogHandlers[analogInput]?.update(analogStateGetters[analogInput]!!().toDouble())
        }
    }

    private fun processInteractions() {
        processActionType(interactionHandlers[InteractionType.RELEASE]!!) { button ->
            isButtonReleased(button)
        }
        processActionType(interactionHandlers[InteractionType.DOUBLE_TAP]!!) { button ->
            isButtonDoubleTapped(button)
        }
        processActionType(interactionHandlers[InteractionType.HOLD]!!) { button ->
            isButtonHeld(button, 500.0)
        }
        processActionType(interactionHandlers[InteractionType.PRESS]!!) { button ->
            isButtonPressed(button)
        }
    }

    private fun processActionType(
        interactions: Map<GamepadButton, VoltActionBuilder<R>.() -> Unit>,
        condition: (GamepadButton) -> Boolean,
    ) {
        interactions.forEach { (button, actionBlock) ->
            if (condition(button)) {
                val builder = VoltActionBuilder(robot).apply(actionBlock)
                runningActions.add(builder.build())
            }
        }
    }

    private fun runActions() {
        val packet = TelemetryPacket()
        runningActions.removeAll { action ->
            action.preview(packet.fieldOverlay())
            !action.run(packet)
        }
        dash?.sendTelemetryPacket(packet)
    }

    /** Tick the manual mode. */
    context(telemetry: Telemetry)
    open fun tick() {
        updateButtonHandlers()
        updateAnalogHandlers()
        processInteractions()
        runActions()
        robot.update()
    }

    /**
     * Checks if a button has just been tapped (released after a short press).
     *
     * @param button the button
     * @return true if the button was just tapped, false otherwise
     */
    protected fun isButtonReleased(button: GamepadButton): Boolean {
        return buttonHandlers[button]?.released() ?: false
    }

    /**
     * Registers an action sequence to be executed when a button is tapped.
     *
     * @param button the button
     * @param block the action sequence to execute
     */
    protected fun onButtonReleased(button: GamepadButton, block: VoltActionBuilder<R>.() -> Unit) {
        interactionHandlers[InteractionType.RELEASE]?.set(button, block)
    }

    /**
     * Checks if a button has just been double-tapped (released after two quick presses).
     *
     * @param button the button
     * @return true if the button was just double-tapped, false otherwise
     */
    protected fun isButtonDoubleTapped(button: GamepadButton): Boolean {
        return buttonHandlers[button]?.doubleTapped() ?: false
    }

    /**
     * Registers an action sequence to be executed when a button is double-tapped.
     *
     * @param button the button
     * @param block the action sequence to execute
     */
    protected fun onButtonDoubleTapped(
        button: GamepadButton,
        block: VoltActionBuilder<R>.() -> Unit,
    ) {
        interactionHandlers[InteractionType.DOUBLE_TAP]?.set(button, block)
    }

    /**
     * Checks if a button is currently being held down for a specified duration.
     *
     * @param button the button
     * @param milliseconds the duration in milliseconds to check for
     * @return true if the button is held for the given duration, false otherwise
     */
    protected fun isButtonHeld(button: GamepadButton, milliseconds: Double): Boolean {
        return buttonHandlers[button]?.held(milliseconds) ?: false
    }

    /**
     * Registers an action sequence to be executed when a button is held down.
     *
     * @param button the button
     * @param block the action sequence to execute
     */
    protected fun onButtonHeld(button: GamepadButton, block: VoltActionBuilder<R>.() -> Unit) {
        interactionHandlers[InteractionType.HOLD]?.set(button, block)
    }

    /**
     * Checks if a button is currently pressed.
     *
     * @param button the button
     * @return true if the button is pressed, false otherwise
     */
    protected fun isButtonPressed(button: GamepadButton): Boolean {
        return buttonHandlers[button]?.pressed ?: false
    }

    /**
     * Registers an action sequence to be executed when a button is pressed.
     *
     * @param button the button
     * @param block the action sequence to execute
     */
    protected fun onButtonPressed(button: GamepadButton, block: VoltActionBuilder<R>.() -> Unit) {
        interactionHandlers[InteractionType.PRESS]?.set(button, block)
    }

    /**
     * Resets the tap count for a specific button. Useful if you want to ignore previous tap counts
     * under certain conditions.
     *
     * @param button the button
     */
    protected fun resetButtonTapCount(button: GamepadButton) {
        buttonHandlers[button]?.reset()
    }

    /**
     * Gets the value of an analog button.
     *
     * @param analogInput the analog button
     */
    protected fun getAnalogValue(analogInput: GamepadAnalogInput): Double {
        return analogHandlers[analogInput]?.value ?: 0.0
    }

    /**
     * Gets the raw value of an analog input, before deadzone and exponentiation.
     *
     * @param analogInput The GamepadAnalogInput enum value.
     * @return The raw analog value.
     */
    protected fun getRawAnalogValue(analogInput: GamepadAnalogInput): Float {
        return analogStateGetters[analogInput]?.invoke() ?: 0.0f
    }
}
