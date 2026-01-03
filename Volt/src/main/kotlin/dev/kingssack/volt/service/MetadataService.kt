package dev.kingssack.volt.service

import android.os.Build
import androidx.annotation.RequiresApi
import dalvik.system.DexFile
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.model.ActionMetadata
import dev.kingssack.volt.model.ParameterMetadata
import dev.kingssack.volt.model.RobotMetadata
import dev.kingssack.volt.robot.Robot
import java.lang.reflect.Method
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction
import org.firstinspires.ftc.robotcore.internal.system.AppUtil

object MetadataService {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllMetadata(): Pair<List<RobotMetadata>, List<ActionMetadata>> {
        val context = AppUtil.getDefContext() ?: return Pair(emptyList(), emptyList())

        val robots = mutableListOf<RobotMetadata>()
        val actions = mutableListOf<ActionMetadata>()

        try {
            val dexFile = DexFile(context.packageCodePath)
            val entries = dexFile.entries()

            while (entries.hasMoreElements()) {
                val className = entries.nextElement()
                // Scan TeamCode and specific Volt packages if needed
                if (
                    className.startsWith("org.firstinspires.ftc.teamcode") ||
                        className.startsWith("dev.kingssack.volt")
                ) {

                    try {
                        // Skip some obviously non-relevant classes to speed up
                        if (className.contains("$"))
                            continue // Skip inner classes for now unless necessary

                        val clazz = Class.forName(className, false, context.classLoader)

                        // Check for Robot
                        if (
                            Robot::class.java.isAssignableFrom(clazz) &&
                                !java.lang.reflect.Modifier.isAbstract(clazz.modifiers)
                        ) {
                            robots.add(
                                RobotMetadata(
                                    simpleName = clazz.simpleName,
                                    qualifiedName = clazz.name,
                                )
                            )
                        }

                        // Check for Actions
                        clazz.methods.forEach { method -> processMethod(method, clazz, actions) }
                    } catch (e: Throwable) {
                        // Ignore load errors (NoClassDefFoundError etc)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(robots, actions)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processMethod(
        method: Method,
        clazz: Class<*>,
        actions: MutableList<ActionMetadata>,
    ) {
        val annotation = method.getAnnotation(VoltAction::class.java) ?: return

        val kFunction = method.kotlinFunction

        val parameters =
            kFunction?.valueParameters?.map { param ->
                ParameterMetadata(
                    name = param.name ?: "arg",
                    type = param.type.toString().substringAfterLast("."), // Simple type name
                    defaultValue = null, // Hard to get the default value without an instance
                )
            }
                ?: method.parameters.map { param ->
                    ParameterMetadata(name = param.name, type = param.type.simpleName)
                }

        // Determine robot type (receiver type if extension, or declaring class)
        // If it's an extension function, the first parameter is the receiver.
        // But kotlin-reflect handles extension receivers differently.

        // For simplicity, let's assume actions are defined within a Robot subclass or Attachment
        // OR are extension functions on a Robot/Attachment.

        // Let's use the declaring class as the simpler approach for now.
        val robotType = clazz.simpleName

        actions.add(
            ActionMetadata(
                id = "${clazz.simpleName}.${method.name}",
                name = annotation.name.ifEmpty { method.name },
                description = annotation.description,
                parameters = parameters,
                robotType = robotType,
            )
        )
    }
}
