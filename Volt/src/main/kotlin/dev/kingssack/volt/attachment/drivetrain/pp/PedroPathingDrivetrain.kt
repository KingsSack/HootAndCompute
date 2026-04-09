package dev.kingssack.volt.attachment.drivetrain.pp

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.BezierCurve
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.PathBuilder
import com.pedropathing.paths.PathChain
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.drivetrain.Drivetrain
import dev.kingssack.volt.integrations.pp.PedroPathingActionBuilder
import dev.kingssack.volt.util.Degrees
import dev.kingssack.volt.util.Radians
import dev.kingssack.volt.util.toRadians
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * A PedroPathing [dev.kingssack.volt.attachment.drivetrain.Drivetrain].
 *
 * A pre-built [dev.kingssack.volt.attachment.drivetrain.Drivetrain] that integrates with the
 * PedroPathing library. Provides actions for autonomous pathing as well as teleop control. [lineTo]
 * and [splineTo] can be used for following single paths, while [path] and [followPath] can follow
 * multi-path chains.
 *
 * @param follower the path follower instance
 * @param initialPose the robot's initial pose
 * @property pose the robot's current pose
 */
abstract class PedroPathingDrivetrain(
    protected val follower: Follower,
    initialPose: Pose = Pose(),
) : Drivetrain() {
    val pose: Pose
        get() = follower.pose

    init {
        follower.setStartingPose(initialPose)
        follower.update()
    }

    fun startTeleOpDrive() {
        follower.startTeleOpDrive()
    }

    override fun setDrivePowers(powers: PoseVelocity2d) {
        follower.setTeleOpDrive(powers.linearVel.x, powers.linearVel.y, powers.angVel)
    }

    /**
     * Follows the given path using PedroPathing.
     *
     * @param pathChain The path chain that defines the sequence of poses and waypoints to follow
     * @return An action that executes the path following behavior
     */
    @VoltAction(name = "Follow Path", description = "Follows the given path using PedroPathing")
    fun pathTo(pathChain: PathChain): Action = Action {
        if (!follower.isBusy) follower.followPath(pathChain, true)
        follower.update()
        follower.isBusy
    }

    /**
     * Follows the given [line].
     *
     * @return the action
     */
    @VoltAction(name = "Line To", description = "Follows the given line")
    fun lineTo(line: BezierLine): Action = pathTo(follower.pathBuilder().addPath(line).build())

    /**
     * Follows the given [line] while linearly interpolating the heading from [initial] to [final].
     *
     * @return the action
     */
    @VoltAction(
        name = "Line To Linear Heading",
        description = "Follows the given line while linearly interpolating the heading in radians",
    )
    fun lineToLinearHeading(line: BezierLine, initial: Radians, final: Radians): Action =
        pathTo(
            follower
                .pathBuilder()
                .addPath(line)
                .setLinearHeadingInterpolation(initial.value, final.value)
                .build()
        )

    /**
     * Follows the given [line] while linearly interpolating the heading from [initial] to [final].
     *
     * @return the action
     */
    @VoltAction(
        name = "Line To Linear Heading",
        description = "Follows the given line while linearly interpolating the heading in degrees",
    )
    fun lineToLinearHeading(line: BezierLine, initial: Degrees, final: Degrees): Action =
        pathTo(
            follower
                .pathBuilder()
                .addPath(line)
                .setLinearHeadingInterpolation(initial.toRadians().value, final.toRadians().value)
                .build()
        )

    /**
     * Follow the given [line] with a constant [heading].
     *
     * @return the action
     */
    @VoltAction(
        name = "Line To Constant Heading",
        description = "Follows the given line with a constant heading in radians",
    )
    fun lineToConstantHeading(line: BezierLine, heading: Radians): Action =
        pathTo(
            follower
                .pathBuilder()
                .addPath(line)
                .setConstantHeadingInterpolation(heading.value)
                .build()
        )

    /**
     * Follow the given [line] with a constant [heading].
     *
     * @return the action
     */
    @VoltAction(
        name = "Line To Constant Heading",
        description = "Follows the given line with a constant heading in degrees",
    )
    fun lineToConstantHeading(line: BezierLine, heading: Degrees): Action =
        pathTo(
            follower
                .pathBuilder()
                .addPath(line)
                .setConstantHeadingInterpolation(heading.toRadians().value)
                .build()
        )

    /**
     * Follow the given [line] while tangentially interpolating the heading.
     *
     * @return the action
     */
    @VoltAction(
        name = "Line To Tangential Heading",
        description = "Follows the given line while tangentially interpolating the heading",
    )
    fun lineToTangentHeading(line: BezierLine): Action =
        pathTo(follower.pathBuilder().addPath(line).setTangentHeadingInterpolation().build())

    /**
     * Follows the given [curve].
     *
     * @return the action
     */
    @VoltAction(name = "Spline To", description = "Follows the given curve")
    fun splineTo(curve: BezierCurve): Action = pathTo(follower.pathBuilder().addPath(curve).build())

    /**
     * Follow the given [curve] while linearly interpolating the heading from [initial] to [final].
     *
     * @return the action
     */
    @VoltAction(
        name = "Spline To Linear Heading",
        description = "Follows the given curve while linearly interpolating the heading in radians",
    )
    fun splineToLinearHeading(curve: BezierCurve, initial: Radians, final: Radians): Action =
        pathTo(
            follower
                .pathBuilder()
                .addPath(curve)
                .setLinearHeadingInterpolation(initial.value, final.value)
                .build()
        )

    /**
     * Follow the given [curve] while linearly interpolating the heading from [initial] to [final].
     *
     * @return the action
     */
    @VoltAction(
        name = "Spline To Linear Heading",
        description = "Follows the given curve while linearly interpolating the heading in degrees",
    )
    fun splineToLinearHeading(curve: BezierCurve, initial: Degrees, final: Degrees): Action =
        pathTo(
            follower
                .pathBuilder()
                .addPath(curve)
                .setLinearHeadingInterpolation(initial.toRadians().value, final.toRadians().value)
                .build()
        )

    /**
     * Follow the given [curve] with a constant [heading].
     *
     * @return the action
     */
    @VoltAction(
        name = "Spline To Constant Heading",
        description = "Follows the given curve with a constant heading in radians",
    )
    fun splineToConstantHeading(curve: BezierCurve, heading: Radians): Action =
        pathTo(
            follower
                .pathBuilder()
                .addPath(curve)
                .setConstantHeadingInterpolation(heading.value)
                .build()
        )

    /**
     * Follow the given [curve] with a constant [heading].
     *
     * @return the action
     */
    @VoltAction(
        name = "Spline To Constant Heading",
        description = "Follows the given curve with a constant heading in degrees",
    )
    fun splineToConstantHeading(curve: BezierCurve, heading: Degrees): Action =
        pathTo(
            follower
                .pathBuilder()
                .addPath(curve)
                .setConstantHeadingInterpolation(heading.toRadians().value)
                .build()
        )

    /**
     * Follow the given [curve] while tangentially interpolating the heading.
     *
     * @return the action
     */
    fun splineToTangentHeading(curve: BezierCurve): Action =
        pathTo(follower.pathBuilder().addPath(curve).setTangentHeadingInterpolation().build())

    /**
     * Creates a path following action starting from [startPose].
     *
     * @return an action that executes a defined path following behavior
     */
    fun path(
        startPose: Pose = pose,
        block: PedroPathingActionBuilder.() -> PedroPathingActionBuilder,
    ): Action {
        return PedroPathingActionBuilder(follower, startPose).block().build()
    }

    /**
     * Creates a path following action from a PathBuilder.
     *
     * @return an action that follows a path
     */
    fun followPath(block: PathBuilder.() -> Unit): Action {
        return pathTo(follower.pathBuilder().apply(block).build())
    }

    context(telemetry: Telemetry)
    override fun update() {
        follower.update()

        super.update()
        with(telemetry) {
            addData("x", follower.pose.x)
            addData("y", follower.pose.y)
            addData("heading (deg)", Math.toDegrees(follower.pose.heading))
        }
    }
}
