---
title: Action Tracing
---

By default, Volt traces all [Actions](../01-actions) added to a [VoltActionBuilder](../03-volt-action-builder). These traces are helpful when debugging and analyzing action flows.

## `TracedAction`

The `TracedAction` class wraps RoadRunner's `Action` class. It injects code in the Action's run logic to keep track of:

1. When the Action **begins**
2. When the Action **finishes**
3. How **long** the Action was running

The class also takes a `label` that can be used to identify the encapsulated `Action`.

All [Actions](../01-actions) added to a [VoltActionBuilder](../03-volt-action-builder) are automatically traced using `TracedAction`.

## `ActionTracer`

The `ActionTracer` handles all `TracedAction`s. It has methods to mark [Actions](../01-actions) as running or completed, as well as to display telemetry on the Driver Station and add the entire trace to a telemetry packet.

### Running Action Telemetry

The `ActionTracer`'s `writeTelemetry` method displays all running [Actions](../01-actions) and their elapsed time in miliseconds.

An output might look like this:

```
=== Running Actions ===

[0] Action (202ms)
```

If there are no running [Actions](../01-actions) the output will look like this:

```
=== Running Actions ===

None
```

### Action Trace

The action trace lists the previous 100 actions stored by the `ActionTracer`. It is automatically displayed on [FTC Dashboard](https://acmerobotics.github.io/ftc-dashboard/).

An output might look like this:

```
action/0/Action 202
```
