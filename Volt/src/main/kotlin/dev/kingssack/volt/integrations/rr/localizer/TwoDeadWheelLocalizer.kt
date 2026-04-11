package dev.kingssack.volt.integrations.rr.localizer

import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.Rotation2d.Companion.exp
import com.acmerobotics.roadrunner.ftc.Encoder
import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.IMU
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit
import kotlin.math.abs
import kotlin.math.sign

/**
 * A [RoadRunnerLocalizer] that uses two dead wheels and an IMU to localize the robot.
 *
 * @param hardwareMap the hardware map
 * @param imu the imu
 * @param inPerTick the number of inches per encoder tick
 * @param pose the initial pose
 * @param params configurable parameters
 * @property par the parallel encoder
 * @property perp the perpendicular encoder
 */
class TwoDeadWheelLocalizer(
    hardwareMap: HardwareMap,
    val imu: IMU,
    private val inPerTick: Double,
    override var pose: Pose2d,
    private val params: LocalizerParams = LocalizerParams(),
) : RoadRunnerLocalizer {
    /**
     * Parameters for [TwoDeadWheelLocalizer].
     *
     * @property parName the name of the parallel encoder motor
     * @property parDirection the direction of the parallel encoder motor
     * @property perpName the name of the perpendicular encoder motor
     * @property perpDirection the direction of the perpendicular encoder motor
     * @property parYTicks y position of the parallel encoder (in tick units)
     * @property perpXTicks x position of the perpendicular encoder (in tick units)
     */
    class LocalizerParams(
        val parName: String = "par",
        val parDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val perpName: String = "perp",
        val perpDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val parYTicks: Double = 0.0,
        val perpXTicks: Double = 0.0,
    )

    val par: Encoder =
        OverflowEncoder(
            RawEncoder(
                hardwareMap.get(DcMotorEx::class.java, params.parName).apply {
                    direction = params.parDirection
                }
            )
        )
    val perp: Encoder =
        OverflowEncoder(
            RawEncoder(
                hardwareMap.get(DcMotorEx::class.java, params.perpName).apply {
                    direction = params.perpDirection
                }
            )
        )

    private var lastParPos = 0
    private var lastPerpPos = 0
    private var lastHeading = Rotation2d(0.0, 0.0)

    private var lastRawHeadingVel = 0.0
    private var headingVelOffset: Double = 0.0
    private var initialized = false

    override fun update(): PoseVelocity2d {
        val parPosVel = par.getPositionAndVelocity()
        val perpPosVel = perp.getPositionAndVelocity()

        val angles = imu.robotYawPitchRollAngles
        // Use degrees here to work around
        // https://github.com/FIRST-Tech-Challenge/FtcRobotController/issues/1070
        val angularVelocityDegrees = imu.getRobotAngularVelocity(AngleUnit.DEGREES)
        val angularVelocity =
            AngularVelocity(
                UnnormalizedAngleUnit.RADIANS,
                Math.toRadians(angularVelocityDegrees.xRotationRate.toDouble()).toFloat(),
                Math.toRadians(angularVelocityDegrees.yRotationRate.toDouble()).toFloat(),
                Math.toRadians(angularVelocityDegrees.zRotationRate.toDouble()).toFloat(),
                angularVelocityDegrees.acquisitionTime,
            )

        val heading = exp(angles.getYaw(AngleUnit.RADIANS))

        // see https://github.com/FIRST-Tech-Challenge/FtcRobotController/issues/617
        val rawHeadingVel = angularVelocity.zRotationRate.toDouble()
        if (abs(rawHeadingVel - lastRawHeadingVel) > Math.PI) {
            headingVelOffset -= sign(rawHeadingVel) * 2 * Math.PI
        }
        lastRawHeadingVel = rawHeadingVel
        val headingVel = headingVelOffset + rawHeadingVel

        if (!initialized) {
            initialized = true

            lastParPos = parPosVel.position
            lastPerpPos = perpPosVel.position
            lastHeading = heading

            return PoseVelocity2d(Vector2d(0.0, 0.0), 0.0)
        }

        val parPosDelta = parPosVel.position - lastParPos
        val perpPosDelta = perpPosVel.position - lastPerpPos
        val headingDelta = heading.minus(lastHeading)

        val twist =
            Twist2dDual(
                Vector2dDual(
                    DualNum<Time>(
                            doubleArrayOf(
                                parPosDelta - params.parYTicks * headingDelta,
                                parPosVel.velocity!! - params.parYTicks * headingVel,
                            )
                        )
                        .times(inPerTick),
                    DualNum<Time>(
                            doubleArrayOf(
                                perpPosDelta - params.perpXTicks * headingDelta,
                                perpPosVel.velocity!! - params.perpXTicks * headingVel,
                            )
                        )
                        .times(inPerTick),
                ),
                DualNum(doubleArrayOf(headingDelta, headingVel)),
            )

        lastParPos = parPosVel.position
        lastPerpPos = perpPosVel.position
        lastHeading = heading

        pose = pose.plus(twist.value())

        return twist.velocity().value()
    }
}
