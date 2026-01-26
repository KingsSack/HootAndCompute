package dev.kingssack.volt.ai

import dev.kingssack.volt.attachment.Attachment

abstract class AIAttachment(name: String) : Attachment(name) {
    init {
        ActionRegistry.registerInstance(this)
    }
}
