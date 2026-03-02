package dev.kingssack.volt.opmode.manual

import com.acmerobotics.roadrunner.InstantAction
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import dev.kingssack.volt.attachment.drivetrain.MecanumDrivetrain
import dev.kingssack.volt.robot.RobotWithMecanumDrivetrain
import dev.kingssack.volt.util.GamepadAnalogInput
import dev.kingssack.volt.util.GamepadButton
import java.util.EnumMap
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * An abstract class that defines the methods for running a manual mode with speed modes for a robot
 * with a mecanum drivetrain.
 *
 * @param T the type of mecanum drivetrain
 * @param R the type of robot with mecanum drivetrain
 * @property params the configuration object for manual control
 * @property x the x-axis input from the gamepad
 * @property y the y-axis input from the gamepad
 * @property rx the rotation input from the gamepad
 */
abstract class SimpleManualModeWithSpeedModes<
    T : MecanumDrivetrain,
    R : RobotWithMecanumDrivetrain<T>,
>(
    private val params: SimpleManualModeWithSpeedModesParams =
        SimpleManualModeWithSpeedModesParams(),
    manualParams: ManualParams = ManualParams(),
) : ManualMode<R>(manualParams) {
    /**
     * Configuration object for manual control.
     *
     * @property minPower the minimum power to register
     * @property turnScale the turn sensitivity
     * @property turbo the speed of the turbo speed mode
     * @property normal the speed of the normal speed mode
     * @property precise the speed of the precise speed mode
     */
    data class SimpleManualModeWithSpeedModesParams(
        val minPower: Double = 0.05,
        val turnScale: Double = 0.9,
        val turbo: Double = 1.0,
        val normal: Double = 0.5,
        val precise: Double = 0.2,
        val slow: Double = 0.1,
    )

    enum class SpeedMode {
        TURBO,
        NORMAL,
        PRECISE,
        SLOW,
    }

    private val speedModes =
        EnumMap(
            mapOf(
                SpeedMode.TURBO to params.turbo,
                SpeedMode.NORMAL to params.normal,
                SpeedMode.PRECISE to params.precise,
                SpeedMode.SLOW to params.slow,
            )
        )

    private var currentSpeedMode = SpeedMode.NORMAL

    var x = 0.0
    var y = 0.0
    var rx = 0.0

    init {
        onButtonReleased(GamepadButton.Y1) {
            +InstantAction { currentSpeedMode = SpeedMode.TURBO }
            +InstantAction { gamepad1.rumble(speedModes[SpeedMode.TURBO]!!, speedModes[SpeedMode.TURBO]!!, 300) }
            +InstantAction { gamepad1.setLedColor(255.0, 0.0, 0.0, 300) }
        }
        onButtonReleased(GamepadButton.B1) {
            +InstantAction { currentSpeedMode = SpeedMode.NORMAL }
            +InstantAction { gamepad1.rumble(speedModes[SpeedMode.NORMAL]!!, speedModes[SpeedMode.NORMAL]!!, 300) }
            +InstantAction { gamepad1.setLedColor(0.0, 0.0, 255.0, 300) }
        }
        onButtonReleased(GamepadButton.A1) {
            +InstantAction { currentSpeedMode = SpeedMode.PRECISE }
            +InstantAction { gamepad1.rumble(speedModes[SpeedMode.PRECISE]!!, speedModes[SpeedMode.PRECISE]!!, 300) }
            +InstantAction { gamepad1.setLedColor(0.0, 255.0, 0.0, 300) }
        }
        onButtonReleased(GamepadButton.X1) {
            +InstantAction { currentSpeedMode = SpeedMode.SLOW }
            +InstantAction { gamepad1.rumble(speedModes[SpeedMode.SLOW]!!, speedModes[SpeedMode.SLOW]!!, 300) }
            +InstantAction { gamepad1.setLedColor(255.0, 255.0, 0.0, 300) }
        }

        onAnalog(GamepadAnalogInput.LEFT_STICK_X1) { value -> x = -value.toDouble() }
        onAnalog(GamepadAnalogInput.LEFT_STICK_Y1) { value -> y = -value.toDouble() }
        onAnalog(GamepadAnalogInput.RIGHT_STICK_X1) { value -> rx = -value * params.turnScale }
    }

    /**
     * Calculates the pose velocity of the robot based on the gamepad input.
     *
     * @return the pose velocity
     */
    fun calculatePoseWithGamepad(): PoseVelocity2d {
        // Apply current speed mode scaling
        val scale = speedModes[currentSpeedMode]!!
        val scaledX = x * scale
        val scaledY = y * scale
        val scaledRx = rx * scale

        // Create linear and angular velocities with scaling
        val linearVelocity = Vector2d(applyMinPower(scaledY), applyMinPower(scaledX))
        val angularVelocity = applyMinPower(scaledRx)

        // Return the calculated pose velocity
        return PoseVelocity2d(linearVelocity, angularVelocity)
    }

    private fun applyMinPower(power: Double): Double {
        return when {
            power > params.minPower -> power
            power < -params.minPower -> power
            else -> 0.0
        }
    }

    context(telemetry: Telemetry)
    override fun tick() {
        robot.drivetrain.setDrivePowers(calculatePoseWithGamepad())
        telemetry.addData("Speed Mode", currentSpeedMode)
        super.tick()
    }
}
