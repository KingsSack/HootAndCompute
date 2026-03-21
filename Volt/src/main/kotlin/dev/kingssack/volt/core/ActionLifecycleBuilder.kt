package dev.kingssack.volt.core

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action

@DslMarker @Target(AnnotationTarget.CLASS) annotation class ActionLifecycleDsl

@ActionLifecycleDsl
class ActionLifecycleBuilder {
    private var init: (() -> Unit)? = null
    private var loop: (TelemetryPacket.() -> Boolean)? = null
    private var cleanup: (() -> Unit)? = null

    private var onStart: (() -> Unit)? = null
    private var onComplete: (() -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    internal fun onStart(block: () -> Unit) {
        onStart = block
    }

    internal fun onComplete(block: () -> Unit) {
        onComplete = block
    }

    internal fun onError(block: (Throwable) -> Unit) {
        onError = block
    }

    /** Sets the initialization block for the action. */
    fun init(block: () -> Unit) {
        init = block
    }

    /** Sets the looping block for the action. */
    fun loop(block: TelemetryPacket.() -> Boolean) {
        loop = block
    }

    /** Sets the cleanup block for the action. */
    fun cleanup(block: () -> Unit) {
        cleanup = block
    }

    fun build() =
        object : Action {
            private var initialized = false

            override fun run(p: TelemetryPacket): Boolean {
                return try {
                    if (!initialized) {
                        init?.invoke()
                        onStart?.invoke()
                        initialized = true
                    }
                    val done = loop?.invoke(p) ?: true
                    if (done) {
                        cleanup?.invoke()
                        onComplete?.invoke()
                    }
                    !done
                } catch (e: Throwable) {
                    onError?.invoke(e)
                    try {
                        cleanup?.invoke()
                    } catch (ce: Throwable) {
                        onError?.invoke(ce)
                        throw ce
                    }
                    throw e
                }
            }
        }
}
