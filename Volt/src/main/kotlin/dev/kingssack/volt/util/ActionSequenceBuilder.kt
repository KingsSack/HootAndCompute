package dev.kingssack.volt.util

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.ParallelAction
import com.acmerobotics.roadrunner.SequentialAction

class ActionSequenceBuilder {
    private val actions = mutableListOf<() -> Action>()

    operator fun (() -> Action).unaryPlus() {
        actions.add(this)
    }

    /** Run actions in parallel. */
    fun parallel(block: ParallelBuilder.() -> Unit) {
        val builder = ParallelBuilder().apply(block)
        actions.add { builder.build() }
    }

    /** Run a sequence of actions. */
    fun sequence(block: ActionSequenceBuilder.() -> Unit) {
        val builder = ActionSequenceBuilder().apply(block)
        actions.add { builder.build() }
    }

    fun build(): SequentialAction = SequentialAction(actions.map { it() })

    class ParallelBuilder {
        private val actions = mutableListOf<() -> Action>()

        operator fun (() -> Action).unaryPlus() {
            actions.add(this)
        }

        fun action(block: () -> Action) {
            actions.add(block)
        }

        fun build(): ParallelAction = ParallelAction(actions.map { it() })
    }
}