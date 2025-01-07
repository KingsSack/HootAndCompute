---
title: Creating an Autonomous OpMode
tags:
   - getting-started
permalink: autonomous.html
sidebar: docs
folder: docs
---

## Create the class

1. First create a new Kotlin class in `TeamCode/src/main/kotlin`

## Make it autonomous

1. Choose whether to use an [OpMode](opmode.html) or [LinearOpMode](linearopmode.html) (recommended)
2. Add the `@Autonomous(name="Name", group="Group")` annotation
3. Override the `runOpMode()` function

```kt
@Autonomous(name = "Test", group = "Test")  
class Test : LinearOpMode() {
	override fun runOpMode() {
		// TODO: Implement
	}
}
```

## Add autonomous mode script

1. Declare `auto` as an [AutonomousMode](kdoc/-team-code/org.firstinspires.ftc.teamcode.autonomous/-autonomous-mode/index.html)
2. Initialize `auto` in `runOpMode`
3. Add a `waitForStart()`
4. If `opModeIsActive()`, execute the [AutonomousMode](kdoc/-team-code/org.firstinspires.ftc.teamcode.autonomous/-autonomous-mode/index.html) with `auto.run()`

```kt
// Autonomous script
private lateinit var auto: AutonomousMode

override fun runOpMode() {
	// Initialize
	auto = Otter(hardwareMap, telemetry, Configuration.testParams)

	// Wait for start
	waitForStart()

	// Execute  
	if (opModeIsActive())
		auto.run()
}
```

>[!Warning]
>You must include the `waitForStart()` to make the OpMode legal

## Result

```kt
package org.firstinspires.ftc.teamcode  
  
import org.firstinspires.ftc.teamcode.autonomous.Otter  
import com.qualcomm.robotcore.eventloop.opmode.Autonomous  
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode  
import com.lasteditguild.volt.autonomous.AutonomousMode  
  
@Autonomous(name = "Test", group = "Test")  
class Test : LinearOpMode() {  
    // Autonomous script  
    private lateinit var auto: AutonomousMode  
  
    override fun runOpMode() {  
        // Initialize  
        auto = Otter(hardwareMap, telemetry, Configuration.otterTestParams)  
  
        // Wait for start  
        waitForStart()  
  
        // Execute  
        if (opModeIsActive())  
            auto.run()  
    }
}
```