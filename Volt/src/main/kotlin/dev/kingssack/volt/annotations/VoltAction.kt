package dev.kingssack.volt.annotations

/**
 * Annotation to mark a method as a Volt Action.
 *
 * @property name The name of the action. Defaults to an empty string.
 * @property description A brief description of the action. Defaults to an empty string.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class VoltAction(
    val name: String = "",
    val description: String = ""
)
