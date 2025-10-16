package dev.kingssack.volt.util

/**
 * Represents buttons on a gamepad.
 * Includes buttons from both gamepad1 and gamepad2.
 */
enum class GamepadButton {
    A1, B1, X1, Y1,
    LEFT_BUMPER1, RIGHT_BUMPER1,
    LEFT_STICK_BUTTON1, RIGHT_STICK_BUTTON1,
    DPAD_UP1, DPAD_DOWN1, DPAD_LEFT1, DPAD_RIGHT1,
    BACK1, START1, GUIDE1,

    A2, B2, X2, Y2,
    LEFT_BUMPER2, RIGHT_BUMPER2,
    LEFT_STICK_BUTTON2, RIGHT_STICK_BUTTON2,
    DPAD_UP2, DPAD_DOWN2, DPAD_LEFT2, DPAD_RIGHT2,
    BACK2, START2, GUIDE2
}

/**
 * Represents analog inputs on a gamepad.
 * Includes inputs from both gamepad1 and gamepad2.
 */
enum class GamepadAnalogInput {
    LEFT_STICK_X1, LEFT_STICK_Y1,
    RIGHT_STICK_X1, RIGHT_STICK_Y1,
    LEFT_TRIGGER1, RIGHT_TRIGGER1,

    LEFT_STICK_X2, LEFT_STICK_Y2,
    RIGHT_STICK_X2, RIGHT_STICK_Y2,
    LEFT_TRIGGER2, RIGHT_TRIGGER2
}
