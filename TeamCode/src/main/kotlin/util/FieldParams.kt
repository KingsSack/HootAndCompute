package org.firstinspires.ftc.teamcode.util

import com.acmerobotics.dashboard.config.Config

/**
 * FieldParams is a configuration object for field parameters.
 *
 * @property basketX the x position of the basket
 * @property basketY the y position of the basket
 * @property lowerBasketHeight the height in rotations of the lower basket
 * @property upperBasketHeight the height in rotations of the upper basket
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
    var basketX: Double = 60.0
    @JvmField
    var basketY: Double = 60.0
    @JvmField
    var lowerBasketHeight: Int = 500
    @JvmField
    var upperBasketHeight: Int = 600

    @JvmField
    var observationX: Double = -66.0
    @JvmField
    var observationY: Double = 66.0

    @JvmField
    var samplePositionsX: DoubleArray = doubleArrayOf(48.0, 60.0, 70.0)
    @JvmField
    var samplePositionsY: DoubleArray = doubleArrayOf(24.0, 24.0, 24.0)
    @JvmField
    var samplePickupPositionsX: DoubleArray = doubleArrayOf(49.0, 60.0, 70.0)
    @JvmField
    var samplePickupPositionsY: DoubleArray = doubleArrayOf(42.0, 42.0, 42.0)
}