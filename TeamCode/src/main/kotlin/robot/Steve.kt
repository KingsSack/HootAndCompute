package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.SequentialAction
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.attachment.*

/**
 * Steve is a robot that has a lift, claw, shoulder, wrist, IMU, distance sensor, and HuskyLens.
 * Steve can collect samples, deposit samples, detect objects, and detect distances.
 *
 * @param hardwareMap for initializing hardware components
 * @param initialPose for setting the initial pose
 *
 * @property lift for lifting the claw
 * @property claw for grabbing objects
 * @property shoulder for extending the claw
 * @property wrist for rotating the claw
 */
class Steve(hardwareMap: HardwareMap, initialPose: Pose2d) : Robot(hardwareMap, initialPose) {
    // Sensors
    private val lidarLeft: DistanceSensor = hardwareMap.get(DistanceSensor::class.java, "lidarl")
    private val lidarRight: DistanceSensor = hardwareMap.get(DistanceSensor::class.java, "lidarr")
    private val huskyLens: HuskyLens = hardwareMap.get(HuskyLens::class.java, "lens")

    // Attachments
    val lift: Lift = Lift(hardwareMap, "liftr", "liftl")
    val claw: Claw = Claw(hardwareMap, "claw")
    val shoulder: Shoulder = Shoulder(hardwareMap, "sh")
    val wrist: Wrist = Wrist(hardwareMap, "wr")

    init {
        attachments = listOf(lift, claw, shoulder, wrist)

        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION)
    }

    /**
     * Collect a sample by extending the extender, closing the claw,
     * and retracting the extender.
     *
     * @return action to collect a sample
     *
     * @see Shoulder
     * @see Claw
     */
    fun collectSample(): Action {
        return SequentialAction(
            shoulder.extend(),
            claw.close(),
            shoulder.retract()
        )
    }

    /**
     * Deposit a sample by lifting the goTo, extending the extender,
     * opening the claw, retracting the extender, and dropping the goTo.
     *
     * @param basketHeight height of the basket
     * @return action to deposit a sample
     *
     * @see Lift
     * @see Shoulder
     * @see Claw
     */
    fun depositSample(basketHeight: Int): Action {
        return SequentialAction(
            lift.goTo(basketHeight + 20),
            shoulder.extend(),
            claw.open(),
            shoulder.retract(),
            lift.drop()
        )
    }

    /**
     * Get detected objects from HuskyLens.
     *
     * @param telemetry for logging
     * @return array of detected objects
     *
     * @see HuskyLens
     * @see HuskyLens.Block
     */
    fun getDetectedObjects(telemetry: Telemetry): Array<out HuskyLens.Block>? {
        // Get objects
        val blocks = huskyLens.blocks()
        telemetry.addData("Block count", blocks.size)
        for (block in blocks) {
            telemetry.addData("Block", block.toString())
        }
        return blocks
    }

    /**
     * Get distance to an obstacle from distance sensor.
     *
     * @param telemetry for logging
     * @return distance to an obstacle
     *
     * @see DistanceSensor
     */
    fun getDistanceToObstacle(telemetry: Telemetry): Double {
        // Get distances
        val distanceLeft = lidarLeft.getDistance(DistanceUnit.MM)
        val distanceRight = lidarRight.getDistance(DistanceUnit.MM)
        val averageDistance = (distanceLeft + distanceRight) / 2

        telemetry.addData("range left", "%.01f mm".format(distanceLeft))
        telemetry.addData("range right", "%.01f mm".format(distanceRight))
        telemetry.addData("average range", "%.01f mm".format(averageDistance))

        return averageDistance
    }
}