package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.animation.AbstractAnimationTask
import ch.scorpion.jabbah.animation.DoubleRange
import ch.scorpion.jabbah.animation.Sequence
import ch.scorpion.jabbah.animation.AnimationTask
import java.awt.Window

/** An [AnimationTask] for animating [Window.opacity]. */
class OpacityAnimation(
	window: Window,
	sequence: Sequence<Double>,
	duration: Double,
	key: String? = null
) : AbstractAnimationTask<Double>(
	window,
	{ window.opacity = it.toFloat() },
	sequence,
	duration,
	key = key
) {
	companion object {

		private const val FULLY_OPAQUE = 1.0
		private const val FULLY_TRANSPARENT = 0.0

		fun fadeOut(window: Window, duration: Double): OpacityAnimation =
			OpacityAnimation(window, DoubleRange(FULLY_OPAQUE, FULLY_TRANSPARENT), duration)
	}
}