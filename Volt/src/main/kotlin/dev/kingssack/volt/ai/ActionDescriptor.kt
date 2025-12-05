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

data class ActionDescriptor(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val parameters: List<ActionParameter>,
    val returns: String = "Action",
)

data class AITool(val name: String, val description: String, val inputSchema: Map<String, Any>)
