package org.firstinspires.ftc.teamcode.util

import com.acmerobotics.dashboard.config.Config

/**
 * FieldParams is a configuration object for field parameters.
 *
 * @property basketX the x position of the basket
 * @property basketY the y position of the basket
 * @property observationX the x position of the observation zone
 * @property observationY the y position of the observation zone
 * @property samplePositionsX the x positions of the sample positions
 * @property samplePositionsY the y positions of the sample positions
 * @property samplePickupPositionsX the x positions of the sample pickup positions
 * @property samplePickupPositionsY the y positions of the sample pickup positions
 */
@Config
object FieldParams {
    @JvmField
    var basketX: Double = 64.0
    @JvmField
    var basketY: Double = 64.0

    @JvmField
    var observationX: Double = -48.0
    @JvmField
    var observationY: Double = 64.0

    @JvmField
    var samplePositionsX: DoubleArray = doubleArrayOf(48.0, 57.0, 66.0)
    @JvmField
    var samplePositionsY: DoubleArray = doubleArrayOf(24.0, 24.0, 24.0)

    @JvmField
    var submersibleX: Double = -12.0
    @JvmField
    var submersibleY: Double = 24.0
}