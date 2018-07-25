package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Represents a [Drawable] that has a distinctive [Point2D] location.
 */
interface Locatable : Drawable {

	companion object {

		/** Moves a [Collection] of [Locatable]s all by the same offset.*/
		fun moveLocatables(locatables: Collection<Locatable>, offset: Point2D) {
			locatables.forEach { it.prepareMoveBy(locatables) }
			locatables.forEach { it.moveBy(offset.x, offset.y) }
			locatables.forEach { it.completeMoveBy() }
		}
	}

    /** The [Point2D] at which this [Locatable] is located.*/
    var location: Point2D

    /** Informs this [Locatable] that it is about to be moved together with other [Locatable]s.*/
    fun prepareMoveBy(components: Collection<Locatable>) {}

    /** Moves this [Locatable] by the specified offset.*/
    fun moveBy(dx: Double, dy: Double) {
        location = location.add(dx, dy)
    }

    /** Informs this [Locatable] that moving previously announced by [prepareMoveBy] has been completed for all [Locatable]s.*/
    fun completeMoveBy() {}
}