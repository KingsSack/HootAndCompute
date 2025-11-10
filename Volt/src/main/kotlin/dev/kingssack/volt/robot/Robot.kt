package dev.kingssack.volt.robot

import dev.kingssack.volt.attachment.Attachment
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf
import org.firstinspires.ftc.robotcore.external.Telemetry
import kotlin.reflect.full.withNullability

/** Represents a robot with attachments. */
abstract class Robot {
    // Attachments
    private var attachments = mutableListOf<Attachment>()

    /** Registers [attachment] to the robot. */
    protected fun registerAttachment(attachment: Attachment) {
        this.attachments.add(attachment)
        attachment.onRegister(this)
    }

    init {
        // Auto-register all Attachment fields
//        @Suppress("UNCHECKED_CAST")
//        this::class
//            .memberProperties
//            .filter { prop -> prop.returnType.isSubtypeOf(typeOf<Attachment>()) }
//            .forEach { prop ->
//                val property = prop as KProperty1<Robot, Attachment>
//                val value = property.get(this)
//                registerAttachment(value)
//            }

        // Helper to check Attachment and Attachment? as desired
        fun KType.isAttachmentOrNullableAttachment(): Boolean {
            val attachment = typeOf<Attachment>()
            val nullableAttachment = attachment.withNullability(true)
            return this.isSubtypeOf(attachment) || this.isSubtypeOf(nullableAttachment)
        }

        this::class
            .memberProperties
            .filterIsInstance<KProperty1<Robot, *>>()
            .filter { prop -> prop.returnType.isAttachmentOrNullableAttachment() }
            .forEach { prop ->
                val value = runCatching { prop.get(this) }.getOrNull()
                if (value is Attachment) {
                    registerAttachment(value)
                }
            }
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
