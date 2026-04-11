package dev.kingssack.volt.integrations.rr.localizer

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Rotation2d.Companion.exp
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.ftc.toOTOSPose
import com.acmerobotics.roadrunner.ftc.toRRPose
import com.qualcomm.hardware.sparkfun.SparkFunOTOS
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * A [RoadRunnerLocalizer] that uses a SparkFun Optical Odometry Tracking Sensor (OTOS) to localize the robot.
 *
 * @param hardwareMap the hardware map
 * @param pose the initial pose
 * @param params configurable parameters
 * @property otos the tracking sensor
 */
class OTOSLocalizer(
    hardwareMap: HardwareMap,
    override var pose: Pose2d,
    params: LocalizerParams = LocalizerParams(),
) : RoadRunnerLocalizer {
    /**
     * Parameters for [OTOSLocalizer].
     *
     * @property sensorName the name of the OTOS in the hardware map
     * @property angularScalar a scalar to apply to the angular velocity and acceleration readings
     * @property linearScalar a scalar to apply to the linear velocity and acceleration readings
     * @property offset an offset to apply to the pose readings, in the OTOS's coordinate system
     */
    class LocalizerParams(
        var sensorName: String = "sensor_otos",
        var angularScalar: Double = 1.0,
        var linearScalar: Double = 1.0,
        var offset: SparkFunOTOS.Pose2D = SparkFunOTOS.Pose2D(0.0, 0.0, 0.0),
    )

    val otos: SparkFunOTOS = hardwareMap.get(SparkFunOTOS::class.java, params.sensorName)

    init {
        otos.position = pose.toOTOSPose()
        otos.setLinearUnit(DistanceUnit.INCH)
        otos.setAngularUnit(AngleUnit.RADIANS)

        otos.calibrateImu()
        otos.setLinearScalar(params.linearScalar)
        otos.setAngularScalar(params.angularScalar)
        otos.offset = params.offset
    }

    override fun update(): PoseVelocity2d {
        val otosPose = SparkFunOTOS.Pose2D()
        val otosVel = SparkFunOTOS.Pose2D()
        val otosAcc = SparkFunOTOS.Pose2D()
        otos.getPosVelAcc(otosPose, otosVel, otosAcc)

        pose = otosPose.toRRPose()
        val fieldVel = Vector2d(otosVel.x, otosVel.y)
        val robotVel = exp(otosPose.h).inverse().times(fieldVel)
        return PoseVelocity2d(robotVel, otosVel.h)
    }
}
