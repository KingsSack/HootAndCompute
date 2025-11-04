package dev.kingssack.volt.opmode.manual

import com.acmerobotics.roadrunner.InstantAction
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.util.GamepadAnalogInput
import dev.kingssack.volt.util.GamepadButton
import java.util.EnumMap

/**
 * SimpleManualModeWithSpeedModes is an abstract class that defines the methods for running a manual
 * mode with speed modes.
 *
 * @property params the configuration object for manual control
 */
abstract class SimpleManualModeWithSpeedModes<R : Robot>(
    robotFactory: (hardwareMap: HardwareMap) -> R,
    private val params: SimpleManualModeWithSpeedModesParams =
        SimpleManualModeWithSpeedModesParams(),
    manualParams: ManualParams = ManualParams(),
) : ManualMode<R>(robotFactory, manualParams) {
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

    init {
        onButtonTapped(GamepadButton.Y1) {
            +{ InstantAction { currentSpeedMode = SpeedMode.TURBO } }
        }
        onButtonTapped(GamepadButton.B1) {
            +{ InstantAction { currentSpeedMode = SpeedMode.NORMAL } }
        }
        onButtonTapped(GamepadButton.A1) {
            +{ InstantAction { currentSpeedMode = SpeedMode.PRECISE } }
        }
        onButtonTapped(GamepadButton.X1) {
            +{ InstantAction { currentSpeedMode = SpeedMode.SLOW } }
        }
    }

    /**
     * Calculates the pose velocity of the robot based on the gamepad input.
     *
     * @return the pose velocity
     */
    fun calculatePoseWithGamepad(): PoseVelocity2d {
        // Get gamepad input with deadzone and exponential scaling
        val x = -getAnalogValue(GamepadAnalogInput.LEFT_STICK_X1)
        val y = -getAnalogValue(GamepadAnalogInput.LEFT_STICK_Y1)
        val rx = -getAnalogValue(GamepadAnalogInput.RIGHT_STICK_X1) * params.turnScale

        // Apply current speed mode scaling
        val scale = speedModes[currentSpeedMode]!!

        // Create linear and angular velocities with scaling
        val linearVelocity = Vector2d(applyMinPower(y * scale), applyMinPower(x * scale))
        val angularVelocity = applyMinPower(rx * scale)

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

    override fun tick() {
        telemetry.addData("Speed Mode", currentSpeedMode)
        super.tick()
    }
}
