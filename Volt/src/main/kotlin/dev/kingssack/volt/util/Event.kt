package dev.kingssack.volt.util

import dev.kingssack.volt.util.buttons.AnalogInput
import dev.kingssack.volt.util.buttons.Button

sealed interface Event {
    sealed interface AutonomousEvent : Event {
        data object Start : AutonomousEvent
    }

    sealed interface ManualEvent : Event {
        sealed interface ButtonEvent : ManualEvent {
            val button: Button
        }

        sealed interface AnalogEvent : ManualEvent {
            val analogInput: AnalogInput
        }

        data class Tap(override val button: Button) : ButtonEvent
        data class Release(override val button: Button) : ButtonEvent
        data class Hold(override val button: Button, val durationMs: Double = 200.0) : ButtonEvent
        data class DoubleTap(override val button: Button) : ButtonEvent
        data class Change(override val analogInput: AnalogInput) : AnalogEvent
        data class Threshold(override val analogInput: AnalogInput, val min: Float = 0.3f) : AnalogEvent

        data class Combo(val buttons: Set<Button>) : ManualEvent
    }
}
