package dev.kingssack.volt.integrations.rr.localizer

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d

interface RoadRunnerLocalizer {
    var pose: Pose2d

    fun update(): PoseVelocity2d
}
