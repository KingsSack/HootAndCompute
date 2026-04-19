package dev.kingssack.volt.opmode

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import dev.frozenmilk.sinister.sdk.apphooks.OnCreateEventLoop
import dev.kingssack.volt.robot.Robot
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

    var currentOpMode: ActiveOpMode<*, *>? = null
        private set

    private fun reportFault(error: Throwable) {
        _state.value = OpModeState.Fault(error)
        VoltLogs.log("OpMode fault: ${error.message ?: error::class.java.simpleName}")
    }

    fun initializeOpMode() {
        initListeners.forEach {
            try {
                it()
            } catch (e: Exception) {
                reportFault(e)
            }
        }
        _state.value = OpModeState.Initializing
    }

    fun <R : Robot, O : VoltOpMode<R>> postInitializeOpMode(opMode: O, robot: R, clazz: Class<O>) {
        currentOpMode = ActiveOpMode(opMode, robot, clazz)
        isActive = true
        postInitListeners.forEach {
            try {
                currentOpMode?.let(it)
            } catch (e: Exception) {
                reportFault(e)
            }
        }
        _state.value = OpModeState.WaitingForStart
    }

    private fun startOpMode() {
        val opMode = currentOpMode ?: return
        startListeners.forEach {
            try {
                it(opMode)
            } catch (e: Exception) {
                reportFault(e)
            }
        }
        _state.value = OpModeState.Running
    }

    private fun stopOpMode() {
        this.isActive = false
        _state.value = OpModeState.Inactive
        stopListeners.forEach {
            try {
                currentOpMode?.let(it)
            } catch (e: Exception) {
                reportFault(e)
            }
        }
        this.currentOpMode = null
    }

    private val startListeners: MutableList<(ActiveOpMode<*, *>) -> Unit> = mutableListOf()
    private val stopListeners: MutableList<(ActiveOpMode<*, *>) -> Unit> = mutableListOf()
    private val initListeners: MutableList<() -> Unit> = mutableListOf()
    private val postInitListeners: MutableList<(ActiveOpMode<*, *>) -> Unit> = mutableListOf()

    @Suppress("unused")
    fun addStartListener(listener: (ActiveOpMode<*, *>) -> Unit) {
        startListeners.add(listener)
    }

    @Suppress("unused")
    fun addStopListener(listener: (ActiveOpMode<*, *>) -> Unit) {
        stopListeners.add(listener)
    }

    @Suppress("unused")
    fun addInitListener(listener: () -> Unit) {
        initListeners.add(listener)
    }

    @Suppress("unused")
    fun addPostInitListener(listener: (ActiveOpMode<*, *>) -> Unit) {
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
                try {
                    startOpMode()
                } catch (e: Exception) {
                    reportFault(e)
                }
            }
        }

        override fun onOpModePostStop(opMode: OpMode?) {
            if (isActive) {
                try {
                    stopOpMode()
                } catch (e: Exception) {
                    reportFault(e)
                }
            }
        }
    }

    @Suppress("unused")
    class ActiveOpMode<R : Robot, O : VoltOpMode<R>>(
        val opMode: O,
        val robot: R,
        val clazz: Class<O>,
    )
}
