package util.messages

import com.acmerobotics.roadrunner.ftc.PositionVelocityPair
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles

class MecanumLocalizerInputsMessage(
    var leftFront: PositionVelocityPair,
    var leftBack: PositionVelocityPair,
    var rightBack: PositionVelocityPair,
    var rightFront: PositionVelocityPair,
    angles: YawPitchRollAngles
) {
    var timestamp: Long = System.nanoTime()
    var yaw: Double = 0.0
    var pitch: Double = 0.0
    var roll: Double = 0.0

    init {
        run {
            this.yaw = angles.getYaw(AngleUnit.RADIANS)
            this.pitch = angles.getPitch(AngleUnit.RADIANS)
            this.roll = angles.getRoll(AngleUnit.RADIANS)
        }
    }
}