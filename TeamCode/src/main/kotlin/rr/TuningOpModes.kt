package org.firstinspires.ftc.teamcode.rr

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.config.reflection.ReflectionConfig
import com.acmerobotics.dashboard.config.variable.CustomVariable
import com.acmerobotics.roadrunner.MotorFeedforward
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.ftc.*
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.teamcode.util.MecanumDrive
import java.util.*

class TuningOpModes {
    companion object {
        private val GROUP: String = "quickstart"

        private fun metaForClass(cls: Class<out OpMode?>): OpModeMeta {
            return OpModeMeta.Builder()
                .setName(cls.simpleName)
                .setGroup(GROUP)
                .setFlavor(OpModeMeta.Flavor.TELEOP)
                .build()
        }

        @JvmStatic
        @OpModeRegistrar
        fun register(manager: OpModeManager) {
            val dvf: DriveViewFactory = object : DriveViewFactory {
                override fun make(h: HardwareMap): DriveView {
                    val md = MecanumDrive(h, Pose2d(0.0, 0.0, 0.0))

                    val leftEncs = ArrayList<Encoder>()
                    val rightEncs = ArrayList<Encoder>()
                    val parEncs = ArrayList<Encoder>()
                    val perpEncs = ArrayList<Encoder>()
                    val localizer = md.localizer
                    leftEncs.add(localizer.leftFront)
                    leftEncs.add(localizer.leftBack)
                    rightEncs.add(localizer.rightFront)
                    rightEncs.add(localizer.rightBack)

                    return DriveView(
                        DriveType.MECANUM,
                        MecanumDrive.inPerTick,
                        MecanumDrive.maxWheelVel,
                        MecanumDrive.minProfileAccel,
                        MecanumDrive.maxProfileAccel,
                        h.getAll(LynxModule::class.java),
                        listOf(md.leftFront, md.leftBack),
                        listOf(md.rightFront, md.rightBack),
                        leftEncs, rightEncs, parEncs, perpEncs,
                        md.lazyImu, md.voltageSensor
                    ) {
                        MotorFeedforward(
                            MecanumDrive.kS,
                            MecanumDrive.kV / MecanumDrive.inPerTick,
                            MecanumDrive.kA / MecanumDrive.inPerTick
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