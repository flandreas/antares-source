package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.animation.AbstractAnimationTask
import ch.scorpion.jabbah.animation.Sequence
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Animates the moving of a [Locatable] along a [Sequence] of [Point2D]s.
 */
class MoveLocatableAnimation(
	locatable: Locatable,
	sequence: Sequence<Point2D>,
	duration: Double,
	isPausable: Boolean = true
) : AbstractAnimationTask<Point2D>(
	target = locatable,
	consumer = { locatable.location = it; locatable.validate() },
	sequence = sequence,
	duration = duration,
	dependsOnSystemSpeed = true,
	isPausable = isPausable)