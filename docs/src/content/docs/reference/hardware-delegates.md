---
title: Hardware Property Delegates
---

Hardware property delegates exist in the `Robot` base class to help get hardware classes from the FTC `HardwareMap`.

## Available Hardware Delegates

|Delegate|Type|Example|
|---|---|---|
|`motor`|`DcMotor`|`val lift by motor("lift")`|
|`motorEx`|`DcMotorEx`|`val shooter by motorEx("shooter")`|
|`servo`|`Servo`|`val claw by servo("claw")`|
|`crServo`|`CRServo`|`val intake by crServo("intake")`|
|`huskyLens`|`HuskyLens`|`val huskyLens by huskyLens("huskyLens")`|
|`distanceSensor`|`Rev2mDistanceSensor`|`val lidar by distanceSensor("lidar")`|
|`colorSensor`|`NormalizedColorSensor`|`val classifier by colorSensor("classifier")`|
|`imu`|`IMU`|`val imu by imu("imu")`|
|`lazyImu`|`LazyImu`|`val imu by lazyImu("imu", orientation)`|
|`led`|`LED`|`val indicator by led("indicator")`|
|`ledDriver`|`RevBlinkinLedDriver`|`val rgb by ledDriver("rgb")`|
|`analogInput`|`AnalogInput`|`val potentiometer by analogInput("potentiometer")`|

:::caution

All hardware property delegates have a parameter called `name`, this must match the name configured on the Robot Controller using the Driver Station.

:::
