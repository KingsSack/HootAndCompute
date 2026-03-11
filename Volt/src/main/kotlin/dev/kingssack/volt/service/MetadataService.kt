package dev.kingssack.volt.service

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.Scanner.Companion.INDEPENDENT
import dev.frozenmilk.sinister.targeting.NarrowSearch
import dev.frozenmilk.sinister.targeting.SearchTarget
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.Attachment
import dev.kingssack.volt.model.ActionMetadata
import dev.kingssack.volt.model.EventMetadata
import dev.kingssack.volt.model.ParameterMetadata
import dev.kingssack.volt.model.RobotMetadata
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event
import dev.kingssack.volt.util.telemetry.ActionTracer
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

object MetadataService {
    private val robots = mutableListOf<RobotMetadata>()
    private val actions = mutableListOf<ActionMetadata>()
    private val events = mutableListOf<EventMetadata>()

    fun getRobots(): List<RobotMetadata> = robots.toList()

    fun getActions(): List<ActionMetadata> = actions.toList()

    fun getEvents(): List<EventMetadata> = events.toList()

    fun getRobotById(id: String): RobotMetadata? = robots.find { it.id == id }

    @Suppress("unused")
    private object Scan : Scanner {
        override val loadAdjacencyRule = INDEPENDENT
        override val unloadAdjacencyRule = INDEPENDENT
        override val targets: SearchTarget = NarrowSearch()

        override fun scan(loader: ClassLoader, cls: Class<*>) {
            if (
                Robot::class.java.isAssignableFrom(cls) &&
                    !Modifier.isAbstract(cls.modifiers) &&
                    !cls.isInterface
            ) {
                if (robots.none { it.id == cls.name }) {
                    processRobot(cls)
                }
            }

            if (
                Event::class.java.isAssignableFrom(cls) &&
                    !Modifier.isAbstract(cls.modifiers) &&
                    !cls.isInterface
            ) {
                if (events.none { it.id == cls.name }) {
                    processEvent(cls)
                }
            }
        }

        override fun unload(loader: ClassLoader, cls: Class<*>) {
            robots.clear()
            actions.clear()
            events.clear()
        }
    }

    private fun processRobot(cls: Class<*>) {
        val robotActions = mutableListOf<ActionMetadata>()

        cls.declaredMethods.forEach { method ->
            if (method.isAnnotationPresent(VoltAction::class.java)) {
                createActionMetadata(method, cls, method.name)?.let {
                    robotActions.add(it)
                    if (actions.none { a -> a.id == it.id }) actions.add(it)
                }
            }
        }

        var currentCls: Class<*>? = cls
        while (currentCls != null && currentCls != Any::class.java) {
            currentCls.declaredFields.forEach { field ->
                if (Attachment::class.java.isAssignableFrom(field.type)) {
                    val attachmentCls = field.type
                    attachmentCls.declaredMethods.forEach { method ->
                        if (method.isAnnotationPresent(VoltAction::class.java)) {
                            createActionMetadata(
                                    method,
                                    attachmentCls,
                                    "${field.name}.${method.name}",
                                )
                                ?.let {
                                    robotActions.add(it)
                                    if (actions.none { a -> a.id == it.id }) actions.add(it)
                                }
                        }
                    }
                }
            }
            currentCls = currentCls.superclass
        }

        val constructorParams = extractConstructorParams(cls)

        val typeSignature = renderTypeSignature(cls)

        val factoryArgs = constructorParams.joinToString(", ") { $$"${$${it.name}}" }
        val factoryExpression =
            if (factoryArgs.isNotEmpty()) "${cls.simpleName}(it, $factoryArgs)"
            else "${cls.simpleName}(it)"

        robots.add(
            RobotMetadata(
                id = cls.name,
                name = cls.simpleName,
                actions = robotActions,
                constructorParams = constructorParams,
                typeSignature = typeSignature,
                factoryExpression = factoryExpression,
            )
        )
    }

    private fun extractConstructorParams(cls: Class<*>): List<ParameterMetadata> {
        val primaryConstructor = cls.kotlin.primaryConstructor ?: return emptyList()
        return primaryConstructor.valueParameters
            .filter { it.type.classifier != HardwareMap::class && it.type.classifier != ActionTracer::class }
            .map { param ->
                ParameterMetadata(
                    name = param.name ?: "arg",
                    type = param.type.toString().substringAfterLast(".").replace("?", ""),
                    defaultValue = if (param.isOptional) "default" else null,
                )
            }
    }

    private fun createActionMetadata(
        method: Method,
        declaringClass: Class<*>,
        accessPath: String,
    ): ActionMetadata? {
        val annotation = method.getAnnotation(VoltAction::class.java) ?: return null
        val actionId = "${declaringClass.simpleName}.${method.name}"

        val kFunction = method.kotlinFunction

        val parameters =
            kFunction?.valueParameters?.map { param ->
                ParameterMetadata(
                    name = param.name ?: "arg",
                    type = param.type.classifier.toString().substringAfterLast("."),
                    defaultValue = null,
                )
            }
                ?: method.parameters.map { param ->
                    ParameterMetadata(name = param.name, type = param.type.simpleName)
                }

        return ActionMetadata(
            id = actionId,
            name = annotation.name.ifEmpty { method.name },
            description = annotation.description,
            enableAITool = annotation.enableAITool,
            parameters = parameters,
            declaringClass = declaringClass.simpleName,
            accessPath = accessPath,
        )
    }

    private fun processEvent(cls: Class<*>) {
        val opModeType =
            if (Event.ManualEvent::class.java.isAssignableFrom(cls)) "ManualMode"
            else "AutonomousMode"

        val constructorParams = extractConstructorParams(cls)

        events.add(
            EventMetadata(
                id = cls.name,
                name = cls.simpleName,
                opModeType = opModeType,
                parameters = constructorParams,
            )
        )
    }

    private fun renderTypeSignature(cls: Class<*>): String {
        val superType = cls.genericSuperclass
        return if (superType is ParameterizedType) {
            renderJavaType(superType)
        } else {
            cls.simpleName
        }
    }

    private fun renderJavaType(type: Type): String {
        return when (type) {
            is Class<*> -> type.simpleName
            is ParameterizedType -> {
                val rawType =
                    (type.rawType as? Class<*>)?.simpleName
                        ?: type.rawType.toString().substringAfterLast(".")

                val args = type.actualTypeArguments.joinToString(", ") { renderJavaType(it) }
                "$rawType<$args>"
            }
            is TypeVariable<*> -> type.name
            is WildcardType -> {
                val lowerBounds = type.lowerBounds
                val upperBounds = type.upperBounds

                when {
                    lowerBounds.isNotEmpty() -> "in ${renderJavaType(lowerBounds.first())}"
                    upperBounds.isNotEmpty() && upperBounds.first() != Any::class.java ->
                        "out ${renderJavaType(upperBounds.first())}"
                    else -> "*"
                }
            }
            is GenericArrayType -> "${renderJavaType(type.genericComponentType)}Array"
            else -> type.toString().substringAfterLast(".")
        }
    }
}
