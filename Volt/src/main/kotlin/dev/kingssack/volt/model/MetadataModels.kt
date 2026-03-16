package dev.kingssack.volt.model

data class RobotMetadata(
    val id: String,
    val name: String,
    val actions: List<ActionMetadata>,
    val constructorParams: List<ParameterMetadata>,
    val typeSignature: String,
    val factoryExpression: String
)

data class ActionMetadata(
    val id: String,
    val name: String,
    val description: String,
    val enableAITool: Boolean,
    val parameters: List<ParameterMetadata>,
    val declaringClass: String,
    val accessPath: String
)

data class EventMetadata(
    val id: String,
    val name: String,
    val opModeType: String,
    val parameters: List<ParameterMetadata>
)

data class ParameterMetadata(
    val name: String,
    val type: String,
    val defaultValue: String? = null
)
