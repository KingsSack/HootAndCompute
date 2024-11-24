package util.messages

class MecanumCommandMessage(
    var voltage: Double,
    var leftFrontPower: Double,
    var leftBackPower: Double,
    var rightBackPower: Double,
    var rightFrontPower: Double
) {
    var timestamp: Long = System.nanoTime()
}