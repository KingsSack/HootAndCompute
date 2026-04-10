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
    class LocalizerParams(var parYTicks: Double = 0.0, var perpXTicks: Double = 0.0)

    val driver: GoBildaPinpointDriver =
        hardwareMap.get(GoBildaPinpointDriver::class.java, "pinpoint")
    private val initialParDirection = GoBildaPinpointDriver.EncoderDirection.FORWARD
    private val initialPerpDirection = GoBildaPinpointDriver.EncoderDirection.FORWARD

    private var txWorldPinpoint: Pose2d
    private var txPinpointRobot = Pose2d(0.0, 0.0, 0.0)

    init {
        val mmPerTick = inPerTick * 25.4
        driver.setEncoderResolution(1 / mmPerTick, DistanceUnit.MM)
        driver.setOffsets(
            mmPerTick * params.parYTicks,
            mmPerTick * params.perpXTicks,
            DistanceUnit.MM,
        )
        driver.setEncoderDirections(initialParDirection, initialPerpDirection)
        driver.resetPosAndIMU()

        txWorldPinpoint = pose
    }

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
