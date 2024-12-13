---
title: Creating an Attachment
tags:
    - getting-started
permalink: creating-an-attachment.html
sidebar: docs
folder: docs
---

## What is an [Attachment]()

An [Attachment]() is anything that has a function on the [Robot](). This could be a motor, a servo, a sensor, or anything else that can be controlled. Attachments are used to control the [Robot]() and interact with the environment.

There should be one [Attachment]() per "interaction" your [Robot]() can perform.

## Create the class

1. First create a new Kotlin class in `TeamCode/src/main/kotlin/attachment`

## Make it an attachment

1. Make it inherit the [Attachement]() class
2. Add the `init` body
3. Override the `update(telemetry: Telemetry)` function

```kt
class ExampleAttachment(hardwareMap: HardwareMap, name: String) : Attachment() {
    init {
        // Initialize
    }
    
    override fun update(telemetry: Telemetry) {
        // Update
    }
}
```

## Add an action

1. Create a new `inner class` called `Control`
2. Make it inherit the [ControlAction]() class
3. Override the `init()` function
4. Override the `update(packet: TelemetryPacket): Boolean` function

```kt
inner class Control : ControlAction() {
    override fun init() {
        // Initialize
    }
    
    override fun update(packet: TelemetryPacket): Boolean {
        // Update
        return false
    }
}
```

## Add a motor

1. Create a new val called `motor`
2. Set the motor's `mode` to `DcMotor.RunMode.RUN_USING_ENCODER`
3. Set the motor's `zeroPowerBehavior` to `DcMotor.ZeroPowerBehavior.BRAKE`
4. Add it to the `motors` list in the `init` body

```kt
val motor = hardwareMap.dcMotor[name]

init {
    // Set mode
    motor.mode = DcMotorSimple.RunMode.RUN_USING_ENCODER
    
    // Set zero power behavior
    motor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    
    // Add to list
    motors = listOf(motor)
}
```

## Log the position of the motor

1. Modify the `update` function to log the position of `motor`

```kt
override fun update(telemetry: Telemetry) {
    // Log motor position
    telemetry.addData("Motor Position", motor.currentPosition)
}
```

## Control the motor

1. Modify the `Control` class to control `motor`

```kt
inner class Control(power: Double, time: Double) : ControlAction() {
    val runtime = ElapsedTime()
    
    override fun init() {
        // Reset runtime
        runtime.reset()
        
        // Set motor power
        motor.power = 1.0
    }
    
    override fun update(packet: TelemetryPacket): Boolean {
        // Check if time has passed
        if (runtime.seconds() > time) {
            // Stop motor
            motor.power = 0.0
            return true
        }
        return false
    }
}
```

## Add an action

1. Create a function called `use(time: Double)` that returns a new `Action`

```kt
fun use(time: Double): Action {
    // Move for time seconds with max power
    return Control(1.0, time)
}
```

## Result

```kt
package org.firstinspires.ftc.teamcode.attachment

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

class ExampleAttachment(hardwareMap: HardwareMap, name: String) : Attachment() {
    val motor = hardwareMap.dcMotor[name]

    init {
        motor.mode = DcMotorSimple.RunMode.RUN_USING_ENCODER
        motor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        motors = listOf(motor)
    }

    inner class Control(power: Double, time: Double) : ControlAction() {
        val runtime = ElapsedTime()

        override fun init() {
            runtime.reset()
            motor.power = 1.0
        }

        override fun update(packet: TelemetryPacket): Boolean {
            if (runtime.seconds() > time) {
                motor.power = 0.0
                return true
            }
            return false
        }
    }
    fun use(time: Double): Action {
        return Control(1.0, time)
    }

    override fun update(telemetry: Telemetry) {
        telemetry.addData("Motor Position", motor.currentPosition)
    }
}
```
