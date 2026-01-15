package dev.kingssack.volt.ai

import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.annotations.AIAction
import dev.kingssack.volt.annotations.AIParam
import dev.kingssack.volt.attachment.Attachment
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters

abstract class AIAttachment(name: String) : Attachment(name) {
    init {
        registerAIActions()
    }

    private fun registerAIActions() {
        this::class
            .memberFunctions
            .filter { it.findAnnotation<AIAction>() != null }
            .forEach { function ->
                val annotation = function.findAnnotation<AIAction>()!!

                val parameters =
                    function.valueParameters.mapNotNull { parameter ->
                        val paramAnnotation = parameter.findAnnotation<AIParam>()
                        ActionParameter(
                            name = parameter.name ?: return@mapNotNull null,
                            type = parameter.type.toString(),
                            description = paramAnnotation?.description ?: "",
                            min = paramAnnotation?.min,
                            max = paramAnnotation?.max,
                        )
                    }

                val descriptor =
                    ActionDescriptor(
                        id = annotation.id,
                        name = annotation.name,
                        description = annotation.description,
                        category = annotation.category,
                        parameters = parameters,
                    )

                ActionRegistry.register(descriptor) { parameters ->
                    val args =
                        function.valueParameters
                            .map { parameter -> parameters[parameter.name] }
                            .toTypedArray()
                    function.call(this, *args) as Action
                }
            }
    }
}
