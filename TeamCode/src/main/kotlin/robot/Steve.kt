package org.firstinspires.ftc.teamcode.robot

import org.firstinspires.ftc.teamcode.attachment.Claw
import org.firstinspires.ftc.teamcode.attachment.Extender
import org.firstinspires.ftc.teamcode.attachment.Lift
import com.qualcomm.hardware.dfrobot.HuskyLens
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.util.Controller

class Steve(hardwareMap: HardwareMap) : Robot {
    // Control
    override val control = Controller()

    // Sensors
    private lateinit var imu: IMU
    private lateinit var distanceSensor: DistanceSensor
    private lateinit var huskyLens: HuskyLens

    // Attachments
    lateinit var lift: Lift
    lateinit var claw: Claw
    lateinit var extender: Extender

    // Drive parameters
    // private val countsPerMotorRev: Double = 560.0 // Encoder counts per motor revolution
    // private val driveGearReduction: Double = 1.0  // Gear reduction from external gears
    // private val wheelDiameterMM: Double = 96.0    // Wheel diameter in mm
    // private val wheelBaseWidthMM: Double = 460.0  // Wheelbase width in mm

    init {
        // Register hardware
        registerSensors(hardwareMap)
        registerAttachments(hardwareMap)

        // Reset IMU
        imu.resetYaw()

        // Set huskylens mode
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.OBJECT_RECOGNITION)
    }

    override fun registerSensors(hardwareMap: HardwareMap) {
        // Register sensors
        imu = hardwareMap.get(IMU::class.java, "imu")
        distanceSensor = hardwareMap.get(DistanceSensor::class.java, "lidar")
        huskyLens = hardwareMap.get(HuskyLens::class.java, "lens")
    }

    override fun registerAttachments(hardwareMap: HardwareMap) {
        // Register attachments
        lift = Lift(hardwareMap, "liftr", "liftl")
        claw = Claw(hardwareMap, "claw")
        extender = Extender(hardwareMap, "extend")
    }

    fun getDetectedObjects(telemetry: Telemetry): Array<out HuskyLens.Block>? {
        // Get objects
        val blocks = huskyLens.blocks()
        telemetry.addData("Block count", blocks.size)
        for (block in blocks) {
            telemetry.addData("Block", block.toString())
        }
        return blocks
    }

    fun getDistanceToObstacle(telemetry: Telemetry): Double {
        // Get distance
        val distance = distanceSensor.getDistance(DistanceUnit.MM)
        telemetry.addData("range", "%.01f mm".format(distance))
        return distance
    }
}