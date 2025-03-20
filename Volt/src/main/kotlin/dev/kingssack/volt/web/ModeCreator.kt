package dev.kingssack.volt.web

import android.util.Log
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.Gamepad
import dev.kingssack.volt.opmode.AutonomousOpMode
import dev.kingssack.volt.opmode.CustomOpModes
import dev.kingssack.volt.opmode.ManualOpMode
import dev.kingssack.volt.opmode.autonomous.AutonomousMode
import dev.kingssack.volt.opmode.manual.ManualMode
import dev.kingssack.volt.robot.Robot
import dev.kingssack.volt.robot.RobotWithMecanumDrive
import org.firstinspires.ftc.robotcore.external.Telemetry
import com.qualcomm.robotcore.hardware.HardwareMap
import com.pedropathing.localization.Pose

/**
 * Utility class for creating and registering autonomous and manual modes.
 */
class ModeCreator {
    companion object {
        private const val TAG = "ModeCreator"
        
        /**
         * Create and register an autonomous mode from a configuration.
         */
        fun createAndRegisterAutonomousMode(config: ModeCreatorHandler.AutonomousModeConfig) {
            Log.d(TAG, "Creating autonomous mode: ${config.name}")
            
            val opMode = object : AutonomousOpMode() {
                override fun runOpMode() {
                    // Create the robot based on the robot type
                    val robot = createRobot(config.robotType, hardwareMap)
                    
                    // Create the autonomous mode
                    auto = createAutonomousMode(config, robot, telemetry)
                    
                    // Run the standard autonomous mode execution
                    super.runOpMode()
                }
            }
            
            // Register the op mode
            CustomOpModes.autos.add(opMode)
            Log.d(TAG, "Registered autonomous mode: ${config.name}")
        }
        
        /**
         * Create and register a manual mode from a configuration.
         */
        fun createAndRegisterManualMode(config: ModeCreatorHandler.ManualModeConfig) {
            Log.d(TAG, "Creating manual mode: ${config.name}")
            
            val opMode = object : ManualOpMode() {
                override fun init() {
                    // Create the robot based on the robot type
                    val robot = createRobot(config.robotType, hardwareMap)
                    
                    // Create the manual mode
                    manual = createManualMode(config, robot, gamepad1, gamepad2, telemetry)
                }
            }
            
            // Register the op mode
            CustomOpModes.teleops.add(opMode)
            Log.d(TAG, "Registered manual mode: ${config.name}")
        }
        
        /**
         * Create a robot based on the robot type.
         */
        private fun createRobot(robotType: String, hardwareMap: HardwareMap): Robot {
            return when (robotType) {
                "RobotWithMecanumDrive" -> {
                    // Create a RobotWithMecanumDrive with default parameters
                    RobotWithMecanumDrive(
                        hardwareMap,
                        Pose(0.0, 0.0, 0.0)
                    )
                }
                else -> {
                    throw IllegalArgumentException("Unknown robot type: $robotType")
                }
            }
        }
        
        /**
         * Create an autonomous mode from a configuration.
         */
        private fun createAutonomousMode(
            config: ModeCreatorHandler.AutonomousModeConfig,
            robot: Robot,
            telemetry: Telemetry
        ): AutonomousMode {
            return object : AutonomousMode() {
                override val robot: Robot = robot
                
                init {
                    // Add actions to the sequence based on the configuration
                    for (actionConfig in config.sequence) {
                        val action = createAction(actionConfig, robot)
                        actionSequence.add { action }
                    }
                }
            }
        }
        
        /**
         * Create a manual mode from a configuration.
         */
        private fun createManualMode(
            config: ModeCreatorHandler.ManualModeConfig,
            robot: Robot,
            gamepad1: Gamepad,
            gamepad2: Gamepad,
            telemetry: Telemetry
        ): ManualMode {
            return object : ManualMode(gamepad1, gamepad2, telemetry) {
                override val robot: Robot = robot
                
                init {
                    // Add interactions based on the configuration
                    for (mappingConfig in config.mappings) {
                        val action = createAction(mappingConfig.action, robot)
                        
                        when (mappingConfig.controlType) {
                            "button" -> {
                                // Add a button interaction
                                interactions.add(Interaction(
                                    { isButtonTapped(mappingConfig.control) },
                                    { action }
                                ))
                            }
                            "toggle" -> {
                                // Add a toggle interaction
                                interactions.add(ToggleInteraction(
                                    { isButtonTapped(mappingConfig.control) },
                                    { action },
                                    { action }
                                ))
                            }
                            "analog" -> {
                                // Add an analog interaction
                                interactions.add(Interaction(
                                    { getAnalogValue(mappingConfig.control) > 0.5 },
                                    { action }
                                ))
                            }
                        }
                    }
                }
            }
        }
        
        /**
         * Create an action from a configuration.
         */
        private fun createAction(config: ModeCreatorHandler.ActionConfig, robot: Robot): Action {
            return when {
                robot is RobotWithMecanumDrive && config.id == "pathTo" -> {
                    // Get parameters
                    val x = (config.parameters.find { it.name == "x" }?.value as? Number)?.toDouble() ?: 0.0
                    val y = (config.parameters.find { it.name == "y" }?.value as? Number)?.toDouble() ?: 0.0
                    val heading = (config.parameters.find { it.name == "heading" }?.value as? Number)?.toDouble() ?: 0.0
                    
                    // Create the action
                    robot.pathTo(Pose(x, y, heading))
                }
                config.id == "wait" -> {
                    // Get parameters
                    val seconds = (config.parameters.find { it.name == "seconds" }?.value as? Number)?.toDouble() ?: 1.0
                    
                    // Create a wait action
                    object : Action {
                        private var startTime = 0L
                        
                        override fun run(p: com.acmerobotics.dashboard.telemetry.TelemetryPacket): Boolean {
                            if (startTime == 0L) {
                                startTime = System.currentTimeMillis()
                            }
                            
                            val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
                            return elapsedTime < seconds
                        }
                    }
                }
                else -> {
                    throw IllegalArgumentException("Unknown action: ${config.id} for robot type: ${robot.javaClass.simpleName}")
                }
            }
        }
    }
}