---
title: Creating a Manual OpMode
description: Learn how to make a Manual OpMode with Volt.
sidebar:
    badge: 
        text: Outdated
        variant: danger
---

## Create the class

1. First create a new Kotlin class in `TeamCode/src/main/kotlin`

## Make it a TeleOp

1. Choose whether to use an [OpMode](opmode.html) (recommended) or [LinearOpMode](linearopmode.html)
2. Add the `@TeleOp(name="Name", group="Group")` annotation
3. Override the `init()` and `loop()` functions

```kotlin
@TeleOp(name = "Test", group = "Test")  
class Test : OpMode() {
	override fun init() {
		// TODO: Implement
	}

	override fun loop() {
		// TODO: Implement
	}
}
```

## Add manual mode script

1. Declare `manual` as an [ManualMode](kdoc/-team-code/org.firstinspires.ftc.teamcode.manual/-manual-mode/index.html)
2. Initialize `manual` in `init`
3. Call `tick` in `loop`

```kotlin
// Manual script  
private lateinit var manual: ManualMode  

override fun init() {  
	// Initialize  
	manual = Manual(hardwareMap, telemetry, Configuration.testParams, gamepad1, gamepad2)
}

override fun loop() {
	// Runs every tick
	manual.tick(telemetry)
}
```

## Result

```kotlin
package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.manual.Manual
import com.lasteditguild.volt.manual.ManualMode

@TeleOp(name = "Test", group = "Test")
class Test : OpMode() {
   // Manual script  
   private lateinit var manual: ManualMode

   override fun init() {
      // Initialize  
      manual = Manual(hardwareMap, telemetry, Configuration.testParams,
         gamepad1, gamepad2)
   }

   override fun loop() {
      // Runs every tick
      manual.tick(telemetry)
   }
}
```