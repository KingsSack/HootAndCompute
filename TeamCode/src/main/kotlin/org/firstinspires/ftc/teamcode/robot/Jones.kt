package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.hardware.*
import dev.kingssack.volt.attachment.drivetrain.MecanumDrivetrain
import dev.kingssack.volt.core.voltAction
import dev.kingssack.volt.robot.RobotWithMecanumDrivetrain
import java.util.concurrent.TimeUnit
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.attachment.Classifier
import org.firstinspires.ftc.teamcode.attachment.Launcher
import org.firstinspires.ftc.teamcode.attachment.Pusher
import org.firstinspires.ftc.teamcode.util.AprilTagAiming
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor

/**
 * Jones is a robot for the 2025-2026 DECODE FTC Season.
 *
 * @param hardwareMap for initializing hardware components
 * @param T the type of mecanum drivetrain
 * @property drivetrain the mecanum drivetrain used by the robot
 * @property visionPortal the vision portal for processing camera input
 * @property launcher the launcher attachment for firing artifacts
 * @property classifier the classifier attachment for classifying and releasing artifacts
 * @property pusher the pusher attachment for pushing artifacts into the launcher
 * @property aprilTagAiming the AprilTag aiming utility for aligning with targets using AprilTags
 * @see MecanumDrivetrain
 */
@Config
abstract class Jones<T : MecanumDrivetrain>(hardwareMap: HardwareMap, override val drivetrain: T) :
    RobotWithMecanumDrivetrain<T>(hardwareMap, drivetrain) {
    companion object {
        @JvmField var launcherLeftP: Double = 90.0
        @JvmField var launcherLeftI: Double = 0.0
        @JvmField var launcherLeftD: Double = 0.1
        @JvmField var launcherLeftF: Double = 13.9
        @JvmField var launcherRightP: Double = 90.0
        @JvmField var launcherRightI: Double = 0.0
        @JvmField var launcherRightD: Double = 0.1
        @JvmField var launcherRightF: Double = 13.2
        @JvmField var launcherMaxVelocity: Double = 2800.0
        @JvmField var launcherTargetVelocity: Double = 1340.0
        @JvmField var launcherMediumVelocity: Double = 1300.0
        @JvmField var launcherLowVelocity: Double = 1240.0
        @JvmField var exposureMs: Int = 6
        @JvmField var gain: Int = 230
    }

    // --- Hardware ---

    private val rgb by ledDriver("rgb")

    private val lidarLeft by distanceSensor("lsl")
    private val lidarRight by distanceSensor("lsr")

    private val gateServo by servo("gs")
    private val classifierServo by servo("cs")
    private val classifierSensor1 by colorSensor("cs1")
    private val classifierSensor2 by colorSensor("cs2")
    private val classifierSensor3 by colorSensor("cs3")

    private val pusherServo by servo("ps")

    private val leftLauncherMotor by motorEx("fll")
    private val rightLauncherMotor by motorEx("flr")

    // --- Attachments ---

    val launcher = attachment {
        Launcher(
            leftLauncherMotor,
            rightLauncherMotor,
            lidarLeft,
            PIDFCoefficients(launcherLeftP, launcherLeftI, launcherLeftD, launcherLeftF),
            PIDFCoefficients(launcherRightP, launcherRightI, launcherRightD, launcherRightF),
            launcherMaxVelocity,
            launcherTargetVelocity,
        )
    }

    val classifier = attachment {
        Classifier(
            classifierSensor1,
            classifierSensor2,
            classifierSensor3,
            gateServo,
            classifierServo,
            rgb,
        )
    }

    val pusher = attachment { Pusher(pusherServo) }

    // --- AprilTag Detection ---

    private val aprilTag: AprilTagProcessor = AprilTagProcessor.easyCreateWithDefaults()
    val visionPortal: VisionPortal =
        VisionPortal.easyCreateWithDefaults(
            hardwareMap.get(WebcamName::class.java, "Webcam 1"),
            aprilTag,
        )

    val aprilTagAiming = AprilTagAiming()

    init {
        rgb.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED)

        try {
            while (
                visionPortal != null &&
                    visionPortal.cameraState != VisionPortal.CameraState.STREAMING
            ) {
                Thread.sleep(100)
            }

            val exposureControl = visionPortal.getCameraControl(ExposureControl::class.java)
            val gainControl = visionPortal.getCameraControl(GainControl::class.java)

            if (exposureControl.mode != ExposureControl.Mode.Manual)
                exposureControl.mode = ExposureControl.Mode.Manual
            exposureControl.setExposure(exposureMs.toLong(), TimeUnit.MILLISECONDS)
            gainControl.gain = gain
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    // --- Actions ---

    /**
     * Fire all stored artifacts using the [launcher] and [classifier] at [targetVelocity].
     *
     * @return an [Action] that fires all stored artifacts
     */
    fun fireAllStoredArtifacts(targetVelocity: Double = launcherTargetVelocity) = voltAction {
        +launcher.enable(targetVelocity)
        +classifier.releaseAllArtifacts()
        +launcher.disable()
    }

    // --- Helpers ---

    /**
     * Get detected AprilTags from the webcam using the [aprilTag] processor.
     *
     * @param id optional ID to filter detected tags; if null, returns all detected tags
     * @return list of detected AprilTags
     */
    context(telemetry: Telemetry)
    fun getDetectedAprilTags(id: Int? = null): List<AprilTagDetection> {
        val detections = aprilTag.detections
        telemetry.addData("Detected Tags", detections.size)

        val result =
            if (id == null) {
                detections
            } else {
                detections.filter { it.id == id }
            }

        for (detection in detections) {
            telemetry.addData("Detected ID", detection.id)
        }

        return result
    }

    /**
     * Get distance to an obstacle from the distance sensor.
     *
     * @return distance to an obstacle
     * @see DistanceSensor
     */
    context(telemetry: Telemetry)
    fun getDistanceToObstacle(): Double {
        // Get distances
        val distanceLeft = lidarLeft.getDistance(DistanceUnit.MM)
        val distanceRight = lidarRight.getDistance(DistanceUnit.MM)
        val averageDistance = (distanceLeft + distanceRight) / 2

        with(telemetry) {
            addData("Left Range", "%.01f mm".format(distanceLeft))
            addData("Right Range", "%.01f mm".format(distanceRight))
            addData("Average Range", "%.01f mm".format(averageDistance))
        }

        return averageDistance
    }
}
