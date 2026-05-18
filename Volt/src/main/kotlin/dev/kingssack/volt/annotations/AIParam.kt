package dev.kingssack.volt.annotations

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AIParam(
    val description: String,
    val required: Boolean = true,
    val enum: Array<String> = [],
)
