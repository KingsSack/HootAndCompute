---
title: What are Actions
---

[Actions](https://rr.brott.dev/docs/v1-0/actions/) is a concept from [RoadRunner](https://rr.brott.dev/), another library for FTC

> Actions help you define simple behaviors that are easy to combine into large routines.

## Actions in [Volt](../../api/Volt)

Actions encapsulate all logic that controls your robot. They can be found in three main places:

- [Attachments](../../guides/02-attachments)
- [Robots](../../guides/01-robots)
- [OpModes](../../guides/03-opmodes)

Actions have three stages of logic:

1. **Initial logic**: Runs once when the `Action` is triggered
2. **Loop logic**: Runs continuously until the `Action` is complete
3. **Cleanup logic**: Runs once after the loop has completed

```kotlin
fun enable(target: Double = targetVelocity): Action = action {
    init {
        // Runs once when the Action is triggered
        setVelocity(target)
    }
    
    loop {
        // Runs continuously until the Action is complete
        // Has implicit access to a TelemetryPacket providing the put function:
        put("Left flywheel velocity", leftMotor.velocity)
        put("Right flywheel velocity", rightMotor.velocity)

        isAtSpeed // Returns true when the Action is complete
    }
    
    cleanup {
        // Runs once after the loop has completed
        // Not needed in this instance
    }
}
```

The [VoltActionBuilder](../03-volt-action-builder) can be used to build **new** actions from **existing** actions:

```kotlin
fun rampUp(target: Double = targetVelocity): Action = voltAction {
    +enable(target / 4) // Enables at 1/4 of target
    +enable(target / 2) // After reaching 1/4 of target, enables at 1/2 of target
    +enable(target) // After reaching 1/2 of target, enables at target
}
```
