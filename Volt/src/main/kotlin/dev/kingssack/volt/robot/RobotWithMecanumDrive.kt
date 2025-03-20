package dev.kingssack.volt.robot

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.localization.Encoder
import com.pedropathing.localization.Localizers
import com.pedropathing.localization.Pose
import com.pedropathing.localization.constants.DriveEncoderConstants
import com.pedropathing.pathgen.BezierLine
import com.pedropathing.pathgen.Path
import com.pedropathing.pathgen.Point
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap

class RobotWithMecanumDrive(
    hardwareMap: HardwareMap,
    var pose: Pose,
    private val params: DriveParams = DriveParams()
) : Robot() {
    class DriveParams(
        val localizers: Localizers = Localizers.DRIVE_ENCODERS,
        val mass: Double = 10.0,

        val leftFrontMotorName: String = "leftFront",
        val leftRearMotorName: String = "leftRear",
        val rightFrontMotorName: String = "rightFront",
        val rightRearMotorName: String = "rightRear",

        val leftFrontMotorDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.REVERSE,
        val leftRearMotorDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.REVERSE,
        val rightFrontMotorDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,
        val rightRearMotorDirection: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD,

        val forwardTicksToInches: Double = 1.0,
        val strafeTicksToInches: Double = 1.0,
        val turnTicksToInches: Double = 1.0,

        val robot_Width: Double = 1.0,
        val robot_Length: Double = 1.0,

        val leftFrontEncoderDirection: Double = Encoder.REVERSE,
        val rightFrontEncoderDirection: Double = Encoder.REVERSE,
        val leftRearEncoderDirection: Double = Encoder.FORWARD,
        val rightRearEncoderDirection: Double = Encoder.FORWARD
    )

    init {
        FollowerConstants.localizers = params.localizers
        FollowerConstants.mass = params.mass

        FollowerConstants.leftFrontMotorName = params.leftFrontMotorName
        FollowerConstants.leftRearMotorName = params.leftRearMotorName
        FollowerConstants.rightFrontMotorName = params.rightFrontMotorName
        FollowerConstants.rightRearMotorName = params.rightRearMotorName

        FollowerConstants.leftFrontMotorDirection = params.leftFrontMotorDirection
        FollowerConstants.leftRearMotorDirection = params.leftRearMotorDirection
        FollowerConstants.rightFrontMotorDirection = params.rightFrontMotorDirection
        FollowerConstants.rightRearMotorDirection = params.rightRearMotorDirection

        DriveEncoderConstants.forwardTicksToInches = params.forwardTicksToInches
        DriveEncoderConstants.strafeTicksToInches = params.strafeTicksToInches
        DriveEncoderConstants.turnTicksToInches = params.turnTicksToInches

        DriveEncoderConstants.robot_Width = params.robot_Width
        DriveEncoderConstants.robot_Length = params.robot_Length

        DriveEncoderConstants.leftFrontEncoderDirection = params.leftFrontEncoderDirection
        DriveEncoderConstants.rightFrontEncoderDirection = params.rightFrontEncoderDirection
        DriveEncoderConstants.leftRearEncoderDirection = params.leftRearEncoderDirection
        DriveEncoderConstants.rightRearEncoderDirection = params.rightRearEncoderDirection
    }

    private val follower = Follower(hardwareMap)

    inner class PathToAction(private val path: Path) : Action {
        override fun run(p: TelemetryPacket): Boolean {
            if (!follower.isBusy) {
                follower.followPath(path, true)
            }
            pose = follower.pose
            return false
        }
    }

    fun pathTo(pose: Pose): Action {
        return PathToAction(Path(BezierLine(Point(this.pose.x, this.pose.y), Point(pose.x, pose.y))))
    }
}