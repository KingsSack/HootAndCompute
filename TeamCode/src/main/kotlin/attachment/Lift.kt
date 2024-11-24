package attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap


class Lift(hardwareMap: HardwareMap, rightName: String, leftName: String) : Attachment {
    // Constants
    private val maxPosition: Int = 3000
    private val maxPower: Double = 0.8

    // Initialize lifters
    private val liftRight = hardwareMap.get(DcMotor::class.java, rightName)
    private val liftLeft = hardwareMap.get(DcMotor::class.java, leftName)

    init {
        // Set motor directions
        liftRight.direction = DcMotorSimple.Direction.REVERSE
        liftLeft.direction = DcMotorSimple.Direction.FORWARD

        // Set zero power behavior
        liftRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        liftLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Set motor modes
        liftRight.mode = DcMotor.RunMode.RUN_USING_ENCODER
        liftLeft.mode = DcMotor.RunMode.RUN_USING_ENCODER
    }

    private class LiftUp(private val liftRight: DcMotor, private val liftLeft: DcMotor, private val power: Double, private val targetPosition: Int) : Action {
        private var initialized = false

        override fun run(p: TelemetryPacket): Boolean {
            if (!initialized) {
                // Set power
                liftRight.power = power
                liftLeft.power = power
                initialized = true
            }

            // Get positions
            val rightPosition: Int = liftRight.currentPosition
            val leftPosition: Int = liftLeft.currentPosition
            p.put("Right Position", rightPosition)
            p.put("Left Position", leftPosition)

            if (rightPosition < targetPosition && leftPosition < targetPosition)
                return true

            // At target position
            liftRight.power = 0.0
            liftLeft.power = 0.0
            return false
        }
    }
    fun liftUp() : Action {
        return LiftUp(liftRight, liftLeft, maxPower, maxPosition)
    }

//    class LiftDown(private val liftRight: DcMotor, private val liftLeft: DcMotor) : Action {
//        var initialized: Boolean = false
//
//        override fun init(p: TelemetryPacket): Boolean {
//            if (!initialized) {
//                liftRight.setPower(-0.8)
//                liftLeft.setPower(-0.8)
//                initialized = true
//            }
//
//            val rightPos: Int = liftRight.currentPosition
//            val leftPos: Int = liftLeft.currentPosition
//            p.put("liftPos", pos)
//            if (rightPos > 0 && leftPos > 0) {
//                return true
//            } else {
//                liftRight.setPower(0)
//                liftLeft.setPower(0)
//                return false
//            }
//        }
//    }
//    fun liftDown() : Action {
//        return LiftDown(liftRight, liftLeft)
//    }
}