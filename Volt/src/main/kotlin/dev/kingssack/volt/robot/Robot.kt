package dev.kingssack.volt.robot

import com.acmerobotics.roadrunner.ftc.LazyHardwareMapImu
import com.acmerobotics.roadrunner.ftc.LazyImu
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.hardware.rev.Rev2mDistanceSensor
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.LED
import com.qualcomm.robotcore.hardware.NormalizedColorSensor
import com.qualcomm.robotcore.hardware.Servo
import dev.kingssack.volt.attachment.Attachment
import dev.kingssack.volt.util.telemetry.ActionTracer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.firstinspires.ftc.robotcore.external.Telemetry

/** Represents the state of a robot. */
sealed interface RobotState {
    data object Initializing : RobotState

    data object Idle : RobotState

    data object Running : RobotState

    data class Fault(val error: Throwable) : RobotState
}

/** Represents a robot with attachments. */
open class Robot(private val hardwareMap: HardwareMap) {
    private val _state = MutableStateFlow<RobotState>(RobotState.Initializing)
    val state = _state.asStateFlow()

    internal val attachments = mutableListOf<Attachment>()

    protected fun registerAttachment(attachment: Attachment) {
        attachments.add(attachment)
    }

    protected fun <T : Attachment> attachment(factory: () -> T): T =
        factory().also { registerAttachment(it) }

    /**
     * Helper function to create a motor property delegate.
     *
     * @param name the name of the motor
     * @return a Lazy that gets the motor from the hardware map
     */
    fun motor(name: String): Lazy<DcMotor> = lazy { hardwareMap.dcMotor.get(name) }

    /**
     * Helper function to create a motorEx property delegate.
     *
     * @param name the name of the motorEx
     * @return a Lazy that gets the motorEx from the hardware map
     */
    fun motorEx(name: String): Lazy<DcMotorEx> = lazy {
        hardwareMap.get(DcMotorEx::class.java, name)
    }

    /**
     * Helper function to create a servo property delegate.
     *
     * @param name the name of the servo
     * @return a Lazy that gets the servo from the hardware map
     */
    fun servo(name: String): Lazy<Servo> = lazy { hardwareMap.servo.get(name) }

    /**
     * Helper function to create a continuous rotation servo property delegate.
     *
     * @param name the name of the continuous rotation servo
     * @return a Lazy that gets the continuous rotation servo from the hardware map
     */
    fun crServo(name: String): Lazy<CRServo> = lazy { hardwareMap.crservo.get(name) }

    /**
     * Helper function to create a HuskyLens property delegate.
     *
     * @param name the name of the HuskyLens
     * @return a Lazy that gets the HuskyLens from the hardware map
     */
    fun huskyLens(name: String): Lazy<HuskyLens> = lazy {
        hardwareMap.get(HuskyLens::class.java, name)
    }

    /**
     * Helper function to create a Rev2mDistanceSensor property delegate.
     *
     * @param name the name of the Rev2mDistanceSensor
     * @return a Lazy that gets the Rev2mDistanceSensor from the hardware
     */
    fun distanceSensor(name: String): Lazy<Rev2mDistanceSensor> = lazy {
        hardwareMap.get(Rev2mDistanceSensor::class.java, name)
    }

    /**
     * Helper function to create a NormalizedColorSensor property delegate.
     *
     * @param name the name of the NormalizedColorSensor
     * @return a Lazy that gets the NormalizedColorSensor from the hardware map
     */
    fun colorSensor(name: String): Lazy<NormalizedColorSensor> = lazy {
        hardwareMap.get(NormalizedColorSensor::class.java, name)
    }

    /**
     * Helper function to create an IMU property delegate.
     *
     * @param name the name of the IMU
     * @return a Lazy that gets the IMU from the hardware map
     */
    fun imu(name: String): Lazy<IMU> = lazy { hardwareMap.get(IMU::class.java, name) }

    /**
     * Helper function to create a lazy IMU property delegate.
     *
     * @param name the name of the IMU
     * @param orientation the orientation of the IMU on the robot
     * @return a Lazy that gets the lazy IMU from the hardware map
     */
    fun lazyImu(name: String, orientation: RevHubOrientationOnRobot): Lazy<LazyImu> = lazy {
        LazyHardwareMapImu(hardwareMap, name, orientation)
    }

    /**
     * Helper function to create an LED property delegate.
     *
     * @param name the name of the LED
     * @return a Lazy that gets the LED from the hardware map
     */
    fun led(name: String): Lazy<LED> = lazy { hardwareMap.led.get(name) }

    /**
     * Helper function to create an LED driver property delegate.
     *
     * @param name the name of the LED driver
     * @return a Lazy that gets the LED driver from the hardware map
     */
    fun ledDriver(name: String): Lazy<RevBlinkinLedDriver> = lazy {
        hardwareMap.get(RevBlinkinLedDriver::class.java, name)
    }

    /**
     * Helper function to create an AnalogInput property delegate.
     *
     * @param name the name of the AnalogInput
     * @return a Lazy that gets the AnalogInput from the hardware map
     */
    fun analogInput(name: String): Lazy<AnalogInput> = lazy { hardwareMap.analogInput.get(name) }

    /**
     * Updates the robot.
     *
     * @param telemetry for updating telemetry
     */
    context(telemetry: Telemetry)
    open fun update() {
        with(telemetry) {
            addLine()
            ActionTracer.writeTelemetry()

            if (state.value is RobotState.Fault) {
                telemetry.update()
                return
            }

            addLine()
            addLine("=== Attachments ===")
            addLine()

            _state.value = RobotState.Idle

            for (attachment in attachments) {
                attachment.update()

                if (attachment.isBusy() && state.value == RobotState.Idle) {
                    _state.value = RobotState.Running
                }
            }

            telemetry.update()
        }
    }
}
