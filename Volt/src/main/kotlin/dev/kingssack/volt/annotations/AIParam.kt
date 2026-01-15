package dev.kingssack.volt.annotations

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AIParam(
    val description: String,
    val min: Double = Double.MIN_VALUE,
    val max: Double = Double.MAX_VALUE,
)
