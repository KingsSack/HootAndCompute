---
title: Creating an Attachment
tags:
    - getting-started
permalink: creating-an-attachment.html
sidebar: docs
folder: docs
---

# What is an [Attachment](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/index.html)?

An [Attachment](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/index.html) is anything
that has a function on the [Robot](kdoc/volt/-volt/com.lasteditguild.volt.robot/-robot/index.html).
This could be a motor, a servo, a sensor, or anything else that can be controlled.
Attachments are used to control the [Robot](kdoc/volt/-volt/com.lasteditguild.volt.robot/-robot/index.html)
and interact with the environment.

There should be one [Attachment](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/index.html) per "interaction"
your [Robot](kdoc/volt/-volt/com.lasteditguild.volt.robot/-robot/index.html) can perform.

# Creating an [Attachment](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/index.html)

Create a new Kotlin class in `TeamCode/src/main/kotlin/attachment` that inherits the [Attachment](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/index.html) class.

The [Attachment](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/index.html) class contains:
1. A list of motors
2. A list of servos
3. A list of continuous rotation servos
4. A boolean that is true when the attachment's [ControlAction](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/-control-action/index.html) is running
5. A function called `update(telemetry: Telemetry)` that is called every tick
6. A function called `stop()` that is called when the robot is stopped

```kotlin
class Example : Attachment() {
    init {
        // Initialize
    }
    
    override fun update(telemetry: Telemetry) {
        // Update
    }
}
```

## Add a motor


### Defining the motor

Add the parameters `hardwareMap` and `name` for initializing a motor. Create a new `DcMotor`.

```kotlin
class Example(hardwareMap: HardwareMap, name: String) : Attachment() {
    val motor = hardwareMap.dcMotor[name]

    init {
        // Initialize
    }

    inner class Control : ControlAction() {
        override fun init() {
            // Start
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Logic
            return false
        }

        override fun handleStop() {
            // End
        }
    }

    override fun update(telemetry: Telemetry) {
        // Update
    }
}
```

### Setting up the motor

Set the motor's mode to `DcMotor.RunMode.RUN_USING_ENCODER` to use the motor's built-in encoder.
Set the motor's zero-power behavior
to `DcMotor.ZeroPowerBehavior.BRAKE` so the motor holds its position when the motor's power is set to `0.0`.
Add the motor to the `motors` list
so that it can be managed by the [Attachment](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/index.html) class.

```kotlin
class Example(hardwareMap: HardwareMap, name: String) : Attachment() {
    val motor = hardwareMap.dcMotor[name]

    init {
        // Set mode
        motor.mode = DcMotorSimple.RunMode.RUN_USING_ENCODER

        // Set zero power behavior
        motor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        // Add to list
        motors = listOf(motor)
    }

    inner class Control : ControlAction() {
        override fun init() {
            // Start
        }

        override fun update(packet: TelemetryPacket): Boolean {
            // Logic
            return false
        }

        override fun handleStop() {
            // End
        }
    }

    override fun update(telemetry: Telemetry) {
        // Update
    }
}
```

## Log the position of the motor

Modify the `update(telemetry: Telemetry)` function to log the position of `motor`.

```kotlin
override fun update(telemetry: Telemetry) {
    // Log motor position
    telemetry.addLine("==== EXAMPLE ====")
    telemetry.addData("Position", motor.currentPosition)
    telemetry.addLine()
}
```

## Add logic

### Setup the [ControlAction](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/-control-action/index.html)

Create a new `inner class` called `Control`
that inherits [ControlAction](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/-control-action/index.html).
This class will be used to define the [Attachment](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/index.html)'s logic.

The [ControlAction](kdoc/volt/-volt/com.lasteditguild.volt.attachment/-attachment/-control-action/index.html) class contains:
1. A function called `init()` that is called when the action is triggered
2. A function called `update(packet: TelemetryPacket): Boolean` that is called while the action is running and returns whether the action is complete
3. A function called `handleStop()` that is called when the action is stopped

```kotlin
inner class Control : ControlAction() {
    override fun init() {
        // Start
    }
    
    override fun update(packet: TelemetryPacket): Boolean {
        // Logic
        return false
    }
    
    override fun handleStop() {
        // End
    }
}
```

### Control the motor

Add parameters for a `power` and a `targetPosition`. Create an instance variable for whether the motor is `reversing`.
Set the motor's power in the `init()` function.
Determine whether the motor has reached the `targetPosition`. Stop the motor in the `handleStop()` function.

```kotlin
inner class Control(
    private val power: Double,
    private val targetPosition: Int
) : ControlAction() {
    private var reversing = false

    override fun init() {
        // Determine reversing
        reversing = targetPosition < motor.currentPosition

        // Set power
        motor.power = if (reversing) -power else power
    }

    override fun update(packet: TelemetryPacket): Boolean {
        // Get position
        val currentPosition = motor.currentPosition

        if (reversing) {
            // Reverse
            if (currentPosition > targetPosition)
                return false
        } else {
            // Forward
            if (currentPosition < targetPosition)
                return false
        }
        return true
    }

    override fun handleStop() {
        // Stop the motor
        motor.power = 0.0
    }
}
```

### Add an action

Create a function called `use(time: Double)` that returns a new `Action`.

```kotlin
fun goTo(power: Double, position: Int): Action {
    // Return Control
    return Control(power, position)
}
```

# Result

```kotlin
package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

class Example(hardwareMap: HardwareMap, name: String) : Attachment() {
    val motor = hardwareMap.dcMotor[name]

    init {
        motor.mode = DcMotorSimple.RunMode.RUN_USING_ENCODER
        motor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        motors = listOf(motor)
    }

    inner class Control(
        private val power: Double,
        private val targetPosition: Int
    ) : ControlAction() {
        private var reversing = false

        override fun init() {
            reversing = targetPosition < motor.currentPosition
            motor.power = if (reversing) -power else power
        }

        override fun update(packet: TelemetryPacket): Boolean {
            val currentPosition = motor.currentPosition
            if (reversing) {
                if (currentPosition > targetPosition)
                    return false
            } else {
                if (currentPosition < targetPosition)
                    return false
            }
            return true
        }

        override fun handleStop() {
            motor.power = 0.0
        }
    }
    fun goTo(power: Double, position: Int): Action {
        return Control(power, position)
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addLine("==== EXAMPLE ====")
        telemetry.addData("Position", motor.currentPosition)
        telemetry.addLine()
    }
}
```
