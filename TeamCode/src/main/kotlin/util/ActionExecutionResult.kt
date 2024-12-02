package org.firstinspires.ftc.teamcode.util

// Helper classes for result and execution
data class ActionExecutionResult(
    val success: Boolean,
    val needRecovery: Boolean = false
)