package dev.kingssack.volt.attachment.drivetrain

import com.acmerobotics.roadrunner.PoseVelocity2d
import dev.kingssack.volt.attachment.Attachment

/** Base class for all drivetrain attachments. */
abstract class Drivetrain : Attachment("Drivetrain") {
    /**
     * Set the drive powers.
     *
     * @param powers the drive powers
     * @see PoseVelocity2d
     */
    abstract fun setDrivePowers(powers: PoseVelocity2d)
}
