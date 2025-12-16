package dev.kingssack.volt.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class VoltAction(
    val name: String = "",
    val description: String = ""
)
