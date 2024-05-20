package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.attachments.Arm
import org.firstinspires.ftc.teamcode.attachments.Hook
import org.firstinspires.ftc.teamcode.attachments.Launcher
import kotlin.math.abs
import kotlin.math.max

class Bartholomew {
    // Motors
    lateinit var testMotor: DcMotor

    // Initialize
    fun init(hardwareMap: HardwareMap) {
        testMotor = hardwareMap.get(DcMotor::class.java, "Motor")
    }
}

class Fredrick {
    // Drive Motors
    lateinit var leftFrontDrive: DcMotor
    lateinit var rightFrontDrive: DcMotor
    lateinit var leftRearDrive: DcMotor
    lateinit var rightRearDrive: DcMotor

    // Attachments
    lateinit var arm: Arm
    lateinit var hook: Hook
    lateinit var launcher: Launcher

    // Runtime
    val runtime = ElapsedTime()

    // Initialize
    fun init(hardwareMap: HardwareMap) {
        // Drive Motor init
        rightFrontDrive = hardwareMap.get(DcMotor::class.java, "MotorRF")
        leftFrontDrive = hardwareMap.get(DcMotor::class.java, "MotorLF")
        rightRearDrive = hardwareMap.get(DcMotor::class.java, "MotorRR")
        leftRearDrive = hardwareMap.get(DcMotor::class.java, "MotorLR")

        // Direction
        leftFrontDrive.direction = DcMotorSimple.Direction.REVERSE
        leftRearDrive.direction = DcMotorSimple.Direction.REVERSE

        // Attachments init
        arm = Arm(hardwareMap)
        hook = Hook(hardwareMap)
        launcher = Launcher(hardwareMap)
    }

    // Drive
    fun drive(gamepad: Gamepad) {
        // Drive
        val x : Float = gamepad.left_stick_x
        val y : Float = -gamepad.left_stick_y
        val rx : Float = gamepad.right_stick_x

        // Power
        var rfPower : Double = (y - x - rx).toDouble()
        var lfPower : Double = (y + x + rx).toDouble()
        var rrPower : Double = (y + x - rx).toDouble()
        var lrPower : Double = (y - x + rx).toDouble()

        // Normalize
        val maxPower : Float = max(1.toFloat(), (abs(x) + abs(y) + abs(rx)))
        rfPower /= maxPower
        lfPower /= maxPower
        rrPower /= maxPower
        lrPower /= maxPower

        // Slow mode
        if (gamepad.right_bumper) {
            rfPower *= 0.5
            lfPower *= 0.5
            rrPower *= 0.5
            lrPower *= 0.5
        }

        // Set power
        rightFrontDrive.power = rfPower
        leftFrontDrive.power = lfPower
        rightRearDrive.power = rrPower
        leftRearDrive.power = lrPower
    }
}
