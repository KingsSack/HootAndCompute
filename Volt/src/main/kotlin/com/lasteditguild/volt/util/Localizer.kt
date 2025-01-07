package com.lasteditguild.volt.util

import com.acmerobotics.roadrunner.Time
import com.acmerobotics.roadrunner.Twist2dDual

interface Localizer {
    fun update(): Twist2dDual<Time>
}