package dev.kingssack.volt.core

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.ParallelAction
import com.acmerobotics.roadrunner.SequentialAction
import dev.kingssack.volt.robot.Robot
import kotlin.concurrent.atomics.AtomicBoolean
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
    private val isBuilding = AtomicBoolean(false)

    operator fun Action.unaryPlus() {
        check(isBuilding.load()) { "Actions can only be added during the build process." }
        actions.add(this)
    }

    /**
     * Defines a block of actions to be run in parallel. The block receives this builder as its
     * receiver, so all verbs are available.
     */
    fun parallel(block: VoltActionBuilder<R>.() -> Unit) {
        check(isBuilding.load()) {
            "Cannot start a parallel block after the builder has been finalized."
        }
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
        check(isBuilding.load()) {
            "Cannot start a sequence block after the builder has been finalized."
        }
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
        isBuilding.store(true)
        try {
            return SequentialAction(actions)
        } finally {
            isBuilding.store(false)
        }
    }
}
