package org.firstinspires.ftc.teamcode.autonomous

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

interface Autonomous {
    // Register
    fun registerDrive(hardwareMap: HardwareMap, initialPose: Pose2d)

    // Tick
    fun tick(telemetry: Telemetry)
}