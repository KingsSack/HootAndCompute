package util.messages

import com.acmerobotics.roadrunner.PoseVelocity2dDual
import com.acmerobotics.roadrunner.Time

class DriveCommandMessage(poseVelocity: PoseVelocity2dDual<Time>) {
    var timestamp: Long = System.nanoTime()
    var forwardVelocity: Double = poseVelocity.linearVel.x[0]
    var forwardAcceleration: Double = poseVelocity.linearVel.x[1]
    var lateralVelocity: Double = poseVelocity.linearVel.y[0]
    var lateralAcceleration: Double = poseVelocity.linearVel.y[1]
    var angularVelocity: Double = poseVelocity.angVel[0]
    var angularAcceleration: Double = poseVelocity.angVel[1]
}