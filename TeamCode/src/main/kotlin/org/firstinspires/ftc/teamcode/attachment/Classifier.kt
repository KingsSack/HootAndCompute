package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.roadrunner.Action
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.hardware.NormalizedColorSensor
import com.qualcomm.robotcore.hardware.NormalizedRGBA
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.ElapsedTime
import dev.kingssack.volt.annotations.VoltAction
import dev.kingssack.volt.attachment.Attachment
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * [Classifier] is an [Attachment] that classifies and releases artifacts.

 * @param sectorOneSensor the [NormalizedColorSensor] used to detect the color of the artifact
 * @param sectorTwoSensor the [NormalizedColorSensor] used to detect the color of the artifact
 * @param sectorThreeSensor the [NormalizedColorSensor] used to detect the color of the artifact
 * @param gate the [Servo] used to open and close the gate that releases artifacts from the classifier
 * @param classifier the [Servo] used to rotate the classifier to different positions
 * @param rgb the LED driver used to control lights in the [Classifier]
 */
class Classifier(
    sectorOneSensor: NormalizedColorSensor,
    sectorTwoSensor: NormalizedColorSensor,
    sectorThreeSensor: NormalizedColorSensor,
    private val gate: Servo,
    private val classifier: Servo,
    private val rgb: RevBlinkinLedDriver,
) : Attachment("Classifier") {
    companion object {
        private const val GATE_CLOSED = 0.6
        private const val GATE_OPEN = 0.0

        private const val SATURATION_THRESHOLD = 0.15f

        private const val PURPLE_HUE_MIN = 200f
        private const val PURPLE_HUE_MAX = 350f
        private const val GREEN_HUE_MIN = 140f
        private const val GREEN_HUE_MAX = 200f

        private const val SECTOR_ONE_POSITION = 0.0
        private const val SECTOR_TWO_POSITION = 1.0
        private const val SECTOR_THREE_POSITION = 0.6
        private const val MAX_CLASSIFIER_POSITION = 1.0

        private const val SECTOR_ONE_TIME = 0.0
        private const val SECTOR_TWO_TIME = 1.0
        private const val SECTOR_THREE_TIME = 0.5
        private const val MAX_CLASSIFIER_TIME = 1.0

        private const val TRANSFER_TIME = 1.2
        private const val RESET_TIME = 0.4
    }

    private data class SectorConfig(val position: Double, val rotateTime: Double)

    private data class Sector(
        val sensor: NormalizedColorSensor,
        val config: SectorConfig,
    )

    private val sectors = listOf(
        Sector(sectorOneSensor, SectorConfig(SECTOR_ONE_POSITION, SECTOR_ONE_TIME)),
        Sector(sectorTwoSensor, SectorConfig(SECTOR_TWO_POSITION, SECTOR_TWO_TIME)),
        Sector(sectorThreeSensor, SectorConfig(SECTOR_THREE_POSITION, SECTOR_THREE_TIME)),
    )

    enum class SectorState {
        PURPLE,
        GREEN,
        EMPTY,
    }

    private fun normalizedRgbaToHsv(colors: NormalizedRGBA): FloatArray {
        val r = colors.red
        val g = colors.green
        val b = colors.blue

        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min

        val hue =
            when {
                delta == 0f -> 0f
                max == r -> 60f * (((g - b) / delta) % 6f)
                max == g -> 60f * (((b - r) / delta) + 2)
                else -> 60f * (((r - g) / delta) + 4)
            }.let { if (it < 0) it + 360f else it }

        val saturation = if (max == 0f) 0f else delta / max
        return floatArrayOf(hue, saturation, max)
    }

    private fun getSectorState(sensor: NormalizedColorSensor): SectorState {
        val (hue, saturation) = normalizedRgbaToHsv(sensor.normalizedColors)

        if (saturation < SATURATION_THRESHOLD) return SectorState.EMPTY

        return when (hue) {
            in PURPLE_HUE_MIN..PURPLE_HUE_MAX -> SectorState.PURPLE
            in GREEN_HUE_MIN..GREEN_HUE_MAX -> SectorState.GREEN
            else -> SectorState.EMPTY
        }
    }

    init {
        gate.position = GATE_CLOSED
        classifier.position = SECTOR_ONE_POSITION
    }

    // --- Sector Lookup ---

    private fun findSector(predicate: (SectorState) -> Boolean): Sector? =
        sectors.firstOrNull { predicate(getSectorState(it.sensor)) }

    private fun findArtifactSector(state: SectorState): Sector? = findSector { it == state }

    private fun findNextSector(): Sector? = findSector { it != SectorState.EMPTY }

    // --- Actions ---

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
    @VoltAction(
        name = "Release Artifact",
        description = "Releases an artifact of the specified type",
    )
    fun releaseArtifact(type: ReleaseType): Action = action {
        val runtime = ElapsedTime()
        var state = ReleaseState.POSITIONING
        var rotateTime = 0.0

        init {
            requireReady()
            runtime.reset()
            rgb.setPattern(RevBlinkinLedDriver.BlinkinPattern.SHOT_WHITE)

            val target = when (type) {
                ReleaseType.PURPLE -> findArtifactSector(SectorState.PURPLE)
                ReleaseType.GREEN -> findArtifactSector(SectorState.GREEN)
                ReleaseType.NEXT -> findNextSector()
            }

            if (target == null) { state = ReleaseState.DONE }
            else {
                classifier.position = target.config.position
                rotateTime = target.config.rotateTime
            }
        }

        loop {
            when (state) {
                ReleaseState.POSITIONING -> {
                    if (runtime.seconds() > rotateTime) {
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
                        classifier.position = sectors[0].config.position
                        state = ReleaseState.DONE
                    }
                }
                ReleaseState.DONE -> return@loop true
            }
            return@loop false
        }

        cleanup { rgb.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED) }
    }

    /**
     * Releases all stored artifacts by opening the gate and rotating the classifier.
     *
     * @return an [Action] that releases all artifacts
     */
    @VoltAction(name = "Release All Artifacts", description = "Releases all stored artifacts")
    fun releaseAllArtifacts(): Action = action {
        val runtime = ElapsedTime()
        var state = ReleaseState.TRANSFERRING

        init {
            requireReady()
            runtime.reset()
            gate.position = GATE_OPEN
        }

        loop {
            when (state) {
                ReleaseState.TRANSFERRING -> {
                    if (runtime.seconds() > TRANSFER_TIME) {
                        classifier.position = MAX_CLASSIFIER_POSITION
                        state = ReleaseState.POSITIONING
                        runtime.reset()
                    }
                }
                ReleaseState.POSITIONING -> {
                    if (runtime.seconds() > MAX_CLASSIFIER_TIME + TRANSFER_TIME) {
                        gate.position = GATE_CLOSED
                        state = ReleaseState.RESETTING
                    }
                }
                ReleaseState.RESETTING -> {
                    if (runtime.seconds() > RESET_TIME) {
                        classifier.position = sectors[0].config.position
                        state = ReleaseState.DONE
                    }
                }
                ReleaseState.DONE -> return@loop true
            }
            return@loop false
        }
    }

    /**
     * Rotates the classifier to the specified [position] (1, 2, or 3).
     *
     * @return an [Action] that rotates the classifier to the specified position
     */
    fun goToPos(position: Int): Action = action {
        init {
            val index = (position - 1).coerceIn(sectors.indices)
            classifier.position = sectors[index].config.position
        }

        loop { true }
    }

    context(telemetry: Telemetry)
    override fun update() {
        super.update()
        with(telemetry) {
            addData("Sector One", getSectorState(sectors[0].sensor))
            addData("Sector Two", getSectorState(sectors[1].sensor))
            addData("Sector Three", getSectorState(sectors[2].sensor))
        }
    }
}
