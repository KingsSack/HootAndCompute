package dev.kingssack.volt.ai

data class ActionDescriptor(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val parameters: List<ActionParameter>,
    val returns: String = "Action",
)
