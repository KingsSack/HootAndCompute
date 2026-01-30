package dev.kingssack.volt.robot

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.attachment.Attachment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

open class DefaultRobot(override val hardwareMap: HardwareMap) : Robot {
    override val _state = MutableStateFlow<RobotState>(RobotState.Initializing)
    override val state = _state.asStateFlow()

    override val attachments = mutableListOf<Attachment>()

    init {
        setState(RobotState.Idle)
    }
}
