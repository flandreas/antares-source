package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawableContainer

/**
 * Represents a [Locatable] that can be moved interactively.
 */
interface Movable : Locatable {

	companion object {

		/** Moves a [Collection] of [Locatable]s all by the same offset.*/
		fun moveBy(movables: Collection<Movable>, offset: Point2D) {
			moveBy(movables, offset.x, offset.y)
		}

		fun moveBy(movables: Collection<Movable>, dx: Double, dy: Double) {
			movables.forEach { it.prepareMoveBy(movables) }
			movables.forEach { it.moveBy(dx, dy) }
			movables.forEach { it.completeMoveBy() }
		}
	}

	/** The unique identification of this [Movable] in its containing [DrawableContainer]. */
	val id: Int

	/** Informs this [Movable] that it is about to be moved together with other [Movable]s.*/
	fun prepareMoveBy(components: Collection<Movable>) {}

	/** Informs this [Movable] that moving previously announced by [prepareMoveBy] has been completed for all [Movable]s.*/
	fun completeMoveBy() {}
}