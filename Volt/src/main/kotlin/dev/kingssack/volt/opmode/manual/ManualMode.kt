package dev.kingssack.volt.opmode.manual

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.sinister.sdk.opmodes.OpModeScanner.RegistrationHelper
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.AnalogHandler
import dev.kingssack.volt.util.ButtonHandler
import dev.kingssack.volt.util.GamepadAnalogInput
import dev.kingssack.volt.util.GamepadButton
import java.util.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

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
    abstract val name: String
    open val group : String = OpModeMeta.DefaultGroup
    override fun register(registrationHelper: RegistrationHelper) {
        registrationHelper.register(OpModeMeta.Builder().setName(name).setGroup(group).setFlavor(OpModeMeta.Flavor.TELEOP).setSource(OpModeMeta.Source.EXTERNAL_LIBRARY).build(), javaClass.getDeclaredConstructor().newInstance())
    }
    /**
     * Configuration object for manual control.
     *
     * @property deadzone the minimum joystick input to register
     * @property inputExp the input exponential for fine control
     */
    data class ManualParams(val deadzone: Float = 0.1f, val inputExp: Float = 2.0f)

    private enum class InteractionType {
        RELEASE,
        TAP,
        DOUBLE_TAP,
        HOLD,
    }

    private val interactionHandlers =
        mapOf(
            InteractionType.RELEASE to
                mutableMapOf<GamepadButton, VoltActionBuilder<R>.() -> Unit>(),
            InteractionType.TAP to mutableMapOf<GamepadButton, VoltActionBuilder<R>.() -> Unit>(),
            InteractionType.DOUBLE_TAP to
                mutableMapOf<GamepadButton, VoltActionBuilder<R>.() -> Unit>(),
            InteractionType.HOLD to mutableMapOf<GamepadButton, VoltActionBuilder<R>.() -> Unit>(),
        )

    private val instantHoldHandlers = mutableMapOf<GamepadButton, R.() -> Unit>()
    private val instantAnalogHandlers = mutableMapOf<GamepadAnalogInput, R.(Float) -> Unit>()

    private val buttonHandlers = EnumMap<GamepadButton, ButtonHandler>(GamepadButton::class.java)
    private val analogHandlers =
        EnumMap<GamepadAnalogInput, AnalogHandler>(GamepadAnalogInput::class.java)

    private var runningActions = mutableListOf<Action>()
    private val dash: FtcDashboard? = FtcDashboard.getInstance()

    private fun initializeInputMappings() {
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
        GamepadButton.entries.forEach { btn ->
            val state = btn.get(gamepad1, gamepad2)
            buttonHandlers[btn]?.update(state)
        }
    }

    private fun updateAnalogHandlers() {
        GamepadAnalogInput.entries.forEach { btn ->
            val state = btn.get(gamepad1, gamepad2)
            analogHandlers[btn]?.update(state)
        }
    }

    private fun processInteractions() {
        processActionType(interactionHandlers[InteractionType.RELEASE]!!) { button ->
            isButtonReleased(button)
        }
        processActionType(interactionHandlers[InteractionType.TAP]!!) { button ->
            isButtonTapped(button)
        }
        processActionType(interactionHandlers[InteractionType.DOUBLE_TAP]!!) { button ->
            isButtonDoubleTapped(button)
        }
        processActionType(interactionHandlers[InteractionType.HOLD]!!) { button ->
            isButtonHeld(button)
        }

        instantHoldHandlers.forEach { (button, block) ->
            if (buttonHandlers[button]?.pressed == true) {
                robot.block()
            }
        }

        instantAnalogHandlers.forEach { (input, block) ->
            val value = getAnalogValue(input)
            robot.block(value)
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
     * Checks if a button has just been released.
     *
     * @param button the button
     * @return true if the button was just released, false otherwise
     */
    @Suppress("unused")
    protected fun isButtonReleased(button: GamepadButton): Boolean {
        return buttonHandlers[button]?.released() == true
    }

    /**
     * Registers an action sequence to be executed when a button is released.
     *
     * @param button the button
     * @param block the action sequence to execute
     */
    @Suppress("unused")
    protected fun onButtonReleased(button: GamepadButton, block: VoltActionBuilder<R>.() -> Unit) {
        interactionHandlers[InteractionType.RELEASE]?.set(button, block)
    }

    /**
     * Checks if a button has just been tapped.
     *
     * @param button the button
     * @return true if the button was just tapped, false otherwise
     */
    @Suppress("unused")
    protected fun isButtonTapped(button: GamepadButton): Boolean {
        return buttonHandlers[button]?.justPressed() == true
    }

    /**
     * Registers an action sequence to be executed when a button is tapped.
     *
     * @param button the button
     * @param block the action sequence to execute
     */
    @Suppress("unused")
    protected fun onButtonTapped(button: GamepadButton, block: VoltActionBuilder<R>.() -> Unit) {
        interactionHandlers[InteractionType.TAP]?.set(button, block)
    }

    /**
     * Checks if a button has just been double-tapped (released after two quick presses).
     *
     * @param button the button
     * @return true if the button was just double-tapped, false otherwise
     */
    @Suppress("unused")
    protected fun isButtonDoubleTapped(button: GamepadButton): Boolean {
        return buttonHandlers[button]?.doubleTapped() == true
    }

    /**
     * Registers an action sequence to be executed when a button is double-tapped.
     *
     * @param button the button
     * @param block the action sequence to execute
     */
    @Suppress("unused")
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
    @Suppress("unused")
    protected fun isButtonHeld(button: GamepadButton, milliseconds: Double = 500.0): Boolean {
        return buttonHandlers[button]?.held(milliseconds) == true
    }

    /**
     * Registers an action sequence to be executed when a button is held down.
     *
     * @param button the button
     * @param block the action sequence to execute
     */
    @Suppress("unused")
    protected fun onButtonHeld(button: GamepadButton, block: VoltActionBuilder<R>.() -> Unit) {
        interactionHandlers[InteractionType.HOLD]?.set(button, block)
    }

    /**
     * Checks if a button is currently pressed.
     *
     * @param button the button
     * @return true if the button is pressed, false otherwise
     */
    @Suppress("unused")
    protected fun isButtonPressed(button: GamepadButton): Boolean {
        return buttonHandlers[button]?.pressed == true
    }

    /**
     * Resets the tap count for a specific button. Useful if you want to ignore previous tap counts
     * under certain conditions.
     *
     * @param button the button
     */
    @Suppress("unused")
    protected fun resetButtonTapCount(button: GamepadButton) {
        buttonHandlers[button]?.reset()
    }

    /**
     * Gets the value of an analog button.
     *
     * @param input the analog input
     */
    @Suppress("unused")
    protected fun getAnalogValue(input: GamepadAnalogInput): Float {
        return analogHandlers[input]?.value ?: 0.0f
    }

    /**
     * Gets the raw value of an analog input, before deadzone and exponentiation.
     *
     * @param input the analog input
     * @return the raw analog value
     */
    @Suppress("unused")
    protected fun getRawAnalogValue(input: GamepadAnalogInput): Float {
        return input.get(gamepad1, gamepad2)
    }

    /**
     * Registers a block to be executed continuously while a button is held down.
     *
     * @param button the button
     */
    @Suppress("unused")
    protected fun whileButtonHeld(button: GamepadButton, block: R.() -> Unit) {
        instantHoldHandlers[button] = block
    }

    /**
     * Registers a block to be executed continuously with the value of an analog input.
     *
     * @param input the analog input
     */
    @Suppress("unused")
    protected fun onAnalog(input: GamepadAnalogInput, block: R.(Float) -> Unit) {
        instantAnalogHandlers[input] = block
    }
}
