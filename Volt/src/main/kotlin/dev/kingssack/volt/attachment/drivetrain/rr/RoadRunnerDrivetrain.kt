package dev.kingssack.volt.attachment.drivetrain.rr

import com.acmerobotics.roadrunner.*
import com.acmerobotics.roadrunner.ftc.LazyHardwareMapImu
import com.acmerobotics.roadrunner.ftc.throwIfModulesAreOutdated
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.VoltageSensor
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.drivetrain.Drivetrain
import dev.kingssack.volt.util.Degrees
import dev.kingssack.volt.integrations.rr.localizer.RoadRunnerLocalizer
import dev.kingssack.volt.util.Radians
import dev.kingssack.volt.util.toRadians
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.util.*

/**
 * A RoadRunner [dev.kingssack.volt.attachment.drivetrain.Drivetrain].
 *
 * @param hardwareMap the hardware map
 * @param orientation the control hub's orientation
 * @param T the type of kinematics to use
 * @property voltageSensor the voltage sensor
 * @property lazyImu the lazy IMU
 * @property localizer the localizer
 */
abstract class RoadRunnerDrivetrain<T : Any>(
    hardwareMap: HardwareMap,
    orientation: RevHubOrientationOnRobot,
) : Drivetrain() {
    protected abstract val kinematics: T

    protected abstract val defaultTurnConstraints: TurnConstraints
    protected abstract val defaultVelConstraint: VelConstraint
    protected abstract val defaultAccelConstraint: AccelConstraint

    val voltageSensor: VoltageSensor = hardwareMap.voltageSensor.iterator().next()

    val lazyImu = LazyHardwareMapImu(hardwareMap, "imu", orientation)

    abstract val localizer: RoadRunnerLocalizer

    private val poseHistory = LinkedList<Pose2d>()

    init {
        throwIfModulesAreOutdated(hardwareMap)

        for (module in hardwareMap.getAll(LynxModule::class.java)) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO)
        }
    }

    private fun updatePoseEstimate(): PoseVelocity2d {
        val vel = localizer.update()
        poseHistory.add(localizer.pose)

        while (poseHistory.size > 100) {
            poseHistory.removeFirst()
        }

        return vel
    }

    /**
     * Create a new trajectory action builder.
     *
     * @param beginPose the current pose of the robot
     * @return the action builder
     */
    abstract fun driveActionBuilder(beginPose: Pose2d): TrajectoryActionBuilder

    /**
     * Strafe from [from] to [to].
     *
     * @return the action
     */
    @VoltAction(name = "Strafe To", "Strafe the robot to a certain position")
    fun strafeTo(from: Pose2d, to: Vector2d): Action = driveActionBuilder(from).strafeTo(to).build()

    /**
     * Strafe from [from] to [to].
     *
     * @return the action
     */
    @VoltAction(name = "Strafe To Linear Heading", "Strafe the robot to a certain pose")
    fun strafeToLinearHeading(from: Pose2d, to: Pose2d): Action =
        driveActionBuilder(from).strafeToLinearHeading(to.position, to.heading).build()

    /**
     * Spline from [from] to [to] with [tangent].
     *
     * @return the action
     */
    @VoltAction(name = "Spline To", "Follow a spline to a certain pose")
    fun splineTo(from: Pose2d, to: Vector2d, tangent: Rotation2d): Action =
        driveActionBuilder(from).splineTo(to, tangent).build()

    /**
     * Spline from [from] to [to] with [tangent].
     *
     * @return the action
     */
    @VoltAction(name = "Spline To Linear Heading", "Follow a spline to a certain pose")
    fun splineToLinearHeading(from: Pose2d, to: Pose2d, tangent: Rotation2d): Action =
        driveActionBuilder(from).splineToLinearHeading(to, tangent).build()

    /**
     * Turn from [from] to [to].
     *
     * @return the action
     */
    @VoltAction(name = "Turn To Radians", "Turn the robot to a certain heading in radians")
    fun turnTo(from: Pose2d, to: Radians): Action =
        driveActionBuilder(from).turnTo(to.value).build()

    /**
     * Turn from [from] to [to].
     *
     * @return the action
     */
    @VoltAction(name = "Turn To Degrees", "Turn the robot to a certain heading in degrees")
    fun turnTo(from: Pose2d, to: Degrees): Action =
        driveActionBuilder(from).turnTo(to.toRadians().value).build()

    /**
     * Turn a certain number of [radians].
     *
     * @param from the starting pose
     * @return the action
     */
    @VoltAction(name = "Turn Radians", "Turn the robot a certain number of radians")
    fun turn(from: Pose2d, radians: Radians): Action =
        driveActionBuilder(from).turn(radians.value).build()

    /**
     * Turn a certain number of [degrees].
     *
     * @param from the starting pose
     * @return the action
     */
    @VoltAction(name = "Turn Degrees", "Turn the robot a certain number of degrees")
    fun turn(from: Pose2d, degrees: Degrees): Action =
        driveActionBuilder(from).turn(degrees.toRadians().value).build()

    /**
     * Build a trajectory action.
     *
     * @param from the starting pose
     * @return the built action
     */
    fun trajectory(
        from: Pose2d = localizer.pose,
        block: TrajectoryActionBuilder.() -> TrajectoryActionBuilder,
    ): Action {
        return driveActionBuilder(from).block().build()
    }

    context(telemetry: Telemetry)
    override fun update() {
        updatePoseEstimate()

        super.update()
        with(telemetry) {
            addData("x", localizer.pose.position.x)
            addData("y", localizer.pose.position.y)
            addData("heading (deg)", Math.toDegrees(localizer.pose.heading.toDouble()))
        }
    }
}
