package dev.kingssack.volt.util

import com.qualcomm.robotcore.hardware.Gamepad

/** Represents buttons on a gamepad. Includes buttons from both gamepad1 and gamepad2. */
enum class GamepadButton(val get: (Gamepad, Gamepad) -> Boolean) {
    A1({ g1, _ -> g1.a }),
    B1({ g1, _ -> g1.b }),
    X1({ g1, _ -> g1.x }),
    Y1({ g1, _ -> g1.y }),
    LEFT_BUMPER1({ g1, _ -> g1.left_bumper }),
    RIGHT_BUMPER1({ g1, _ -> g1.right_bumper }),
    LEFT_STICK_BUTTON1({ g1, _ -> g1.left_stick_button }),
    RIGHT_STICK_BUTTON1({ g1, _ -> g1.right_stick_button }),
    DPAD_UP1({ g1, _ -> g1.dpad_up }),
    DPAD_DOWN1({ g1, _ -> g1.dpad_down }),
    DPAD_LEFT1({ g1, _ -> g1.dpad_left }),
    DPAD_RIGHT1({ g1, _ -> g1.dpad_right }),
    BACK1({ g1, _ -> g1.back }),
    START1({ g1, _ -> g1.start }),
    GUIDE1({ g1, _ -> g1.guide }),
    A2({ _, g2 -> g2.a }),
    B2({ _, g2 -> g2.b }),
    X2({ _, g2 -> g2.x }),
    Y2({ _, g2 -> g2.y }),
    LEFT_BUMPER2({ _, g2 -> g2.left_bumper }),
    RIGHT_BUMPER2({ _, g2 -> g2.right_bumper }),
    LEFT_STICK_BUTTON2({ _, g2 -> g2.left_stick_button }),
    RIGHT_STICK_BUTTON2({ _, g2 -> g2.right_stick_button }),
    DPAD_UP2({ _, g2 -> g2.dpad_up }),
    DPAD_DOWN2({ _, g2 -> g2.dpad_down }),
    DPAD_LEFT2({ _, g2 -> g2.dpad_left }),
    DPAD_RIGHT2({ _, g2 -> g2.dpad_right }),
    BACK2({ _, g2 -> g2.back }),
    START2({ _, g2 -> g2.start }),
    GUIDE2({ _, g2 -> g2.guide }),
}

/** Represents analog inputs on a gamepad. Includes inputs from both gamepad1 and gamepad2. */
enum class GamepadAnalogInput(val get: (Gamepad, Gamepad) -> Float) {
    LEFT_STICK_X1({ g1, _ -> g1.left_stick_x }),
    LEFT_STICK_Y1({ g1, _ -> g1.left_stick_y }),
    RIGHT_STICK_X1({ g1, _ -> g1.right_stick_x }),
    RIGHT_STICK_Y1({ g1, _ -> g1.right_stick_y }),
    LEFT_TRIGGER1({ g1, _ -> g1.left_trigger }),
    RIGHT_TRIGGER1({ g1, _ -> g1.right_trigger }),
    LEFT_STICK_X2({ _, g2 -> g2.left_stick_x }),
    LEFT_STICK_Y2({ _, g2 -> g2.left_stick_y }),
    RIGHT_STICK_X2({ _, g2 -> g2.right_stick_x }),
    RIGHT_STICK_Y2({ _, g2 -> g2.right_stick_y }),
    LEFT_TRIGGER2({ _, g2 -> g2.left_trigger }),
    RIGHT_TRIGGER2({ _, g2 -> g2.right_trigger }),
}
