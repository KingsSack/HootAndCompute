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
 * @property robot The robot instance the actions will control
 */
@VoltBuilderDsl
class VoltActionBuilder<R : Robot>(val robot: R) {
    private val actions = mutableListOf<Action>()

    operator fun Action.unaryPlus() {
        actions.add(TracedAction(inferName(this), this))
    }

    private fun inferName(action: Action): String {
        return when (action) {
            is SequentialAction -> "Sequence"
            is ParallelAction -> "Parallel"
            is InstantAction -> "Instant"
            else -> action.javaClass.simpleName ?: "Action"
        }
    }

    /** Adds an [Action] that will run until the specified time duration [dt] has elapsed. */
    fun wait(dt: Double) {
        var beginNs: Long = -1
        actions.add(
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
        actions.add(ParallelAction(extractActions(block)))
    }

    /** Adds a sequential block of actions that will run one after another. */
    fun sequence(block: VoltActionBuilder<R>.() -> Unit) {
        actions.add(SequentialAction(extractActions(block)))
    }

    /** Adds an instant action that executes a block of code immediately. */
    fun instant(block: () -> Unit) {
        actions.add(InstantAction(block))
    }

    private fun extractActions(block: VoltActionBuilder<R>.() -> Unit): List<Action> {
        return VoltActionBuilder(robot).apply(block).actions
    }

    internal fun build(): SequentialAction {
        return SequentialAction(actions)
    }
}

context(robot: R)
fun <R : Robot> voltAction(block: VoltActionBuilder<R>.() -> Unit): Action {
    return VoltActionBuilder(robot).apply(block).build()
}
