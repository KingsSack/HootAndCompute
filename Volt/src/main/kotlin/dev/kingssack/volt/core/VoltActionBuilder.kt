package dev.kingssack.volt.core

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.InstantAction
import com.acmerobotics.roadrunner.ParallelAction
import com.acmerobotics.roadrunner.SequentialAction
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.telemetry.TracedAction
import java.lang.System.nanoTime

@DslMarker annotation class VoltBuilderDsl

/**
 * Builder for creating complex action sequences for a [Robot]
 *
 * @param R The type of Robot the actions will operate on
 * @param robot The robot instance the actions will control
 */
@VoltBuilderDsl
class VoltActionBuilder<R : Robot>(private val robot: R) {
    private val _actions = mutableListOf<Action>()

    private fun addAction(action: Action) {
        _actions.add(TracedAction(inferName(action), action))
    }

    private fun inferName(action: Action): String {
        return when (action) {
            is SequentialAction -> "Sequence"
            is ParallelAction -> "Parallel"
            is InstantAction -> "Instant"
            else -> action.javaClass.simpleName.ifBlank { "Action" }
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
    fun parallel(block: VoltActionBuilder<R>.() -> Unit) {
        addAction(ParallelAction(extractActions(block)))
    }

    /** Adds a sequential block of actions that will run one after another. */
    fun sequence(block: VoltActionBuilder<R>.() -> Unit) {
        addAction(SequentialAction(extractActions(block)))
    }

    /** Adds an instant action that executes a block of code immediately. */
    fun instant(block: () -> Unit) {
        addAction(InstantAction(block))
    }

    private fun extractActions(block: VoltActionBuilder<R>.() -> Unit): List<Action> {
        return VoltActionBuilder(robot).apply(block)._actions
    }

    internal fun build(): SequentialAction {
        return SequentialAction(_actions)
    }
}

context(robot: R)
fun <R : Robot> voltAction(block: VoltActionBuilder<R>.() -> Unit): Action {
    return VoltActionBuilder(robot).apply(block).build()
}
