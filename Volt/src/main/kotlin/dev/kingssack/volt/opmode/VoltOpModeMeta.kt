package dev.kingssack.volt.opmode

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

/**
 * Metadata for [VoltOpMode] instances
 *
 * @param name the name of the opmode
 * @param group the group of the opmode
 * @param autoTransition the pre selected teleop
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class VoltOpModeMeta(
    val name: String,
    val group: String = OpModeMeta.DefaultGroup,
    val autoTransition: String = "",
)
