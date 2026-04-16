package dev.kingssack.volt.util

import dev.kingssack.volt.util.buttons.AnalogInput
import dev.kingssack.volt.util.buttons.Button

sealed interface Event {
    sealed interface AutonomousEvent : Event {
        data object Start : AutonomousEvent

        /** Triggers when [trigger] becomes true. Only runs once. */
        data class First(val trigger: () -> Boolean) : AutonomousEvent

        /** Triggers each tick that [trigger] is true. */
        class When(val trigger: () -> Boolean) : AutonomousEvent
    }

    sealed interface ManualEvent : Event {
        sealed interface ButtonEvent : ManualEvent {
            val button: Button
        }

        sealed interface AnalogEvent : ManualEvent {
            val analogInput: AnalogInput
        }

        /** Triggers when [button] is pressed. */
        data class Tap(override val button: Button) : ButtonEvent

        /** Triggers when [button] is released. */
        data class Release(override val button: Button) : ButtonEvent

        /** Triggers when [button] is held for at least [durationMs] milliseconds. */
        data class Hold(override val button: Button, val durationMs: Double = 200.0) : ButtonEvent

        /** Triggers when [button] is tapped twice in quick succession. */
        data class DoubleTap(override val button: Button) : ButtonEvent

        /** Triggers when [analogInput] changes. */
        data class Change(override val analogInput: AnalogInput) : AnalogEvent

        /** Triggers when [analogInput] crosses the [min] threshold. */
        data class Threshold(override val analogInput: AnalogInput, val min: Float = 0.3f) :
            AnalogEvent

        /** Triggers when all [buttons] are pressed simultaneously. */
        data class Combo(val buttons: Set<Button>) : ManualEvent
    }
}
