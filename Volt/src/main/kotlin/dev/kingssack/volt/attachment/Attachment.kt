package dev.kingssack.volt.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.firstinspires.ftc.robotcore.external.Telemetry

/** Represents the state of an attachment. */
sealed interface AttachmentState {
    data object Idle : AttachmentState

    data object Running : AttachmentState

    data class Fault(val error: Throwable) : AttachmentState
}

/** Base class for all attachments in the robot system. */
abstract class Attachment(val name: String) {
    private val _state = MutableStateFlow<AttachmentState>(AttachmentState.Idle)
    val state: StateFlow<AttachmentState> = _state.asStateFlow()

    private var lastFaultHash: Int? = null

    /** Sets the state of the attachment to [newState]. */
    protected fun setState(newState: AttachmentState) {
        _state.value = newState
    }

    /** Checks if the attachment is currently busy (Running). */
    fun isBusy() = state.value is AttachmentState.Running

    /** Checks if the attachment is currently in a faulted state. */
    fun isFaulted() = state.value is AttachmentState.Fault

    /** Ensures the attachment is in the Idle state, throwing an exception if not. */
    fun requireReady() =
        check(state.value is AttachmentState.Idle) {
            "Attachment $name is not ready. Current state: ${state.value}"
        }

    @DslMarker @Target(AnnotationTarget.CLASS) annotation class AttachmentActionDsl

    @AttachmentActionDsl
    inner class AttachmentActionBuilder {
        private var init: (() -> Unit)? = null
        private var loop: (TelemetryPacket.() -> Boolean)? = null
        private var cleanup: (() -> Unit)? = null

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

        fun build(): Action =
            object : Action {
                private var initialized = false

                override fun run(p: TelemetryPacket): Boolean {
                    return try {
                        if (!initialized) {
                            init?.invoke()
                            setState(AttachmentState.Running)
                            initialized = true
                        }
                        val done = loop?.invoke(p) ?: true
                        if (done) {
                            cleanup?.invoke()
                            setState(AttachmentState.Idle)
                        }
                        !done
                    } catch (e: Throwable) {
                        setState(AttachmentState.Fault(e))
                        try {
                            cleanup?.invoke()
                        } catch (ce: Throwable) {
                            setState(AttachmentState.Fault(ce))
                            throw ce
                        }
                        throw e
                    }
                }
            }
    }

    /**
     * Creates an action using the [AttachmentActionBuilder].
     *
     * @return an [Action] that can be executed by an OpMode
     */
    fun action(block: AttachmentActionBuilder.() -> Unit): Action =
        AttachmentActionBuilder().apply(block).build()

    /** Updates the telemetry with the current state of the attachment. */
    context(telemetry: Telemetry)
    open fun update() {
        with(telemetry) {
            addLine()
            addLine("$name-->")
            addData("State", state.value)
            (state.value as? AttachmentState.Fault)?.let { fault ->
                val hash = fault.error.message?.hashCode() ?: 0
                if (lastFaultHash != hash) {
                    addData("Error", fault.error.message ?: "Unknown")
                    fault.error.stackTrace.take(2).forEachIndexed { i, line ->
                        addData("Stack_$i", line.toString())
                    }
                    lastFaultHash = hash
                }
            } ?: run { lastFaultHash = null }
        }
    }

    /** Stops the attachment and resets its state to Idle. */
    open fun stop() {
        _state.value = AttachmentState.Idle
    }
}
