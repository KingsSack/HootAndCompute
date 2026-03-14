---
title: Volt Action Builder
---

`VoltActionBuilder` is the DSL class for composing [Actions](../01-actions) into sequences with timing, parallelism, and control flow.
It is used by the `then` function in [AutonomousModes](../../guide/04-autonomous-mode) and [ManualModes](../../guide/05-manual-mode)
and the `voltAction` function in other classes (usually [Robots](../../guide/01-robots)).

The builder collects actions into a list and produces a single `SequentialAction` when built.

:::note

Every action added is automatically wrapped in a `TracedAction` for telemetry. See [Action Tracing](../04-action-tracing) to learn more.

:::

## DSL Operators

### `+action` (unary plus)

Enqueues [Actions](../01-actions) into the current sequence. [Actions](../01-actions) run in the order they are added:

```kotlin
+claw.open()
+claw.close()
```

### `wait(seconds)`

Inserts a time delay between actions. The argument is in seconds:

```kotlin
+claw.open()
wait(0.5)
+claw.close()
```

### `parallel`

Runs all child actions simultaneously. The block completes when every child has finished:

```kotlin
parallel {
    +drivetrain.path { lineTo(launchPose) }
    +launcher.enable()
}
// both are done before this runs
+classifier.releaseArtifact()
```

### `sequence`

Runs child actions one after another. This is the explicit form of what the top-level builder does implicitly. It can be used to create a sequential action inside a `parallel` block:

```kotlin
parallel {
    +drivetrain.path { lineTo(pose) }
    sequence {
        +launcher.enable()
        wait(1.0)
        +pusher.push()
    }
}
```

### `instant`

Executes a block of code immediately as a one-shot action. Use it for state mutations, telemetry updates, or any synchronous side effect that does not need a loop:

```kotlin
instant { targetVelocity = 1500.0 }
+launcher.enable(targetVelocity)
```

## From Simple to Complex

### Simple sequence

Two actions run back to back:

```kotlin
+claw.open()
+claw.close()
```

### Adding timing

Insert delays between actions:

```kotlin
+claw.open()
wait(0.5)
+arm.lower()
wait(0.3)
+claw.close()
```

### Parallel execution

Spin up the launcher while driving to position:

```kotlin
parallel {
    +drivetrain.path { lineTo(launchPose) }
    +launcher.enable()
}
+pusher.push()
```

### Nested composition

A parallel block where one branch is itself a sequence:

```kotlin
parallel {
    +drivetrain.path { lineTo(launchPose) }
    sequence {
        +launcher.enable()
        wait(1.0)
        +classifier.releaseArtifact()
    }
}
parallel {
    +launcher.disable()
    +drivetrain.path { lineTo(finalPose) }
}
instant { blackboard["endPose"] = drivetrain.pose }
```

### Loops and control flow

Standard Kotlin control flow works inside the builder. The builder lambda is regular Kotlin code, only `+`, `wait`, `parallel`, `sequence`, and `instant` interact with the action queue:

```kotlin "repeat(amount)"
repeat(amount) {
    +launcher.enable()
    +storage.release()
    wait(0.6)
    +storage.close()
    wait(0.4)
}
+launcher.disable()
```

## `voltAction` Function

While `VoltActionBuilder` is used internally by [Event](../02-events) bindings, you can also use it directly in [Robot](../../guides/01-robots) subclasses to compose attachment actions into higher-level behaviors. The `voltAction` function is the entry point for this:

```kotlin
fun fireAllStoredArtifacts(targetVelocity: Double) = voltAction {
    +launcher.enable(targetVelocity)
    +classifier.releaseAllArtifacts()
    +launcher.disable()
}
```

`voltAction` uses Kotlin's `context(robot: R)` parameter, so it has implicit access to the robot instance. It returns a standard RoadRunner `Action` that can itself be enqueued with `+` inside other builders or bound to events:

```kotlin
fun fire(amount: Int) = voltAction {
    repeat(amount) {
        +launcher.enable()
        +storage.release()
        wait(0.6)
        +storage.close()
        wait(0.4)
    }
    +launcher.disable()
}
```
