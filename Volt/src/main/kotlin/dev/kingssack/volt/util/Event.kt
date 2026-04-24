package dev.kingssack.volt.util

import dev.kingssack.volt.util.buttons.AnalogInput
import dev.kingssack.volt.util.buttons.Button

/**
 * Represents an event that can trigger an action. Events can be either autonomous (triggered by
 * time or conditions) or manual (triggered by user input).
 *
 * @param P the type of parameter to supply when the event triggers
 * @property parameter the parameter associated with the event
 */
sealed interface Event<P> {
    val parameter: P

    fun shouldTrigger(): Boolean

    sealed interface AutonomousEvent<P> : Event<P> {
        /** Triggers once when the event is first checked. */
        data object Start : AutonomousEvent<Any> {
            override val parameter: Any = Unit

            override fun shouldTrigger() = false
        }

        /** Triggers when [condition] becomes true. Only runs once. */
        data class First(val condition: () -> Boolean) : AutonomousEvent<Any> {
            override val parameter: Any = Unit

            private var triggered = false

            override fun shouldTrigger(): Boolean {
                if (!triggered && condition()) {
                    triggered = true
                    return true
                }
                return false
            }
        }

        /** Triggers when [condition] becomes true. */
        data class When(val condition: () -> Boolean) : AutonomousEvent<Any> {
            override val parameter: Any = Unit

            private var state = false

            override fun shouldTrigger(): Boolean {
                if (state xor condition()) {
                    state = !state
                    return state
                }
                return false
            }
        }
    }

    sealed interface ManualEvent<P> : Event<P> {
        sealed interface ButtonEvent : ManualEvent<Any> {
            override val parameter: Any
                get() = Unit

            val button: Button
        }

        sealed interface AnalogEvent : ManualEvent<Float> {
            val analogInput: AnalogInput
        }

        /** Triggers when [button] is pressed. */
        data class Tap(override val button: Button) : ButtonEvent {
            override fun shouldTrigger() = button.handler.tappedThisTick
        }

        /** Triggers when [button] is released. */
        data class Release(override val button: Button) : ButtonEvent {
            override fun shouldTrigger() = button.handler.releasedThisTick
        }

        /** Triggers when [button] is held for at least [durationMs] milliseconds. */
        data class Hold(override val button: Button, private val durationMs: Double = 200.0) :
            ButtonEvent {
            override fun shouldTrigger() = button.handler.held(durationMs)
        }

        /** Triggers when [button] is tapped twice in quick succession. */
        data class DoubleTap(override val button: Button) : ButtonEvent {
            override fun shouldTrigger() = button.handler.doubleTappedThisTick
        }

        /** Triggers when [analogInput] changes. */
        data class Change(override val analogInput: AnalogInput) : AnalogEvent {
            override val parameter: Float
                get() = analogInput.handler.value

            override fun shouldTrigger() = analogInput.handler.changed
        }

        /** Triggers when [analogInput] crosses the [min] threshold. */
        data class Threshold(override val analogInput: AnalogInput, val min: Float = 0.3f) :
            AnalogEvent {
            override val parameter: Float
                get() = analogInput.handler.value

            override fun shouldTrigger(): Boolean {
                val handler = analogInput.handler
                return handler.changed && handler.value > min
            }
        }

        /** Triggers when all [buttons] are pressed simultaneously. */
        data class Combo(val buttons: Set<Button>) : ManualEvent<Any> {
            override val parameter: Any
                get() = Unit

            override fun shouldTrigger() =
                buttons.all { it.handler.pressed } && buttons.any { it.handler.tappedThisTick }
        }
    }
}
