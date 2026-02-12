package dev.kingssack.volt.util.buttons

import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.manual.ManualMode
import dev.kingssack.volt.robot.Robot

class ControlScope<R : Robot>(private val mode: ManualMode<R>) {
    /** Registers an event handler for when the button is tapped. */
    fun GamepadButton.onTap(block: VoltActionBuilder<R>.() -> Unit) {
        mode.registerButtonEvent(this, ButtonEvent.Tap, block)
    }

    /** Registers an event handler for when the button is released. */
    fun GamepadButton.onRelease(block: VoltActionBuilder<R>.() -> Unit) {
        mode.registerButtonEvent(this, ButtonEvent.Release, block)
    }

    /** Registers an event handler for when the button is held for a certain duration. */
    fun GamepadButton.onHold(durationMs: Double = 200.0, block: VoltActionBuilder<R>.() -> Unit) {
        mode.registerButtonEvent(this, ButtonEvent.Hold(durationMs), block)
    }

    /** Registers an event handler for when the button is double-tapped. */
    fun GamepadButton.onDoubleTap(block: VoltActionBuilder<R>.() -> Unit) {
        mode.registerButtonEvent(this, ButtonEvent.DoubleTap, block)
    }

    /**
     * Registers an event handler for when the button is pressed, regardless of how long it's held.
     */
    fun GamepadButton.whilePressed(block: R.() -> Unit) {
        mode.registerInstantButton(this, block)
    }

    /** Registers an event handler for when the analog input changes. */
    fun GamepadAnalogInput.onChange(block: R.(Float) -> Unit) {
        mode.registerAnalogEvent(this, AnalogEvent.Change, block)
    }

    /** Registers an event handler for when the analog input goes above a certain threshold. */
    fun GamepadAnalogInput.whenAbove(threshold: Float = 0.3f, block: R.(Float) -> Unit) {
        mode.registerAnalogEvent(this, AnalogEvent.Threshold(threshold), block)
    }

    /** Registers an event handler for when a combination of buttons is pressed together. */
    fun combo(vararg buttons: GamepadButton) = ButtonComboBuilder(buttons.toSet(), mode)

    inner class ButtonComboBuilder(
        private val buttons: Set<GamepadButton>,
        private val mode: ManualMode<R>,
    ) {
        /**
         * Registers an event handler for when the specified combination of buttons is pressed
         * together.
         */
        infix fun then(block: VoltActionBuilder<R>.() -> Unit) {
            mode.registerButtonCombo(buttons, block)
        }
    }
}
