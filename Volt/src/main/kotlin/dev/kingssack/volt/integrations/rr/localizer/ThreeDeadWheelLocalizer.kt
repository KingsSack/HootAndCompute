package dev.kingssack.volt.integrations.rr.localizer

import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.ftc.Encoder
import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * A [RoadRunnerLocalizer] that uses three dead wheels to localize the robot.
 *
 * @param hardwareMap the hardware map
 * @param inPerTick the number of inches per encoder tick
 * @param pose the initial pose
 * @param params configurable parameters
 * @property par0 the first parallel encoder
 * @property par1 the second parallel encoder
 * @property perp the perpendicular encoder
 */
class ThreeDeadWheelLocalizer(
    hardwareMap: HardwareMap,
    private val inPerTick: Double,
    override var pose: Pose2d,
    private val params: LocalizerParams = LocalizerParams(),
) : RoadRunnerLocalizer {
    /**
     * Parameters for [ThreeDeadWheelLocalizer].
     *
     * @property par0Name the name of the first parallel encoder
     * @property par0Direction the direction of the first parallel encoder
     * @property par1Name the name of the second parallel encoder
     * @property par1Direction the direction of the second parallel encoder
     * @property perpName the name of the perpendicular encoder
     * @property perpDirection the direction of the perpendicular encoder
     * @property par0YTicks y position of the first parallel encoder (in tick units)
     * @property par1YTicks y position of the second parallel encoder (in tick units)
     * @property perpXTicks x position of the perpendicular encoder (in tick units)
     */
    class LocalizerParams(
        val par0Name: String = "par0",
        val par0Direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val par1Name: String = "par1",
        val par1Direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val perpName: String = "perp",
        val perpDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val par0YTicks: Double = 0.0,
        val par1YTicks: Double = 1.0,
        val perpXTicks: Double = 0.0,
    )

    val par0: Encoder =
        OverflowEncoder(
            RawEncoder(
                hardwareMap.get(DcMotorEx::class.java, params.par0Name).apply {
                    direction = params.par0Direction
                }
            )
        )
    val par1: Encoder =
        OverflowEncoder(
            RawEncoder(
                hardwareMap.get(DcMotorEx::class.java, params.par1Name).apply {
                    direction = params.par1Direction
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

    private var lastPar0Pos = 0
    private var lastPar1Pos = 0
    private var lastPerpPos = 0
    private var initialized = false

    override fun update(): PoseVelocity2d {
        val par0PosVel = par0.getPositionAndVelocity()
        val par1PosVel = par1.getPositionAndVelocity()
        val perpPosVel = perp.getPositionAndVelocity()

        if (!initialized) {
            initialized = true

            lastPar0Pos = par0PosVel.position
            lastPar1Pos = par1PosVel.position
            lastPerpPos = perpPosVel.position

            return PoseVelocity2d(Vector2d(0.0, 0.0), 0.0)
        }

        val par0PosDelta = par0PosVel.position - lastPar0Pos
        val par1PosDelta = par1PosVel.position - lastPar1Pos
        val perpPosDelta = perpPosVel.position - lastPerpPos

        val twist =
            Twist2dDual(
                Vector2dDual(
                    DualNum<Time>(
                            doubleArrayOf(
                                (params.par0YTicks * par1PosDelta -
                                    params.par1YTicks * par0PosDelta) /
                                    (params.par0YTicks - params.par1YTicks),
                                (params.par0YTicks * par1PosVel.velocity!! -
                                    params.par1YTicks * par0PosVel.velocity!!) /
                                    (params.par0YTicks - params.par1YTicks),
                            )
                        )
                        .times(inPerTick),
                    DualNum<Time>(
                            doubleArrayOf(
                                (params.perpXTicks / (params.par0YTicks - params.par1YTicks) *
                                    (par1PosDelta - par0PosDelta) + perpPosDelta),
                                (params.perpXTicks / (params.par0YTicks - params.par1YTicks) *
                                    (par1PosVel.velocity!! - par0PosVel.velocity!!) +
                                    perpPosVel.velocity!!),
                            )
                        )
                        .times(inPerTick),
                ),
                DualNum(
                    doubleArrayOf(
                        (par0PosDelta - par1PosDelta) / (params.par0YTicks - params.par1YTicks),
                        (par0PosVel.velocity!! - par1PosVel.velocity!!) /
                            (params.par0YTicks - params.par1YTicks),
                    )
                ),
            )

        lastPar0Pos = par0PosVel.position
        lastPar1Pos = par1PosVel.position
        lastPerpPos = perpPosVel.position

        pose = pose.plus(twist.value())

        return twist.velocity().value()
    }
}
