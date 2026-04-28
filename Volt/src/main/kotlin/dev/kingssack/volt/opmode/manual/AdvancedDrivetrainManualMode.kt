package dev.kingssack.volt.opmode.manual

import dev.kingssack.volt.attachment.drivetrain.Drivetrain
import dev.kingssack.volt.robot.DrivetrainRobot
import dev.kingssack.volt.util.Event.ManualEvent.Tap
import dev.kingssack.volt.util.buttons.Button
import java.util.*

/**
 * A [ManualMode] that defines events for controlling a [DrivetrainRobot]
 *
 * @param T the type of drivetrain to control
 * @param R the type of robot with drivetrain
 * @param params the configuration object for manual control
 * @param manualParams the configuration object for manual control
 */
abstract class AdvancedDrivetrainManualMode<T : Drivetrain, R : DrivetrainRobot<T>>(
    private val params: SpeedModesParams = SpeedModesParams(),
    drivetrainControlsParams: DrivetrainControlsParams = DrivetrainControlsParams(),
    manualParams: ManualParams = ManualParams(),
) : DrivetrainManualMode<T, R>(drivetrainControlsParams, manualParams) {
    /**
     * Configuration object for speed modes.
     *
     * @property turbo the speed of the turbo speed mode
     * @property normal the speed of the normal speed mode
     * @property precise the speed of the precise speed mode
     * @property slow the speed of the slow speed mode
     */
    data class SpeedModesParams(
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

    init {
        // Speed mode controls
        Tap(Button.Y1) then
            {
                instant {
                    val speed = speedModes[SpeedMode.TURBO]!!
                    speedScale = speed
                    gamepad1.rumble(speed, speed, 300)
                    gamepad1.setLedColor(255.0, 0.0, 0.0, 300)
                }
            }
        Tap(Button.B1) then
            {
                instant {
                    val speed = speedModes[SpeedMode.NORMAL]!!
                    speedScale = speed
                    gamepad1.rumble(speed, speed, 300)
                    gamepad1.setLedColor(0.0, 0.0, 255.0, 300)
                }
            }
        Tap(Button.A1) then
            {
                instant {
                    val speed = speedModes[SpeedMode.PRECISE]!!
                    speedScale = speed
                    gamepad1.rumble(speed, speed, 300)
                    gamepad1.setLedColor(0.0, 255.0, 0.0, 300)
                }
            }
        Tap(Button.X1) then
            {
                instant {
                    val speed = speedModes[SpeedMode.SLOW]!!
                    speedScale = speed
                    gamepad1.rumble(speed, speed, 300)
                    gamepad1.setLedColor(255.0, 255.0, 0.0, 300)
                }
            }
    }

    override fun tick() {
        super.tick()
        telemetry.addData("Speed Scale", speedScale)
    }
}
