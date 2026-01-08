package org.firstinspires.ftc.teamcode.attachment

import android.graphics.Color
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.NormalizedColorSensor
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.ElapsedTime
import dev.kingssack.volt.attachment.Attachment
import dev.kingssack.volt.attachment.ServoAttachment
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.attachment.Classifier.Companion.GATE_CLOSED
import org.firstinspires.ftc.teamcode.attachment.Classifier.Companion.GATE_TIME

/**
 * [Classifier] is a [ServoAttachment] that controls a [gate] used for launching artifacts.
 */
class Classifier(
    private val gate: Servo,
    private val classifier: Servo,
    private val sectorOne: NormalizedColorSensor,
    private val sectorTwo: NormalizedColorSensor,
    private val sectorThree: NormalizedColorSensor
) : Attachment("Classifier") {
    companion object {
        private const val CAROUSEL_POSITION_1 = 0.0
        private const val CAROUSEL_POSITION_2 = 0.33
        private const val CAROUSEL_POSITION_3 = 0.67

        private const val GATE_CLOSED = 0.0
        private const val GATE_OPEN = 1.0
        private const val GATE_TIME = 1.0

        private const val PURPLE_HUE_MIN = 250f
        private const val PURPLE_HUE_MAX = 320f
        private const val GREEN_HUE_MIN = 80f
        private const val GREEN_HUE_MAX = 160f
    }

    enum class SectorState {
        PURPLE,
        GREEN,
        EMPTY
    }

    private val sectors = listOf(sectorOne, sectorTwo, sectorThree)

    private fun getSectorState(sectorSensor: NormalizedColorSensor): SectorState {
        val hsvValues = FloatArray(3)
        Color.colorToHSV(sectorSensor.normalizedColors.toColor(), hsvValues)

        val hue = hsvValues[0]
        val saturation = hsvValues[1]

        if (saturation < 0.02) {
            return SectorState.EMPTY
        }

        return when (hue) {
            in PURPLE_HUE_MIN..PURPLE_HUE_MAX -> SectorState.PURPLE
            in GREEN_HUE_MIN..GREEN_HUE_MAX -> SectorState.GREEN
            else -> SectorState.EMPTY
        }
    }

    private fun findNextSector(): NormalizedColorSensor? {
        if (getSectorState(sectorOne) != SectorState.EMPTY) {
            return sectorOne
        } else if (getSectorState(sectorTwo) != SectorState.EMPTY) {
            return sectorTwo
        } else if (getSectorState(sectorThree) != SectorState.EMPTY) {
            return sectorThree
        }
        return null
    }

    private fun findArtifactSector(state: SectorState): NormalizedColorSensor? {
        for (sector in sectors) {
            if (getSectorState(sector) == state) {
                return sector
            }
        }
        return null
    }


    fun releasePurple(): Action = action {
        val runtime = ElapsedTime()

        init {
            requireReady()

            runtime.reset()

            val purpleSector = findArtifactSector(SectorState.PURPLE)
            if (purpleSector != null) {
                classifier.position = when (purpleSector) {
                    sectorOne -> CAROUSEL_POSITION_1
                    sectorTwo -> CAROUSEL_POSITION_2
                    sectorThree -> CAROUSEL_POSITION_3
                    else -> CAROUSEL_POSITION_1
                }
            }

            gate.position = GATE_OPEN
        }

        loop {
            if (runtime.seconds() > GATE_TIME) {
                gate.position = GATE_CLOSED
                true
            }
            false
        }
    }

    fun releaseGreen(): Action = action {
        val runtime = ElapsedTime()

        init {
            requireReady()

            runtime.reset()

            val greenSector = findArtifactSector(SectorState.GREEN)
            if (greenSector != null) {
                classifier.position = when (greenSector) {
                    sectorOne -> CAROUSEL_POSITION_1
                    sectorTwo -> CAROUSEL_POSITION_2
                    sectorThree -> CAROUSEL_POSITION_3
                    else -> CAROUSEL_POSITION_1
                }
            }

            gate.position = GATE_OPEN
        }

        loop {
            if (runtime.seconds() > GATE_TIME) {
                gate.position = GATE_CLOSED
                true
            }
            false
        }
    }


    fun releaseNext(): Action = action {
        val runtime = ElapsedTime()

        init {
            requireReady()

            runtime.reset()

            val nextSector = findNextSector()
            if (nextSector != null) {
                classifier.position = when (nextSector) {
                    sectorOne -> CAROUSEL_POSITION_1
                    sectorTwo -> CAROUSEL_POSITION_2
                    sectorThree -> CAROUSEL_POSITION_3
                    else -> CAROUSEL_POSITION_1
                }
            }

            gate.position = GATE_OPEN
        }
        loop {
            if (runtime.seconds() > GATE_TIME) {
                gate.position = GATE_CLOSED
                true
            }
            false
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
        }
    }
}
