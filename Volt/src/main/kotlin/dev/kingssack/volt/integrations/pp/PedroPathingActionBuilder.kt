package dev.kingssack.volt.integrations.pp

import com.acmerobotics.roadrunner.Action
import com.pedropathing.follower.Follower
import com.pedropathing.geometry.BezierCurve
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose

@DslMarker annotation class PedroPathingActionBuilderDsl

/**
 * Builder that creates an Action from PedroPathing Paths.
 *
 * @param follower the follower used to build the path and follow it when the action is executed
 * @param startPose the starting pose of the path
 */
@PedroPathingActionBuilderDsl
class PedroPathingActionBuilder(private val follower: Follower, startPose: Pose) {
    private val builder = follower.pathBuilder()
    private var lastPose = startPose

    /**
     * Adds a linear segment to the path from the [lastPose] to the [endPose]. The heading will be
     * interpolated linearly between the start and end poses.
     */
    fun lineTo(endPose: Pose): PedroPathingActionBuilder {
        builder.addPath(BezierLine(lastPose, endPose))
        builder.setLinearHeadingInterpolation(lastPose.heading, endPose.heading)
        lastPose = endPose
        return this
    }

    /**
     * Adds a linear segment to the path from the [lastPose] to the [endPose]. The heading will be
     * held constant at the [lastPose]'s heading throughout the segment, regardless of the direction
     * of travel.
     */
    fun lineToConstantHeading(endPose: Pose): PedroPathingActionBuilder {
        builder.addPath(BezierLine(lastPose, endPose))
        builder.setConstantHeadingInterpolation(lastPose.heading)
        lastPose = endPose
        return this
    }

    /**
     * Adds a linear segment to the path from the [lastPose] the the [endPose]. The heading will be
     * interpolated tangent to the direction of travel along the segment, meaning the robot will
     * always face the direction it is moving in.
     */
    fun lineToTangentHeading(endPose: Pose): PedroPathingActionBuilder {
        builder.addPath(BezierLine(lastPose, endPose))
        builder.setTangentHeadingInterpolation()
        lastPose = endPose
        return this
    }

    /**
     * Adds a bézier curve segment to the path defined by the [lastPose], [controlPoses], and the
     * [endPose]. The heading will be interpolated linearly between the start and end poses.
     */
    fun splineTo(endPose: Pose, vararg controlPoses: Pose): PedroPathingActionBuilder {
        builder.addPath(BezierCurve(lastPose, *controlPoses, endPose))
        builder.setLinearHeadingInterpolation(lastPose.heading, endPose.heading)
        lastPose = endPose
        return this
    }

    /**
     * Adds a bézier curve to the path defined by the [lastPose], [controlPoses], and the [endPose].
     * The heading will be held constant at the [lastPose]'s heading throughout the segment,
     * regardless of the direction of travel.
     */
    fun splineToConstantHeading(
        endPose: Pose,
        vararg controlPoses: Pose,
    ): PedroPathingActionBuilder {
        builder.addPath(BezierCurve(lastPose, *controlPoses, endPose))
        builder.setConstantHeadingInterpolation(lastPose.heading)
        lastPose = endPose
        return this
    }

    /**
     * Adds a bézier curve to the path defined by the [lastPose], [controlPoses], and the [endPose].
     * The heading will be interpolated tangent to the direction of travel along the segment,
     * meaning the robot will always face the direction it is moving in.
     */
    fun splineToTangentHeading(
        endPose: Pose,
        vararg controlPoses: Pose,
    ): PedroPathingActionBuilder {
        builder.addPath(BezierCurve(lastPose, *controlPoses, endPose))
        builder.setTangentHeadingInterpolation()
        lastPose = endPose
        return this
    }

    internal fun build(): Action = Action {
        if (!follower.isBusy) follower.followPath(builder.build(), true)
        follower.update()
        follower.isBusy
    }
}
