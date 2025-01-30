package org.firstinspires.ftc.teamcode.rr

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.config.reflection.ReflectionConfig
import com.acmerobotics.dashboard.config.variable.CustomVariable
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.MotorFeedforward
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.PoseVelocity2d
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.ftc.*
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.*
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import dev.kingssack.volt.util.Drawing.drawRobot
import dev.kingssack.volt.robot.SimpleRobotWithMecanumDrive
import org.firstinspires.ftc.teamcode.robot.Steve
import java.util.*


@Config
@TeleOp(name = "RoadRunner - Test", group = "org/firstinspires/ftc/teamcode/rrg/firstinspires/ftc/teamcode/rr")
class RoadRunnerTest : LinearOpMode() {
    companion object Config {
        @JvmField
        var initialX: Double = 24.0
        @JvmField
        var initialY: Double = 66.0
        @JvmField
        var initialHeading: Double = -90.0
    }

    private lateinit var robot: SimpleRobotWithMecanumDrive

    override fun runOpMode() {
        registerDrive(hardwareMap, Pose2d(Vector2d(initialX, initialY), initialHeading))

        waitForStart()

        while (opModeIsActive()) {
            robot.setDrivePowers(
                PoseVelocity2d(
                    Vector2d(
                        -gamepad1.left_stick_y.toDouble(),
                        -gamepad1.left_stick_x.toDouble()
                    ),
                    -gamepad1.right_stick_x.toDouble()
                )
            )

            robot.update(telemetry)

            telemetry.addData("x", robot.pose.position.x)
            telemetry.addData("y", robot.pose.position.y)
            telemetry.addData("heading (deg)", Math.toDegrees(robot.pose.heading.toDouble()))
            telemetry.update()

            val packet = TelemetryPacket()
            packet.fieldOverlay().setStroke("#3F51B5")
            drawRobot(packet.fieldOverlay(), robot.pose)
            FtcDashboard.getInstance().sendTelemetryPacket(packet)
        }
    }

    private fun registerDrive(hardwareMap: HardwareMap, initialPose: Pose2d) {
        robot = SimpleRobotWithMecanumDrive(hardwareMap, initialPose)
    }
}

@Config
@TeleOp(name = "RoadRunner - Tuning", group = "org/firstinspires/ftc/teamcode/rrg/firstinspires/ftc/teamcode/rr")
class RoadRunnerTuning : LinearOpMode() {
    companion object Config {
        @JvmField
        var initialX: Double = 0.0
        @JvmField
        var initialY: Double = 0.0
        @JvmField
        var initialHeading: Double = 0.0
        @JvmField
        var distance: Double = 32.0
    }

    private lateinit var robot: SimpleRobotWithMecanumDrive

    override fun runOpMode() {
        robot = SimpleRobotWithMecanumDrive(hardwareMap, Pose2d(initialX, initialY, initialHeading))

        waitForStart()

        while (opModeIsActive()) {
            runBlocking(
                robot.driveActionBuilder(Pose2d(initialX, initialY, initialHeading))
                    .lineToX(initialX + distance)
                    .lineToX(initialX)
                    .build())
        }
    }
}

@Config
@TeleOp(name = "RoadRunner - Spline Test", group = "org/firstinspires/ftc/teamcode/rrg/firstinspires/ftc/teamcode/rr")
class RoadRunnerSplineTest : LinearOpMode() {
    companion object Config {
        @JvmField
        var initialX: Double = 0.0
        @JvmField
        var initialY: Double = 0.0
        @JvmField
        var initialHeading: Double = 0.0
    }

    private lateinit var robot: SimpleRobotWithMecanumDrive

    override fun runOpMode() {
        val beginPose = Pose2d(initialX, initialY, initialHeading)
        robot = SimpleRobotWithMecanumDrive(hardwareMap, beginPose)

        waitForStart()

        runBlocking(
            robot.driveActionBuilder(beginPose)
                .splineTo(Vector2d(30.0, 30.0), Math.PI / 2)
                .splineTo(Vector2d(0.0, 60.0), Math.PI)
                .build()
        )
    }
}

class TuningOpModes {
    companion object {
        private val GROUP: String = "org/firstinspires/ftc/teamcode/rrg/firstinspires/ftc/teamcode/rr"

        private fun metaForClass(cls: Class<out OpMode?>): OpModeMeta {
            return OpModeMeta.Builder()
                .setName("RoadRunner - ${cls.simpleName}")
                .setGroup(GROUP)
                .setFlavor(OpModeMeta.Flavor.TELEOP)
                .build()
        }

        @JvmStatic
        @OpModeRegistrar
        fun register(manager: OpModeManager) {
            val dvf: DriveViewFactory = object : DriveViewFactory {
                override fun make(h: HardwareMap): DriveView {
                    val md = SimpleRobotWithMecanumDrive(h, Pose2d(0.0, 0.0, 0.0))

                    val leftEncs = ArrayList<Encoder>()
                    val rightEncs = ArrayList<Encoder>()
                    val parEncs = ArrayList<Encoder>()
                    val perpEncs = ArrayList<Encoder>()
                    val localizer = md.localizer
                    leftEncs.add(localizer.leftFrontEncoder)
                    leftEncs.add(localizer.leftBackEncoder)
                    rightEncs.add(localizer.rightFrontEncoder)
                    rightEncs.add(localizer.rightBackEncoder)

                    return DriveView(
                        DriveType.MECANUM,
                        Steve.inPerTick,
                        Steve.maxWheelVel,
                        Steve.minProfileAccel,
                        Steve.maxProfileAccel,
                        h.getAll(LynxModule::class.java),
                        listOf(md.leftFront, md.leftBack),
                        listOf(md.rightFront, md.rightBack),
                        leftEncs, rightEncs, parEncs, perpEncs,
                        md.lazyImu, md.voltageSensor
                    ) {
                        MotorFeedforward(
                            Steve.kS,
                            Steve.kV / Steve.inPerTick,
                            Steve.kA / Steve.inPerTick
                        )
                    }
                }
            }

            manager.register(metaForClass(AngularRampLogger::class.java), AngularRampLogger(dvf))
            manager.register(metaForClass(ForwardPushTest::class.java), ForwardPushTest(dvf))
            manager.register(metaForClass(ForwardRampLogger::class.java), ForwardRampLogger(dvf))
            manager.register(metaForClass(LateralPushTest::class.java), LateralPushTest(dvf))
            manager.register(metaForClass(LateralRampLogger::class.java), LateralRampLogger(dvf))
            manager.register(metaForClass(ManualFeedforwardTuner::class.java), ManualFeedforwardTuner(dvf))
            manager.register(
                metaForClass(MecanumMotorDirectionDebugger::class.java),
                MecanumMotorDirectionDebugger(dvf)
            )
            manager.register(metaForClass(DeadWheelDirectionDebugger::class.java), DeadWheelDirectionDebugger(dvf))

            FtcDashboard.getInstance().withConfigRoot { configRoot: CustomVariable ->
                for (c in listOf<Class<out Any?>>(
                    AngularRampLogger::class.java,
                    ForwardRampLogger::class.java,
                    LateralRampLogger::class.java,
                    ManualFeedforwardTuner::class.java,
                    MecanumMotorDirectionDebugger::class.java
                )) {
                    configRoot.putVariable(c.simpleName, ReflectionConfig.createVariableFromClass(c))
                }
            }
        }
    }
}