package dev.kingssack.volt.util

object VoltLogs {
    fun log(message: String) {
        logs.add(message)
    }
    val logs = mutableListOf<String>()
}