package dev.kingssack.volt.ai

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AIAction(
    val id: String,
    val name: String,
    val description: String,
    val category: String = "general",
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AIParam(
    val description: String,
    val min: Double = Double.MIN_VALUE,
    val max: Double = Double.MAX_VALUE,
)
