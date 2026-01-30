package dev.kingssack.volt.opmode

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import dev.frozenmilk.sinister.sdk.apphooks.OnCreateEventLoop
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.VoltLogs

object VoltOpModeWrapper {
    var isActive = false
        private set
    enum class OpModeState {
        INACTIVE,
        INITIALIZING,
        WAITING_FOR_START,
        STARTED
    }
    var state = OpModeState.INACTIVE
    var currentOpMode: ActiveOpMode<*, *>? = null
        private set
    fun <R: Robot, O: VoltOpMode<R>> initializeOpMode(opMode: O, robot: R, clazz: Class<O>) {
        currentOpMode = ActiveOpMode(opMode, robot, clazz)
        isActive = true
        initListeners.forEach {
            try {
                it(currentOpMode!!)
            } catch (e : Error) {
                VoltLogs.log("error in listener: ${e.message.toString()}")
            }
        }
        state = OpModeState.INITIALIZING
    }
    fun postInitializeOpMode() {
            postInitListeners.forEach {
                try {
                    it(currentOpMode!!)
                } catch (e : Error) {
                    VoltLogs.log("error in listener: ${e.message.toString()}")
                }
            }
        state = OpModeState.WAITING_FOR_START
    }
    private fun startOpMode() {
        startListeners.forEach {
            try {
                it(currentOpMode!!)
            } catch (e : Error) {
                VoltLogs.log("error in listener: ${e.message.toString()}")
            }
        }
        state = OpModeState.STARTED
    }
    private fun stopOpMode() {
        this.isActive = false
        state = OpModeState.INACTIVE
        stopListeners.forEach {
            try {
                it(currentOpMode!!)
            } catch (e : Error) {
                VoltLogs.log("error in listener: ${e.message.toString()}")
            }
        }
        this.currentOpMode = null
    }

    private val startListeners : MutableList<(ActiveOpMode<*, *>) -> Unit> = mutableListOf()
    private val stopListeners : MutableList<(ActiveOpMode<*, *>) -> Unit> = mutableListOf()
    private val initListeners : MutableList<(ActiveOpMode<*, *>) -> Unit> = mutableListOf()
    private val postInitListeners : MutableList<(ActiveOpMode<*, *>) -> Unit> = mutableListOf()

    @Suppress("unused")
    fun addStartListener(listener: (ActiveOpMode<*, *>) -> Unit) {
        startListeners.add(listener)
    }
    @Suppress("unused")
    fun addStopListener(listener: (ActiveOpMode<*, *>) -> Unit) {
        stopListeners.add(listener)
    }
    @Suppress("unused")
    fun addInitListener(listener: (ActiveOpMode<*, *>) -> Unit) {
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
            startOpMode()
        }

        override fun onOpModePostStop(opMode: OpMode?) {
            if (isActive) {
                stopOpMode()
            }
        }
    }

    @Suppress("unused")
    class ActiveOpMode <R: Robot, O: VoltOpMode<R>>(
        public val opMode: O,
        public val robot: R,
        public val clazz: Class<O>
    )
}