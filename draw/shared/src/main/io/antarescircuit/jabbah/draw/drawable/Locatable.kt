package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.Drawable

/**
 * Represents a [Drawable] that has a distinctive [Point2D] location.
 */
interface Locatable : Drawable {

    /** The [Point2D] at which this [Locatable] is located.*/
    var location: Point2D

	/** Moves this [Locatable] by the specified offset.*/
	fun moveBy(dx: Double, dy: Double) {
		location = location.add(dx, dy)
	}
}