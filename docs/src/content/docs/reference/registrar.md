
A Registrar can be used to create classes like the MultiAutonomousMode.
To use it, create a class extending AutonomousMode or ManualMode, and inside add an object (or a class with an "INSTANCE" property in Java) extending VoltOpMode.Registrar.
you can then implement the "register" function, which takes a VoltRegistrationHelper and a class as parameters. The method is called for each child of your class and the class itself (if it is not abstract). if your class is sealed or has no children the class can be ignored. the RegistrationHelper allows you to register your opmodes, and can be used multiple times per class. the builtin classes require a VoltOpModeMeta annotation to construct their OpModeMetas, but they are not required.
Opmodes can be registered through a no parameter constructor, or a function that returns the OpMode

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
