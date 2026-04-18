package dev.kingssack.volt.util

import dev.kingssack.volt.util.buttons.AnalogHandler
import dev.kingssack.volt.util.buttons.AnalogInput
import dev.kingssack.volt.util.buttons.Button
import dev.kingssack.volt.util.buttons.ButtonHandler
import java.util.*

sealed interface Event {
    sealed interface AutonomousEvent : Event {
        fun trigger(): Boolean

        /** Triggers once when the event is first checked. */
        data object Start : AutonomousEvent {
            override fun trigger() = true
        }

        /** Triggers when [condition] becomes true. Only runs once. */
        data class First(
            val condition: () -> Boolean,
        ) : AutonomousEvent {
            private var triggered = false

            override fun trigger(): Boolean {
                if (!triggered && condition()) {
                    triggered = true
                    return true
                }
                return false
            }
        }

        /** Triggers when [condition] becomes true. */
        data class When(
            val condition: () -> Boolean,
        ) : AutonomousEvent {
            private var state = false

            override fun trigger(): Boolean {
                if (state xor condition()) {
                    state = !state
                    return state
                }
                return false
            }
        }
    }

    sealed interface ManualEvent : Event {
        fun trigger(
            buttonHandlers: EnumMap<Button, ButtonHandler>,
            analogHandlers: EnumMap<AnalogInput, AnalogHandler>,
        ): Boolean

        sealed interface ButtonEvent : ManualEvent {
            val button: Button
        }

        sealed interface AnalogEvent : ManualEvent {
            val analogInput: AnalogInput
        }

        /** Triggers when [button] is pressed. */
        data class Tap(
            override val button: Button,
        ) : ButtonEvent {
            override fun trigger(
                buttonHandlers: EnumMap<Button, ButtonHandler>,
                analogHandlers: EnumMap<AnalogInput, AnalogHandler>,
            ) = buttonHandlers[button]?.tappedThisTick == true
        }

        /** Triggers when [button] is released. */
        data class Release(
            override val button: Button,
        ) : ButtonEvent {
            override fun trigger(
                buttonHandlers: EnumMap<Button, ButtonHandler>,
                analogHandlers: EnumMap<AnalogInput, AnalogHandler>,
            ) = buttonHandlers[button]?.releasedThisTick == true
        }

        /** Triggers when [button] is held for at least [durationMs] milliseconds. */
        data class Hold(
            override val button: Button,
            private val durationMs: Double = 200.0,
        ) : ButtonEvent {
            override fun trigger(
                buttonHandlers: EnumMap<Button, ButtonHandler>,
                analogHandlers: EnumMap<AnalogInput, AnalogHandler>,
            ) = buttonHandlers[button]?.held(durationMs) == true
        }

        /** Triggers when [button] is tapped twice in quick succession. */
        data class DoubleTap(
            override val button: Button,
        ) : ButtonEvent {
            override fun trigger(
                buttonHandlers: EnumMap<Button, ButtonHandler>,
                analogHandlers: EnumMap<AnalogInput, AnalogHandler>,
            ) = buttonHandlers[button]?.doubleTappedThisTick == true
        }

        /** Triggers when [analogInput] changes. */
        data class Change(
            override val analogInput: AnalogInput,
        ) : AnalogEvent {
            override fun trigger(
                buttonHandlers: EnumMap<Button, ButtonHandler>,
                analogHandlers: EnumMap<AnalogInput, AnalogHandler>,
            ) = analogHandlers[analogInput]?.changed == true
        }

        /** Triggers when [analogInput] crosses the [min] threshold. */
        data class Threshold(
            override val analogInput: AnalogInput,
            val min: Float = 0.3f,
        ) : AnalogEvent {
            override fun trigger(
                buttonHandlers: EnumMap<Button, ButtonHandler>,
                analogHandlers: EnumMap<AnalogInput, AnalogHandler>,
            ): Boolean {
                val handler = analogHandlers[analogInput] ?: return false
                return handler.changed && handler.value > min
            }
        }

        /** Triggers when all [buttons] are pressed simultaneously. */
        data class Combo(
            val buttons: Set<Button>,
        ) : ManualEvent {
            override fun trigger(
                buttonHandlers: EnumMap<Button, ButtonHandler>,
                analogHandlers: EnumMap<AnalogInput, AnalogHandler>,
            ) = buttons.all { buttonHandlers[it]?.pressed == true } &&
                buttons.any { buttonHandlers[it]?.tappedThisTick == true }
        }
    }
}
