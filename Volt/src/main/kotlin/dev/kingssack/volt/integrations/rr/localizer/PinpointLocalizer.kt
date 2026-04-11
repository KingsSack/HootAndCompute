package dev.kingssack.volt.integrations.rr.localizer

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Rotation2d.Companion.fromDouble
import com.acmerobotics.roadrunner.Vector2d
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit
import java.util.*

/**
 * A [RoadRunnerLocalizer] that uses a Pinpoint Odometry Computer to localize the robot.
 *
 * @param hardwareMap the hardware map
 * @param inPerTick the number of inches per encoder tick
 * @param pose the initial pose
 * @param params configurable parameters
 * @property driver the Pinpoint driver
 */
class PinpointLocalizer(
    hardwareMap: HardwareMap,
    inPerTick: Double,
    override var pose: Pose2d,
    params: LocalizerParams = LocalizerParams(),
) : RoadRunnerLocalizer {
    /**
     * Parameters for [PinpointLocalizer].
     *
     * @property driverName the name of the Pinpoint driver in the hardware map
     * @property parDirection the direction of the parallel encoder
     * @property perpDirection the direction of the perpendicular encoder
     * @property parYTicks the number of ticks the parallel encoder is offset in the Y direction
     * @property perpXTicks the number of ticks the perpendicular encoder is offset in the X
     *   direction
     */
    class LocalizerParams(
        val driverName: String = "pinpoint",
        val parDirection: GoBildaPinpointDriver.EncoderDirection =
            GoBildaPinpointDriver.EncoderDirection.FORWARD,
        val perpDirection: GoBildaPinpointDriver.EncoderDirection =
            GoBildaPinpointDriver.EncoderDirection.FORWARD,
        var parYTicks: Double = 0.0,
        var perpXTicks: Double = 0.0,
    )

    private val mmPerTick = inPerTick * 25.4

    val driver: GoBildaPinpointDriver =
        hardwareMap.get(GoBildaPinpointDriver::class.java, params.driverName).apply {
            setEncoderResolution(1 / mmPerTick, DistanceUnit.MM)
            setOffsets(
                mmPerTick * params.parYTicks,
                mmPerTick * params.perpXTicks,
                DistanceUnit.MM,
            )
            setEncoderDirections(params.parDirection, params.perpDirection)
            resetPosAndIMU()
        }

    private var txPinpointRobot = Pose2d(0.0, 0.0, 0.0)

    override fun update(): PoseVelocity2d {
        driver.update()
        if (
            Objects.requireNonNull(driver.deviceStatus) == GoBildaPinpointDriver.DeviceStatus.READY
        ) {
            txPinpointRobot =
                Pose2d(
                    driver.getPosX(DistanceUnit.INCH),
                    driver.getPosY(DistanceUnit.INCH),
                    driver.getHeading(UnnormalizedAngleUnit.RADIANS),
                )
            val worldVelocity =
                Vector2d(driver.getVelX(DistanceUnit.INCH), driver.getVelY(DistanceUnit.INCH))
            val robotVelocity = fromDouble(-txPinpointRobot.heading.log()).times(worldVelocity)

            return PoseVelocity2d(
                robotVelocity,
                driver.getHeadingVelocity(UnnormalizedAngleUnit.RADIANS),
            )
        }
        return PoseVelocity2d(Vector2d(0.0, 0.0), 0.0)
    }
}
