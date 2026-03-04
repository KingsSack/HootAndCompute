package dev.kingssack.volt.util

import dev.kingssack.volt.util.buttons.GamepadButton

internal sealed interface Event {
    sealed interface AutonomousEvent : Event {
        data object Start : AutonomousEvent
    }

    sealed interface ManualEvent : Event {
        sealed interface ButtonEvent {
            data object Tap : ButtonEvent

            data class Hold(val durationMs: Double = 200.0) : ButtonEvent

            data object Release : ButtonEvent

            data object DoubleTap : ButtonEvent

            data class Combo(val buttons: Set<GamepadButton>) : ButtonEvent
        }

        sealed interface AnalogEvent {
            data object Change : AnalogEvent

            data class Threshold(val min: Float = 0.3f) : AnalogEvent
        }
    }
}
