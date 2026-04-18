package dev.kingssack.volt.core

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.InstantAction
import com.acmerobotics.roadrunner.ParallelAction
import com.acmerobotics.roadrunner.SequentialAction
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.telemetry.TracedAction
import java.lang.System.nanoTime

@DslMarker annotation class VoltBuilderDsl

/** Builder for creating complex action sequences for a [Robot] */
@VoltBuilderDsl
class VoltActionBuilder {
    private val _actions = mutableListOf<Action>()

    private fun addAction(action: Action) {
        _actions.add(TracedAction(inferName(action), action))
    }

    private fun inferName(action: Action): String {
        return when (action) {
            is SequentialAction -> "Sequence"
            is ParallelAction -> "Parallel"
            is InstantAction -> "Instant"
            else -> {
                val actionClass = action.javaClass

                val enclosingMethod = actionClass.enclosingMethod
                val annotatedName =
                    enclosingMethod
                        ?.annotations
                        ?.filterIsInstance<VoltAction>()
                        ?.firstOrNull()
                        ?.name

                actionClass.simpleName.ifBlank {
                    annotatedName?.takeIf { it.isNotBlank() }
                        ?: enclosingMethod?.name
                        ?: "Action of ${actionClass.declaringClass?.simpleName} extending ${actionClass.superclass?.simpleName}"
                }
            }
        }
    }

    /** Adds an [Action] to the current sequence. */
    operator fun Action.unaryPlus() {
        addAction(this)
    }

    /** Adds an [Action] that will run until the specified time duration [dt] has elapsed. */
    fun wait(dt: Double) {
        var beginNs: Long = -1
        addAction(
            Action {
                if (beginNs == -1L) {
                    beginNs = nanoTime()
                }
                val elapsedNs = nanoTime() - beginNs
                return@Action (elapsedNs / 1e9) < dt
            }
        )
    }

    /** Adds a parallel block of actions that will run simultaneously. */
    fun parallel(block: VoltActionBuilder.() -> Unit) {
        addAction(ParallelAction(extractActions(block)))
    }

    /** Adds a sequential block of actions that will run one after another. */
    fun sequence(block: VoltActionBuilder.() -> Unit) {
        addAction(SequentialAction(extractActions(block)))
    }

    /** Adds an instant action that executes a block of code immediately. */
    fun instant(block: () -> Unit) {
        addAction(InstantAction(block))
    }

    fun action(block: ActionLifecycleBuilder.() -> Unit) {
        addAction(ActionLifecycleBuilder().apply(block).build())
    }

    private fun extractActions(block: VoltActionBuilder.() -> Unit): List<Action> {
        return VoltActionBuilder().apply(block)._actions
    }

    internal fun build(): SequentialAction {
        return SequentialAction(_actions)
    }
}

fun voltAction(block: VoltActionBuilder.() -> Unit): Action {
    return VoltActionBuilder().apply(block).build()
}
