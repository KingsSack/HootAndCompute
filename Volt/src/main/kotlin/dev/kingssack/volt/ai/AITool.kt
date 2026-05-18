package dev.kingssack.volt.ai

import com.acmerobotics.roadrunner.Action
import kotlinx.serialization.json.JsonObject

data class AITool(
    val name: String,
    val description: String,
    val parameters: JsonObject,
    val invoke: (Map<String, Any?>) -> Action,
)
