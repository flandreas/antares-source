package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.Animator

/** Displays a toast message.*/
expect object Toast {
	fun show(message: String, animator: Animator = AnimationModule.constantSpeedAnimator)
}