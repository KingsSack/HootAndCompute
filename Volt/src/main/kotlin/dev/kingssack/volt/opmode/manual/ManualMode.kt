package dev.kingssack.volt.opmode.manual

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.AnalogHandler
import dev.kingssack.volt.util.ButtonHandler
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * ManualMode is an abstract class that defines the methods for running a manual mode.
 *
 * @param telemetry for logging
 */
abstract class ManualMode(
    gamepad1: Gamepad,
    gamepad2: Gamepad,
    private val telemetry: Telemetry,
    private val params: ManualParams = ManualParams()
) {
    /**
     * Configuration object for manual control.
     *
     * @property deadzone the minimum joystick input to register
     * @property inputExp the input exponential for fine control
     */
    class ManualParams(
        val deadzone: Double = 0.1,
        val inputExp: Double = 2.0
    )

    // Buttons
    private val buttonHandlers = mutableMapOf<String, ButtonHandler>()
    private val analogHandlers = mutableMapOf<String, AnalogHandler>()

    private val buttons = listOf(
        "a1" to gamepad1::a, "b1" to gamepad1::b, "x1" to gamepad1::x, "y1" to gamepad1::y,
        "left_bumper1" to gamepad1::left_bumper, "right_bumper1" to gamepad1::right_bumper,
        "left_stick_button1" to gamepad1::left_stick_button, "right_stick_button1" to gamepad1::right_stick_button,
        "dpad_up1" to gamepad1::dpad_up, "dpad_down1" to gamepad1::dpad_down,
        "dpad_left1" to gamepad1::dpad_left, "dpad_right1" to gamepad1::dpad_right,
        "back1" to gamepad1::back, "start1" to gamepad1::start, "guide1" to gamepad1::guide,
        "a2" to gamepad2::a, "b2" to gamepad2::b, "x2" to gamepad2::x, "y2" to gamepad2::y,
        "left_bumper2" to gamepad2::left_bumper, "right_bumper2" to gamepad2::right_bumper,
        "left_stick_button2" to gamepad2::left_stick_button, "right_stick_button2" to gamepad2::right_stick_button,
        "dpad_up2" to gamepad2::dpad_up, "dpad_down2" to gamepad2::dpad_down,
        "dpad_left2" to gamepad2::dpad_left, "dpad_right2" to gamepad2::dpad_right,
        "back2" to gamepad2::back, "start2" to gamepad2::start, "guide2" to gamepad2::guide
    )

    private val analogButtons = listOf(
        "left_stick_x1" to gamepad1::left_stick_x, "left_stick_y1" to gamepad1::left_stick_y,
        "right_stick_x1" to gamepad1::right_stick_x, "right_stick_y1" to gamepad1::right_stick_y,
        "left_trigger1" to gamepad1::left_trigger, "right_trigger1" to gamepad1::right_trigger,
        "left_stick_x2" to gamepad2::left_stick_x, "left_stick_y2" to gamepad2::left_stick_y,
        "right_stick_x2" to gamepad2::right_stick_x, "right_stick_y2" to gamepad2::right_stick_y,
        "left_trigger2" to gamepad2::left_trigger, "right_trigger2" to gamepad2::right_trigger
    )

    init {
        buttons.forEach { (name, _) -> buttonHandlers[name] = ButtonHandler() }
        analogButtons.forEach { (name, _) -> analogHandlers[name] = AnalogHandler(params.deadzone, params.inputExp) }
    }

    private fun updateButtonHandlers() {
        buttons.forEach { (name, buttonGetter) -> buttonHandlers[name]?.update(buttonGetter.get()) }
    }

    private fun updateAnalogHandlers() {
        analogButtons.forEach { (name, analogGetter) -> analogHandlers[name]?.update(analogGetter.get().toDouble()) }
    }

    // Robot
    abstract val robot: Robot

    private val dash: FtcDashboard? = FtcDashboard.getInstance()

    private var runningActions = mutableListOf<Action>()

    // Interactions
    protected val interactions = mutableListOf<Interaction>()

    /**
     * An interaction is a trigger-action pair updated every tick.
     *
     * @param trigger the trigger condition
     * @param action the action to run
     */
    open inner class Interaction(private val trigger: () -> Boolean, private val action: () -> Action) {
        open fun update() {
            if (trigger()) runningActions.add(action())
        }
    }

    /**
     * An interaction that toggles between two actions.
     *
     * @param trigger the trigger condition
     * @param actionOne the first action
     * @param actionTwo the second action
     */
    inner class ToggleInteraction(
        private val trigger: () -> Boolean,
        private val actionOne: () -> Action,
        private val actionTwo: () -> Action
    ) : Interaction(trigger, { actionOne() }) {
        private var lastState = false

        override fun update() {
            if (trigger()) {
                if (lastState) runningActions.add(actionOne())
                else runningActions.add(actionTwo())
                lastState = !lastState
            }
        }
    }

    private fun updateInteractions() {
        interactions.forEach { it.update() }
    }

    /**
     * Runs the actions in the list of running actions.
     */
    private fun runActions() {
        val packet = TelemetryPacket()
        val newActions: MutableList<Action> = ArrayList()
        for (action in runningActions) {
            action.preview(packet.fieldOverlay())
            if (action.run(packet)) {
                newActions.add(action)
            }
        }
        runningActions = newActions
        dash?.sendTelemetryPacket(packet)
        telemetry.update()
    }

    /**
     * Tick the manual mode.
     *
     * @param telemetry for logging
     */
    open fun tick(telemetry: Telemetry) {
        updateButtonHandlers()
        updateAnalogHandlers()

        updateInteractions()

        // Run actions
        runActions()

        // Update robot
        robot.update(telemetry)
    }

    /**
     * Checks if a button has just been tapped (released after a short press).
     *
     * @param name the name of the button
     * @return true if the button was just tapped, false otherwise
     */
    protected fun isButtonTapped(name: String): Boolean {
        return buttonHandlers[name]?.tapped() ?: false
    }

    /**
     * Checks if a button has just been double-tapped (released after two quick presses).
     *
     * @param name name of the button
     * @return true if the button was just double-tapped, false otherwise
     */
    protected fun isButtonDoubleTapped(name: String): Boolean {
        return buttonHandlers[name]?.doubleTapped() ?: false
    }

    /**
     * Checks if a button is currently being held down for a specified duration.
     *
     * @param name The name of the button
     * @param milliseconds The duration in milliseconds to check for.
     * @return True if the button is held for the given duration, false otherwise.
     */
    protected fun isButtonHeld(name: String, milliseconds: Double): Boolean {
        return buttonHandlers[name]?.held(milliseconds) ?: false
    }

    /**
     * Checks if a button is currently pressed.
     *
     * @param name name of the button
     * @return true if the button is pressed, false otherwise
     */
    protected fun isButtonPressed(name: String): Boolean {
        return buttonHandlers[name]?.pressed ?: false
    }

    /**
     * Resets the tap count for a specific button.
     * Useful if you want to ignore previous tap counts under certain conditions.
     *
     * @param name name of the button
     */
    protected fun resetButtonTapCount(name: String) {
        buttonHandlers[name]?.reset()
    }

    /**
     * Gets the value of an analog button.
     *
     * @param name the name of the analog button
     */
    protected fun getAnalogValue(name: String): Double {
        return analogHandlers[name]?.value ?: 0.0
    }
}