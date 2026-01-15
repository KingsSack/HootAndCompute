package dev.kingssack.volt.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AIAction(
    val id: String,
    val name: String,
    val description: String,
    val category: String = "general",
)
