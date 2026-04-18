package dev.kingssack.volt.opmode

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import dev.frozenmilk.sinister.sdk.apphooks.OnCreateEventLoop
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.Event
import dev.kingssack.volt.util.VoltLogs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface OpModeState {
    data object Inactive : OpModeState

    data object Initializing : OpModeState

    data object WaitingForStart : OpModeState

    data object Running : OpModeState

    data class Fault(val error: Throwable) : OpModeState
}

object VoltOpModeWrapper {
    private val _state = MutableStateFlow<OpModeState>(OpModeState.Inactive)
    val state = _state.asStateFlow()

    var isActive = false
        private set

    var currentOpMode: ActiveOpMode<*, *, *>? = null
        private set

    fun initializeOpMode() {
        initListeners.forEach {
            try {
                it()
            } catch (e: Exception) {
                VoltLogs.log("Error in listener: ${e.message.toString()}")
            }
        }
        _state.value = OpModeState.Initializing
    }

    fun <R : Robot, E : Event, O : VoltOpMode<R, E>> postInitializeOpMode(
        opMode: O,
        robot: R,
        clazz: Class<O>,
    ) {
        currentOpMode = ActiveOpMode(opMode, robot, clazz)
        isActive = true
        postInitListeners.forEach {
            try {
                it(currentOpMode!!)
            } catch (e: Exception) {
                VoltLogs.log("Error in listener: ${e.message.toString()}")
            }
        }
        _state.value = OpModeState.WaitingForStart
    }

    private fun startOpMode() {
        startListeners.forEach {
            try {
                it(currentOpMode!!)
            } catch (e: Exception) {
                VoltLogs.log("Error in listener: ${e.message.toString()}")
            }
        }
        _state.value = OpModeState.Running
    }

    private fun stopOpMode() {
        this.isActive = false
        _state.value = OpModeState.Inactive
        stopListeners.forEach {
            try {
                it(currentOpMode!!)
            } catch (e: Exception) {
                VoltLogs.log("Error in listener: ${e.message.toString()}")
            }
        }
        this.currentOpMode = null
    }

    private val startListeners: MutableList<(ActiveOpMode<*, *, *>) -> Unit> = mutableListOf()
    private val stopListeners: MutableList<(ActiveOpMode<*, *, *>) -> Unit> = mutableListOf()
    private val initListeners: MutableList<() -> Unit> = mutableListOf()
    private val postInitListeners: MutableList<(ActiveOpMode<*, *, *>) -> Unit> = mutableListOf()

    @Suppress("unused")
    fun addStartListener(listener: (ActiveOpMode<*, *, *>) -> Unit) {
        startListeners.add(listener)
    }

    @Suppress("unused")
    fun addStopListener(listener: (ActiveOpMode<*, *, *>) -> Unit) {
        stopListeners.add(listener)
    }

    @Suppress("unused")
    fun addInitListener(listener: () -> Unit) {
        initListeners.add(listener)
    }

    @Suppress("unused")
    fun addPostInitListener(listener: (ActiveOpMode<*, *, *>) -> Unit) {
        postInitListeners.add(listener)
    }

    @Suppress("unused")
    private object OpModeStopHook : OnCreateEventLoop, OpModeManagerNotifier.Notifications {
        override fun onCreateEventLoop(context: Context, ftcEventLoop: FtcEventLoop) {
            val opModeManager = ftcEventLoop.opModeManager
            opModeManager.registerListener(this)
        }

        override fun onOpModePreInit(opMode: OpMode?) {}

        override fun onOpModePreStart(opMode: OpMode?) {
            if (isActive) {
                startOpMode()
            }
        }

        override fun onOpModePostStop(opMode: OpMode?) {
            if (isActive) {
                stopOpMode()
            }
        }
    }

    @Suppress("unused")
    class ActiveOpMode<R : Robot, E : Event, O : VoltOpMode<R, E>>(
        val opMode: O,
        val robot: R,
        val clazz: Class<O>,
    )
}
