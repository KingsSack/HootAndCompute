package robot

import attachment.Arm
import com.qualcomm.robotcore.hardware.*
import kotlin.math.abs
import kotlin.math.max

class Goose : Robot() {
    // Drive motors
    private lateinit var leftFrontDrive : DcMotor
    private lateinit var rightFrontDrive : DcMotor
    private lateinit var leftRearDrive : DcMotor
    private lateinit var rightRearDrive : DcMotor

    // Sensors
    private lateinit var imu : IMU

    // Attachments
    private lateinit var arm : Arm

    override fun init(hardwareMap: HardwareMap) {
        // Register hardware
        registerMotors(hardwareMap)
        registerSensors(hardwareMap)
        registerAttachments(hardwareMap)

        // Reset IMU
        imu.resetYaw()
    }

    override fun driveWithGamepad(gamepad: Gamepad) {
        // Get gamepad input
        val x : Float = gamepad.left_stick_x
        val y : Float = -gamepad.left_stick_y
        val rx : Float = gamepad.right_stick_x

        // Calculate power
        var rfPower : Double = (y - x - rx).toDouble()
        var lfPower : Double = (y + x + rx).toDouble()
        var rrPower : Double = (y + x - rx).toDouble()
        var lrPower : Double = (y - x + rx).toDouble()

        // Normalize power
        val maxPower : Float = max(1.toFloat(), (abs(x) + abs(y) + abs(rx)))
        rfPower /= maxPower
        lfPower /= maxPower
        rrPower /= maxPower
        lrPower /= maxPower

        // Slow mode logic
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

    fun liftArmWithGamepad(gamepad: Gamepad) {
        // Control arm
        arm.liftArm(gamepad.left_stick_y.toDouble())
    }

    override fun halt() {
        // Stop all drive motors
        leftFrontDrive.power = 0.0
        rightFrontDrive.power = 0.0
        leftRearDrive.power = 0.0
        rightRearDrive.power = 0.0
    }

    private fun registerSensors(hardwareMap: HardwareMap) {
        // Register sensors
        imu = hardwareMap.get(IMU::class.java, "imu")
    }

    private fun registerMotors(hardwareMap: HardwareMap) {
        // Register motors
        leftFrontDrive = hardwareMap.get(DcMotor::class.java, "lf")
        rightFrontDrive = hardwareMap.get(DcMotor::class.java, "rf")
        leftRearDrive = hardwareMap.get(DcMotor::class.java, "lr")
        rightRearDrive = hardwareMap.get(DcMotor::class.java, "rr")

        // Set motor directions
        leftFrontDrive.direction = DcMotorSimple.Direction.FORWARD
        rightFrontDrive.direction = DcMotorSimple.Direction.REVERSE
        leftRearDrive.direction = DcMotorSimple.Direction.FORWARD
        rightRearDrive.direction = DcMotorSimple.Direction.REVERSE
    }

    private fun registerAttachments(hardwareMap: HardwareMap) {
        // Register attachments
        arm = Arm(hardwareMap, "lm")
    }
}