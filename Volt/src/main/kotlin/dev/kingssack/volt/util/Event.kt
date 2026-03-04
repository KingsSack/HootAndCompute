package dev.kingssack.volt.util

import dev.kingssack.volt.util.buttons.ButtonHandler
import dev.kingssack.volt.util.buttons.GamepadButton

internal sealed interface Event {
    sealed interface AutonomousEvent : Event {
        data object Start : AutonomousEvent
    }

    sealed interface ManualEvent : Event {
        sealed interface ButtonEvent {
            fun triggered(handler: ButtonHandler): Boolean

            data object Tap : ButtonEvent {
                override fun triggered(handler: ButtonHandler) = handler.justPressed()
            }

            data class Hold(val durationMs: Double = 200.0) : ButtonEvent {
                override fun triggered(handler: ButtonHandler) = handler.held(durationMs)
            }

            data object Release : ButtonEvent {
                override fun triggered(handler: ButtonHandler) = handler.justReleased()
            }

            data object DoubleTap : ButtonEvent {
                override fun triggered(handler: ButtonHandler) = handler.doubleTapped()
            }

            data class Combo(val buttons: Set<GamepadButton>) : ButtonEvent {
                override fun triggered(handler: ButtonHandler) = false
            }
        }

        sealed interface AnalogEvent {
            fun triggered(value: Float): Boolean

            data object Change : AnalogEvent {
                override fun triggered(value: Float) = true
            }

            data class Threshold(val min: Float = 0.3f) : AnalogEvent {
                override fun triggered(value: Float): Boolean = value >= min
            }
        }
    }
}
