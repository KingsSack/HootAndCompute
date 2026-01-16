package dev.kingssack.volt.core

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.ParallelAction
import com.acmerobotics.roadrunner.SequentialAction
import dev.kingssack.volt.robot.Robot
import java.lang.System.nanoTime

@DslMarker annotation class VoltBuilderDsl

/**
 * Builder for creating complex action sequences for a [Robot].
 *
 * @param R The type of Robot the actions will operate on.
 * @property robot The robot instance the actions will control.
 */
@VoltBuilderDsl
class VoltActionBuilder<R : Robot>(val robot: R) {
    private val actions = mutableListOf<Action>()

    operator fun Action.unaryPlus() {
        actions.add(this)
    }

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

    fun parallel(block: VoltActionBuilder<R>.() -> Unit) {
        actions.add(ParallelAction(extractActions(block)))
    }

    fun sequence(block: VoltActionBuilder<R>.() -> Unit) {
        actions.add(SequentialAction(extractActions(block)))
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
