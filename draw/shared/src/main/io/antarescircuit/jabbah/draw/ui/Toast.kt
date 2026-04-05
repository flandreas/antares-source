package io.antarescircuit.jabbah.draw.ui

import io.antarescircuit.jabbah.animation.AnimationModule
import io.antarescircuit.jabbah.animation.Animator

/** Displays a toast message.*/
expect object Toast {
	fun show(message: String, animator: Animator = AnimationModule.constantSpeedAnimator)
}