package dev.kingssack.volt.util.buttons

sealed interface AnalogEvent {
    data object Change : AnalogEvent
    data class Threshold(val min: Float = 0.3f) : AnalogEvent
}
