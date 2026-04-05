package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.animation.AbstractAnimationTask
import io.antarescircuit.jabbah.animation.CompositeSequence
import io.antarescircuit.jabbah.animation.PointRange
import io.antarescircuit.jabbah.animation.Sequence
import io.antarescircuit.jabbah.base.geom.Point2D

class ShakeLocatableAnimation(
	private val locatable: Locatable,
	duration: Double = DEF_DURATION,
	range: Double = DEF_RANGE,
	cycleCount: Int = DEF_CYCLE_COUNT
) : AbstractAnimationTask<Point2D>(
	target = locatable,
	consumer = { locatable.location = it; locatable.validate() },
	sequence = createShakeSequence(locatable.location, range, cycleCount),
	duration = duration,
	dependsOnSystemSpeed = false,
	isPausable = false
) {

	companion object {
		private const val DEF_DURATION = 200.0
		private const val DEF_RANGE = 4.0
		private const val DEF_CYCLE_COUNT = 2

		private fun createShakeSequence(start: Point2D, range: Double, cycleCount: Int): Sequence<Point2D> {
			val sequences = mutableListOf<Sequence<Point2D>>()
			for (i in 1..cycleCount) {
				sequences.add(PointRange(start, start.add(range, 0.0)))
				sequences.add(PointRange(start.add(range, 0.0), start.add(-range, 0.0)))
				sequences.add(PointRange(start.add(-range, 0.0), start))
			}
			return CompositeSequence(*sequences.toTypedArray())
		}
	}

	private val startLocation = locatable.location

	override fun handleStopped() {
		// Compensate for potential animation inaccuracies
		locatable.location = startLocation
		locatable.validate()
	}
}