import com.qualcomm.robotcore.hardware.HardwareMap

abstract class Attachment(hardwareMap: HardwareMap) {
    init {
        this.init(hardwareMap)
    }

    // Initialize attachment
    abstract fun init(hardwareMap: HardwareMap)
}