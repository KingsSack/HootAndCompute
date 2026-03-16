package dev.kingssack.volt.opmode.manual

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import dev.kingssack.volt.core.VoltActionBuilder
import dev.kingssack.volt.opmode.VoltOpMode
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event
import dev.kingssack.volt.util.buttons.AnalogHandler
import dev.kingssack.volt.util.buttons.AnalogInput
import dev.kingssack.volt.util.buttons.Button
import dev.kingssack.volt.util.buttons.ButtonHandler
import dev.kingssack.volt.util.telemetry.ActionTracer
import java.util.*
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

/**
 * ManualMode is an abstract class that defines the methods for running a manual mode.
 *
 * @param R the robot type
 * @property params the configuration object for manual control
 */
abstract class ManualMode<R : Robot>(private val params: ManualParams = ManualParams()) :
    VoltOpMode<R>() {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(
            registrationHelper: VoltRegistrationHelper,
            clazz: Class<VoltOpMode<*>>,
        ) {
            if (clazz.isAnnotationPresent(VoltOpModeMeta::class.java)) {
                val annotation = clazz.getAnnotation(VoltOpModeMeta::class.java)
                if (annotation != null) {
                    registrationHelper.register(
                        clazz.getDeclaredConstructor(),
                        OpModeMeta.Builder()
                            .setName(annotation.name)
                            .setGroup(annotation.group)
                            .setFlavor(OpModeMeta.Flavor.TELEOP)
                            .setSource(OpModeMeta.Source.EXTERNAL_LIBRARY)
                            .build(),
                    )
                }
            }
        }
    }

    /**
     * Configuration object for manual control.
     *
     * @property deadzone the minimum joystick input to register
     * @property inputExp the input exponential for fine control
     */
    data class ManualParams(val deadzone: Float = 0.05f, val inputExp: Float = 2.0f)

    private val buttonHandlers = EnumMap<Button, ButtonHandler>(Button::class.java)
    private val analogHandlers = EnumMap<AnalogInput, AnalogHandler>(AnalogInput::class.java)

    private var runningActions = mutableListOf<Action>()
    private val dash: FtcDashboard? = FtcDashboard.getInstance()

    private val buttonBindings =
        mutableListOf<Pair<Event.ManualEvent.ButtonEvent, VoltActionBuilder<R>.() -> Unit>>()

    private val analogBindings =
        mutableListOf<Pair<Event.ManualEvent.AnalogEvent, VoltActionBuilder<R>.(Float) -> Unit>>()

    private val comboBindings =
        mutableListOf<Pair<Event.ManualEvent.Combo, VoltActionBuilder<R>.() -> Unit>>()

    private val instantButtons = EnumMap<Button, R.() -> Unit>(Button::class.java)

    /** Maps an action to a button event */
    protected infix fun Event.ManualEvent.ButtonEvent.then(block: VoltActionBuilder<R>.() -> Unit) {
        buttonBindings.add(this to block)
    }

    /** Maps an action to an analog event */
    protected infix fun Event.ManualEvent.AnalogEvent.then(
        block: VoltActionBuilder<R>.(Float) -> Unit
    ) {
        analogBindings.add(this to block)
    }

    /** Create a combo event with [buttons] */
    protected fun combo(vararg buttons: Button) = Event.ManualEvent.Combo(buttons.toSet())

    /** Maps an action to a combo event */
    protected infix fun Event.ManualEvent.Combo.then(block: VoltActionBuilder<R>.() -> Unit) {
        comboBindings.add(this to block)
    }

    /** A place to define actions to be triggered by events */
    abstract fun defineEvents()

    private fun initializeInputMappings() {
        Button.entries.forEach { button -> buttonHandlers[button] = ButtonHandler() }
        AnalogInput.entries.forEach { analog ->
            analogHandlers[analog] = AnalogHandler(params.deadzone, params.inputExp)
        }
    }

    /** Initializes inputs and defines manual events */
    init {
        initializeInputMappings()
        defineEvents()
    }

    override fun begin() {
        while (opModeIsActive()) tick()
    }

    /** Tick the manual mode. */
    open fun tick() =
        context(telemetry) {
            updateInputState()
            processEvents()
            runActions()
            robot.update()
        }

    private fun updateInputState() {
        Button.entries.forEach { button ->
            val state = button.get(gamepad1, gamepad2)
            buttonHandlers[button]?.update(state)
        }

        AnalogInput.entries.forEach { analog ->
            val state = analog.get(gamepad1, gamepad2)
            analogHandlers[analog]?.update(state)
        }
    }

    private fun processEvents() {
        buttonBindings.forEach { (event, action) ->
            val handler = buttonHandlers[event.button] ?: return@forEach
            val triggered =
                when (event) {
                    is Event.ManualEvent.Tap -> handler.tappedThisTick
                    is Event.ManualEvent.Release -> handler.releasedThisTick
                    is Event.ManualEvent.Hold -> handler.held(event.durationMs)
                    is Event.ManualEvent.DoubleTap -> handler.doubleTappedThisTick
                }
            if (triggered) triggerAction(action)
        }

        analogBindings.forEach { (event, action) ->
            val handler = analogHandlers[event.analogInput] ?: return@forEach
            val triggered =
                when (event) {
                    is Event.ManualEvent.Change -> handler.changed
                    is Event.ManualEvent.Threshold -> handler.changed && handler.value > event.min
                }
            if (triggered) triggerAnalogAction(handler.value, action)
        }

        comboBindings.forEach { (event, action) ->
            val allPressed = event.buttons.all { buttonHandlers[it]?.pressed == true }
            val anyJustPressed = event.buttons.any { buttonHandlers[it]?.tappedThisTick == true }
            if (allPressed && anyJustPressed) triggerAction(action)
        }

        instantButtons.forEach { (button, block) ->
            if (buttonHandlers[button]?.pressed == true) robot.block()
        }
    }

    private fun triggerAction(block: VoltActionBuilder<R>.() -> Unit) {
        val builder = VoltActionBuilder(robot).apply(block)
        runningActions.add(builder.build())
    }

    private fun triggerAnalogAction(value: Float, block: VoltActionBuilder<R>.(Float) -> Unit) {
        val builder = VoltActionBuilder(robot).apply { block(value) }
        runningActions.add(builder.build())
    }

    private fun runActions() {
        val packet = TelemetryPacket()
        runningActions.removeAll { action ->
            action.preview(packet.fieldOverlay())
            !action.run(packet)
        }
        context(packet) { ActionTracer.writePacket() }
        dash?.sendTelemetryPacket(packet)
    }
}
