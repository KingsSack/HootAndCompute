package dev.kingssack.volt.service

import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.Scanner.Companion.INDEPENDENT
import dev.frozenmilk.sinister.targeting.NarrowSearch
import dev.frozenmilk.sinister.targeting.SearchTarget
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.model.ActionMetadata
import dev.kingssack.volt.model.ParameterMetadata
import dev.kingssack.volt.model.RobotMetadata
import dev.kingssack.volt.robot.Robot
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

object MetadataService {
    private val robots = mutableListOf<RobotMetadata>()
    private val actions = mutableListOf<ActionMetadata>()

    fun getAllMetadata(): Pair<List<RobotMetadata>, List<ActionMetadata>> = Pair(robots, actions)

    @Suppress("unused")
    private object Scan : Scanner {
        override val loadAdjacencyRule = INDEPENDENT
        override val unloadAdjacencyRule = INDEPENDENT
        override val targets: SearchTarget = NarrowSearch()

        override fun scan(
            loader: ClassLoader,
            cls: Class<*>,
        ) {
            if (
                Robot::class.java.isAssignableFrom(cls) &&
                    !Modifier.isAbstract(cls.modifiers) &&
                    !cls.isInterface
            ) {
                if (robots.none { it.qualifiedName == cls.name }) {
                    robots.add(RobotMetadata(simpleName = cls.simpleName, qualifiedName = cls.name))
                }
            }

            cls.declaredMethods.forEach { method ->
                if (method.isAnnotationPresent(VoltAction::class.java)) {
                    processMethod(method, cls)
                }
            }
        }

        override fun unload(loader: ClassLoader, cls: Class<*>) {
            robots.clear()
            actions.clear()
        }
    }

    private fun processMethod(method: Method, clazz: Class<*>) {
        val annotation = method.getAnnotation(VoltAction::class.java) ?: return

        // Prevent duplicates
        val actionId = "${clazz.simpleName}.${method.name}"
        if (actions.any { it.id == actionId }) return

        val kFunction = method.kotlinFunction

        val parameters =
            kFunction?.valueParameters?.map { param ->
                ParameterMetadata(
                    name = param.name ?: "arg",
                    type = param.type.toString().substringAfterLast("."),
                    defaultValue = null,
                )
            }
                ?: method.parameters.map { param ->
                    ParameterMetadata(name = param.name, type = param.type.simpleName)
                }

        val robotType = clazz.simpleName

        actions.add(
            ActionMetadata(
                id = actionId,
                name = annotation.name.ifEmpty { method.name },
                description = annotation.description,
                parameters = parameters,
                robotType = robotType,
            )
        )
    }
}
