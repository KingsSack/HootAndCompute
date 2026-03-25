---
title: OpMode Registrars
---

A Registrar can be used to create classes like `MultiAutonomousMode`.
To use it, create a class extending `AutonomousMode` or `ManualMode`, and inside add an object (or a class with an "INSTANCE" property in Java) extending `VoltOpMode.Registrar`.
You can then implement the `register` function, which takes a `VoltRegistrationHelper` and a `Class` as parameters. The method is called for each child class and the class itself (if it is not abstract). if your class is sealed or has no children the class can be ignored.
The `registrationHelper` allows you to register your [OpModes](../../guides/03-opmodes), and can be used multiple times per class. The builtin classes require a `VoltOpModeMeta` annotation to construct their `OpModeMeta`s, but they are not required.
[OpModes](../../guides/03-opmodes) can be registered through a no parameter constructor, or a function that returns the [OpMode](../../guides/03-opmodes).

## Example

```kotlin
class ManualModeWithCustomRegistrar : ManualMode {
    @Suppress("unused")
    object Register : Registrar() {
        override fun register(
            registrationHelper: VoltRegistrationHelper,
            clazz: Class<VoltOpMode<*>>,
        ) {
            registrationHelper.register(
                clazz.getDeclaredConstructor(),
                OpModeMeta.Builder()
                    .setName(clazz.simpleName)
                    .setFlavor(OpModeMeta.Flavor.TELEOP)
                    .build(),
            )
        }
    }
}
```
