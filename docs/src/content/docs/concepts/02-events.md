---
title: What are Events
---

Events are the primary way to map gamepad inputs and autonomous triggers to [Actions](../01-actions) in Volt. Instead of writing imperative `if (gamepad.a)` checks every loop, you declare **what** should happen **when** an event occurs, and Volt handles the detection and dispatching for you.

Events are defined inside the `defineEvents()` method of your [OpMode](../../guides/03-opmodes).

## The Event Hierarchy

`Event` is a sealed interface with two branches, one for each OpMode type:

```
Event
├── AutonomousEvent
│   └── Start
└── ManualEvent
    ├── ButtonEvent
    │   ├── Tap
    │   ├── Release
    │   ├── Hold
    │   └── DoubleTap
    ├── AnalogEvent
    │   ├── Change
    │   └── Threshold
    └── Combo
```

- **`AutonomousEvent`** types are used in [AutonomousModes](../../guides/04-autonomous-mode) and respond to lifecycle triggers.
- **`ManualEvent`** types are used in [ManualModes](../../guides/05-manual-mode) and respond to gamepad input.

## Binding Events with `then`

Events are bound to actions using the `then` infix function. The left side is the event, and the right side is a [VoltActionBuilder](../03-volt-action-builder) lambda that defines what actions to run when the event fires:

```kotlin
Tap(Button.A1) then { +someAction() }
```

This reads as: "when button A on gamepad 1 is tapped, run `someAction()`."

:::note

The `then` lambda is a [VoltActionBuilder](../03-volt-action-builder) block, so you have access to the full action DSL. Learn more on the [Volt Action Builder](../03-volt-action-builder) page.

:::

## Autonomous Events

Autonomous events are used in [AutonomousModes](../../guides/04-autonomous-mode). Currently, there's only one `AutonomousEvent`.
Import it with:

```kotlin
import dev.kingssack.volt.util.Event.AutonomousEvent.Start
```

### `Start`

Triggers when the autonomous period begins (after the Play button is selected on the Driver Station). This is a `data object`, so you reference it directly without parentheses:

```kotlin
Start then {
    +robot.drivetrain.path { lineTo(targetPose) }
    +robot.launcher.enable()
    wait(1.5)
    +robot.launcher.disable()
    instant { blackboard["endPose"] = robot.drivetrain.pose }
}
```

## Manual Events

Manual events are used in [ManualModes](../../guides/05-manual-mode). Import them with:

```kotlin
import dev.kingssack.volt.util.Event.ManualEvent.*
```

### `Tap(button)`

Triggers once when a button is pressed down.

```kotlin
Tap(Button.A2) then { +releaseArtifact() }
```

### `Release(button)`

Triggers once when a button is released.

```kotlin
Release(Button.B2) then { +retract() }
```

`Tap` and `Release` can be paired on the same button to create press-and-hold behavior:

```kotlin
Tap(Button.B2) then { +push() }
Release(Button.B2) then { +retract() }
```

### `Hold(button, durationMs)`

Triggers after a button has been held for at least `durationMs` milliseconds. Defaults to `200.0` ms.

```kotlin
Hold(Button.A1) then { +chargeShot() }
Hold(Button.A1, 500.0) then { +superChargeShot() }
```

### `DoubleTap(button)`

Triggers when a button is pressed twice in quick succession (within 300 ms).

```kotlin
DoubleTap(Button.Y1) then { +toggleMode() }
```

### `Change(analogInput)`

Fires whenever an analog input value changes. The `then` lambda for analog events receives the current `Float` value as a parameter:

```kotlin
Change(AnalogInput.RIGHT_TRIGGER1) then { value ->
    instant {
        if (value > 0.5) robot.aim()
        else robot.resetAim()
    }
}
```

### `Threshold(analogInput, min)`

Fires when an analog input changes **and** its value exceeds the `min` threshold. Defaults to `0.3f`.

```kotlin
Threshold(AnalogInput.LEFT_TRIGGER2) then { value ->
    +setMotorPower(value)
}

Threshold(AnalogInput.RIGHT_TRIGGER1, 0.5f) then { value ->
    +boost(value)
}
```

### `Combo(buttons)`

Fires when multiple buttons are all held simultaneously and at least one was just pressed. Use the `combo()` helper function to create combo events:

```kotlin
combo(Button.LEFT_BUMPER1, Button.RIGHT_BUMPER1) then {
    +emergencyStop()
}
```

## Button and AnalogInput Enums

Volt provides enum entries for every gamepad input. Entries suffixed with `1` read from gamepad 1, and entries suffixed with `2` read from gamepad 2.

### `Button`

| Gamepad 1 | Gamepad 2 |
|---|---|
| `A1`, `B1`, `X1`, `Y1` | `A2`, `B2`, `X2`, `Y2` |
| `LEFT_BUMPER1`, `RIGHT_BUMPER1` | `LEFT_BUMPER2`, `RIGHT_BUMPER2` |
| `LEFT_STICK_BUTTON1`, `RIGHT_STICK_BUTTON1` | `LEFT_STICK_BUTTON2`, `RIGHT_STICK_BUTTON2` |
| `DPAD_UP1`, `DPAD_DOWN1`, `DPAD_LEFT1`, `DPAD_RIGHT1` | `DPAD_UP2`, `DPAD_DOWN2`, `DPAD_LEFT2`, `DPAD_RIGHT2` |
| `BACK1`, `START1`, `GUIDE1` | `BACK2`, `START2`, `GUIDE2` |
| `TOUCHPAD_FINGER_ONE1`, `TOUCHPAD_FINGER_TWO1` | `TOUCHPAD_FINGER_ONE2`, `TOUCHPAD_FINGER_TWO2` |

### `AnalogInput`

| Gamepad 1 | Gamepad 2 |
|---|---|
| `LEFT_STICK_X1`, `LEFT_STICK_Y1` | `LEFT_STICK_X2`, `LEFT_STICK_Y2` |
| `RIGHT_STICK_X1`, `RIGHT_STICK_Y1` | `RIGHT_STICK_X2`, `RIGHT_STICK_Y2` |
| `LEFT_TRIGGER1`, `RIGHT_TRIGGER1` | `LEFT_TRIGGER2`, `RIGHT_TRIGGER2` |
| `TOUCHPAD_FINGER_ONE_X1`, `TOUCHPAD_FINGER_ONE_Y1` | `TOUCHPAD_FINGER_ONE_X2`, `TOUCHPAD_FINGER_ONE_Y2` |
| `TOUCHPAD_FINGER_TWO_X1`, `TOUCHPAD_FINGER_TWO_Y1` | `TOUCHPAD_FINGER_TWO_X2`, `TOUCHPAD_FINGER_TWO_Y2` |

## Putting It Together

A typical `defineEvents()` override organizes bindings by attachment using extension functions:

```kotlin
override fun defineEvents() {
    super.defineEvents()
    robot.launcher.controls()
    robot.pusher.controls()
}

private fun Launcher.controls() {
    Tap(Button.RIGHT_BUMPER2) then { +enable(targetVelocity) }
    Tap(Button.LEFT_BUMPER2) then { +disable() }
}

private fun Pusher.controls() {
    Tap(Button.B2) then { +push() }
    Release(Button.B2) then { +retract() }
}
```
