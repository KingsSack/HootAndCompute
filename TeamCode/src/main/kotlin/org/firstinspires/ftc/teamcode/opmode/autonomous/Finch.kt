package org.firstinspires.ftc.teamcode.opmode.autonomous

import com.pedropathing.geometry.Pose
import dev.kingssack.volt.opmode.VoltOpModeMeta
import dev.kingssack.volt.opmode.autonomous.MultiDualAutonomousMode
import dev.kingssack.volt.util.Event.AutonomousEvent.Start
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.teamcode.robot.GabePP
import org.firstinspires.ftc.teamcode.util.toRadians

@Suppress("unused")
@VoltOpModeMeta("Finch", OpModeMeta.DefaultGroup, "Manatee")
class Finch() :
    MultiDualAutonomousMode<GabePP, FinchStartingPosition>() {
    override val robot: GabePP = GabePP(hardwareMap, sw(if (type == FinchStartingPosition.WALL) Pose(56.0, 9.0, 90.0.toRadians()) else Pose(26.0, 133.0, 142.0.toRadians())))
    private var launchPose: Pose = sw(when (type) {
        FinchStartingPosition.WALL -> Pose(64.0, 100.0, 140.0.toRadians())
        FinchStartingPosition.GOAL -> Pose(64.0, 125.0, 148.0.toRadians())
    })

    init {
        blackboard["allianceColor"] = color
    }

    override fun defineEvents() {
        // Drives to the launch zone and fires preloaded artifacts
        Start then
            {
                +robot.drivetrain.path { lineTo(launchPose) }
                +robot.fire(3)
                instant { blackboard["endPose"] = robot.drivetrain.pose }
            }
    }
}
enum class FinchStartingPosition {
    WALL,
    GOAL
}
