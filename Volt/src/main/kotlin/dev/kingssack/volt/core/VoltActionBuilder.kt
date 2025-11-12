package dev.kingssack.volt.core

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.ParallelAction
import com.acmerobotics.roadrunner.SequentialAction
import dev.kingssack.volt.robot.Robot
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@DslMarker annotation class VoltBuilderDsl

/**
 * Builder for creating complex action sequences for a [Robot].
 *
 * @param R The type of Robot the actions will operate on.
 * @property robot The robot instance the actions will control.
 */
@OptIn(ExperimentalAtomicApi::class)
@VoltBuilderDsl
class VoltActionBuilder<R : Robot>(val robot: R) {
    private val actions = mutableListOf<Action>()

    operator fun Action.unaryPlus() {
        actions.add(this)
    }

    /**
     * Defines a block of actions to be run in parallel. The block receives this builder as its
     * receiver, so all verbs are available.
     */
    fun parallel(block: VoltActionBuilder<R>.() -> Unit) {
        val parallelActions = mutableListOf<Action>()
        val tempBuilder =
            VoltActionBuilder(robot).apply {
                fun Action.unaryPlus() {
                    parallelActions.add(this)
                }
            }
        tempBuilder.block()
        actions.add(ParallelAction(parallelActions))
    }

    /** Defines a sub-sequence of actions. This is useful for organization. */
    fun sequence(block: VoltActionBuilder<R>.() -> Unit) {
        val sequenceActions = mutableListOf<Action>()
        val tempBuilder =
            VoltActionBuilder(robot).apply {
                fun Action.unaryPlus() {
                    sequenceActions.add(this)
                }
            }
        tempBuilder.block()
        actions.add(SequentialAction(sequenceActions))
    }

    internal fun build(): SequentialAction {
        return SequentialAction(actions)
    }
}
