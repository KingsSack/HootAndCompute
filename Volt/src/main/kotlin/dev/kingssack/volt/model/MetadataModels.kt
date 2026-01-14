package dev.kingssack.volt.model

data class ActionMetadata(
    val id: String,
    val name: String,
    val description: String,
    val parameters: List<ParameterMetadata>,
    val robotType: String
)

data class ParameterMetadata(
    val name: String,
    val type: String,
    val defaultValue: String? = null
)

data class RobotMetadata(
    val simpleName: String,
    val qualifiedName: String
)
