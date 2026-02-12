package dev.kingssack.volt.annotations

/**
 * Annotation to mark a method as a Volt Action.
 *
 * @property name of the action
 * @property description of the action
 * @property enableAITool whether this action should be exposed to AI tools
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class VoltAction(
    val name: String = "",
    val description: String = "",
    val enableAITool: Boolean = false,
)
