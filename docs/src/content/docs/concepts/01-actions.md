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

1. Initial logic
2. Loop logic
3. Cleanup logic

Initial logic runs once when the Action is run. Loop logic runs continuously until the Action is complete. Cleanup logic runs once after the loop has completed.

Sequences of Actions can be built from other actions using the [VoltActionBuilder](../03-volt-action-builder)
