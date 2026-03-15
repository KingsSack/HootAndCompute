---
title: Flow Editor
sidebar:
    badge:
        text: "Experimental"
        variant: "danger"
---

> [Actions](../01-actions) are the core execution primitive in Volt — reusable units of robot behavior 
> with init, loop, and cleanup stages. They originate from 
> [RoadRunner](https://rr.brott.dev/) and are extended by Volt with tracing and 
> composition.

The Volt Flow Editor is a visual interface that allows you to create and manage action flows without writing code.
It provides a drag-and-drop canvas where you can design action flows by connecting different nodes together.

## Accessing the Flow Editor

You can access the editor by going to [192.168.43.1:8080/volt](http://192.168.43.1:8080/volt) in your browser while connected to your Robot Controller's Wi-Fi network.

## Nodes

The Volt Flow Editor features three types of nodes:

1. **Action Nodes**: These nodes represent your [Actions](../01-actions) that can be executed. They can be configured with specific parameters and settings defined within your codebase.
2. **Event Nodes**: These nodes represent your [Events](../02-events) that can trigger action flows.
3. **Control Nodes**: These nodes allow you to control the flow of actions, such as branching, looping, and conditional logic.

:::note

To make your [Actions](../01-actions) available in the editor use the `@VoltAction` annotation:

```kotlin {1}
@VoltAction(name = "Enable Launcher", description = "Enables the launcher at target velocity")
fun enable(target: Double = targetVelocity): Action = action {
    init { setVelocity(target) }
    loop {
        put("Left flywheel velocity", leftMotor.velocity)
        put("Right flywheel velocity", rightMotor.velocity)

        isAtSpeed
    }
}
```

With this annotation, the [Action](../01-actions) will available in the editor whenever the selected [Robot](../../guides/01-robot) has access to it.
 
:::

## Deployment

Currently, the editor will generate the code for your flow when deployed. You can copy and paste this code into your codebase to access it from the Driver Station.
