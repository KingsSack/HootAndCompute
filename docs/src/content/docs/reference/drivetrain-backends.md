---
title: RoadRunner vs PedroPathing
---

Volt integrates with both [RoadRunner](https://rr.brott.dev/) and
[PedroPathing](https://pedropathing.com/docs/pathing/installation) for localization, autonomous
pathing, and manual control.

:::note

If you have not added a drivetrain yet, start with
[Adding a Drivetrain](../../guides/04-drivetrains).

:::

## Quick comparison

| Factor | RoadRunner | PedroPathing |
| --- | --- | --- |
| Drivetrain support | Mecanum, tank | Mecanum, swerve |
| Motion style | Pose-based trajectories | Path-based lines, splines, and path chains |
| Choose this if | You already use RoadRunner or need tank support | You already use PedroPathing or need swerve support |

If you already have one backend tuned and working, keep using it.

## How to choose

Choose the backend in this order:

1. **Hardware support**: Does it match your drivetrain?
2. **Action style**: Do you prefer trajectories or paths?
3. **Tuning effort**: Do you already have one tuned?

## Hardware support

- **RoadRunner** works with **mecanum** and **tank** drivetrains.
- **PedroPathing** works with **mecanum** and **swerve** drivetrains.
- Both integrate with Volt's localization options.
- **PedroPathing** also supports **three dead wheels + an IMU**.

## Actions

### RoadRunner

RoadRunner is the better fit if you want to start from a pose and build a trajectory from
there.

- `strafeTo(from: Pose2d, to: Vector2d)`
- `strafeToLinearHeading(from: Pose2d, to: Pose2d)`
- `splineTo(from: Pose2d, to: Vector2d, tangent: Rotation2d)`
- `splineToLinearHeading(from: Pose2d, to: Pose2d, tangent: Rotation2d)`
- `turnTo(from: Pose2d, to: Radians)`
- `turnTo(from: Pose2d, to: Degrees)`
- `turn(from: Pose2d, radians: Radians)`
- `turn(from: Pose2d, degrees: Degrees)`
- `trajectory(from: Pose2d, block: TrajectoryActionBuilder.() -> TrajectoryActionBuilder)`

### PedroPathing

PedroPathing is the better fit if you want to compose lines, splines, and path chains directly.

- `pathTo(pathChain: PathChain)`
- `lineTo(line: BezierLine)`
- `lineToLinearHeading(line: BezierLine, initial: Radians, final: Radians)`
- `lineToLinearHeading(line: BezierLine, initial: Degrees, final: Degrees)`
- `lineToConstantHeading(line: BezierLine, heading: Radians)`
- `lineToConstantHeading(line: BezierLine, heading: Degrees)`
- `lineToTangentHeading(line: BezierLine)`
- `splineTo(line: BezierLine)`
- `splineToLinearHeading(curve: BezierCurve, initial: Radians, final: Radians)`
- `splineToLinearHeading(curve: BezierCurve, initial: Degrees, final: Degrees)`
- `splineToConstantHeading(curve: BezierCurve, heading: Radians)`
- `splineToConstantHeading(curve: BezierCurve, heading: Degrees)`
- `splineToTangentHeading(curve: BezierCurve)`
- `path(startPose: Pose, block: PedroPathingActionBuilder.() -> PedroPathingActionBuilder)`
- `followPath(block: PathBuilder.() -> Unit)`

## Tuning

Both backends require tuning before they will drive accurately. See [Tuning Your Robot](./tuning)
for the backend-specific setup guides.
