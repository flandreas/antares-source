package io.antarescircuit.jabbah.draw.ui

import io.antarescircuit.jabbah.animation.AbstractAnimationTask
import io.antarescircuit.jabbah.animation.DoubleRange
import io.antarescircuit.jabbah.animation.Sequence
import io.antarescircuit.jabbah.animation.AnimationTask
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