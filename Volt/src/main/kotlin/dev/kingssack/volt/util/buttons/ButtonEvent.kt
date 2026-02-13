package dev.kingssack.volt.util.buttons

sealed interface ButtonEvent {
    data object Tap : ButtonEvent
    data class Hold(val durationMs: Double = 200.0) : ButtonEvent
    data object Release : ButtonEvent
    data object DoubleTap : ButtonEvent
    data class Combo(val buttons: Set<GamepadButton>) : ButtonEvent
}
