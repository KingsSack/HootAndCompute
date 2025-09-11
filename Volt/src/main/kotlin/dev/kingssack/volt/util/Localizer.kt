package dev.kingssack.volt.util

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d


interface Localizer {
    var pose: Pose2d

    fun update(): PoseVelocity2d
}