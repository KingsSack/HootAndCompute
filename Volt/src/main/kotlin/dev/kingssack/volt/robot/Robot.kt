package dev.kingssack.volt.robot

import com.acmerobotics.roadrunner.ftc.LazyHardwareMapImu
import com.acmerobotics.roadrunner.ftc.LazyImu
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.hardware.rev.Rev2mDistanceSensor
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.LED
import com.qualcomm.robotcore.hardware.NormalizedColorSensor
import com.qualcomm.robotcore.hardware.Servo
import dev.kingssack.volt.attachment.Attachment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf
import org.firstinspires.ftc.robotcore.external.Telemetry

/** Represents a robot with attachments. */
abstract class Robot(protected val hardwareMap: HardwareMap) {
    private var attachments = mutableListOf<Attachment>()

    init {
        fun KType.isAttachmentOrNullableAttachment(): Boolean {
            val attachment = typeOf<Attachment>()
            val nullableAttachment = attachment.withNullability(true)
            return this.isSubtypeOf(attachment) || this.isSubtypeOf(nullableAttachment)
        }

        this::class
            .memberProperties
            .filterIsInstance<KProperty1<Robot, *>>()
            .filter { prop -> prop.returnType.isAttachmentOrNullableAttachment() }
            .forEach { prop ->
                val value = runCatching { prop.get(this) }.getOrNull()
                if (value is Attachment) {
                    attachments.add(value)
                }
            }
    }

    /**
     * Helper function to create a motor property delegate.
     *
     * @param name the name of the motor
     * @return a ReadOnlyProperty that gets the motor from the hardware map
     */
    protected fun motor(name: String): ReadOnlyProperty<Any?, DcMotor> = ReadOnlyProperty { _, _ ->
        hardwareMap.dcMotor.get(name)
    }

    /**
     * Helper function to create a servo property delegate.
     *
     * @param name the name of the servo
     * @return a ReadOnlyProperty that gets the servo from the hardware map
     */
    protected fun servo(name: String): ReadOnlyProperty<Any?, Servo> = ReadOnlyProperty { _, _ ->
        hardwareMap.servo.get(name)
    }

    /**
     * Helper function to create a continuous rotation servo property delegate.
     *
     * @param name the name of the continuous rotation servo
     * @return a ReadOnlyProperty that gets the continuous rotation servo from the hardware map
     */
    protected fun crServo(name: String): ReadOnlyProperty<Any?, CRServo> = ReadOnlyProperty { _, _ ->
        hardwareMap.crservo.get(name)
    }

    /**
     * Helper function to create a HuskyLens property delegate.
     *
     * @param name the name of the HuskyLens
     * @return a ReadOnlyProperty that gets the HuskyLens from the hardware map
     */
    protected fun huskyLens(name: String): ReadOnlyProperty<Any?, HuskyLens> =
        ReadOnlyProperty { _, _ ->
            hardwareMap.get(HuskyLens::class.java, name)
        }

    /**
     * Helper function to create a Rev2mDistanceSensor property delegate.
     *
     * @param name the name of the Rev2mDistanceSensor
     * @return a ReadOnlyProperty that gets the Rev2mDistanceSensor from the hardware
     */
    protected fun distanceSensor(name: String): ReadOnlyProperty<Any?, Rev2mDistanceSensor> =
        ReadOnlyProperty { _, _ ->
            hardwareMap.get(Rev2mDistanceSensor::class.java, name)
        }

    /**
     * Helper function to create a NormalizedColorSensor property delegate.
     *
     * @param name the name of the NormalizedColorSensor
     * @return a ReadOnlyProperty that gets the NormalizedColorSensor from the hardware map
     */
    protected fun colorSensor(name: String): ReadOnlyProperty<Any?, NormalizedColorSensor> =
        ReadOnlyProperty { _, _ ->
            hardwareMap.get(NormalizedColorSensor::class.java, name)
        }

    /**
     * Helper function to create an IMU property delegate.
     *
     * @param name the name of the IMU
     * @return a ReadOnlyProperty that gets the IMU from the hardware map
     */
    protected fun imu(name: String): ReadOnlyProperty<Any?, IMU> = ReadOnlyProperty { _, _ ->
        hardwareMap.get(IMU::class.java, name)
    }

    /**
     * Helper function to create a lazy IMU property delegate.
     *
     * @param name the name of the IMU
     * @param orientation the orientation of the IMU on the robot
     * @return a ReadOnlyProperty that gets the lazy IMU from the hardware map
     */
    protected fun lazyImu(
        name: String,
        orientation: RevHubOrientationOnRobot,
    ): ReadOnlyProperty<Any?, LazyImu> = ReadOnlyProperty { _, _ ->
        LazyHardwareMapImu(hardwareMap, name, orientation)
    }

    /**
     * Helper function to create an LED property delegate.
     *
     * @param name the name of the LED
     * @return a ReadOnlyProperty that gets the LED from the hardware map
     */
    protected fun led(name: String): ReadOnlyProperty<Any?, LED> = ReadOnlyProperty { _, _ ->
        hardwareMap.led.get(name)
    }

    /**
     * Helper function to create an AnalogInput property delegate.
     *
     * @param name the name of the AnalogInput
     * @return a ReadOnlyProperty that gets the AnalogInput from the hardware map
     */
    protected fun analogInput(name: String): ReadOnlyProperty<Any?, AnalogInput> =
        ReadOnlyProperty { _, _ ->
            hardwareMap.analogInput.get(name)
        }

    /**
     * Updates the robot.
     *
     * @param telemetry for updating telemetry
     */
    context(telemetry: Telemetry)
    open fun update() {
        attachments.forEach { it.update() }

        // Update telemetry
        telemetry.update()
    }
}
