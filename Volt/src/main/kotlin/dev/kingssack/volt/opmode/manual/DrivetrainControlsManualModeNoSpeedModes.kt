package dev.kingssack.volt.opmode.manual

import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import dev.kingssack.volt.attachment.drivetrain.Drivetrain
import dev.kingssack.volt.robot.DrivetrainRobot
import dev.kingssack.volt.util.Event.ManualEvent.Change
import dev.kingssack.volt.util.buttons.AnalogInput

/**
 * An abstract class that defines the methods for running a manual mode with speed modes for a robot
 * with a mecanum drivetrain.
 *
 * @param params the configuration object for manual control
 * @param T the type of drivetrain to control
 * @param R the type of robot with drivetrain
 * @property x the x-axis input from the gamepad
 * @property y the y-axis input from the gamepad
 * @property rx the rotation input from the gamepad
 */
abstract class DrivetrainControlsManualModeNoSpeedModes<T : Drivetrain, R : DrivetrainRobot<T>>(
    private val params: SimpleManualModeWithSpeedModesParams =
        SimpleManualModeWithSpeedModesParams(),
    manualParams: ManualParams = ManualParams(),
) : ManualMode<R>(manualParams) {
    /**
     * Configuration object for manual control.
     *
     * @property minPower the minimum power to register
     * @property turnScale the turn sensitivity
     */
    data class SimpleManualModeWithSpeedModesParams(
        val minPower: Double = 0.05,
        val turnScale: Double = 0.9,
        val speedScale: Double = 0.4,
    )

    var x = 0.0
    var y = 0.0
    var rx = 0.0

    init {
        // Speed mode controls
        Tap(Button.Y1) then {
            instant {
                currentSpeedMode = SpeedMode.TURBO
                gamepad1.rumble(
                    speedModes[SpeedMode.TURBO]!!,
                    speedModes[SpeedMode.TURBO]!!,
                    300,
                )
                gamepad1.setLedColor(255.0, 0.0, 0.0, 300)
            }
        }
        Tap(Button.B1) then {
            instant {
                currentSpeedMode = SpeedMode.NORMAL
                gamepad1.rumble(
                    speedModes[SpeedMode.NORMAL]!!,
                    speedModes[SpeedMode.NORMAL]!!,
                    300,
                )
                gamepad1.setLedColor(0.0, 0.0, 255.0, 300)
            }
        }
        Tap(Button.A1) then {
            instant {
                currentSpeedMode = SpeedMode.PRECISE
                gamepad1.rumble(
                    speedModes[SpeedMode.PRECISE]!!,
                    speedModes[SpeedMode.PRECISE]!!,
                    300,
                )
                gamepad1.setLedColor(0.0, 255.0, 0.0, 300)
            }
        }
        Tap(Button.X1) then {
            instant {
                currentSpeedMode = SpeedMode.SLOW
                gamepad1.rumble(speedModes[SpeedMode.SLOW]!!, speedModes[SpeedMode.SLOW]!!, 300)
                gamepad1.setLedColor(255.0, 255.0, 0.0, 300)
            }
        }

        // Movement controls
        Change(AnalogInput.LEFT_STICK_X1) then { value -> instant { x = -value.toDouble() } }
        Change(AnalogInput.LEFT_STICK_Y1) then { value -> instant { y = -value.toDouble() } }
        Change(AnalogInput.RIGHT_STICK_X1) then { value ->
            instant { rx = -value * params.turnScale }
        }
    }

    /**
     * Calculates the pose velocity of the robot based on the gamepad input.
     *
     * @return the pose velocity
     */
    fun calculatePoseWithGamepad(): PoseVelocity2d {
        // Apply current speed mode scaling
        val scale = params.speedScale
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

    override fun tick() {
        super.tick()
        robot.drivetrain.setDrivePowers(calculatePoseWithGamepad())
    }
}
