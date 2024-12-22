---
title: Creating a Manual OpMode
tags:
   - getting-started
permalink: manual.html
sidebar: docs
folder: docs
---

# Creating a Manual OpMode

## Create the class

1. First create a new Kotlin class in`TeamCode/src/main/kotlin`

## Make it a TeleOp

1. Choose whether to use an [OpMode](opmode.html) (recommended) or [LinearOpMode](linearopmode.html)
2. Add the `@TeleOp(name="Name", group="Group")` annotation
3. Override the `init()` and `loop()` functions

```kt
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

```kt
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

```kt
package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.manual.Manual
import org.firstinspires.ftc.teamcode.manual.ManualMode

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