package dev.kingssack.volt.robot

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.SequentialAction
import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry

/** Represents a robot with attachments. */
abstract class Robot {
    // Attachments
    protected var attachments = mutableListOf<Attachment>()

    /** Registers [attachments] to the robot. */
    fun registerAttachments(vararg attachments: Attachment) {
        attachments.forEach {
            this.attachments.add(it)
            it.onRegister(this)
        }
    }

    /** Builds a sequence of actions to be run sequentially. */
    fun sequence(block: SequenceBuilder.() -> Unit): SequentialAction =
        SequenceBuilder().apply(block).build()

    class SequenceBuilder {
        private val actions = mutableListOf<Action>()

        fun then(action: Action) {
            actions.add(action)
        }

        fun build(): SequentialAction = SequentialAction(actions)
    }

    /**
     * Updates the robot.
     *
     * @param telemetry for updating telemetry
     */
    open fun update(telemetry: Telemetry) {
        attachments.forEach { it.update(telemetry) }

        // Update telemetry
        telemetry.update()
    }
}
