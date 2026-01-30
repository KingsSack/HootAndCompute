package org.firstinspires.ftc.teamcode.attachment

import android.graphics.Color
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.NormalizedColorSensor
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.ElapsedTime
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.Attachment
import dev.kingssack.volt.attachment.ServoAttachment
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * [Classifier] is a [ServoAttachment] that controls a [gate], [classifier], [sectorOne],
 * [sectorTwo], and [sectorThree] used for launching artifacts.
 */
class Classifier(
    private val gate: Servo,
    private val classifier: Servo,
    private val sectorOne: NormalizedColorSensor,
    private val sectorTwo: NormalizedColorSensor,
    private val sectorThree: NormalizedColorSensor,
) : Attachment("Classifier") {
    companion object {
        private const val CAROUSEL_POSITION_1 = 0.0
        private const val CAROUSEL_POSITION_2 = 1.0
        private const val CAROUSEL_POSITION_3 = 0.5

        private const val GATE_CLOSED = 0.6 // Fully closed position
        private const val GATE_OPEN = 0.0 // Fully open position

        private const val SATURATION_THRESHOLD = 0.02f

        private const val PURPLE_HUE_MIN = 200f
        private const val PURPLE_HUE_MAX = 300f
        private const val GREEN_HUE_MIN = 80f
        private const val GREEN_HUE_MAX = 180f

        private const val MAX_CLASSIFY_TIME = 0.6 // The maximum time to classify an artifact
        private const val TRANSFER_TIME = 1.5 // The time to transfer an artifact through the gate
        private const val RESET_TIME = 0.5 // The time to clos the gate
    }

    enum class SectorState {
        PURPLE,
        GREEN,
        EMPTY,
    }

    private val sectors = listOf(sectorOne, sectorTwo, sectorThree)

    init {
        gate.position = GATE_CLOSED
        classifier.position = CAROUSEL_POSITION_1
    }

    private fun getSectorState(sectorSensor: NormalizedColorSensor): SectorState {
        val hsvValues = FloatArray(3)
        Color.colorToHSV(sectorSensor.normalizedColors.toColor(), hsvValues)

        val hue = hsvValues[0]
        val saturation = hsvValues[1]

        if (saturation < SATURATION_THRESHOLD) {
            return SectorState.EMPTY
        }

        return when (hue) {
            in PURPLE_HUE_MIN..PURPLE_HUE_MAX -> SectorState.PURPLE
            in GREEN_HUE_MIN..GREEN_HUE_MAX -> SectorState.GREEN
            else -> SectorState.EMPTY
        }
    }

    private fun findNextSector(): NormalizedColorSensor? =
        sectors.firstOrNull { getSectorState(it) != SectorState.EMPTY }

    private fun findArtifactSector(state: SectorState): NormalizedColorSensor? {
        for (sector in sectors) {
            if (getSectorState(sector) == state) {
                return sector
            }
        }
        return null
    }

    private fun getCarouselPosition(sector: NormalizedColorSensor): Double {
        return when (sector) {
            sectorOne -> CAROUSEL_POSITION_1
            sectorTwo -> CAROUSEL_POSITION_2
            sectorThree -> CAROUSEL_POSITION_3
            else -> CAROUSEL_POSITION_1
        }
    }

    enum class ReleaseType {
        PURPLE,
        GREEN,
        NEXT,
    }

    private enum class ReleaseState {
        POSITIONING,
        TRANSFERRING,
        RESETTING,
        DONE,
    }

    /**
     * Releases an artifact of the specified [type] by positioning the classifier, opening the gate,
     * and resetting the classifier.
     *
     * @return an [Action] that performs the release
     */
    @VoltAction(name = "Release Artifact", description = "Releases an artifact of the specified type")
    fun releaseArtifact(type: ReleaseType): Action = action {
        val runtime = ElapsedTime()
        var state = ReleaseState.POSITIONING
        var targetSector: NormalizedColorSensor? = null

        init {
            requireReady()
            runtime.reset()

            targetSector =
                when (type) {
                    ReleaseType.PURPLE -> findArtifactSector(SectorState.PURPLE)
                    ReleaseType.GREEN -> findArtifactSector(SectorState.GREEN)
                    ReleaseType.NEXT -> findNextSector()
                }
            if (targetSector == null) state = ReleaseState.DONE
            else classifier.position = getCarouselPosition(targetSector!!)
        }

        loop {
            when (state) {
                ReleaseState.POSITIONING -> {
                    if (runtime.seconds() > MAX_CLASSIFY_TIME) {
                        gate.position = GATE_OPEN
                        state = ReleaseState.TRANSFERRING
                        runtime.reset()
                    }
                }
                ReleaseState.TRANSFERRING -> {
                    if (runtime.seconds() > TRANSFER_TIME) {
                        gate.position = GATE_CLOSED
                        state = ReleaseState.RESETTING
                        runtime.reset()
                    }
                }
                ReleaseState.RESETTING -> {
                    if (runtime.seconds() > RESET_TIME) {
                        classifier.position = CAROUSEL_POSITION_1
                        state = ReleaseState.DONE
                    }
                }
                ReleaseState.DONE -> return@loop true
            }
            return@loop false
        }
    }

    /**
     * Releases all stored artifacts by opening the gate and rotating the classifier.
     *
     * @return an [Action] that releases all artifacts
     */
    @VoltAction(name = "Release All Artifacts", description = "Releases all stored artifacts")
    fun releaseAllArtifacts(): Action = action {
        val runtime = ElapsedTime()

        init {
            requireReady()
            runtime.reset()

            gate.position = GATE_OPEN
            classifier.position = 1.0
        }

        loop {
            if (runtime.seconds() > MAX_CLASSIFY_TIME) {
                gate.position = GATE_CLOSED
                classifier.position = CAROUSEL_POSITION_1
                return@loop true
            }
            return@loop false
        }
    }

    context(telemetry: Telemetry)
    override fun update() {
        super.update()
        with(telemetry) {
            addData("Carousel Position", classifier.position)
            addData("Gate Position", gate.position)
            addData("Sector One", getSectorState(sectorOne))
            addData("Sector Two", getSectorState(sectorTwo))
            addData("Sector Three", getSectorState(sectorThree))

            // Display HSV values for each sector
            val hsvValues = FloatArray(3)
            Color.colorToHSV(sectorOne.normalizedColors.toColor(), hsvValues)
            telemetry.addData("Sector One Hue", hsvValues[0].toString())
            telemetry.addData("Sector One Saturation", hsvValues[1].toString())
            telemetry.addData(
                "Sector One Colors",
                "R: ${sectorOne.normalizedColors.red}, G: ${sectorOne.normalizedColors.green}, B: ${sectorOne.normalizedColors.blue}, A: ${sectorOne.normalizedColors.alpha}",
            )
            Color.colorToHSV(sectorTwo.normalizedColors.toColor(), hsvValues)
            telemetry.addData("Sector Two Hue", hsvValues[0].toString())
            telemetry.addData("Sector Two Saturation", hsvValues[1].toString())
            telemetry.addData(
                "Sector Two Colors",
                "R: ${sectorTwo.normalizedColors.red}, G: ${sectorTwo.normalizedColors.green}, B: ${sectorTwo.normalizedColors.blue}, A: ${sectorTwo.normalizedColors.alpha}",
            )
            Color.colorToHSV(sectorThree.normalizedColors.toColor(), hsvValues)
            telemetry.addData("Sector Three Hue", hsvValues[0].toString())
            telemetry.addData("Sector Three Saturation", hsvValues[1].toString())
            telemetry.addData(
                "Sector Three Colors",
                "R: ${sectorThree.normalizedColors.red}, G: ${sectorThree.normalizedColors.green}, B: ${sectorThree.normalizedColors.blue}, A: ${sectorThree.normalizedColors.alpha}",
            )
        }
    }
}
