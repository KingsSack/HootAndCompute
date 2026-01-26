package dev.kingssack.volt.ai

data class ActionParameter(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = true,
    val defaultValue: Any? = null,
    val min: Double? = null,
    val max: Double? = null,
)
